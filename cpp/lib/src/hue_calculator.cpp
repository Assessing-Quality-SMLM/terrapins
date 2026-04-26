#include "hue_calculator.hpp"

#include <cmath>

namespace imp
{
	HueCalculator::HueCalculator(double hue_per_level) 
		: hue_per_level_(hue_per_level)
	{
	}

	HueCalculator HueCalculator::from(std::uint8_t n_levels, std::uint32_t max_hue)
	{
		const double n_levels_double = static_cast<double>(n_levels);
		const double max_hue_double = static_cast<double>(max_hue);
		const double hue_per_level = max_hue_double / n_levels_double;
		return HueCalculator(hue_per_level);
	}

	HueCalculator HueCalculator::from_opencv(std::uint8_t n_levels)
	{
		return from(n_levels, OPENCV_MAX_HUE_LEVELS);
	}

	HueCalculator HueCalculator::rainbow_from_opencv(std::uint8_t n_levels)
	{
		const auto max_hue = (3.0/4.0) * static_cast<double>(OPENCV_MAX_HUE_LEVELS);
		const auto max_int_hue = static_cast<std::uint32_t>(std::ceil(max_hue));
		return from(n_levels, max_int_hue);
	}

	// levels start from 1 so this has 1 based logic
	double HueCalculator::hue_for_level(std::uint8_t level) const
	{		
		const auto level_double = static_cast<double>(level - 1);
		return level_double * hue_per_level_;
	}

	double HueCalculator::hue_per_level() const
	{
		return hue_per_level_;
	}
}