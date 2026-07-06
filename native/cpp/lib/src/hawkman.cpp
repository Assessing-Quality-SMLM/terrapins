#include "hawkman.hpp"

#include "hawkman_inner.hpp"
#include "hue_calculator.hpp"
#include "imp.hpp"
#include "results.hpp"
#include "settings.hpp"

#include <opencv2/core.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/imgcodecs.hpp> // to set imread flag
#include <opencv2/imgproc.hpp> // boxFilter
#include <opencv2/core/types.hpp>

// #include <dlib/opencv/cv_image.h>
// #include <dlib/opencv.h>
// #include <dlib/image_processing.h>
// #include <dlib/image_transforms.h>
// #include "dlib/image_transforms/morphological_operations.h"

#include <diplib.h>
#include <dip_opencv_interface.h>
#include <diplib/binary.h>

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

namespace hkmn 
{
	using BINARY_TYPE = unsigned char;
	using FLOATING_TYPE = double;

    constexpr int CV_FLOATING_DEPTH = CV_64F;
    // constexpr int CV_FLOATING_DEPTH = CV_64FC1;
    
    constexpr int CV_BINARY_DEPTH = CV_8U;

	constexpr BINARY_TYPE DLIB_BINARY_ZERO = 0;
	constexpr BINARY_TYPE DLIB_BINARY_ONE = 255;   

    // using GRAY_TYPE = unsigned char;
    // using GRAY_TYPE = float;
    using GRAY_TYPE = double;

    constexpr double MAX_HUE_LEVEL_CV_f64 = 179.0;

    template<typename T>
    void display_image(const cv::Mat& image)
    {
        for (auto row = 0; row < image.rows; row++)
        {
            std::cout << (double)image.at<T>(row, 0);
            for(auto col = 1; col < image.cols; col++)
            {
                std::cout << ", " << (double)image.at<T>(row, col);
            }
            std::cout << "\n";
        }
    }

    template<typename T>
    void nan_check(const cv::Mat& image)
    {
    	for (auto row = 0; row < image.rows; row++)
        {
            for(auto col = 0; col < image.cols; col++)
            {
            	const double value = image.at<T>(row, col);
            	if (std::isfinite(value))
            		continue;
                std::cout << std::format("{},{} = {} is not finite\n", row, col, value);
            }
        }
    }

    double get_normaliser(const cv::Mat& image)
    {
        const double max = imp::get_max(image);
        return 1.0 / max;
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

    cv::Mat blur_to_scale(const cv::Mat& image, double scale_number)
    {
        const double conv = std::ceil(scale_number / 2.0);
        const int kernel_size = get_kernel_size((int)conv);
        // std::cout << "Convolution kernel size " << kernel_size << "\n";
        return imp::imagej_convolve_cv(image, kernel_size);
    }

    cv::Mat threshold_image(const cv::Mat& gauss_blur, const cv::Mat& thresh_image, const cv::Mat& psf_image, const ThresholdSettings& settings)
    {
        cv::Mat binary_result = cv::Mat::zeros(gauss_blur.size(), CV_BINARY_DEPTH);       
        for(int row = 0; row < gauss_blur.rows; row++)
        {
            for(int col = 0; col < gauss_blur.cols; col++)
            {
                const FLOATING_TYPE thresh_value = thresh_image.at<FLOATING_TYPE>(row, col);
                const double threshold = settings.threshold() * thresh_value;
                const FLOATING_TYPE psf_value = psf_image.at<FLOATING_TYPE>(row, col);
                const double smooth = settings.smoothing() * psf_value;
                const double value = threshold + smooth + settings.offset();
                if (gauss_blur.at<FLOATING_TYPE>(row, col) > value)
                {
                    binary_result.at<BINARY_TYPE>(row, col) = DLIB_BINARY_ONE;
                }
            }
        }
        return binary_result;
    }

    bool dilate_erode(cv::Mat& ref_bin, cv::Mat& ref_skel, cv::Mat& test_bin, cv::Mat& test_skel, double scale_number)
    {
        int max_iter = (int)std::ceil(scale_number / 2.0);
        for(int idx = 0; idx < max_iter; idx++)
        {
            imp::imagej_dilate_cv(ref_bin);
            imp::imagej_dilate_cv(ref_skel);
            imp::imagej_dilate_cv(test_bin);
            imp::imagej_dilate_cv(test_skel);
        }
        for(int idx = 0; idx < max_iter; idx++)
        {
            imp::imagej_erode_cv(ref_bin);
            imp::imagej_erode_cv(ref_skel);
            imp::imagej_erode_cv(test_bin);
            imp::imagej_erode_cv(test_skel);
        }

        imp::imagej_dilate_cv(ref_skel);
        imp::imagej_dilate_cv(test_skel);
        return true;
    }

    int get_normalisation_value(const double ref_psf_value, const double test_psf_value, const double scalar)
    {
    	// re-wrote the expression from ~ln427
        const auto scale_factor = 127.0 * scalar;
        auto normaliser = scale_factor * (ref_psf_value + test_psf_value);
        normaliser = std::min(normaliser, 255.0);
        normaliser = std::max(normaliser, 0.0);
        // return (int)std::ceil(normaliser);
        return (int)normaliser;
    }

    double get_correlation_threshold(const double pixel_value)
    {
    	// 0.5 / 0.85 = 1/2 / 17/20 = 1/2 * 20/17 = 20 / 34 = 10 / 17 = 0.5882352941
        const auto value = (0.5 / 0.85) * pixel_value;
        return std::min(value, 0.5);
    }

    cv::Mat create_colour_confidence_map(const cv::Mat& skeleton_cm, const cv::Mat& resolution_cm, const cv::Mat& ref_psf, const cv::Mat& test_psf)
    {
        cv::Mat test_binary = cv::Mat::zeros(skeleton_cm.size(), CV_BINARY_DEPTH);
        cv::Mat ref_binary = cv::Mat::zeros(skeleton_cm.size(), CV_BINARY_DEPTH);
        for (auto row = 0; row < skeleton_cm.rows; row++) 
        {
            for (auto col = 0; col < skeleton_cm.cols; col++)
            {
                //  set threshold for good correlation
                auto skeleton_value = get_correlation_threshold(skeleton_cm.at<FLOATING_TYPE>(row, col));
                const auto resolution_value = get_correlation_threshold(resolution_cm.at<FLOATING_TYPE>(row, col));
                if (resolution_value == 0.5 && skeleton_value == 0.0)
                    skeleton_value = 0.5;

                const auto test_scalar = (1.0 - skeleton_value - resolution_value);
                const auto ref_psf_value = ref_psf.at<FLOATING_TYPE>(row, col);
                const auto test_psf_value = test_psf.at<FLOATING_TYPE>(row, col);
                const auto test_value = get_normalisation_value(ref_psf_value, test_psf_value, test_scalar);
                test_binary.at<BINARY_TYPE>(row, col) = (BINARY_TYPE)test_value;

                const auto ref_scalar = skeleton_value + resolution_value;
                const auto ref_value = get_normalisation_value(ref_psf_value, test_psf_value, ref_scalar);
                ref_binary.at<BINARY_TYPE>(row, col) = (BINARY_TYPE)ref_value;
            }
        }

        return imp::combine_to_single_image(test_binary, ref_binary, ref_binary);
    }

    double half_global_correlation(double correlation)
    {
    	const auto thresh = (0.5 / 0.85) * correlation;
        return std::min(thresh, 0.5);
    }

    double global_correlation(double skeleton_correation, double resolution_correlation)
    {
        const auto skel_score = half_global_correlation(skeleton_correation);
        const auto res_score = half_global_correlation(resolution_correlation);
        return skel_score + res_score;
    }

    bool write_score(const std::string& filename, int level, double score)
    {
        auto mode = std::ios_base::out | std::ios_base::app;
        auto stream = std::ofstream(filename, mode);
        if (!stream.is_open())
        {
            std::cout << std::format("could not open {}\n", filename);
            return false;
        }
        try 
        {
            stream << std::format("{},{}\n", level, score);
            stream.close();
            const auto state = stream.rdstate();
            if (state & std::ios_base::goodbit)
                return true;
            if (state & std::ios_base::eofbit)
                std::cout << "Failure: stream at end of file\n";
            if (state & std::ios_base::failbit)
                std::cout << "Failure: encountered fail bit\n";
            if (state & std::ios_base::badbit)
                std::cout << "Failure: encountered bad bit\n";
            return false;

        } 
        catch (const std::ios_base::failure& e) 
        {
            std::cout << e.what() << "\n";
            return false;
        }
    }

    bool iterate(const cv::Mat& ref, const cv::Mat& ref_psf, const cv::Mat& test, const cv::Mat& test_psf, int scale_number, const Settings& settings, Results& results)
    {
        const double scale_d = (double)scale_number;
        const double sigma = scale_d / 2.355;
        const auto scale_number_filename = std::format("{}.tiff", scale_number);

        std::cout << std::format("blurring with gausian (sigma={})\n", sigma);
        const auto ref_blur = imp::gaussian_blur_and_normalise_cv(ref, sigma);
        const auto ref_blur_path = settings.output_directory_path() / std::format("ref_gb_{}.tiff", scale_number);
        imp::write_tiff(ref_blur, ref_blur_path.string());

        const auto test_blur = imp::gaussian_blur_and_normalise_cv(test, sigma);
        imp::write_tiff(test_blur, (settings.output_directory_path() / std::format("test_gb_{}.tiff", scale_number)).string());

        const auto ref_thresh = blur_to_scale(ref_blur, scale_d);
        const auto test_thresh = blur_to_scale(test_blur, scale_d);

        const auto ref_filename = std::format("ref_thresh_{}.tiff", scale_number);        
        imp::write_tiff(ref_thresh, (settings.output_directory_path() / ref_filename).string());

        const auto test_filename = std::format("test_thresh_{}.tiff", scale_number);
        imp::write_tiff(test_thresh, (settings.output_directory_path() / test_filename).string());

        // std::cout << "Creating ref bin\n";
        auto ref_bin = threshold_image(ref_blur, ref_thresh, ref_psf, settings.fwhm_threshold_settings());
        imp::write_tiff(ref_bin, (settings.output_directory_path() / std::format("ref_bin_{}.tiff", scale_number)).string());

        // std::cout << "Creating ref skel\n";
        auto ref_skel = threshold_image(ref_blur, ref_thresh, ref_psf, settings.skeleton_threshold_settings());
        imp::write_tiff(ref_skel, (settings.output_directory_path() / std::format("ref_skel_{}.tiff", scale_number)).string());
        
        // std::cout << "Creating test bin\n";
        auto test_bin = threshold_image(test_blur, test_thresh, test_psf, settings.fwhm_threshold_settings());
        imp::write_tiff(test_bin, (settings.output_directory_path() / std::format("test_bin_{}.tiff", scale_number)).string());
        
        // std::cout << "Creating test skel\n";
        auto test_skel = threshold_image(test_blur, test_thresh, test_psf, settings.skeleton_threshold_settings());
        imp::write_tiff(test_skel, (settings.output_directory_path() / std::format("test_skel_{}.tiff", scale_number)).string());
		
        if (settings.dilate_erode())
            dilate_erode(ref_bin, ref_skel, test_bin, test_skel, scale_d);

        // const auto ref_bin_double = convert_to_floating_point(ref_bin);
        // const auto test_bin_double = convert_to_floating_point(test_bin);
		const auto binary_correlation_map = imp::correlation_map_integral(ref_bin, test_bin, 2 * scale_number);

        // std::cout << "Creating Sharpening map\n";
        const auto correlation = imp::correlate<BINARY_TYPE>(ref_bin, test_bin);
        std::cout << std::format("correlation at {}: {}\n", scale_number, correlation);
        const auto sharp_score_name = settings.sharpening_map_dir() / "score";
        results.write_sharpening(scale_number, correlation);
        // write_score(sharp_score_name.string(), scale_number, correlation);

        const auto xor_bin = imp::x_or(ref_bin, test_bin);
        const auto sharpening_map = imp::combine_to_single_image(test_bin, ref_bin, xor_bin);

        const auto sharpening_map_path = settings.sharpening_map_dir() / scale_number_filename;
        imp::write_tiff(sharpening_map, sharpening_map_path.string());

        imp::skeletonise(ref_skel);
        imp::write_tiff(ref_skel, (settings.output_directory_path() / std::format("ref_skel_{}.tiff", scale_number)).string());
        // display_image<BINARY_TYPE>(ref_skel);

        imp::skeletonise(test_skel);
        imp::write_tiff(test_skel, (settings.output_directory_path() / std::format("test_skel_{}.tiff", scale_number)).string());

        const auto ref_skel_double = imp::convert_to_floating_point(ref_skel);
        const auto ref_skel_blur = imp::imagej_gaussian_blur(ref_skel_double, sigma);
        // display_image<FLOATING_TYPE>(ref_skel_blur);
        imp::write_tiff(ref_skel_blur, (settings.output_directory_path() / std::format("ref_skel_blur_{}.tiff", scale_number)).string()); 

        const auto test_skel_double = imp::convert_to_floating_point(test_skel);
        const auto test_skel_blur = imp::imagej_gaussian_blur(test_skel_double, sigma);
        imp::write_tiff(test_skel_blur, (settings.output_directory_path() / std::format("test_skel_blur_{}.tiff", scale_number)).string());

        // std::cout << "Creating Structure map\n";
        const auto skel_correlation = imp::correlate<FLOATING_TYPE>(ref_skel_blur, test_skel_blur);
        std::cout << std::format("skeleton correlation: {}\n", skel_correlation);
        const auto struct_score_name = settings.structure_map_dir() / "score";
        results.write_structure(scale_number, skel_correlation);
        // write_score(struct_score_name.string(), scale_number, skel_correlation);

        const auto xor_skel = imp::x_or(test_skel, ref_skel);

        const auto skeleton_map = imp::combine_to_single_image(test_skel, ref_skel, xor_skel);
        const auto skeleton_map_path = settings.skeleton_map_dir() / scale_number_filename;
        imp::write_tiff(skeleton_map, skeleton_map_path.string());

        if (settings.blur_skeleton())
        {
        	const auto blurred_test_binary = imp::convert_to_binary(test_skel_blur);
        	const auto blurred_ref_binary = imp::convert_to_binary(ref_skel_blur);

        	const auto xor_skel_double = imp::convert_to_floating_point(xor_skel);
        	const auto blurred_xor_skel = imp::imagej_gaussian_blur(xor_skel_double, sigma);
        	const auto blurred_xor_binary = imp::convert_to_binary(blurred_xor_skel);

        	const auto structure_map = imp::combine_to_single_image(blurred_test_binary, blurred_ref_binary, blurred_xor_binary);
        	const auto structure_map_path = settings.structure_map_dir() / scale_number_filename;
        	imp::write_tiff(structure_map, structure_map_path.string());
        }
        else
        {
        	const auto structure_map = imp::combine_to_single_image(test_skel, ref_skel, xor_skel);
        	const auto structure_map_path = settings.structure_map_dir() / scale_number_filename;
        	imp::write_tiff(structure_map, structure_map_path.string());
        }

        const auto ref_sr_info = cv::countNonZero(ref_skel);
        const auto test_sr_info = cv::countNonZero(test_skel);
        const auto corr_map_skel  = imp::correlation_map_integral(ref_skel_blur, test_skel_blur, scale_number);

        const auto confidence_map_dir = settings.confidence_map_dir();
        auto filename = std::format("blurred_correlation_map_{}.tiff", scale_number);
        const auto corr_map_skel_path = confidence_map_dir / filename;
        imp::write_tiff(corr_map_skel, (settings.output_directory_path() / corr_map_skel_path.string()).string());

        filename = std::format("binary_correlation_map_{}.tiff", scale_number);
        const auto blurred_map = confidence_map_dir / filename;
        imp::write_tiff(binary_correlation_map, (settings.output_directory_path() / blurred_map.string()).string());

        const auto confidence_map = create_colour_confidence_map(corr_map_skel, binary_correlation_map, ref_blur, test_blur);
        const auto confidence_map_path = settings.confidence_map_dir() / scale_number_filename;
        imp::write_tiff(confidence_map, confidence_map_path.string());
        
        const auto global_corr = global_correlation(skel_correlation, correlation);
        std::cout << "Global correlation: " << global_corr << "\n";
        const auto conf_score_name = settings.confidence_map_dir() / "score";
        results.write_global_score(scale_number, global_corr);
        // write_score(conf_score_name.string(), scale_number, global_corr);
        
        // std::cout << "Adding to confidence stack\n";
        const auto ok = results.add_confidence_map(confidence_map);
        // std::cout << "Added to stack\n";
        return ok;
    }

    cv::Mat generate_scale_image(const Settings& settings, const int n_rows)
    {
        const auto n_cols = 30;

        const auto size = cv::Size(n_cols, n_rows);
        cv::Mat hsv_image = cv::Mat::zeros(size, CV_8UC3);
        cv::Mat hue_channel = cv::Mat::zeros(size, CV_BINARY_DEPTH);
        cv::Mat saturation_channel = cv::Mat::ones(size, CV_BINARY_DEPTH) * 127;
        cv::Mat value_channel = cv::Mat::ones(size, CV_BINARY_DEPTH) * 255;
        const auto n_levels = settings.n_levels();
        const double double_n_levels = static_cast<double>(n_levels);
        const double double_rows = static_cast<double>(n_rows);
        const double hue_per_level = MAX_HUE_LEVEL_CV_f64 / double_n_levels;
        const auto hue_calculator = imp::HueCalculator::rainbow_from_opencv(n_levels);
        for(auto row = 0; row < hsv_image.rows; row++)
        {
            const double rows = static_cast<double>(row + 1);
            const double progress = rows / double_rows;
            const double level = std::ceil(double_n_levels * progress);
            const double hue_double = hue_calculator.hue_for_level(level);
            const std::uint8_t hue_value = hue_calculator.hue_for_level_t<std::uint8_t>(level);
            for (auto col = 0; col < hsv_image.cols; col++)
            {            
                hue_channel.at<BINARY_TYPE>(row, col) = hue_value;
            }
        }
        cv::Mat channels[3]{ hue_channel, saturation_channel, value_channel };
        cv::merge(channels, 3, hsv_image);
        const auto bgr_image = imp::convert_hsv_to_bgr(hsv_image);
        cv::putText(bgr_image, std::format("{}", settings.start_level()), cv::Point(0, 10), cv::FONT_ITALIC, 0.5, cv::Scalar(0, 0, 0));
        cv::putText(bgr_image, std::format("{}", n_levels), cv::Point(0, n_rows), cv::FONT_ITALIC, 0.5, cv::Scalar(0, 0, 0));
        return bgr_image;
    }

    cv::Mat create_single_image(const std::vector<cv::Mat>& confidence_stack, const Settings& settings)
    {
        const auto size = confidence_stack[0].size();
        cv::Mat hsv_result = cv::Mat::zeros(confidence_stack[0].size(), CV_8UC3); // 3 (unsigned) byte entries
        //cv::Mat channels[3];
        cv::Mat flags = cv::Mat::zeros(size, CV_32S);
        const auto n_levels = settings.n_levels();
        const auto hue_calculator = imp::HueCalculator::rainbow_from_opencv(n_levels);
        const auto con_scales = settings.consecutive_scales();
        const auto threshold = settings.artifact_threshold();
        cv::Mat hue_channel = cv::Mat::ones(size, CV_BINARY_DEPTH) * hue_calculator.hue_for_level_t<BINARY_TYPE>(1);
        cv::Mat saturation_channel = cv::Mat::ones(size, CV_BINARY_DEPTH) * 127;
        cv::Mat value_channel = cv::Mat::zeros(size, CV_BINARY_DEPTH);
        using INTEGER_TYPE = std::int32_t;
        for(auto level = settings.start_level(); level < settings.end_level(); level++)
        {
            std::cout << "processing level " << (int)level << "\n";
            const cv::Mat& image = confidence_stack[level - 1];

            for(auto row = 0; row < hsv_result.rows; row++)
            {
                for (auto col = 0; col < hsv_result.cols; col++)
                {
                    const auto pixel = image.at<cv::Vec3b>(row, col);
                    const BINARY_TYPE channel_1 = pixel(2);
                    const BINARY_TYPE channel_2 = pixel(1);
                    const BINARY_TYPE res = channel_1 + channel_2;
                    //auto result_pixel = *result_it;
                    if (level == 1 && res > 0)
                    {
                        const BINARY_TYPE value = res;
                        value_channel.at<BINARY_TYPE>(row, col) = res;
                        flags.at<INTEGER_TYPE>(row, col) = con_scales;
                    }
                    // if (level > 1 && res > 0)
                    if (level > 1)
                    {
                        // std::cout << "HERE\n";
                        const double ratio = (double)channel_1 / (double)res;
                        INTEGER_TYPE& flag = flags.at<INTEGER_TYPE>(row, col);
                        if (ratio > threshold && flag > 0)
                        {
                            const std::uint8_t value = hue_calculator.hue_for_level_t<std::uint8_t>(level);
                            // std::cout << std::format("{},{}={}\n", row, col, level);
                            hue_channel.at<BINARY_TYPE>(row, col) = value;
                        }
                        else
                        {
                            flags.at<INTEGER_TYPE>(row, col) = flag - 1;
                        }
                    }
                }
            }
        }
        cv::Mat channels[3]{ hue_channel, saturation_channel, value_channel };
        cv::merge(channels, 3, hsv_result);
        // std::cout << "Converting colour space\n";
        return imp::convert_hsv_to_bgr(hsv_result);
    }

    bool process_thread(const cv::Mat& ref, const cv::Mat& ref_half_blur, const cv::Mat& test, const cv::Mat& test_half_blur, const Settings& settings, Results& results, std::atomic_size_t* scale_counter)
    {
        while(*scale_counter < settings.end_level())
        {
            const auto scale = scale_counter->fetch_add(1, std::memory_order::relaxed);
            // the flush is important for tools watching the console output
            // they need it to pump messages out to the user
            std::cout << std::format("Analysing at scale: {}\n", scale) << std::flush;
            if (!iterate(ref, ref_half_blur, test, test_half_blur, scale, settings, results))
            {
                return false;
            }
        }
        // std::cout << "Thread ending\n";
        return true;
    }

    bool process_single_thread(const cv::Mat& ref, const cv::Mat& ref_half_blur, const cv::Mat& test, const cv::Mat& test_half_blur, const Settings& settings, Results& results)
    {
        for(auto scale = settings.start_level(); scale < settings.end_level(); scale++)
        {
            std::cout << std::format("Analysing at scale: {}\n", scale);
            if (!iterate(ref, ref_half_blur, test, test_half_blur, scale, settings, results))
            {
                results.finalise();
                return false;
            }
        }
        return true;
    }

    bool process_mt(const cv::Mat& ref, const cv::Mat& ref_half_blur, const cv::Mat& test, const cv::Mat& test_half_blur, const Settings& settings, Results& results, std::uint8_t n_threads)
    {
        auto threads = std::vector<std::thread>();
        auto counter = std::atomic_size_t{settings.start_level()};
        for(auto t = 0; t < n_threads; t++)
        {
            threads.push_back(std::thread(&process_thread, ref, ref_half_blur, test, test_half_blur, settings, std::ref(results), &counter));
        }
        for (auto& thread : threads) 
        {
            thread.join();
        }
        return true;
    }

    bool run_hawkman(const cv::Mat& ref, const cv::Mat& test, const Settings& settings)
    {
        const auto psf = settings.psf();
        std::cout << "psf set to: " << psf << "\n";
        const auto ref_half_blur = imp::half_psf_blur_cv(ref, psf);
        const auto ref_half_blur_path = settings.output_directory_path() / "ref_half_psf_blur.tiff";
        imp::write_tiff(ref_half_blur, ref_half_blur_path.string());
        
        const auto test_half_blur = imp::half_psf_blur_cv(test, psf);
        const auto test_half_blur_path = settings.output_directory_path() / "test_half_psf_blur.tiff";
        imp::write_tiff(ref_half_blur, test_half_blur_path.string());

        auto results = Results();
        if (!results.initialise_at(settings.output_directory_path().string()))
        {
            std::cout << "Could not setup results object\n";
            return false;
        }

        std::cout << std::format("Detected support for {} threads\n", settings.system_threads());
        const auto n_threads = settings.usable_threads();
        std::cout << std::format("Using: {} threads\n", n_threads);

        bool ok;
        if (n_threads <= 1)
            ok = process_single_thread(ref, ref_half_blur, test, test_half_blur, settings, results);
        else
            ok = process_mt(ref, ref_half_blur, test, test_half_blur, settings, results, n_threads);

        if (ok && settings.create_single_image_summary())
        {
            std::cout << "Creating resolution map\n";
            const auto resolution_map = create_single_image(results.confidence_stack(), settings);
            const auto resolution_path = settings.output_directory_path() / "resolution_map.tiff";
            std::cout << "Writing resolution map\n";
            imp::write_tiff(resolution_map, resolution_path.string());

            const auto resolution_scale_map = generate_scale_image(settings, resolution_map.rows);
            const auto resolution_scale_map_path = settings.output_directory_path() / "resolution_scale_map.tiff";
            std::cout << "Writing resolution scale map\n";            
            imp::write_tiff(resolution_scale_map, resolution_scale_map_path.string());

            std::vector<cv::Mat> images = { resolution_map, resolution_scale_map};
            cv::Mat combined_resolution_map;            
            cv::hconcat(images, combined_resolution_map);
            
            const auto combined_resolution_scale_map_path = settings.output_directory_path() / "combined_resolution_scale_map.tiff";
            std::cout << "Writing combined resolution scale map\n";
            imp::write_tiff(combined_resolution_map, combined_resolution_scale_map_path.string());
        }
        return ok && results.finalise();
    }

    int run(const Settings& settings)
    {
        std::filesystem::create_directories(settings.confidence_map_dir());
        std::filesystem::create_directories(settings.sharpening_map_dir());
        std::filesystem::create_directories(settings.structure_map_dir());
        std::filesystem::create_directories(settings.skeleton_map_dir());

        std::cout << "Loading reference (HAWK) image: " << settings.ref_image() << "\n";
        auto ref_cv_image = imp::load_tiff(settings.ref_image());
        auto ref_cv = ref_cv_image.open_cv_image();
        ref_cv.convertTo(ref_cv, CV_FLOATING_DEPTH);

        if (settings.flatten_intensities())
        {
            std::cout << "Flattening\n";
            ref_cv = imp::flatten<FLOATING_TYPE>(ref_cv);
        }
        const auto ref_image_path = settings.output_directory_path() / "ref_cv.tiff";
        imp::write_tiff(ref_cv, ref_image_path.string());
        
        std::cout << "Loading test (No HAWK) image: " << settings.test_image() << "\n";
        auto test_cv_image = imp::load_tiff(settings.test_image());
        cv::Mat test_cv = test_cv_image.open_cv_image();
        test_cv.convertTo(test_cv, CV_FLOATING_DEPTH);
        
        if (settings.flatten_intensities())
        {
            std::cout << "Flattening\n";
            test_cv = imp::flatten<FLOATING_TYPE>(test_cv);
        }
        const auto test_image_path = settings.output_directory_path() / "test_cv.tiff";
        imp::write_tiff(test_cv, test_image_path.string());
        
        run_hawkman(ref_cv, test_cv, settings);

        return 0;
    }
}
