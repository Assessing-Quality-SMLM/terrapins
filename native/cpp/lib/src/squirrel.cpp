#include "consts.hpp"
#include "imp.hpp"
#include "resizing.hpp"
#include "squirrel_pw_results.hpp"
#include "squirrel_inner.hpp"
#include "squirrel.hpp"

#include <nlopt.hpp>

#include <opencv2/core/types.hpp>
#include <opencv2/imgproc.hpp>

#include <dip_opencv_interface.h>
#include <diplib/analysis.h>
#include <diplib/geometry.h>

#include <atomic>
#include <cmath>
#include <format>
#include <fstream>
#include <iostream>
#include <limits>
#include <optional>
#include <ostream>
// #include <ranges>
#include <stdexcept>
#include <thread>
#include <tuple>
#include <vector>

namespace sqrl 
{
    constexpr int CV_FLOATING_DEPTH = imp::CV_FLOATING_DEPTH;

   	constexpr double MIN_SIGMA =  0.000001;

    class SolverResults
    {
    public:
    	explicit SolverResults()
    	{
    	}

		void add_location(double alpha, double beta, double sigma, double rmse)
		{
			alphas_.push_back(alpha);
			betas_.push_back(beta);
			sigmas_.push_back(sigma);
			rmse_.push_back(rmse);
		}

    	void set_clippped(bool value)
    	{
    		clipped_ = value;
    	}

    	std::ostream& write_to(std::ostream& stream) const
    	{
    		stream << std::format("clipped,{}\n", clipped_);
    		stream << std::format("alpha,beta,sigma,rmse\n");
    		// for (std::tuple<const double&, const double&> item : std::views::zip(sigmas_, rmse_))
    			// stream << std::format("{},{}\n", std::get<0>(item), std::get<1>(item));
    		for(auto idx = 0; idx < n_elements(); idx++)
    		{
    			stream << std::format("{},{},{},{}\n", alphas_[idx], betas_[idx], sigmas_[idx], rmse_[idx]);
    		}
    		return stream;
    	}

    private:
    	size_t n_elements() const
    	{
    		return sigmas_.size();
    	}

    private:
    	bool clipped_ = false;
		std::vector<double> alphas_;
		std::vector<double> betas_;
		std::vector<double> sigmas_;
		std::vector<double> rmse_;
    };
   
	class OF_Data
	{
	public:
		explicit OF_Data(SolverResults& results, const cv::Mat& wf_image, const cv::Mat& sr_image)
			:results_(results)
			,wf_image_(wf_image)
			,sr_image_(sr_image)
		{

		}

		const cv::Mat& wf_image() const
		{
			return wf_image_;
		}

		const cv::Mat& sr_image() const
		{
			return sr_image_;
		}

		void add_location(double alpha, double beta, double sigma, double value)
		{
			results_.add_location(alpha, beta, sigma, value);
		}

		SolverResults& results_;
		const cv::Mat& wf_image_;
		const cv::Mat& sr_image_;
	};

	void get_error_map(double* const wf, double* const sr, double* const map, size_t n_pixels, bool show_positive_negative)
	{
		for(auto idx = 0; idx < n_pixels; idx++)
        {
        	// std::cout << idx << "\n";
            double wf_value = wf[idx];
            double sr_value = sr[idx];

            const auto difference = wf_value - sr_value;
            double value = show_positive_negative ? difference : std::abs(difference);
            // std::cout << std::format("{} - {} = {} -> {}\n", wf_value, sr_value, difference, value);
            map[idx] = value;
        }
	}

	cv::Mat error_map(const cv::Mat& wf, const cv::Mat& sr, bool show_positive_negative)
	{
		auto wf_data = (double* const)wf.data;
		auto sr_data = (double* const)sr.data;
		const auto n_pixels = wf.rows * wf.cols;
		// std::cout << std::format("n pixels: {}\n", n_pixels);
		cv::Mat map;
		map.create(wf.size(), CV_FLOATING_DEPTH);
		auto map_data = (double* const )map.data;
		get_error_map(wf_data, sr_data, map_data, n_pixels, show_positive_negative);
		return map;
    }

	bool is_whole(double value)
	{
		double integral;
    	double fractional = std::modf(value, &integral);
    	return fractional == 0.0;
	}	

	double rmse(double* const image_a, double* const image_b, size_t n_elements)
	{
        double total = 0.0;
        for(auto idx = 0; idx < n_elements; idx++)
        {
        	const auto value_a = image_a[idx];
        	const auto value_b = image_b[idx];
        	const auto diff = value_a - value_b;
        	// std::cout << std::format("{} - {} = {}\n", value_a, value_b, diff);
        	total += diff * diff;
        }
        // std::cout << std::format("total: {}\n", total);
        const auto mse = total / ((double)n_elements);
        // std::cout << std::format("mse: {}\n", mse);
        const auto rmse = sqrt(mse);
        // std::cout << std::format("rmse: {}\n", rmse);
        // std::exit(2);
        return rmse;
	}

	double image_rmse(const cv::Mat& image_a, const cv::Mat& image_b)
	{
		// std::cout << "Calculating rmse\n";
		const auto image_a_data = (imp::FLOATING_TYPE* const)image_a.data;
		const auto image_b_data = (imp::FLOATING_TYPE* const)image_b.data;
		const auto n_elements = image_a.rows * image_a.cols;
		return rmse(image_a_data, image_b_data, n_elements);
	}

	imp::FLOATING_TYPE pearsons(imp::FLOATING_TYPE* const image_a, imp::FLOATING_TYPE* const image_b, size_t n_elements)
	{
        imp::FLOATING_TYPE total = 0.0;
        imp::FLOATING_TYPE x_sum = 0.0;
        imp::FLOATING_TYPE x_sqr_sum = 0.0;
        imp::FLOATING_TYPE y_sum = 0.0;
        imp::FLOATING_TYPE y_sqr_sum = 0.0;
        imp::FLOATING_TYPE x_y_sum = 0.0;
        for(auto idx = 0; idx < n_elements; idx++)
        {
        	const auto x = image_a[idx];
        	const auto y = image_b[idx];
        	x_sum += x;
        	x_sqr_sum += (x * x);
        	y_sum += y;
        	y_sqr_sum += (y * y);
        	x_y_sum += (x * y);
        }
        const auto numerator = (n_elements * x_y_sum) - (x_sum * y_sum);
        const auto denom_a = (n_elements * x_sqr_sum) - (x_sum * x_sum);
        const auto denom_b = (n_elements * y_sqr_sum) - (y_sum * y_sum);
        const auto denom = std::sqrt(denom_a) * std::sqrt(denom_b);
        return numerator / denom;
	}

	double image_pearsons(const cv::Mat& image_a, const cv::Mat& image_b)
	{
		// std::cout << "Calculating pearsons\n";
		const auto image_a_data = (imp::FLOATING_TYPE* const)image_a.data;
		const auto image_b_data = (imp::FLOATING_TYPE* const)image_b.data;
		const auto n_elements = image_a.rows * image_a.cols;
		return pearsons(image_a_data, image_b_data, n_elements);
	}

	cv::Mat affine_transform(const cv::Mat& image, double alpha, double beta)
	{
		return (image * alpha) + beta;
	}

	cv::Mat affine_blur(const cv::Mat& image, double alpha, double beta, double sigma)
	{	
		const auto blurred = imp::imagej_gaussian_blur(image, sigma);
		return affine_transform(blurred, alpha, beta);
	}

	int get_magnification(const cv::Mat& image, const cv::Size& new_size)
	{		
		return image.rows / new_size.height;
	}	

	std::optional<cv::Mat> blur_and_downscale(const cv::Mat& image, double sigma, const cv::Size& image_size)
	{
		try
		{
			const auto blurred_image = imp::imagej_gaussian_blur(image, sigma);
			return downscale_sum<imp::FLOATING_TYPE>(blurred_image, get_magnification(image, image_size));
		}
		catch(std::exception& e)
		{
			std::cout << "Something went wrong\n" << e.what() << "\n";
			return {};
		}
	}

	cv::Mat blur_downsample_affine(const cv::Mat& image, double alpha, double beta, double sigma, const cv::Size& new_size)
	{
		const auto blurred = imp::imagej_gaussian_blur(image, sigma);
		const auto small_blur = downscale_sum<imp::FLOATING_TYPE>(blurred, get_magnification(image, new_size));
		return affine_transform(small_blur, alpha, beta);
	}

	std::optional<Parameters> calculate_alpha_beta(double* xA, double* y, double* oneA, const size_t n_elements)
	{        
		double N = 0;
        for(auto idx = 0; idx < n_elements; idx++)
        {
            N += oneA[idx] * oneA[idx];
        }

        // std::cout << std::format("N: {}\n", N);        

        double xATxA = 0.0; 
        double xAT1A = 0.0; 
        double yTxA = 0.0; 
        double yT1A = 0.0;

        for(int idx = 0; idx < n_elements; idx++)
        {
            yTxA += y[idx] * xA[idx];
            yT1A += y[idx] * oneA[idx];
            xAT1A += xA[idx] * oneA[idx];
            xATxA += xA[idx] * xA[idx];
        }
        // std::cout << std::format("xATxA: {}\nxAT1A: {}\nyTxA: {}\nyT1A: {}\n", xATxA, xAT1A, yTxA, yT1A);

        double numerator = N * yTxA - yT1A * xAT1A;
        double denominator = N * xATxA - xAT1A * xAT1A;
        double alphaHat = numerator / denominator;
        double betaHat = yT1A / N - alphaHat * (xAT1A / N);

        // std::cout << std::format("numerator: {}\ndenominator: {}\nalphaHat: {}\nbetaHat: {}\n", numerator, denominator, alphaHat, betaHat);
        // std::exit(9);
        Parameters p;
        p.alpha = alphaHat;
        p.beta = betaHat;
        return p;
	}

	std::optional<Parameters> calculate_alpha_beta_from_images(const cv::Mat& wf, const cv::Mat& sr, const cv::Mat& ones)
	{
        const auto sr_data = (double* const)sr.data;
        const auto ones_data = (double* const)ones.data;
        const auto wf_image_data = (double* const)wf.data;
        const auto n_elements = wf.rows * wf.cols;
		return calculate_alpha_beta(sr_data, wf_image_data, ones_data, n_elements);
	}


	std::optional<Parameters> optimal_alpha_beta(const cv::Mat& wf_image, const cv::Mat& sr_image, double sigma)
	{
		// std::cout << std::format("optimal with: {}\n", sigma);
		const auto sr_blurred = blur_and_downscale(sr_image, sigma, wf_image.size());
		if (!sr_blurred)
			return {};

		// the maths would require this but because of how imageJ replicates the border on a gaussian blur
		// the equivalent is to just make an array of ones at wf size.
		// If a role off is requried then the border behaviour needs to be specified.

		// const auto ones = cv::Mat::ones(sr_image.size(), imp::CV_FLOATING_DEPTH);  
		// const auto blurred_ones = imp::imagej_gaussian_blur(ones, sigma);
		// const auto magnification = sr_image.rows / wf_image.rows;
		// const auto ones_blurred = downscale_average(blurred_ones, magnification);		
        // return calculate_alpha_beta_from_images(wf_image, *sr_blurred, ones_blurred);

		const auto ones = cv::Mat::ones(wf_image.size(), imp::CV_FLOATING_DEPTH);  
        return calculate_alpha_beta_from_images(wf_image, *sr_blurred, ones);
	}

	std::optional<nlopt::algorithm> parse(const std::string& name)
	{
		const auto i = nlopt_algorithm_from_string(name.c_str());
		if (i < 0)
			return {};
		return nlopt::algorithm(i);
	}

	nlopt::algorithm get_algorithm(const Settings& settings)
	{
		return settings.optimiser_algorithm()
					   .and_then(parse)
					   .value_or(nlopt::LN_NELDERMEAD);
	}

	void get_alphas(const cv::Mat& widefield, const cv::Mat& sr_image, const double beta_estimation, const double sigma_estimation, double (&alpha_values)[3])
    {
    	const auto widefield_mean = imp::mean(widefield - beta_estimation);
    	const auto sr_mean = imp::mean(imp::imagej_gaussian_blur(sr_image, sigma_estimation));
    	const auto alpha_estimation = widefield_mean / sr_mean;
    	alpha_values[0] = 0.000000001;
    	alpha_values[1] = 20000;
    	alpha_values[2] = alpha_estimation;
    }

    void get_betas(const cv::Mat& widefield, double (&beta_values)[3])
    {
    	double min_value;
    	double max_value;
    	imp::get_min_max(widefield, &min_value, &max_value);
    	beta_values[0] = min_value;
    	beta_values[1] = max_value;
    	beta_values[2] = min_value;
    }

    void get_sigmas(double magnification, double (&sigma_values)[3])
    {
    	const auto max_sigma = 6.0 * magnification;
    	const auto sigma_start = 2.0 * magnification;
    	sigma_values[0] = 0.000000001;
    	sigma_values[1] = max_sigma;
    	sigma_values[2] = sigma_start;
    }

    void display_solver_stop_reason(int result_code)
    {
    	const auto result_reason = nlopt_result_to_string((nlopt_result)result_code);
        std::cout << std::format("Optimisation stopped: {}\n", result_reason);
    }

    void set_solver_stopping_criteria(const Settings& settings, nlopt::opt& optimiser)
    {
    	optimiser.set_ftol_rel(1e-10);
		optimiser.set_ftol_abs(1e-14);
		optimiser.set_maxeval(1000);
    }

	double parameter_of(const std::vector<double>& theta, std::vector<double>& grad, void* f_data)
	{
		OF_Data* data = (OF_Data*)f_data;
		const cv::Mat& wf_image = data->wf_image();
		const cv::Mat& sr_image = data->sr_image();
		const auto sigma = theta[0];
		// std::cout << std::format("Calculating with sigma: {}\n", sigma);
		const auto optimal = optimal_alpha_beta(wf_image, sr_image, sigma);
		if (!optimal)
		{
    		std::cout << std::format("{} -> failed\n", sigma);
    		constexpr double rmse = std::numeric_limits<double>::max();    		
			data->add_location(0.0, 0.0, sigma, rmse);
			return rmse;
			// return NAN;
		}
		// std::cout << std::format("alpha: {} beta: {} sigma: {}\n", optimal->alpha, optimal->beta, sigma);
		// std::cout << std::format("Optimal sigma: {}\n", sigma);
		auto image_transformed = blur_downsample_affine(sr_image, optimal->alpha, optimal->beta, sigma, wf_image.size());
		//image_transformed.resize(wf_image.size());
    	const auto rmse = image_rmse(wf_image, image_transformed);
		data->add_location(optimal->alpha, optimal->beta, sigma, rmse);
    	// std::cout << std::format("{} -> rmse: {}\n", sigma, rmse);
    	// std::exit(2);
    	return rmse;
	}

	std::optional<Parameters> nlopt_single_parameter(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings, const double magnification)
	{
		auto results = SolverResults();
		auto data = OF_Data(results, widefield, sr_image);
		
		constexpr auto N_PARAMS = 1;
		const auto alg = get_algorithm(settings);
		auto optimiser = nlopt::opt(alg, N_PARAMS);
		std::cout << std::format("Using: {} for optimisation\n", optimiser.get_algorithm_name());

		optimiser.set_min_objective(parameter_of, (void*)&data);

		double sigma_parameters[3];
		get_sigmas(magnification, sigma_parameters);
        const auto min_sigma = sigma_parameters[0];
        const auto max_sigma = sigma_parameters[1];
        const auto initial_sigma = sigma_parameters[2];
        std::cout << std::format("sigma space: {},{},{}\n", min_sigma, max_sigma, initial_sigma);
		
		const auto lower_bounds = std::vector<double>{min_sigma};
		optimiser.set_lower_bounds(lower_bounds);

		const auto upper_bounds = std::vector<double>{max_sigma};
		// std::cout << std::format("Bounds: {}-{}\n", 0, max_sigma);
		optimiser.set_upper_bounds(upper_bounds);
		
		set_solver_stopping_criteria(settings, optimiser);
	
		auto initial_guess = std::vector<double>{initial_sigma};
		double double_ll = std::numeric_limits<double>::max();
		const auto result_code = optimiser.optimize(initial_guess, double_ll);
		display_solver_stop_reason(result_code);
		if (result_code < 0)
		{
			std::cout << "Optimisation Failed: " << result_code << "\n";
			return {};
		}
		
		// clip to max
		const auto found_sigma = initial_guess[0];
		const auto clipped = found_sigma > max_sigma;
		results.set_clippped(clipped);

		if (settings.write_optimiser_data())
		{
			const auto filename = settings.output_directory() / "optimiser_data";
			std::ofstream stream = std::ofstream(filename, std::ios::out | std::ios::trunc);
			results.write_to(stream);
		}

		const auto sigma = std::min(initial_guess[0], max_sigma);
		auto parameters = optimal_alpha_beta(widefield, sr_image, sigma);
		if (parameters)
		{
			parameters->sigma = sigma;
		}

		return parameters;
	}

	double multi_parameter_of(const std::vector<double>& theta, std::vector<double>& grad, void* f_data)
    {
        OF_Data* data = (OF_Data*)f_data;
        const cv::Mat& wf_image = data->wf_image();
        const cv::Mat& sr_image = data->sr_image();
        const auto alpha = theta[0];
        const auto beta = theta[1];
        const auto sigma = theta[2];
        // std::cout << std::format("alpha: {} beta: {} sigma: {}\n", alpha, beta, sigma);
        try
        {
        	auto image_transformed = blur_downsample_affine(sr_image, alpha, beta, sigma, wf_image.size());
        	const auto rmse = image_rmse(wf_image, image_transformed);
        	data->add_location(alpha, beta, sigma, rmse);
        	return rmse;
        }
        catch(std::exception e)
        {
        	std::cout << std::format("{},{},{} -> failed: {}\n", alpha, beta, sigma, e.what());
    		constexpr double rmse = std::numeric_limits<double>::max();
			data->add_location(alpha, beta, sigma, rmse);
			return rmse;
        }
        
    }
   
    std::optional<Parameters> nlopt_three_parameter_solve(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings, const double magnification)
    {
        auto results = SolverResults();
        auto data = OF_Data(results, widefield, sr_image);     
        constexpr auto N_PARAMS = 3;
        const auto alg = get_algorithm(settings);
        auto optimiser = nlopt::opt(alg, N_PARAMS);
		std::cout << std::format("Using: {} for optimisation\n", optimiser.get_algorithm_name());

        optimiser.set_min_objective(multi_parameter_of, (void*)&data);
       	
       	double sigma_parameters[3];
		get_sigmas(magnification, sigma_parameters);
        const auto min_sigma = sigma_parameters[0];
        const auto max_sigma = sigma_parameters[1];
        const auto initial_sigma = sigma_parameters[2];
        std::cout << std::format("sigma space: {},{},{}\n", min_sigma, max_sigma, initial_sigma);

        double beta_parameters[3];
        get_betas(widefield, beta_parameters);
        const auto min_beta = beta_parameters[0];
        const auto max_beta = beta_parameters[1];
        const auto initial_beta = beta_parameters[2];
        std::cout << std::format("beta space: {},{},{}\n", min_beta, max_beta, initial_beta);

        double alpha_parameters[3];
		get_alphas(widefield, sr_image, initial_beta, initial_sigma, alpha_parameters);

        const auto min_alpha = alpha_parameters[0];
        const auto max_alpha = alpha_parameters[1];
        const auto initial_alpha = alpha_parameters[2];
        std::cout << std::format("alpha space: {},{},{}\n", min_alpha, max_alpha, initial_alpha);
       
        auto initial_guess = std::vector<double>{initial_alpha, initial_beta, initial_sigma};
        const auto lower_bounds = std::vector<double>{min_alpha, min_beta, min_sigma};
        optimiser.set_lower_bounds(lower_bounds);

        const auto upper_bounds = std::vector<double>{max_alpha, max_beta, max_sigma};
        optimiser.set_upper_bounds(upper_bounds);

        set_solver_stopping_criteria(settings, optimiser);
  
        double double_ll = std::numeric_limits<double>::max();
        const auto result_code = optimiser.optimize(initial_guess, double_ll);
        display_solver_stop_reason(result_code);
        if (result_code < 0)
        {
            std::cout << "Optimisation Failed: " << result_code << "\n";
            return {};
        }
        const auto found_alpha = initial_guess[0];
        const auto found_beta = initial_guess[1];
        const auto found_sigma = initial_guess[2];
        // clip sigma to max
        const auto clipped = found_sigma > max_sigma;
        results.set_clippped(clipped);

        if (settings.write_optimiser_data())
        {
            const auto filename = settings.output_directory() / "optimiser_data";
            std::ofstream stream = std::ofstream(filename, std::ios::out | std::ios::trunc);
            results.write_to(stream);
        }

        const auto sigma = std::min(found_sigma, max_sigma);
        auto alt_parameters = optimal_alpha_beta(widefield, sr_image, sigma);
        std::cout << std::format("alt {},{},{}\n", alt_parameters->alpha, alt_parameters->beta, sigma);

        Parameters found_parameters;
        found_parameters.alpha = found_alpha;
        found_parameters.beta = found_beta;
        found_parameters.sigma = sigma;
        std::cout << std::format("found {},{},{}\n", found_parameters.alpha, found_parameters.beta, found_parameters.sigma);
        return found_parameters;
    }

	std::optional<Parameters> search_for_parameters(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings, const double magnification)
	{
		if (settings.three_parameter_solve())
		{
			std::cout << "using 3 parameter solver\n";
			return nlopt_three_parameter_solve(widefield, sr_image, settings, magnification);
		}
		std::cout << "using single parameter uniqorn solver\n";
		return nlopt_single_parameter(widefield, sr_image, settings, magnification);
	}

	std::ostream& write_registration_details_to(std::ostream& stream, const std::string& method, bool success, double dim_0_shift, double dim_1_shift)
	{
		return stream << std::format("method,success,dim_0,dim_1\n{},{},{},{}", method, success, dim_0_shift, dim_1_shift);
	}

	void write_registration_details(const std::filesystem::path& filename, const std::string& method, bool success, double dim_0_shift, double dim_1_shift)
	{
		std::ofstream stream = std::ofstream(filename.string(), std::ios::out | std::ios::trunc);
		write_registration_details_to(stream, method, success, dim_0_shift, dim_1_shift);
	}

	std::optional<cv::Mat> diplib_subpixel_register_images(const cv::Mat& big_widefield, const cv::Mat& sr_image, const Settings& settings)
	{		
		const auto dip_widefield = dip_opencv::MatToDip(big_widefield);
		const auto dip_sr = dip_opencv::MatToDip(sr_image);
		
		const auto method = settings.registration_method();
		const auto registration_details_filename = settings.output_directory() / "registration_details";
		try
		{
			const auto shift =  dip::FindShift(dip_sr, dip_widefield, method);
			std::cout << std::format("shift by {},{} (r,c)\n", shift[1], shift[0]);			
			const auto shifted = dip::Shift(dip_sr, shift);
			auto shifted_image = dip_opencv::CopyDipToMat(shifted);
			const auto shifted_filename = settings.output_directory() / "shifted_image.tiff";
			imp::write_tiff_to(shifted_image, shifted_filename);
			write_registration_details(registration_details_filename, method, true, shift[0], shift[1]);
			return shifted_image;
		}
		catch (std::exception& e)
		{
			std::cout << std::format("Registration failed: {}\n", e.what());
			write_registration_details(registration_details_filename, method, false, 0.0, 0.0);
			return {};
		}
	}
	
	std::optional<cv::Mat> area_registration(const cv::Mat& big_widefield, const cv::Mat& sr_image, const Settings& settings)
	{
		constexpr int MIN_SHIFT = -10;
		constexpr int MAX_SHIFT = (-MIN_SHIFT) + 1;

		const auto dip_sr = dip_opencv::MatToDip(sr_image);

		int best_row = MIN_SHIFT;
		int best_col = MIN_SHIFT;
		double best = 0.0;
		const auto registration_details_filename = settings.output_directory() / "registration_details";
		try
		{
			for (auto shift_row = MIN_SHIFT; shift_row < MAX_SHIFT; shift_row++)
			{
				for (auto shift_col = MIN_SHIFT; shift_col < MAX_SHIFT; shift_col++)
				{
					const auto shift = dip::FloatArray{static_cast<double>(shift_col), static_cast<double>(shift_row)};
					const auto shifted = dip::Shift(dip_sr, shift);
					const auto shifted_image = dip_opencv::CopyDipToMat(shifted);
					const auto score = image_pearsons(big_widefield, shifted_image);
					if (score > best)
					{
						best_row = shift_row;
						best_col = shift_col;
						best = score;
					}
				}
			}
			
			const auto shift = dip::FloatArray{static_cast<double>(best_col), static_cast<double>(best_row)};
			std::cout << std::format("shift by {},{} (r,c)\n", best_row, best_col);
			const auto shifted = dip::Shift(dip_sr, shift);
			const auto shifted_image = dip_opencv::CopyDipToMat(shifted);
			const auto shifted_filename = settings.output_directory() / "shifted_image.tiff";
			imp::write_tiff_to(shifted_image, shifted_filename);
			write_registration_details(registration_details_filename, std::string(REGISTRATION_AREA), true, shift[0], shift[1]);
			return shifted_image;
		}
		catch (std::exception& e)
		{
			std::cout << std::format("Registration failed: {}\n", e.what());
			write_registration_details(registration_details_filename, std::string(REGISTRATION_AREA), false, 0.0, 0.0);
			return {};
		}
	}

	std::optional<cv::Mat> register_images(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings, double magnification)
	{
		const auto big_widefield = upscale_with_replication<imp::FLOATING_TYPE>(widefield, magnification);
		const auto big_wf_filename = settings.output_directory() / "big_widefield.tiff";
		imp::write_tiff_to(big_widefield, big_wf_filename);
		
		const auto method = settings.registration_method();
		std::cout << std::format("Registering using {} method\n", method);
		if (method == REGISTRATION_AREA)
			return area_registration(big_widefield, sr_image, settings);
		else
			return diplib_subpixel_register_images(big_widefield, sr_image, settings);
	}

	int downstep_index(int index, double magnification)
	{
		// std::cout << std::format("mag: {}\n", magnification);
		const double index_d = (double)index;
		const double new_index_value = index_d / magnification;
		const auto new_index = std::floor(new_index_value);
		// std::cout << std::format("{} -> {}\n", index, new_index);
		return (int) new_index;
	}

	void get_magnified_border(const Border& sr_border, Border& wf_border, double magnification)
	{
		wf_border.top = downstep_index(sr_border.top, magnification);
		wf_border.left = downstep_index(sr_border.left, magnification);
		wf_border.bottom = downstep_index(sr_border.bottom, magnification);
		wf_border.right = downstep_index(sr_border.right, magnification);
	}

	Border Border::upscale_wf_by(int value) const
	{
		const auto x = value - 1;
		auto border = Border();
		border.top = top * value;
		border.left = left * value;
		border.bottom = (bottom * value) + x;
		border.right = (right * value) + x;
		return border;
	}

	cv::Rect Border::to_rect() const
	{
		const auto x = left;
	    const auto y = top;
	    const auto width = right - left + 1;
	    const auto height = bottom - top + 1;
	    // std::cout << std::format("left: {} top: {} right: {} bottom: {}\n", left, top, right, bottom);
	    // std::cout << std::format("x: {} y: {} w: {} h: {}\n", x, y, width, height);
        return cv::Rect(x, y, width, height);
	}

	cv::Rect get_rect(int margin, int n_rows, int n_cols)
	{
		const auto x = margin;
		const auto y = margin;
		const auto width = n_cols - (margin * 2);
		const auto height = n_rows - (margin * 2);
        return cv::Rect(x, y, width, height);		
	}

	cv::Rect get_rect_from(const cv::Mat& image, int margin)
	{
		return get_rect(margin, image.rows, image.cols);
	}

	std::ostream& write_metrics_to(std::ostream& stream, const double rmse, const double pearsons)
	{
		return stream << std::format("rmse,pearons\n{},{}", rmse, pearsons);
	}

	void write_metrics(const std::filesystem::path& filename, const double rmse, const double pearsons)
	{
		std::ofstream stream = std::ofstream(filename.string(), std::ios::out | std::ios::trunc);
		write_metrics_to(stream, rmse, pearsons);
	}

	void create_error_map_from(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings, const double magnification)
	{
		std::cout << "Searching for parameters\n";
		const auto parameters = search_for_parameters(widefield, sr_image, settings, magnification);
		if (!parameters)
		{
			std::cout << "Optimisation failed\n";
			return;
		}
		std::cout << std::format("alpha: {} beta: {} sigma: {}\n", parameters->alpha, parameters->beta, parameters->sigma);

		const auto blurred_sr = imp::imagej_gaussian_blur(sr_image, parameters->sigma);
		const auto small_sr_blurred = downscale_sum<imp::FLOATING_TYPE>(blurred_sr, magnification);
		const auto small_blurred_sr_affine_transformed = affine_transform(small_sr_blurred, parameters->alpha, parameters->beta);
		const auto new_sr_filename = settings.output_directory() / "sr_affine_blurred.tiff";
		imp::write_tiff_to(small_blurred_sr_affine_transformed, new_sr_filename);

		const auto wf_crop_rect = get_rect_from(widefield, settings.wf_border_size());
		const auto wf_crop = widefield(wf_crop_rect);
		const auto wf_crop_filename = settings.output_directory() / "wf_crop.tiff";
		imp::write_tiff_to(wf_crop, wf_crop_filename);

		const auto small_sr_crop = small_blurred_sr_affine_transformed(wf_crop_rect);
		const auto small_sr_crop_filename = settings.output_directory() / "small_sr_crop.tiff";
		imp::write_tiff_to(small_sr_crop, small_sr_crop_filename);

		const auto rmse = image_rmse(wf_crop, small_sr_crop);
		const auto pearsons = image_pearsons(wf_crop, small_sr_crop);
		std::cout << std::format("RSP (Resolution Scaled Pearson-Correlation): {}\nRSE (Resolution Scaled Error): {}\n", pearsons, rmse);

		const auto metrics_filename = settings.output_directory() / "metrics";
		write_metrics(metrics_filename, rmse, pearsons);

		const auto small_error_map = error_map(widefield, small_blurred_sr_affine_transformed, settings.show_positive_negative());
		const auto small_error_map_filename = settings.output_directory() / "small_error_map.tiff";
		imp::write_tiff_to(small_error_map, small_error_map_filename);
		
		auto big_map = upsample_with_interpolation(small_error_map, sr_image.size());
		if (!settings.show_positive_negative())
		{
			imp::abs_mut<imp::FLOATING_TYPE>(big_map);
		}
		const auto big_error_map_filename = settings.output_directory() / "big_map.tiff";
		imp::write_tiff_to(big_map, big_error_map_filename);

		const auto sr_crop_rect = get_rect_from(sr_image, settings.wf_border_size() * magnification);
		cv::Mat error_map = cv::Mat::zeros(big_map.size(), CV_FLOATING_DEPTH);
		// cv::Mat roi = error_map(sr_crop_rect);
		big_map(sr_crop_rect).copyTo(error_map(sr_crop_rect));
		const auto error_map_filename = settings.output_directory() / "error_map.tiff";
		imp::write_tiff_to(error_map, error_map_filename);

		// return map;
	}

	double estimate_magnification_from(int wf, int sr)
	{
		return ((double)sr) / ((double)wf);
	}

	double estimate_magnification(const cv::Mat& wf, const cv::Mat& sr)
	{
		return estimate_magnification_from(wf.cols, sr.cols);
	}

	std::vector<Patch> get_patches(int n_rows, int n_cols, const Settings& settings)
	{
		const auto patch_size = settings.patch_size();
		const auto step_size = settings.step_size();
		auto patches = std::vector<Patch>();
		for (auto row = 0; row < n_rows; row+=step_size)
		{
			const auto row_end = row + patch_size;
			if (row_end > n_rows - 1)
				return patches;
			for (auto col = 0; col < n_cols; col+=step_size)
			{
				const auto col_end = col + patch_size;
				if (col_end > n_cols - 1)
					break;
				patches.emplace_back(Patch::from(row, row_end, col, col_end));
			}
		}
		return patches;
	}

	std::vector<Patch> get_patches_over(const cv::Mat& image, const Settings& settings)
	{
		return get_patches(image.rows, image.cols, settings);
	}

	void process_patch(const cv::Mat& wf, const cv::Mat sr, const Settings& settings, const Patch& patch, const double magnification, PWResults& results)
	{
		const auto sr_patch_size = settings.patch_size();
		const auto wf_patch_size = downstep_index(sr_patch_size, magnification);

		const auto sr_roi = patch.to_rect();
		cv::Mat sr_patch = sr(sr_roi);

		const auto wf_x = downstep_index(sr_roi.x, magnification);
		const auto wf_y = downstep_index(sr_roi.y, magnification);
		const auto wf_roi = cv::Rect(wf_x, wf_y, wf_patch_size, wf_patch_size);
		cv::Mat wf_patch = wf(wf_roi);

		const auto parameters = search_for_parameters(wf_patch, sr_patch, settings, magnification);
		if (!parameters)
			return;
		const auto row = patch.centre_row();
		const auto col = patch.centre_col();
		results.add(row, col, *parameters);
	}

	void process_mt(const cv::Mat& wf, const cv::Mat sr, const Settings& settings, double magnification, const std::vector<Patch>& patches, PWResults& results, std::atomic_size_t* patch_counter)
	{
		while(*patch_counter < patches.size())
        {
            const auto patch_idx = patch_counter->fetch_add(1, std::memory_order::relaxed);
			std::cout << std::format("patch: {} / {}\n", patch_idx + 1, patches.size());
			const auto patch = patches[patch_idx];
			process_patch(wf, sr, settings, patch, magnification, results);
        }
	}

	void process_patches_mt(const cv::Mat& wf, const cv::Mat sr, const Settings& settings, double magnification, const std::vector<Patch>& patches,  PWResults& results)
	{
        auto threads = std::vector<std::thread>();
        auto counter = std::atomic_size_t{0};
        const auto n_threads = imp::usable_threads(settings.n_threads());
        for(unsigned int t = 0; t < n_threads; t++)
        {
            threads.push_back(std::thread(&process_mt, wf, sr, settings, magnification, patches, std::ref(results), &counter));
        }
        for (auto& thread : threads) 
        {
            thread.join();
        }
	}

	void process_patches(const cv::Mat& wf, const cv::Mat sr, const Settings& settings, double magnification, const std::vector<Patch>& patches,  PWResults& results)
	{
		for (auto idx = 0; idx < patches.size(); idx++)
		// for (const auto [index, patch] : std::views::enumerate(patches))
		{
			const auto& patch = patches[idx];
			const auto index = idx;
			std::cout << std::format("patch: {} / {}\n", index + 1, patches.size());
			process_patch(wf, sr, settings, patch, magnification, results);
		}
	}

	void create_patchwise_error_map_from(const cv::Mat& wf, const cv::Mat& sr, const Settings& settings, double magnification)
	{	
		const auto patches = get_patches_over(sr, settings);
		auto results = PWResults::from(sr.size());
		if (settings.use_mt())
		{
			process_patches_mt(wf, sr, settings, magnification, patches, results);
		}
		else
		{
			process_patches(wf, sr, settings, magnification, patches, results);
		}
		results.write_to(settings.output_directory());
	}

	bool run_squirrel(const cv::Mat& orig_widefield, const cv::Mat& orig_sr_image, const Settings& settings)
	{
		const auto output_directory = settings.output_directory();
		if (!std::filesystem::exists(output_directory))
		{
			std::cout << std::format("creating: {}\n", output_directory.string());
			if (!std::filesystem::create_directories(output_directory))
			{
				std::cerr << "Could not create output directory\n";
				return false;
			}
		}

		auto sr_image = orig_sr_image.clone();
		auto widefield = orig_widefield.clone();

		const auto magnification = estimate_magnification(widefield, sr_image);
		std:: cout << "magnification: " << magnification << "\n";
		if (!is_whole(magnification))
		{
			std::cout << std::format("magnification of {} is not a whole number", magnification);
			return false;
		}

		if (settings.perform_registration())
		{
			try
			{
				std::cout << "Registering Images\n";
				const auto maybe_image = register_images(widefield, sr_image, settings, magnification);
				if(maybe_image)
				{
					sr_image = (*maybe_image).clone();
				}
			}
			catch (const std::exception& e)
			{
				std::cout << "Registration failed: " << e.what() << "\n";
			}
			// cv::namedWindow("new sr", cv::WINDOW_NORMAL);
			// cv::imshow("new sr", new_sr);
		}

		// if (settings.crop_borders())
		// {
		// 	const auto cropped_images = crop_borders(widefield, sr_image, magnification);
		// 	if (cropped_images)
		// 	{
		// 		widefield = std::get<0>(*cropped_images);
		// 		sr_image = std::get<1>(*cropped_images);
		// 	}
		// 	else
		// 	{
		// 		std::cout << "No border to crop\n";
		// 	}
		// }		
		
		if (settings.patchwise())
		{
			create_patchwise_error_map_from(widefield, sr_image, settings, magnification);
		}
		else
		{
			create_error_map_from(widefield, sr_image, settings, magnification);
		}
		return true;
	}

	void display_algorithms()
	{
		for (int algorithm_idx = 0; algorithm_idx != NLOPT_NUM_ALGORITHMS; algorithm_idx++ )
		{
			auto alg = static_cast<nlopt_algorithm>(algorithm_idx);
			std::cout << std::format("{} = {}\n", algorithm_idx, nlopt_algorithm_to_string(alg));
		}
	}

	int run(const Settings& settings)
	{
		if (settings.display_algorithms())
		{
			display_algorithms();
			return 0;
		}
		std::cout << "Loading reference image: " << settings.wf_image() << "\n";
        auto ref_cv_image = imp::load_tiff(settings.wf_image());
        auto ref_cv = ref_cv_image.open_cv_image();
        ref_cv.convertTo(ref_cv, CV_FLOATING_DEPTH);


        std::cout << "Loading super resolution image: " << settings.sr_image() << "\n";
        auto test_cv_image = imp::load_tiff(settings.sr_image());
        cv::Mat test_cv = test_cv_image.open_cv_image();
        test_cv.convertTo(test_cv, CV_FLOATING_DEPTH);


        if(run_squirrel(ref_cv, test_cv, settings))
			return 0;
		return 1;
	}
}
