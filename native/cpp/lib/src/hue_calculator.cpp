#include "hue_calculator.hpp"

#include <cmath>

namespace imp
{
	HueCalculator::HueCalculator(double offset, double hue_per_level) 
		: offset_(offset)
		, hue_per_level_(hue_per_level)
	{
	}

	HueCalculator HueCalculator::cold_to_hot_opencv(std::uint8_t n_levels)
	{
		const double hue_per_level = 120.0 / static_cast<double>(n_levels - 1);
		return HueCalculator(120.0, hue_per_level);
	}

	// levels start from 1 so this has 1 based logic
	double HueCalculator::hue_for_level(std::uint8_t level) const
	{		
		const auto level_double = static_cast<double>(level - 1);
		return offset_ - (level_double * hue_per_level_);
	}

	double HueCalculator::hue_per_level() const
	{
		return hue_per_level_;
	}
}