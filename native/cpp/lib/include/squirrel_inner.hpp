#ifndef SQUIRREL_INNER_HPP_
#define SQUIRREL_INNER_HPP_

#include "squirrel_parameters.hpp"
#include "squirrel_settings.hpp"
#include "squirrel_patch.hpp"

#include <opencv2/core.hpp>

#include <optional>
#include <vector>

namespace sqrl 
{
	struct Border
    {
    public:
    	Border upscale_wf_by(int value) const;
    	cv::Rect to_rect() const;    	
    public:
    	int top = 0;
    	int left = 0;
    	int bottom = 0;
    	int right = 0;
    };

    bool is_whole(double value);

    // cv::Mat block_magnify(const cv::Mat& image, double magnification);
   	// cv::Mat downscale_average(const cv::Mat& image, double magnification);
	// cv::Mat downscale(const cv::Mat& image, const cv::Size& image_size);

    int downstep_index(int index, double magnification);

	double rmse(double* const image_a, double* const image_b, size_t n_elements);
	double image_rmse(const cv::Mat& image_a, const cv::Mat& image_b);
	double image_pearsons(const cv::Mat& image_a, const cv::Mat& image_b);
	cv::Mat affine_transform(const cv::Mat& image, double alpha, double beta);
	std::optional<cv::Mat> blur_and_downscale(const cv::Mat& image, double sigma, const cv::Size& image_size);
	cv::Mat blur_downsample_affine(const cv::Mat& image, double alpha, double beta, double sigma, const cv::Size& new_size);
	double image_rmse(const cv::Mat& image_a, const cv::Mat& image_b);
	double image_pearsons(const cv::Mat& image_a, const cv::Mat& image_b);

	std::optional<cv::Mat> register_images(const cv::Mat& widefield, const cv::Mat& sr_image, const Settings& settings, double magnification);

	
	// assumes all images are the same size
	// and sr + ones have been blurred and downscaled to wf size
	// prior to calling
	std::optional<Parameters> calculate_alpha_beta_from_images(const cv::Mat& wf, const cv::Mat& sr, const cv::Mat& ones);
	std::optional<Parameters> optimal_alpha_beta(const cv::Mat& wf_image, const cv::Mat& sr_image, double sigma);
	cv::Mat error_map(const cv::Mat& image_a, const cv::Mat& image_b, bool show_positive_negative);
	cv::Mat generate_colour_bar(const cv::Mat& error_map);

	std::vector<Patch> get_patches_over(const cv::Mat& image, const Settings& settings);

}
#endif //SQUIRREL_INNER_HPP_