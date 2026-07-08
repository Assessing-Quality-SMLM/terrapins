use num_traits::{Zero, One};

use std::ops::{AddAssign, Sub, Div, Mul};

fn _variance_with_mean<T, I: Iterator<Item=T>>(items : I, offset: T) -> (T, T)
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Mul<Output = T> + Copy + std::fmt::Display
{
	let mut n = T::zero();
	let mut mean = T::zero();
	let mut m_2 = T::zero();
	for item in items
	{
		n += T::one();
		let delta = item - mean;
		// println!("delta: {delta}");
		mean += delta / n;
		// println!("mu: {mean}");
		let delta_2 = item - mean; // uses updated mean
		// println!("delta_2: {delta_2}");
		m_2 += delta * delta_2;
		// println!("m_2: {m_2}");
	}

	let var = m_2 / (n - offset);
	(mean, var)
}

pub fn variance_with_mean<T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy + std::fmt::Display, I: Iterator<Item=T>>(items : I) -> (T, T)
{
	_variance_with_mean(items, T::zero())
}

pub fn variance_unbiased_with_mean<T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy + std::fmt::Display, I: Iterator<Item=T>>(items : I) -> (T, T)
{
	_variance_with_mean(items, T::one())
}

pub fn variance<T>(items : &[T]) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy + std::fmt::Display
{
	let (_mean, var) = variance_unbiased_with_mean(items.iter().copied());
	var
}