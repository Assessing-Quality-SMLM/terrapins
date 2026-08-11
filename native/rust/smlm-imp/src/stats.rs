use crate::{Image};

use sig_proc::stats::{mean as s_mean, variance as s_var, std as s_std};

pub trait Stats 
{
	fn mean(&self) -> f64;
	fn var_with(&self, mean: f64) -> f64;
	fn var(&self) -> f64
	{
		self.var_with(self.mean())
	}
	fn std(&self) -> f64;
}

impl<I> Stats for I
where I: Image<Data=f64>
{
	fn mean(&self) -> f64
	{
		s_mean(self.data().iter().map(|d| *d))
	}

	fn var_with(&self, mean: f64) -> f64
	{
		s_var(self.data().iter().copied(), mean)
	}

	fn std(&self) -> f64
	{
		s_std(self.var())
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	use crate::images::BorrowedImage;

	#[test]
	fn mean() 
	{
		let data : [f64; 4] = [1.0, 2.0, 3.0, 4.0];
		assert_eq!(BorrowedImage::new((1, 4), &data).mean(), 2.5);
	}

	#[test]	
	fn var() 
	{
		let data : [f64; 4] = [1.0, 2.0, 3.0, 4.0];
		assert_eq!(BorrowedImage::new((1, 4), &data).var(), 1.25);
	}

	#[test]	
	fn std() 
	{
		let data : [f64; 4] = [1.0, 2.0, 3.0, 4.0];
		assert_eq!(BorrowedImage::new((1, 4), &data).std(), 1.118033988749895);
	}
}