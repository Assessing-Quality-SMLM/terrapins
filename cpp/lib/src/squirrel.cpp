#include "imp.hpp"
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

    class SolverResults
    {
    public:
    	explicit SolverResults()
    	{
    	}

		void add_location(double sigma, double rmse)
		{
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
    		stream << std::format("sigma,rmse\n");
    		// for (std::tuple<const double&, const double&> item : std::views::zip(sigmas_, rmse_))
    			// stream << std::format("{},{}\n", std::get<0>(item), std::get<1>(item));
    		for(auto idx = 0; idx < n_elements(); idx++)
    		{
    			stream << std::format("{},{}\n", sigmas_[idx], rmse_[idx]);
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

		void add_location(double sigma, double value)
		{
			results_.add_location(sigma, value);
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

	cv::Mat cv_magnify(const cv::Mat& image, cv::Size new_size)
	{
		cv::Mat new_image;
		cv::resize(image, new_image, new_size, 0, 0, cv::INTER_CUBIC);
		return new_image;
	}

	cv::Mat diplib_magnify(const cv::Mat& image, cv::Size new_size)
	{
		// std::cout << "new size - height: " << new_size.height << " width: " << new_size.width << "\n";
		const auto dip_image = dip_opencv::MatToDip(image);
 		// auto s = dip_image.Sizes();
 		const double zoom_rows = ((double)new_size.height) / ((double)image.rows);
 		const double zoom_cols = ((double)new_size.width) / ((double)image.cols);
 		// std::cout << std::format("{} X {}\n", zoom_rows, zoom_cols);
 		// std::cout << std::format("{} X {}\n", s[0], s[1]);
 		const auto resampled = dip::Resampling(dip_image, {zoom_cols, zoom_rows}, {0, 0}, "4-cubic");
 		// s = resampled.Sizes();
 		// std::cout << std::format("{} X {}\n", s[0], s[1]);
		return dip_opencv::CopyDipToMat(resampled);
	}

	cv::Mat magnify(const cv::Mat& image, cv::Size new_size)
	{
		// return cv_magnify(image, new_size);
		return diplib_magnify(image, new_size);
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
		// std::cout << "Affine transform\n";
		const auto new_image = affine_transform(image, alpha, beta);
		// std::cout << std::format("Blurring with sigma: {}\n", sigma);
		return imp::imagej_gaussian_blur(new_image, sigma);
	}

	cv::Mat downscale(const cv::Mat& image, const cv::Size& new_size)
	{
		cv::Mat new_image;
		// std::cout << "resizing\n";
		// pixel area relation - suggested for decimation by opencv docs
		cv::resize(image, new_image, new_size, 0, 0, cv::INTER_NEAREST);
		// std::cout << "resize ok\n";
		return new_image;
	}

	// cv::Mat downscale(const cv::Mat& image, const cv::Size& new_size)
	// {
 	// 	const auto dip_image = dip_opencv::MatToDip(image);
 	// 	auto s = dip_image.Sizes();
 	// 	const double zoom_rows = ((double)new_size.height) / ((double)image.rows);
 	// 	const double zoom_cols = ((double)new_size.width) / ((double)image.cols);
 	// 	std::cout << std::format("{} X {}\n", zoom_rows, zoom_cols);
 	// 	std::cout << std::format("{} X {}\n", s[0], s[1]);
 	// 	const auto resampled = dip::Resampling(dip_image, {zoom_rows, zoom_cols}, {0, 0}, "nearest");
 	// 	s = resampled.Sizes();
 	// 	std::cout << std::format("{} X {}\n", s[0], s[1]);
	// 	return dip_opencv::CopyDipToMat(resampled);
	// }

	std::optional<cv::Mat> blur_and_downscale(const cv::Mat& image, double sigma, const cv::Size& image_size)
	{
		// std::cout << std::format("Blurring with {}\n", sigma);
		try
		{
			const auto blurred_image = imp::imagej_gaussian_blur(image, sigma);
			// std::cout << std::format("Blurr ok\n");
			return downscale(blurred_image, image_size);
		}
		catch(std::exception& e)
		{
			std::cout << "Something went wrong\n" << e.what() << "\n";
			return {};
		}
	}

	cv::Mat affine_blur_and_downscale(const cv::Mat& image, double alpha, double beta, double sigma, const cv::Size& new_size)
	{
		// std::cout << "Affine blur\n";
		auto blurred_image = affine_blur(image, alpha, beta, sigma);
		// std::cout << "Downscale\n";
		return downscale(blurred_image, new_size);
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
		// std::cout << std::format("sr blurred and downscaled\n");
		cv::Mat ones = cv::Mat::ones(sr_image.size(), imp::CV_FLOATING_DEPTH);
		// std::cout << std::format("ones blurred and downscaled\n");
        const auto ones_blurred = blur_and_downscale(ones, sigma, wf_image.size());
        if (!ones_blurred)
        	return {};
        return calculate_alpha_beta_from_images(wf_image, *sr_blurred, *ones_blurred);
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
    		const auto rmse = std::numeric_limits<double>::max();
			data->add_location(sigma, rmse);
			return rmse;
			// return NAN;
		}
		// std::cout << std::format("alpha: {} beta: {} sigma: {}\n", optimal->alpha, optimal->beta, sigma);
		// std::cout << std::format("Optimal sigma: {}\n", sigma);
		auto image_transformed = affine_blur_and_downscale(sr_image, optimal->alpha, optimal->beta, sigma, wf_image.size());
		//image_transformed.resize(wf_image.size());
    	const auto rmse = image_rmse(wf_image, image_transformed);
		data->add_location(sigma, rmse);
    	// std::cout << std::format("{} -> rmse: {}\n", sigma, rmse);
    	// std::exit(2);
    	return rmse;
	}

	std::optional<Parameters> search_for_parameters(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings)
	{
		auto results = SolverResults();
		auto data = OF_Data(results, widefield, sr_image);

		// const auto magnification = (double)(sr_image.cols / widefield.cols);
		const auto max_sigma = (settings.sigma_nm()  / settings.pixel_size_nm()) * 5.0;

		
		constexpr auto N_PARAMS = 1;
		// auto optimiser = nlopt::opt(nlopt::LN_PRAXIS, N_PARAMS);
		auto optimiser = nlopt::opt(nlopt::GN_DIRECT_L, N_PARAMS);

		optimiser.set_min_objective(parameter_of, (void*)&data);
		
		const auto lower_bounds = std::vector<double>{0};
		optimiser.set_lower_bounds(lower_bounds);

		const auto upper_bounds = std::vector<double>{max_sigma};		
		// std::cout << std::format("Bounds: {}-{}\n", 0, max_sigma);
		optimiser.set_upper_bounds(upper_bounds);
		optimiser.set_ftol_rel(1e-10);
		optimiser.set_ftol_abs(1e-14);
		optimiser.set_maxeval(1000);
	
		auto initial_guess = std::vector<double>{max_sigma / 2.0};
		// auto initial_guess = std::vector<double>{5.797620887171906};
		// auto initial_guess = std::vector<double>{5.0};
		double double_ll = std::numeric_limits<double>::max();
		const auto result_code = optimiser.optimize(initial_guess, double_ll);
		if (result_code < 0)
		{
			std::cout << "Optimisation Failed: " << result_code << "\n";
			return {};
		}
		// for (const auto sigma : data.attempts)
		// {
		// 	std::cout << std::format("{}\n", sigma);
		// }
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

	cv::Mat register_images(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings, double magnification)
	{
		const auto big_widefield = magnify(widefield, sr_image.size());
		const auto big_wf_filename = settings.output_directory() / "big_widefield.tiff";
		imp::write_tiff_to(big_widefield, big_wf_filename);
		// cv::namedWindow("big widefield");
		// cv::imshow("big widefield", big_widefield);
		const auto dip_widefield = dip_opencv::MatToDip(big_widefield);
		const auto dip_sr = dip_opencv::MatToDip(sr_image);
		const auto shift =  dip::FindShift(dip_sr, dip_widefield, "CC");
		std::cout << std::format("shift by");
		for (const auto& item : shift)
			std::cout << std::format(" {}", item);
		std::cout << "\n";
			
		const auto shifted = dip::Shift(dip_sr, shift);
		auto shifted_image = dip_opencv::CopyDipToMat(shifted);
		const auto shifted_filename = settings.output_directory() / "shifted_image.tiff";
		imp::write_tiff_to(shifted_image, shifted_filename);

		// const auto specific_shift = dip::FloatArray{-0.57713, -0.3501053};
		// // const auto specific_shifted = dip::Shift(dip_sr, specific_shift);
		// const auto specific_shifted = dip::Shift(dip_sr, specific_shift, "nearest", {"add zeros", "add zeros"});
		// auto specific_shifted_image = dip_opencv::CopyDipToMat(specific_shifted);
		// imp::write_tiff(specific_shifted_image, "specific_shifted_image.tiff");

		// std::exit(2);
		return shifted_image;
	}

	int get_top_border(const cv::Mat& image)
	{
		for (auto row = 0; row < image.rows; row++)
		{
            for(auto col = 0; col < image.cols; col++)
            {
                const auto value = image.at<imp::FLOATING_TYPE>(row, col);
                if(std::isfinite(value) && value > 0.0)
                	return row; 
            }
        }
        return 0;
	}

	int get_left_border(const cv::Mat& image)
	{
		for(auto col = 0; col < image.cols; col++)
		{
			for (auto row = 0; row < image.rows; row++)
            {
                const auto value = image.at<imp::FLOATING_TYPE>(row, col);
                if(std::isfinite(value) && value > 0.0)
                	return col;
            }
        }
        return 0;
	}

	int get_bottom_border(const cv::Mat& image)
	{
		const auto max_row = image.rows - 1;
		for (auto idx = 0; idx < image.rows; idx++)
		{
			const auto row = (image.rows - 1) - idx;
			for(auto col = 0; col < image.cols; col++)
            {
                const auto value = image.at<imp::FLOATING_TYPE>(row, col);
                if(std::isfinite(value) && value > 0.0)
                	return row;
            }
        }
        return max_row;
	}

	int get_right_border(const cv::Mat& image)
	{
		const auto max_col = image.cols - 1;
		for(auto idx = 0; idx < image.cols; idx++)
		{
			const auto col = (image.cols - 1) - idx;
			for (auto row = 0; row < image.rows; row++)
            {
                const auto value = image.at<imp::FLOATING_TYPE>(row, col);
                if(std::isfinite(value) && value > 0.0)
                	return col; 
            }
        }
        return max_col;
	}

	void get_border(const cv::Mat& image, Border& border)
	{
	    border.top = get_top_border(image);
	    border.left = get_left_border(image);
	    border.bottom = get_bottom_border(image);
	    border.right = get_right_border(image);
	}

	int downstep_index(int index, double magnification)
	{
		// std::cout << std::format("mag: {}\n", magnification);
		const double index_d = (double)index;
		const double new_index_value = index_d / magnification;
		const auto new_index = std::ceil(new_index_value);
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


	std::optional<std::tuple<cv::Mat, cv::Mat>> crop_borders(const cv::Mat& widefield, const cv::Mat& sr_image, double magnification)
	{
		auto sr_border = Border();
		get_border(sr_image, sr_border);
		if (sr_border.top == 0 && sr_border.left == 0 && sr_border.right == sr_image.cols - 1 && sr_border.bottom == sr_image.rows - 1)
		{
			return {};
		}

		auto wf_border = Border();
		get_magnified_border(sr_border, wf_border, magnification);

        const auto sr_rect = sr_border.to_rect();
		cv::Mat new_sr = sr_image(sr_rect);

		const auto wf_rect = wf_border.to_rect();
		cv::Mat new_wf = widefield(wf_rect);

		return std::make_tuple(new_wf, new_sr);
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

	void create_error_map_from(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings)
	{
		std::cout << "Searching for parameters\n";
		const auto parameters = search_for_parameters(widefield, sr_image, settings);
		if (!parameters)
		{
			std::cout << "Optimisation failed\n";
			return;
		}

		std::cout << std::format("alpha: {} beta: {} sigma: {}\n", parameters->alpha, parameters->beta, parameters->sigma);
		const auto new_sr = affine_blur(sr_image, parameters->alpha, parameters->beta, parameters->sigma);
		const auto new_sr_filename = settings.output_directory() / "sr_affine_blurred.tiff";
		imp::write_tiff_to(new_sr, new_sr_filename);
		
		const auto big_widefield = magnify(widefield, new_sr.size());
		const auto big_wf_filename = settings.output_directory() / "big_widefield_error_map.tiff";
		imp::write_tiff_to(big_widefield, big_wf_filename);

		const auto map = error_map(big_widefield, new_sr, settings.show_positive_negative());
		const auto filename = settings.output_directory() / "error_map.tiff";
		imp::write_tiff_to(map, filename);

		const auto small_sr = magnify(new_sr, widefield.size());
		const auto small_sr_filename = settings.output_directory() / "small_sr.tiff";
		imp::write_tiff_to(small_sr, small_sr_filename);
		const auto rmse = image_rmse(widefield, small_sr);
		const auto pearsons = image_pearsons(widefield, small_sr);
		std::cout << std::format("RSP (Resolution Scaled Pearson-Correlation): {}\nRSE (Resolution Scaled Error): {}\n", pearsons, rmse);

		const auto metrics_filename = settings.output_directory() / "metrics";
		write_metrics(metrics_filename, rmse, pearsons);
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

	void process_patch(const cv::Mat& wf, const cv::Mat sr, const Settings& settings, const Patch& patch, double magnification, PWResults& results)
	{
		const auto sr_patch_size = settings.patch_size();
		const auto wf_patch_size = downstep_index(sr_patch_size, magnification);

		const auto sr_roi = patch.to_rect();
		cv::Mat sr_patch = sr(sr_roi);

		const auto wf_x = downstep_index(sr_roi.x, magnification);
		const auto wf_y = downstep_index(sr_roi.y, magnification);
		const auto wf_roi = cv::Rect(wf_x, wf_y, wf_patch_size, wf_patch_size);
		cv::Mat wf_patch = wf(wf_roi);

		const auto parameters = search_for_parameters(wf_patch, sr_patch, settings);
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

		// cv::namedWindow("widefield", cv::WINDOW_NORMAL);
		// cv::imshow("widefield", orig_widefield);

		// cv::namedWindow("sr_image", cv::WINDOW_NORMAL);
		// cv::imshow("sr_image", orig_sr_image);

		auto widefield = orig_widefield;
		auto sr_image = orig_sr_image;

		const auto magnification = estimate_magnification(widefield, sr_image);
		std:: cout << "magnification: " << magnification << "\n";

		if (settings.crop_borders())
		{
			const auto cropped_images = crop_borders(orig_widefield, orig_sr_image, magnification);
			if (cropped_images)
			{
				widefield = std::get<0>(*cropped_images);
				sr_image = std::get<1>(*cropped_images);
			}
			else
			{
				std::cout << "No border to crop\n";
			}
		}

		if (settings.perform_registration())
		{
			try
			{
				std::cout << "Registering Images\n";
				const auto new_sr = register_images(widefield, sr_image, settings, magnification);			
				sr_image = new_sr;
			}
			catch (const std::exception& e)
			{
				std::cout << "Registration failed: " << e.what() << "\n";
			}
			// cv::namedWindow("new sr", cv::WINDOW_NORMAL);
			// cv::imshow("new sr", new_sr);
		}

		// cv::namedWindow("working wf", cv::WINDOW_NORMAL);
		// cv::imshow("working wf", widefield);
		
		// cv::namedWindow("working sr", cv::WINDOW_NORMAL);
		// cv::imshow("working sr", sr_image);
		// cv::waitKey(0);
		
		if (settings.patchwise())
		{
			create_patchwise_error_map_from(widefield, sr_image, settings, magnification);
		}
		else
		{
			create_error_map_from(widefield, sr_image, settings);
		}
		return true;
	}

	int run(const Settings& settings)
	{
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
