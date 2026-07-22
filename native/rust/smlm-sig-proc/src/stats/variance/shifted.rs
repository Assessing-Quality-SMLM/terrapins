use num_traits::{Zero, One};

use std::ops::{AddAssign, Sub, Div, Mul};

fn _variance_with_mean<T, I: Iterator<Item=T>>(items : I, mean: T, offset: T) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Mul<Output = T> + Copy
{
	let mut sum = T::zero();
	let mut sum_sqr = T::zero();
	let mut n = T::zero();
	for item in items
	{
		let x = item - mean;
		n += T::one();
		sum += x;
		sum_sqr += x * x;
	}
	let numerator = sum_sqr - (sum * sum) / n;
	let denom = n - offset;
	numerator / denom
}

pub fn variance_with_mean<T, I: Iterator<Item=T>>(items : I, mean: T) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy
{
	_variance_with_mean(items, mean, T::zero())
}

pub fn variance_unbiased_with_mean<T, I: Iterator<Item=T>>(items : I, mean: T) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy
{
	_variance_with_mean(items, mean, T::one())
}

pub fn variance<T>(items : &[T]) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy
{
	let shift = items[0];
	variance_unbiased_with_mean(items.iter().copied(), shift)
}