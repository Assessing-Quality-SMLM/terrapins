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

	/// Whether a localisation survives this filter.
	///
	/// A value the file never carried is kept. A filter exists to judge a quantity, and it
	/// cannot judge one that is absent; discarding on absence throws away every localisation in
	/// a file that simply lacks the column, which is silent and total. Keeping it makes the
	/// filter a no-op for that quantity, which is what it effectively was before the filter
	/// existed.
	///
	/// Note this is *not* the same as a placeholder value being out of range: a zero
	/// uncertainty is absent (kept), whereas a genuinely measured 5000 nm uncertainty is a
	/// measurement that fails the filter (dropped).
	fn admits(&self, value: f64, measured: bool) -> bool
	{
		!measured || self.within(value)
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
    	self.filter(move |l| f.admits(l.uncertainty(), l.has_measured_uncertainty()))
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
    	self.filter(move |l| f.admits(l.psf_sigma(), l.has_measured_psf_sigma()))
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
    		self.filter(move |l| psf_bounds.admits(l.psf_sigma(), l.has_measured_psf_sigma())
    		                  && uncertainty_bounds.admits(l.uncertainty(), l.has_measured_uncertainty())).collect()
    	}
    }
}


#[cfg(test)]
mod absence_tests
{
	use crate::{AllocatedLocalisation, constants::MISSING};
	use super::*;

	/// x, y and intensity are irrelevant here; the two sigmas are what the filters judge.
	fn loc(psf_sigma: f64, uncertainty: f64) -> AllocatedLocalisation
	{
		AllocatedLocalisation::new(1, 0.0, 0.0, psf_sigma, 1000.0, uncertainty)
	}

	/// The decision table for one filter. Absence is kept because a filter cannot judge a
	/// quantity that is not there; a measurement is judged on its value.
	///
	/// This is the behaviour to review if filtering ever looks wrong - see the README.
	#[test]
	fn psf_sigma_filter_decision_table()
	{
		let bounds = Some((60.0, 200.0));
		let cases = [
			// (psf sigma,   kept?, why)
			(110.0,          true,  "measured, inside the range"),
			(20.0,           false, "measured, below the range"),
			(5000.0,         false, "measured, above the range"),
			(MISSING,        true,  "absent - no column, or an empty field"),
			(0.0,            true,  "a placeholder zero is absence, not a tiny width"),
			(-1.0,           true,  "negative is not a width, so treated as absent"),
		];
		for (sigma, expected, why) in cases
		{
			let kept = [loc(sigma, 15.0)].into_iter().valid(bounds, None).len() == 1;
			assert_eq!(kept, expected, "psf sigma {sigma}: {why}");
		}
	}

	#[test]
	fn uncertainty_filter_decision_table()
	{
		let bounds = Some((0.0, 1000.0));
		let cases = [
			(15.0,           true,  "measured, inside the range"),
			(5000.0,         false, "measured, above the range"),
			(MISSING,        true,  "absent"),
			(0.0,            true,  "the placeholder a fitter with no uncertainty writes"),
		];
		for (uncertainty, expected, why) in cases
		{
			let kept = [loc(110.0, uncertainty)].into_iter().valid(None, bounds).len() == 1;
			assert_eq!(kept, expected, "uncertainty {uncertainty}: {why}");
		}
	}

	#[test]
	fn both_filters_must_admit()
	{
		let psf = Some((60.0, 200.0));
		let unc = Some((0.0, 1000.0));
		assert_eq!([loc(110.0, 15.0)].into_iter().valid(psf, unc).len(), 1);
		assert_eq!([loc(5000.0, 15.0)].into_iter().valid(psf, unc).len(), 0, "bad width rejected");
		assert_eq!([loc(110.0, 5000.0)].into_iter().valid(psf, unc).len(), 0, "bad uncertainty rejected");
		// The case that emptied whole files: neither quantity measured.
		assert_eq!([loc(MISSING, MISSING)].into_iter().valid(psf, unc).len(), 1,
			"a localisation with neither quantity must survive both filters");
	}

	#[test]
	fn no_filters_keeps_everything()
	{
		assert_eq!([loc(MISSING, 0.0), loc(5000.0, 5000.0)].into_iter().valid(None, None).len(), 2);
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