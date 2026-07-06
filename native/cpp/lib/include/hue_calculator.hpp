#ifndef HUE_CALCULATOR_HPP_
#define HUE_CALCULATOR_HPP_

#include <cstdint>
namespace imp
{
    class HueCalculator
    {
    private: 
        static constexpr int OPENCV_MAX_HUE_LEVELS = 179;

        explicit HueCalculator(double hue_per_level);

    public:
        static HueCalculator from(std::uint8_t n_levels, std::uint32_t max_hue);
        static HueCalculator from_opencv(std::uint8_t n_levels);
        static HueCalculator rainbow_from_opencv(std::uint8_t n_levels);

        double hue_per_level() const;
        double hue_for_level(std::uint8_t level) const;
        template<typename T> 
        T hue_for_level_t(std::uint8_t level) const
        {
            return static_cast<T>(hue_for_level(level));
        }

    private:
        const double hue_per_level_ = 1.0;
    };
}
#endif //HUE_CALCULATOR_HPP_