use crate::stats;
use crate::stats::variance::shifted;

use num_traits::{Zero, One};

use std::ops::{AddAssign, Sub, Div};

pub fn variance<T>(items : &[T]) -> T
where T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Copy
{
	let mean = stats::mean(items.iter().copied());
	shifted::variance_unbiased_with_mean(items.iter().copied(), mean)
}