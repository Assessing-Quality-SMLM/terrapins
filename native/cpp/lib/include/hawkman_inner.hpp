#ifndef HAWKMAN_INNER_HPP_
#define HAWKMAN_INNER_HPP_

#include "threshold_settings.hpp"
#include "settings.hpp"

#include <opencv2/core.hpp>

namespace hkmn 
{
    cv::Mat blur_to_scale(const cv::Mat& image, double scale_number);
    cv::Mat threshold_image(const cv::Mat& gauss_blur, const cv::Mat& thresh_image, const cv::Mat& psf_image, const ThresholdSettings& settings);
    cv::Mat create_single_image_hsv(const std::vector<cv::Mat>& confidence_stack, const Settings& settings);
    cv::Mat create_single_image(const std::vector<cv::Mat>& confidence_stack, const Settings& settings);
    double global_correlation(const Settings& settings, double skeleton_correation, double resolution_correlation);
}
#endif //HAWKMAN_INNER_HPP_