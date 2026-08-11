#include "resizing.hpp"

#include <opencv2/imgproc.hpp>

#include <dip_opencv_interface.h>
#include <diplib/geometry.h>

namespace sqrl
{
	cv::Mat cv_resampling(const cv::Mat& image, cv::Size new_size)
	{
		cv::Mat new_image;
		cv::resize(image, new_image, new_size, 0, 0, cv::INTER_NEAREST_EXACT);
		return new_image;
	}

	cv::Mat diplib_resampling(const cv::Mat& image, cv::Size new_size)
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

	cv::Mat upsample_with_interpolation(const cv::Mat& image, cv::Size new_size)
	{		
		return diplib_resampling(image, new_size);
	}
}