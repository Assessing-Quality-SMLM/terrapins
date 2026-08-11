#include "squirrel_pw_results.hpp"
#include "imp.hpp"


namespace sqrl 
{
	PWResults::PWResults(cv::Size map_size)
		: alpha_map_(cv::Mat::zeros(map_size, imp::CV_FLOATING_DEPTH))
		, beta_map_(cv::Mat::zeros(map_size, imp::CV_FLOATING_DEPTH))
		, sigma_map_(cv::Mat::zeros(map_size, imp::CV_FLOATING_DEPTH))
	{
	}

	PWResults PWResults::from(cv::Size map_size)
	{
		return PWResults(map_size);
	}

	void PWResults::add(int row, int col, const Parameters& parameters)
	{
		std::lock_guard<std::mutex> guard(mutex_);
		alpha_map_.at<imp::FLOATING_TYPE>(row, col) = parameters.alpha;
		beta_map_.at<imp::FLOATING_TYPE>(row, col) = parameters.beta;
		sigma_map_.at<imp::FLOATING_TYPE>(row, col) = parameters.sigma;
	}

	void PWResults::write_to(const std::filesystem::path& output_directory)
	{
		imp::write_tiff_to(alpha_map_, output_directory / "alpha_map.tiff");
		imp::write_tiff_to(beta_map_, output_directory / "beta_map.tiff");
		imp::write_tiff_to(sigma_map_, output_directory / "sigma_map.tiff");
	}
}