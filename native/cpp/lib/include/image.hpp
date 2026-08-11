#ifndef IMAGE_HPP_
#define IMAGE_HPP_

#include <opencv2/core.hpp>

namespace imp
{
	class Image
	{
	private:
		Image(cv::Mat image);
		Image();

	public:
		static Image from_open_cv(cv::Mat image);
		static Image from_disk(const std::string& filename);

		const cv::Mat& open_cv_image() const;
		cv::Mat& open_cv_image_mut();
		void set_image_cv(cv::Mat image);

		void to_disk(const std::string& filename) const;

	public:

	private:
		cv::Mat cv_image_;
	};
}
#endif //IMAGE_HPP_