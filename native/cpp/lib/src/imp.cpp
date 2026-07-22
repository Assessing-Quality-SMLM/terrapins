#include "imp.hpp"
#include "hue_calculator.hpp"
#include "results.hpp"
#include "settings.hpp"

#include <opencv2/core.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/imgcodecs.hpp> // to set imread flag
#include <opencv2/imgproc.hpp> // boxFilter
#include <opencv2/core/types.hpp>

#include <diplib.h>
#include <dip_opencv_interface.h>
#include <diplib/binary.h>
#include <diplib/boundary.h>
#include <diplib/linear.h>

#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <format>
#include <filesystem>
#include <fstream>
#include <functional>
#include <ios>
#include <iostream>
#include <optional>
#include <thread>
#include <vector>

namespace imp 
{
	using BINARY_TYPE = unsigned char;
	// using FLOATING_TYPE = double;

    // constexpr int CV_FLOATING_DEPTH = CV_64F;
    // constexpr int CV_FLOATING_DEPTH = CV_64FC1;
    
    constexpr int CV_BINARY_DEPTH = CV_8U;

	// constexpr BINARY_TYPE DLIB_BINARY_ZERO = 0;
	// constexpr BINARY_TYPE DLIB_BINARY_ONE = 255;   

    // using GRAY_TYPE = unsigned char;
    // using GRAY_TYPE = float;
    using GRAY_TYPE = double;

    constexpr double MAX_HUE_LEVEL_CV_f64 = 179.0;


    Image load_tiff(const std::string& filename)
    {
        return Image::from_disk(filename);
    }

    bool write_tiff(const cv::Mat& image, const std::string& filename)
    {
        if (image.type() == CV_FLOATING_DEPTH)
        {
            cv::Mat new_image;
            image.convertTo(new_image, CV_32F);
            return cv::imwrite(filename, new_image);
        }
        return cv::imwrite(filename, image);
    }

    bool write_tiff_to(const cv::Mat& image, const std::filesystem::path& path)
    {
        return write_tiff(image, path.string());
    }

    void get_min_max(const cv::Mat& image, double* min_value, double* max_value)
    {
        cv::minMaxLoc(image, min_value, max_value, nullptr, nullptr );
    }

    double get_max(const cv::Mat& image)
    {
        double min_value;
        double max_value;
        get_min_max(image, &min_value, &max_value);
        return max_value;
    }

    double get_normaliser(const cv::Mat& image)
    {
        const double max = get_max(image);
        return 1.0 / max;
    }

    void normalise_image(cv::Mat& image)
    {
        const double norm = get_normaliser(image);
        // std::cout << std::format("Normalising with {}\n", norm);
        image *= norm;
    }    

    int get_kernel_size(int value)
    {
        return (2 * value) - 1;
    }

    int half_psf_kernel_size(double psf)
    {
        const double x = psf / 4.0;
        return get_kernel_size((int)std::ceil(x));
    }

    cv::Mat convert_to_new_type(const cv::Mat& image, int new_type)
    {
    	cv::Mat new_image;
        image.convertTo(new_image, new_type);
        return new_image;
    }

    cv::Mat convert_to_floating_point(const cv::Mat& image)
    {
    	return convert_to_new_type(image, CV_FLOATING_DEPTH);
    }

    cv::Mat convert_to_binary(const cv::Mat& image)
    {
        return convert_to_new_type(image, CV_BINARY_DEPTH);
    }

    cv::Mat box_filter_cv(const cv::Mat& image, int kernel_size, bool normalise, int border_type)
    {
        cv::Mat new_image;
        cv::boxFilter(image, 
                      new_image, 
                      -1, 
                      cv::Size(kernel_size, kernel_size),  
                      cv::Point(-1,-1), 
                      normalise,
                      border_type);
        return new_image;
    }

    cv::Mat imagej_convolve_cv(const cv::Mat& image, int kernel_size)
    {
    	return box_filter_cv(image, kernel_size, true, cv::BORDER_REPLICATE);
    }

    cv::Mat half_psf_blur_cv(const cv::Mat& image, double psf)
    {
    	cv::Mat new_image = image.clone();
        normalise_image(new_image);
        int kernel_size = half_psf_kernel_size(psf);
        return imagej_convolve_cv(new_image, kernel_size);
    }

    cv::Mat gaussian_blur_cv(const cv::Mat& image, double sigma, int border_type)
    {
    	cv::Mat new_image;
    	// Gaussian kernel size. ksize.width and ksize.height 
    	// can differ but they both must be positive and odd. 
    	// Or, they can be zero's and then they are computed from sigma.
        cv::Size kernel_size = {0, 0};
        cv::GaussianBlur(image, new_image, kernel_size, sigma, sigma, border_type);
        return new_image;
    }

    cv::Mat gaussian_blur_diplib(const cv::Mat& image, double sigma)
    {
        dip::Image cv_wrapper = dip_opencv::MatToDip(image);
        // std::cout << "sigma " << sigma << "\n";
        dip::Image output_image = cv_wrapper.Similar();
        dip::FloatArray sigmas = {sigma, sigma};
        dip::UnsignedArray derivative_order = {0};
        dip::String method = "best";
        // const auto bc = dip::BoundaryCondition::ZERO_ORDER_EXTRAPOLATE;
        dip::StringArray bc = {"zero order"};
        dip::dfloat truncation = 3;
        dip::Gauss(cv_wrapper, output_image, sigmas, derivative_order, method, bc, truncation);

        return dip_opencv::CopyDipToMat(output_image);
    }

    cv::Mat imagej_gaussian_blur(const cv::Mat& image, double sigma)
    {
    	// return gaussian_blur_cv(image, sigma, cv::BORDER_REPLICATE);
        return gaussian_blur_diplib(image, sigma); // many times faster - need it for optimisation loops e.g squirrel
    }

    cv::Mat gaussian_blur_and_normalise_cv(const cv::Mat& image, double sigma)
    {
        auto new_image = imagej_gaussian_blur(image, sigma);
        normalise_image(new_image);
        return new_image;
    }

    bool erode(cv::Mat& image, int kernel_size, int border_type)
    {
        auto kernel = cv::getStructuringElement(cv::MORPH_RECT, {kernel_size, kernel_size});
        cv::erode(image, image, kernel, cv::Point(-1, -1), 1, border_type);
        return true;
    }

    bool imagej_erode_cv(cv::Mat& image)
    {
    	return erode(image, 3, cv::BORDER_ISOLATED);
    }

    bool dilate(cv::Mat& image, int kernel_size, int border_type)
    {
        auto kernel = cv::getStructuringElement(cv::MORPH_RECT, {kernel_size, kernel_size});
        cv::dilate(image, image, kernel, cv::Point(-1, -1), 1, border_type);
        return true;
    }

    bool imagej_dilate_cv(cv::Mat& image)
    {
    	return dilate(image, 3, cv::BORDER_ISOLATED);
    }

    // bool dlib_skeletonise(cv::Mat& image)
    // {
    // 	auto cv_wrapper = dlib::cv_image<BINARY_TYPE>(image);
    //     dlib::skeleton(cv_wrapper);
    //     return true;
    // }

    bool diplib_skeletonise(cv::Mat& image)
    {
    	// std::cout << "converting to diplib\n";
    	auto cv_wrapper = dip_opencv::MatToDip(image) > 0;
    	// std::cout << "creating output image\n";
    	dip::Image output_image( dip::UnsignedArray{ 256, 256 }, 1, dip::DT_UINT8 );
    	// std::cout << "skeletonising\n";
    	dip::EuclideanSkeleton(cv_wrapper, output_image);
    	// std::cout << "copying out to MAT\n";
    	cv::Mat new_image =  dip_opencv::CopyDipToMat(output_image) * 255;
    	// std::cout << "copying back into image\n";
    	new_image.copyTo(image);
    	return true;
    }

    bool skeletonise(cv::Mat& image)
    {
    	// return dlib_skeletonise(image);
    	return diplib_skeletonise(image);
        // return true;
    }

    double mean(const cv::Mat& image)
    {
        return cv::mean(image)[0]; // single channel image so take the first channel
    }

    cv::Mat combine_to_single_image(const cv::Mat& r, const cv::Mat& g, const cv::Mat& b)
    {
        cv::Mat channels[3] = {b, g, r};
        cv::Mat new_image;
        cv::merge(channels, 3, new_image);
        return new_image;
    }

    cv::Mat x_or(const cv::Mat& image_a, const cv::Mat& image_b)
    {
        cv::Mat new_image;
        cv::bitwise_xor(image_a, image_b, new_image);
        return new_image;
    }

    std::vector<long> cumulative_sum(const std::vector<int>& histogram)
    {
        const auto n_bins = histogram.size();
        long sum_value = 0;
        auto cum_sum = std::vector<long>(n_bins, 0);
        for (int bin = 0; bin < n_bins; bin++) 
        {
            sum_value += (long)histogram[bin];
            cum_sum[bin] = sum_value;
        }
        return cum_sum;
    }

    size_t threshold_bin_number(const std::vector<int>& histogram, const std::vector<long>& cum_sum)
    {
        const auto n_bins = histogram.size();
        const auto max_bin = n_bins - 1;
        const double max_threshold = 0.98 * static_cast<double>(cum_sum[max_bin]);
        auto max_bin_number = max_bin;
        for (auto bin = max_bin; bin >= 0; bin--) 
        {
            const auto value = static_cast<double>(cum_sum[bin]);
            if (value < max_threshold)
            {
                return max_bin_number;
            }
            max_bin_number = bin;
            // if (cum_sum[bin] < (0.02 * cum_sum[n_bins])) 
            // {
            //     min_value = bin;
            // }
        }
        return max_bin_number;
    }

    double threshold_value(double max_value, size_t max_bin_number, size_t n_bins)
    {
        double sf = (double)max_bin_number / ((double)n_bins);
        return max_value * sf;
    }


    cv::Mat convert_hsv_to_bgr(const cv::Mat& image)
    {
        cv::Mat result;
        cv::cvtColor(image, result, cv::COLOR_HSV2BGR);
        return result;
    }

	namespace 
    {
		double sq(double x)
        {
			return x * x;
		}

		cv::Mat_<double> multiply(const cv::Mat& a, const cv::Mat& b)
        {
			cv::Mat_<double> ret;
			cv::multiply(a, b, ret, 1.0, CV_64F);
			return ret;
		}
	}

	cv::Mat_<double> box_filter_integral(const cv::Mat_<double>& in, int k_radius)
    {
		cv::Mat_<double> integral_image;
		cv::integral(in, integral_image, CV_64F);

		cv::Mat_<double> filtered;
		filtered.create(in.size());
		
		double norm = 1. / sq(k_radius * 2. + 1.0);

		for (int row = 0; row < in.rows; row++)
        {
			for (int col = 0; col < in.cols; col++)
            {
			   
				// Compute the integral image corners, clipped to the image size.
				// This will effectively pad with zeros.
				//
				// Note that cv::integral pads the leading row and column with zeros
				// in order to make computation of rectangles including the first
				// rown and column straightforward. 
				int row_low = std::max(0, row - k_radius); 
				int row_high = std::min(in.rows, row + k_radius + 1); 
				int col_low = std::max(0, col - k_radius); 
				int col_high = std::min(in.cols, col + k_radius + 1);

				double top_right = integral_image(row_high, col_high);
				double bottom_left = integral_image(row_low, col_low);
				double bottom_right = integral_image(row_low, col_high);
				double top_left = integral_image(row_high, col_low);

				filtered(row, col) = (top_right - bottom_right - top_left + bottom_left) * norm;
			}
        }
		return filtered;
	}

    //Compute pearson's correlation coefficient in square windows.
    //
    // Given inputs a, b:
    //        cov(a,b)
    // r = -------------
    //      sig_a sig_b
    //
    // Standard deviation can also be computed as mean of the squares minus square of the mean:
    //          ________________    ______________
    //  sig_a =√E[ (a - E(a))² ] = √ E[a²] - E[a]²
    //
    // Likewise the covariance can be expanded similarly:
    //
    //  cov(a,b) = E[ (a - E[a])(b - E[b]) ] = E[ab] - E[a]E[b]
    //
    //
    // Putting these together gives:
    //
    //                E[ab] - E[a]E[b]                       E[ab] - E[a]E[b]             E[ab] - E[a]E[b]
    //  r = -----------------------------------  =  ---------------------------------- =  -----------------
    //        ______________   ______________        ________________________________      _______________
    //       √ E[a²] - E[a]²  √ E[b²] - E[b]²       √ (E[a²] - E[a]²)(E[b²] - E[b]²)      √ var(a) var(b) 
    //
    // 
    //  Note   (E[a²] - E[a]²) = var(a)
    //
    // E[.] is the expectation, i.e. mean. Mean over an area is a box filter.
    // Box filters can be implemented in O(1) time with respec to the size so
    // computing only expectations is very efficient.

    // Note that integral images are computed globally, so error
    // accumulation is also global. And divisions are by variances
    // which are squared values

	cv::Mat correlation_map_integral(const cv::Mat& a, const cv::Mat& b, const int range)
	{		
        // if(a.size() != b.size())
        //  throw std::runtime_error("incompatible image sizes");

        const int size = (2 * range) + 1;
		double maxval = std::max(imp::get_max(a), imp::get_max(b));
		const double epsilon = maxval*maxval*1e-6;
		
		cv::Mat_<double> Ea_img = box_filter_integral(a, range);
		cv::Mat_<double> Eb_img = box_filter_integral(b, range);
		cv::Mat_<double> Ea2_img = box_filter_integral(multiply(a, a), range);
		cv::Mat_<double> Eb2_img = box_filter_integral(multiply(b, b), range);
		cv::Mat_<double> Eab_img = box_filter_integral(multiply(a, b), range);
		
		cv::Mat_<double> corr = cv::Mat::zeros(a.size(), CV_64F);

		for (int row = 0; row < a.rows; row++)
        {
			for (int col = 0; col < a.cols; col++)
            {
				const double Ea = Ea_img(row, col);
				const double Eb = Eb_img(row, col);
				const double Ea2 = Ea2_img(row, col);
				const double Eb2 = Eb2_img(row, col);
				const double Eab = Eab_img(row, col);
				
				const double var_a = Ea2 - Ea * Ea;
				const double var_b = Eb2 - Eb * Eb;

				if(std::abs(var_a) <= epsilon || std::abs(var_b) <= epsilon)
                {                    
					corr(row,col) = 0;
                }

                else if (std::abs(var_a) <= epsilon && std::abs(var_b) <= epsilon)
                {
                    corr(row, col) = 1.0;
                }

				else
				{
                    corr(row,col) = (Eab - Ea * Eb) / std::sqrt(var_a * var_b);
                }
			}
        }
		return corr;
	}




}
