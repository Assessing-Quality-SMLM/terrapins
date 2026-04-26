#include "image.hpp"

#include <opencv2/core.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/imgcodecs.hpp> // to set imread flag
#include <opencv2/imgproc.hpp> // boxFilter
#include <opencv2/core/types.hpp>


namespace imp
{
	Image::Image(cv::Mat image) : cv_image_(image)
	{

	}

	Image::Image()
	{

	}

	Image Image::from_open_cv(cv::Mat image)
	{
		return Image(image);
	}

	Image Image::from_disk(const std::string& filename)
	{
		auto cv_image = cv::imread(filename, cv::IMREAD_UNCHANGED);
		return Image::from_open_cv(cv_image);
	}

	const cv::Mat& Image::open_cv_image() const
	{
		return cv_image_;
	}
}
