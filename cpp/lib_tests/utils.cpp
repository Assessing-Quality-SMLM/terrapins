#include "utils.hpp"

namespace tu 
{
	bool within_tolerance(double a, double b, double tolerance)
    {
        const auto dif = a - b;
        const auto dif_2 = dif * dif;
        const auto sqrt_dif_2 = std::sqrt(dif_2);
        return sqrt_dif_2 <= tolerance;
    }

    cv::Mat create_test_image(int size)
    {    
        cv::Mat image;
        image.create(size, size, CV_64F);
        double count = 0.0;
        for (auto row = 0; row < size; row++)
        {
            for(auto col = 0; col < size; col++)
            {
                image.at<double>(row, col) = count++;
            }
        }
        // display_image(image);
        return image;
    }

    

}