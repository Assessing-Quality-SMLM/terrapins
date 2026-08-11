#ifndef RESIZING_HPP_
#define RESIZING_HPP_

#include "zero.hpp"

#include <opencv2/core.hpp>

namespace sqrl
{
	template<typename POLICY, typename T>
	concept ValuePolicy = requires(POLICY p, T u_)
	{
		{std::declval<POLICY>().evaluate(std::declval<T>())} -> std::same_as<T>;
	};

	template<typename T>
	struct SumPolicy
	{
		T evaluate(T sum){return sum;}
	};

	template<typename T>
	struct AveragePolicy
	{
		T evaluate(T sum){return sum / denom;}
		T denom;
	};

	template<typename T, typename P>
	requires imp::num_traits::HasZero<T> && ValuePolicy<P, T>
	cv::Mat_<T> downscale(const cv::Mat_<T>& image, int magnification, P policy)
	{
		const auto n_rows = image.rows / magnification;
		const auto n_cols = image.cols / magnification;
		cv::Mat_<T> new_image = cv::Mat_<T>::zeros(n_rows, n_cols);
		for (auto row = 0; row < new_image.rows; row++)
		{			
			for (auto col = 0; col < new_image.cols; col++)
			{
				const auto row_start = row * magnification;
				const auto row_end = row_start + magnification;
				const auto col_start = col * magnification;
				const auto col_end = col_start + magnification;

				// std::cout << std::format("at: {},{} row {} -> {} col {} -> {}\n", row, col, row_start, row_end, col_start, col_end);
				T sum = imp::num_traits::Zero<T>::zero();
				for (auto r_idx = row_start; r_idx < row_end; r_idx++)
				{
					for (auto c_idx = col_start; c_idx < col_end; c_idx++)
					{
						const T value = image.template at<T>(r_idx, c_idx);
						sum += value;
						// std::cout << std::format("{},{} = {} => {}\n", r_idx, c_idx, value, sum);
					}
				}
				const auto value = policy.evaluate(sum);
				// std::cout << std::format("{},{} = {}\n", row, col, value);
				new_image.template at<T>(row, col) = value;
			}
		}
		return new_image;
	}

	template<typename T>
	requires std::floating_point<T> && imp::num_traits::HasZero<T>
	cv::Mat_<T> downscale_average(const cv::Mat_<T>& image, int magnification)
	{
		return downscale(image, magnification, AveragePolicy<T>{static_cast<T>(magnification * magnification)});
	}

	template<typename T>
	requires imp::num_traits::HasZero<T>
	cv::Mat_<T> downscale_sum(const cv::Mat_<T>& image, int magnification)
	{
		return downscale(image, magnification, SumPolicy<T>());
	}

	template<typename T>
	cv::Mat upscale_with_replication(const cv::Mat_<T>& image, int magnification)
	{
		const auto n_rows = image.rows * magnification;
		const auto n_cols = image.cols * magnification;
		// std::cout << std::format("mag: {} -> {}x{}\n", magnification, n_rows, n_cols);
		cv::Mat new_image = cv::Mat::zeros(n_rows, n_cols, CV_64F);
		for (auto row = 0; row < image.rows; row++)
		{			
			for (auto col = 0; col < image.cols; col++)
			{
				const auto value = image.template at<T>(row, col);
				const auto row_start = row * magnification;
				const auto row_end = row_start + magnification;
				const auto col_start = col * magnification;
				const auto col_end = col_start + magnification;

				// std::cout << std::format("at: {},{} row {} -> {} col {} -> {}\n", row, col, row_start, row_end, col_start, col_end);
				for (auto r_idx = row_start; r_idx < row_end; r_idx++)
				{
					for (auto c_idx = col_start; c_idx < col_end; c_idx++)
					{
						new_image.template at<T>(r_idx, c_idx) = value;
						// std::cout << std::format("{},{} = {}\n", r_idx, c_idx, scaled_value);
					}
				}
			}
		}
		return new_image;
	}

	cv::Mat upsample_with_interpolation(const cv::Mat& image, cv::Size new_size);
}

#endif //RESIZING_HPP_