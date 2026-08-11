pub mod naive;
pub mod shifted;
pub mod two_pass;
pub mod welfords;

use std::ops::{AddAssign, Sub, Div};

use num_traits::{Zero, One};


#[derive(Debug)]
pub enum Method 
{
	Naive,
	Shifted,
	TwoPass,
	Welfords,
}

pub fn variance<T>(items : &[T], method: Method) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy + std::fmt::Display,
{
	match method
	{
		Method::Naive => naive::variance(items),
		Method::Shifted => shifted::variance(items),
		Method::TwoPass => two_pass::variance(items),
		Method::Welfords => welfords::variance(items)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	const DATA: [f64; 4] = [4.0, 7.0, 13.0, 16.0];

	#[test]
	fn basic() 
	{
		assert_eq!(variance(&DATA, Method::Naive), 30.0);
		assert_eq!(variance(&DATA, Method::Shifted), 30.0);
		assert_eq!(variance(&DATA, Method::TwoPass), 30.0);
		assert_eq!(variance(&DATA, Method::Welfords), 30.0);
	}
}