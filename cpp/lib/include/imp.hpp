#ifndef IMP_HPP_
#define IMP_HPP_

#include "image.hpp"
#include "threads.hpp" // so consumers can access

#include <opencv2/core.hpp>

#include <filesystem>
#include <iostream>
#include <vector>

namespace imp 
{
    constexpr int CV_FLOATING_DEPTH = CV_64F;
    using FLOATING_TYPE = double;

    Image load_tiff(const std::string& filename);
    bool write_tiff(const cv::Mat& image, const std::string& filename);
    bool write_tiff_to(const cv::Mat& image, const std::filesystem::path& path);

    cv::Mat convert_to_new_type(const cv::Mat& image, int new_type);
    cv::Mat convert_to_binary(const cv::Mat& image);
    cv::Mat convert_to_floating_point(const cv::Mat& image);
    cv::Mat combine_to_single_image(const cv::Mat& r, const cv::Mat& g, const cv::Mat& b);


    cv::Mat x_or(const cv::Mat& image_a, const cv::Mat& image_b);

	double get_max(const cv::Mat& image);
    void get_min_max(const cv::Mat& image, double* min_value, double* max_value);
    double mean(const cv::Mat& image);
  
    cv::Mat gaussian_blur_cv(const cv::Mat& image, double sigma, int border_type);
    cv::Mat gaussian_blur_and_normalise_cv(const cv::Mat& image, double sigma);
    cv::Mat imagej_gaussian_blur(const cv::Mat& image, double sigma);

    cv::Mat imagej_convolve_cv(const cv::Mat& image, int kernel_size);

	int half_psf_kernel_size(double psf);
    cv::Mat half_psf_blur_cv(const cv::Mat& image, double psf);
    // cv::Mat threshold_image(const cv::Mat& gauss_blur, const cv::Mat& thresh_image, const cv::Mat& psf_image, const ThresholdSettings& settings);
	void normalise_image(cv::Mat& image);

	bool erode(cv::Mat& image, int kernel_size, int border_type);
    bool imagej_erode_cv(cv::Mat& image);

	bool dilate(cv::Mat& image, int kernel_size, int border_type);
    bool imagej_dilate_cv(cv::Mat& image);

	bool skeletonise(cv::Mat& image);

	template<typename T>
	double correlate(const cv::Mat& image_a, const cv::Mat& image_b, double epsilon=0.0)
    {
        const double mean_a = mean(image_a);
        // double var_a = variance<T>(image_a, mean_a);

        const double mean_b = mean(image_b);
        // double var_b = variance<T>(image_b, mean_b);

        auto it_a = image_a.begin<T>();
        auto it_b = image_b.begin<T>();
    
        double var_a = 0.0;
        double var_b = 0.0;
        double corr_coeff = 0.0;
        for(; it_a != image_a.end<T>(); ++it_a, ++it_b)
        {
            const double value_a = *it_a;
            const double value_b = *it_b;
            if (!std::isfinite(value_a))
            {
                // std::cout << std::format("a ({})is not finite\n", value_a);
                continue;
            }
            if (!std::isfinite(value_b))
            {
                // std::cout << std::format("b ({})is not finite\n", value_b);
                continue;
            }
            const double a = value_a - mean_a;
            const double b = value_b - mean_b;         
            corr_coeff += a * b;
            var_a += a * a;
            var_b += b * b;
        }

        if (var_a <= epsilon || var_b <= epsilon)
            return 0.0;

        const auto denom = std::sqrt(var_a * var_b);
        return corr_coeff / denom;
    }

	template<typename T>
	cv::Mat correlation_map(const cv::Mat& image_a, const cv::Mat& image_b, int range)
    {
        int size = (2 * range) + 1;

		// Note that the variances are computed without normalization
		// so the scale needs to account for that
		double maxval = std::max(imp::get_max(image_a), imp::get_max(image_b));
		const double epsilon = maxval*maxval*1e-6*size*size;
        cv::Mat new_image = cv::Mat::zeros(image_a.size(), CV_FLOATING_DEPTH);
        cv::Mat pad_a;
        cv::copyMakeBorder(image_a, pad_a, range, range, range, range, cv::BORDER_CONSTANT, 0.0);
        cv::Mat pad_b;
        cv::copyMakeBorder(image_b, pad_b, range, range, range, range, cv::BORDER_CONSTANT, 0.0);
        for (auto row = 0; row < image_a.rows; row++)
        {
            for (auto col = 0; col < image_a.cols; col++)
            {
                auto rect = cv::Rect(col, row, size, size); // note col (x), row (y), width, height ctor
                auto roi_a = pad_a(rect);
                auto roi_b = pad_b(rect);
                double corr = correlate<T>(roi_a, roi_b, epsilon);
                new_image.at<double>(row, col) = corr;
            }
        }
        return new_image;
    }

	cv::Mat create_colour_confidence_map(const cv::Mat& skeleton_cm, const cv::Mat& resolution_cm, const cv::Mat& ref_psf, const cv::Mat& test_psf);

	double global_correlation(double skeleton_correation, double resolution_correlation);

	template<typename T>
    std::vector<int> generate_histogram(const cv::Mat& image, int n_bins, double* max_value_ptr)
    {
        double min_value;
        // double max_value;
        get_min_max(image, &min_value, max_value_ptr);
        const double max_value = *max_value_ptr;
        const auto range = max_value - min_value;
        // std::cout << "max_value: " << max_value << "\n";
        auto histogram = std::vector<int>(n_bins, 0);
        for (int row = 0; row < image.rows; row++) 
        {
            for (int col = 0; col < image.cols; col++) 
            {
                const double value = (double)image.at<T>(row, col);
                // std::cout << value << "\n";
                if (value <= 0.0) // as per Richards implementation
                {
                    continue;
                }
                else if (value == max_value)
                {
                    // otherwise do n_bins * 1 == n_bins which can't use as index
                    histogram[n_bins - 1]++;
                }
                else
                {
                    const double proportion_through_range = (value - min_value) / range;
                    // std::cout << "proportion_through_range "  << proportion_through_range << "\n";
                    // const double n_bins_d = (double n_bins);
                    // const auto x = n_bins_d  * max_bin_value;
                    const int bin = (int)std::floor((double)n_bins * proportion_through_range);

                    // std::cout << bin << "\n";
                    // std::cout << value << " -> " << bin << "\n";
                    histogram[bin]++;
                }
            }
        }
        return histogram;
    }

    std::vector<long> cumulative_sum(const std::vector<int>& histogram);    
    size_t threshold_bin_number(const std::vector<int>& histogram, const std::vector<long>& cum_sum);
    double threshold_value(double max_value, size_t max_bin_number, size_t n_bins);
	
    template<typename T>
    cv::Mat flatten_image_at(const cv::Mat& image, double max_value)
    {
        // std::cout << "Flattening with: " << max_value << "\n";
        auto flat_image = image.clone();
        auto iter = image.begin<T>();
        auto flat_iter = flat_image.begin<T>();
        for(; iter != image.end<T>(); ++iter, ++flat_iter)
        {
            if (*iter > max_value)
            {
                *flat_iter = max_value;
            }
        }
        return flat_image;
    }

    template<typename T>
    cv::Mat flatten_with(const cv::Mat& image, const std::vector<int>& histogram, double max_value)
    {
        const auto n_bins = histogram.size();
        const auto cum_sum = cumulative_sum(histogram);
        const auto max_bin_number = threshold_bin_number(histogram, cum_sum);
        // std::cout << "max bin number: " << max_bin_number << "\n";        

        const auto max_bin_value = threshold_value(max_value, max_bin_number, n_bins);
        // std::cout << "max bin value: " << max_bin_value << "\n";        
        return flatten_image_at<T>(image, max_bin_value) ;
    }

    template<typename T>
    cv::Mat flatten(const cv::Mat& image)
    {
        const auto n_bins = 256;
        double max_value;
        const auto histogram = generate_histogram<T>(image, n_bins, &max_value);
        // std::cout << "Global max: " << max_value << "\n";
        return flatten_with<T>(image, histogram, max_value);
    }

    cv::Mat convert_hsv_to_bgr(const cv::Mat& image);
	cv::Mat correlation_map_integral(const cv::Mat& a, const cv::Mat& b, const int range);
}
#endif //IMP_HPP_
