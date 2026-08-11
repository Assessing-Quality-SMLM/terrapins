use crate::{FitLocalisation, UncertainLocalisation};

#[derive(Debug)]
struct Bounds
{
	min: f64,
	max: f64
}

impl Bounds
{
	pub fn new(min: f64, max: f64) -> Self
	{
		Self{min, max}
	}

	fn within(&self, value: f64) -> bool
	{
		self.min < value && value < self.max
	}
}

pub trait UncertaintyFilter<T: UncertainLocalisation>: Iterator<Item=T>
{
    fn valid_uncertainty(self, min: f64, max: f64) -> impl Iterator<Item=T>;
}

impl <T: UncertainLocalisation, I: Iterator<Item=T>> UncertaintyFilter<T> for I 
{
    fn valid_uncertainty(self, min: f64, max: f64) -> impl Iterator<Item=T>
    {
    	let f = Bounds::new(min, max);
    	self.filter(move |l| f.within(l.uncertainty()))
    }
}

pub trait PSFSigmaFilter<T: FitLocalisation>: Iterator<Item=T>
{
    fn valid_psf_sigma(self, min: f64, max: f64) -> impl Iterator<Item=T>;
}

impl <T: FitLocalisation, I: Iterator<Item=T>> PSFSigmaFilter<T> for I 
{
    fn valid_psf_sigma(self, min: f64, max: f64) -> impl Iterator<Item=T>
    {
    	let f = Bounds::new(min, max);
    	self.filter(move |l| f.within(l.psf_sigma()))
    }
}

pub trait ValidFilter<T: FitLocalisation + UncertainLocalisation>: Iterator<Item=T>
{
    fn valid(self, psf_bounds: Option<(f64, f64)>, uncertainty_bounds: Option<(f64, f64)>) -> Vec<T>;
}

impl <T: FitLocalisation + UncertainLocalisation, I: Iterator<Item=T>> ValidFilter<T> for I 
{
    fn valid(self, psf_bounds: Option<(f64, f64)>, uncertainty_bounds: Option<(f64, f64)>) -> Vec<T>
    {
    	if psf_bounds.is_none() && uncertainty_bounds.is_none()
    	{
    		self.collect()
    	}
    	else if psf_bounds.is_some() && uncertainty_bounds.is_none()
    	{
    		let (psf_min, psf_max) = psf_bounds.unwrap();
    		self.valid_psf_sigma(psf_min, psf_max).collect()
    	}
    	else if psf_bounds.is_none() && uncertainty_bounds.is_some()
    	{
    		let (uncertainty_min, uncertainty_max) = uncertainty_bounds.unwrap();
    		self.valid_uncertainty(uncertainty_min, uncertainty_max).collect()
    	}
    	else 
    	{
    		let (psf_min, psf_max) = psf_bounds.unwrap();
    		let psf_bounds = Bounds::new(psf_min, psf_max);

    		let (uncertainty_min, uncertainty_max) = uncertainty_bounds.unwrap();
    		let uncertainty_bounds = Bounds::new(uncertainty_min, uncertainty_max);
    		self.filter(move |l| psf_bounds.within(l.psf_sigma()) && uncertainty_bounds.within(l.uncertainty())).collect()
    	}
    }
}


#[cfg(test)]
mod tests 
{
	use crate::AllocatedLocalisation;

use super::*;

	#[test]
	fn bounds_are_exclusive() 
	{
		let lower = 1.0;
		let upper = 2.0;
		let u = Bounds::new(lower, upper);
		assert_eq!(u.within(lower), false);
		assert_eq!(u.within(1.1), true);
		assert_eq!(u.within(upper), false)
	}

	#[test]
	fn uncertainty_filter_adaptor_test()
	{
		let data = [AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0), 
					AllocatedLocalisation::new(0, 6.0, 7.0, 8.0, 9.0, 10.0)];
		let filtered = data.iter().valid_uncertainty(4.9, 6.0).collect::<Vec<&AllocatedLocalisation>>();
		assert_eq!(filtered.len(), 1);
		assert_eq!(filtered[0], &AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0))
	}

	#[test]
	fn psf_sigma_filter_adaptor_test()
	{
		let data = [AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0), 
					AllocatedLocalisation::new(0, 6.0, 7.0, 8.0, 9.0, 10.0)];
		let filtered = data.iter().valid_psf_sigma(2.9, 4.0).collect::<Vec<&AllocatedLocalisation>>();
		assert_eq!(filtered.len(), 1);
		assert_eq!(filtered[0], &AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0))
	}

	#[test]
	fn valid_filter_adaptor_both_test()
	{
		let data = [AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0), 
					AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 5.0),
					AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5),
					AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 1.5)];
		let lower = 1.0;
		let upper = 2.0;
		let filtered = data.iter().valid(Some((lower, upper)), Some((lower, upper)));
		assert_eq!(filtered.len(), 1);
		assert_eq!(filtered[0], &AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 1.5))
	}

	#[test]
	fn valid_filter_adaptor_neither_test()
	{
		let data = [AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0), 
					AllocatedLocalisation::new(0, 1.0, 1.5, 3.0, 4.0, 5.0),
					AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5),
					AllocatedLocalisation::new(0, 1.0, 1.5, 3.0, 4.0, 1.5)];
		let filtered = data.iter().valid(None, None).into_iter().copied().collect::<Vec<AllocatedLocalisation>>();
		assert_eq!(filtered, data)
	}

	#[test]
	fn valid_filter_adaptor_psf_only()
	{
		let data = [AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0), 
					AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 5.0),
					AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5),
					AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 1.5)];
		let lower = 1.0;
		let upper = 2.0;
		let filtered = data.iter().valid(Some((lower, upper)), None);
		assert_eq!(filtered.len(), 2);
		assert_eq!(filtered[0], &AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 5.0));
		assert_eq!(filtered[1], &AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 1.5));
	}

	#[test]
	fn valid_filter_adaptor_uncertainty_only()
	{
		let data = [AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0), 
					AllocatedLocalisation::new(0, 1.0, 1.5, 3.0, 4.0, 5.0),
					AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5),
					AllocatedLocalisation::new(0, 1.0, 1.5, 3.0, 4.0, 1.5)];
		let lower = 1.0;
		let upper = 2.0;
		let filtered = data.iter().valid(None, Some((lower, upper)));
		assert_eq!(filtered.len(), 2);
		assert_eq!(filtered[0], &AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5));
		assert_eq!(filtered[1], &AllocatedLocalisation::new(0, 1.0, 1.5, 3.0, 4.0, 1.5));
	}
}