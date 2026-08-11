#ifndef ZERO_HPP_
#define ZERO_HPP_

#include <concepts>

namespace imp
{
	namespace num_traits
	{	
		template<typename T>
		struct Zero;

		template<>
		struct Zero<unsigned int>
		{
			static constexpr unsigned int zero()
			{
				return 0;
			}
		};

		template<>
		struct Zero<int>
		{
			static constexpr int zero()
			{
				return 0;
			}
		};

		template<>
		struct Zero<double>
		{
			static constexpr double zero()
			{
				return 0.0;
			}
		};

		template<>
		struct Zero<float>
		{
			static constexpr float zero()
			{
				return 0.0;
			}
		};

		template<typename T>
		concept HasZero = requires
		{
			{Zero<T>::zero()} -> std::convertible_to<T>;
		};
	}
}

#endif //ZERO_HPP_