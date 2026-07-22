use num_traits::{Zero, One};

use std::ops::{AddAssign, Sub, Div, Mul};

pub fn variance<T>(items : &[T]) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Mul<Output = T> + Copy
{
	let mut sum = T::zero();
	let mut sum_sqr = T::zero();
	let mut n = T::zero();
	for item in items.iter()
	{
		let x = *item;
		n += T::one();
		sum += x;
		sum_sqr += x * x;
	}
	let numerator = sum_sqr - (sum * sum) / n;
	let denom = n - T::one();
	numerator / denom
}