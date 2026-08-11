pub use self::gaussian::{Gaussian, SampleMethod};

// #[cfg(feature="fit")]
// mod fit;
mod gaussian;
pub mod histogram;
pub mod variance;

use num_traits::{Zero, One, Pow, Float};

use rand::{Rng, SeedableRng};
use rand::distributions::{Distribution as RDistribution, Uniform};
use rand::rngs::StdRng;

use std::ops::{AddAssign, Sub, Div};

pub fn mean<T: Zero + One + AddAssign + Div<Output=T>, I: Iterator<Item=T>>(items : I) -> T
{
	let mut total = T::zero();
	let mut count = T::zero();
	for item in items
	{
		total += item;
		count += T::one();
	}
	total / count
}

pub fn mean_fold<T: Zero + One + AddAssign + Div<Output=T>, I: Iterator<Item=T>>(items : I) -> T
{
	let (total, count) = items.fold((T::zero(), T::zero()), |(total, count), item| (total + item, count + T::one()));
	total / count
}

fn _variance<T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Pow<u8, Output=T> + Copy, I: Iterator<Item=T>>(items : I, mean: T, offset: T) -> T
{
	let mut total = T::zero();
	let mut count = T::zero();
	for item in items
	{
		total += (item - mean).pow(2);
		count += T::one();
	}
	total / (count - offset)
}

pub fn variance<T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Pow<u8, Output=T> + Copy, I: Iterator<Item=T>>(items : I, mean: T) -> T
{
	_variance(items, mean, T::zero())
}

pub fn variance_unbiased<T: Zero + One + AddAssign + Sub<Output=T> + Div<Output=T> + Pow<u8, Output=T> + Copy, I: Iterator<Item=T>>(items : I, mean: T) -> T
{
	_variance(items, mean, T::one())
}

pub fn std<T: Float>(var: T) -> T
{
	var.sqrt()
}

pub fn standard_deviation<T: Float + AddAssign + Pow<u8, Output=T>, I: Iterator<Item=T> + Clone>(items : I) -> T
{
	let m = mean(items.clone());
	std(variance(items, m))
}

pub trait Distribution
{
	type Dist: Distribution;
	fn n_dim() -> usize;
	fn from(theta: &[f64]) -> Self::Dist;
	fn params(&self, theta: &mut [f64]) -> ();
	fn pdf(&self, x: f64) -> f64;
	fn likelihood(&self, data: &[f64]) -> f64
	{
		let mut total = 0.0;
		for value in data
		{
			let temp = self.pdf(*value);
			if temp.is_finite()
			{
				total *= temp;
			}
		}
		total
	}

	fn ln_likelihood(&self, data: &[f64]) -> f64
	{
		let mut total = 0.0;
		for value in data
		{
			let temp = self.pdf(*value);
			if temp.is_finite()
			{
				total += temp.ln();
			}
		}
		total
	}

	// #[cfg(feature="fit")]
	// fn fit(&self, data: &[f64], bounds: Option<(&[f64], &[f64])>) -> Result<Self::Dist, String> 
	// where Self: Sized
	// {
	// 	fit::fit(self, data, bounds)
	// }
}

impl<D: Distribution> Distribution for &D
{
	type Dist = D::Dist;
	fn n_dim() -> usize
	{
		D::n_dim()
	}

	fn from(params: &[f64]) -> Self::Dist
	{
		D::from(params)
	}

	fn params(&self, theta: &mut [f64])
	{
		(*self).params(theta)
	}

	fn pdf(&self, data: f64) -> f64
	{
		(*self).pdf(data)
	}

}

pub fn seeded_rng(seed: u64) -> StdRng
{
	StdRng::seed_from_u64(seed)
}

pub fn uniform_sample_with<R: Rng>(n: usize, upper_bound: usize, mut rng: R) -> Vec<usize>
{
	let dist = Uniform::new_inclusive(0, upper_bound);
	(0..n).map(|_| dist.sample(&mut rng)).collect()
}

pub fn uniform_sample(n: usize, upper_bound: usize) -> Vec<usize>
{
	let rng = rand::thread_rng();
	uniform_sample_with(n, upper_bound, rng)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn basic_mean_test_f64() 
	{
		let me = mean([1.0, 2.0, 3.0, 4.0].into_iter());
		assert_eq!(me, 2.5);
	}

	#[test]
	fn basic_mean_fold_test_f64() 
	{
		let me = mean_fold([1.0, 2.0, 3.0, 4.0].into_iter());
		assert_eq!(me, 2.5);
	}

	#[test]
	fn basic_var_test_f64() 
	{
		let me = variance([1.0, 2.0, 3.0, 4.0].into_iter(), 2.5);
		assert_eq!(me, 1.25);
	}

	#[test]
	fn basic_var_unbiased_test_f64() 
	{
		let me = variance_unbiased([1.0, 2.0, 3.0, 4.0].into_iter(), 2.5);
		assert_eq!(me, 1.6666666666666667);
	}

	#[test]
	fn basic_std_f64() 
	{
		let var : f64 = 4.0;
		assert_eq!(std(var), 2.0);
	}

	#[test]
	fn basic_std_f32() 
	{
		let var : f32 = 4.0;
		assert_eq!(std(var), 2.0);
	}

	#[test]
	fn basic_standard_deviation_test_f64() 
	{
		let std = standard_deviation([1.0, 2.0, 3.0, 4.0].iter().copied());
		assert_eq!(std, 1.118033988749895);
	}

	#[test]
	fn basic_mean_test_i16() 
	{
		let data : [i16; 4] = [1, 2, 3, 4];
		let me = mean(data.into_iter());
		assert_eq!(me, 2);
	}

	#[test]
	fn basic_mean_fold_test_i16() 
	{
		let data : [i16; 4] = [1, 2, 3, 4];
		let me = mean_fold(data.into_iter());
		assert_eq!(me, 2);
	}

	#[test]
	fn basic_var_test_i16() 
	{
		let data : [i16; 4] = [1, 2, 3, 4];
		let me = variance(data.into_iter(), 2);
		assert_eq!(me, 1);
	}

	#[test]
	fn basic_var_unbiased_test_i16() 
	{
		let data : [i16; 4] = [1, 2, 3, 4];
		let me = variance_unbiased(data.into_iter(), 2);
		assert_eq!(me, 2);
	}
}