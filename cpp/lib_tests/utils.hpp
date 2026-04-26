#ifndef UTILS_HPP_
#define UTILS_HPP_

#include <opencv2/core.hpp>

#include <format>
#include <iostream>

namespace tu
{
    template <typename T, typename U = int>
    void display_image(const cv::Mat& image)
    {
        for (auto row = 0; row < image.rows; row++)
        {
            std::cout << std::format("{}", (U)image.at<T>(row, 0));
            for(auto col = 1; col < image.cols; col++)
            {
                std::cout << ", " << std::format("{}", (U)image.at<T>(row, col));
            }
            std::cout << "\n";
        }
    }

    bool within_tolerance(double a, double b, double tolerance);

    template<typename T, bool EXACT = true>
    bool show_differences(const cv::Mat& image_1, const cv::Mat& image_2)
    {
        constexpr double tol = 0.0001;
        auto ok = true;
        for (auto row = 0; row < image_1.rows; row++)
        {
            for(auto col = 0; col < image_1.cols; col++)
            {
                const auto a = image_1.at<T>(row, col);
                const auto b = image_2.at<T>(row, col);
                const auto dif = a - b;
                const auto dif_2 = dif * dif;
                const auto sqrt_dif_2 = std::sqrt(dif_2);
                // std::cout << std::format("{},{} {} vs {}\n", row, col, (int)a, (int)b);
                const auto values_match = EXACT ? (a == b) : within_tolerance((double)a, (double)b, tol);
                if (!values_match)
                {
                    std::cout << std::format("{},{} {} != {}\n", row, col, a, b);
                    ok = false;
                }
            }
        }
        return ok;
    }

    template<typename T, bool EXACT = true>
    bool images_equal(const cv::Mat& image_1, const cv::Mat& image_2)
    {
        return show_differences<T, EXACT>(image_1, image_2);
    }

    cv::Mat create_test_image(int size);
}

#endif //UTILS_HPP_