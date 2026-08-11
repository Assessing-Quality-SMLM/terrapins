use crate::error::Error;

use frc::{Threshold};

const DEFAULT_RANDOM_SPLITS : usize = 0;
const DEFAULT_DRIFT_BLOCK_SIZE : usize = 10; // percentage of data in each block split - a get 5% b gets 5%

#[derive(Debug, Clone, PartialEq)]
pub struct Frc 
{
	threshold: Option<String>,
	filter: Option<(String, Vec<f64>)>,	
	n_random: usize,
	drift_block_size: usize,
}

impl Frc
{
	pub const DEFAULT_N_RANDOM_SPLITS: usize = DEFAULT_RANDOM_SPLITS;
	pub const DEFAULT_DRIFT_BLOCK_SIZE: usize = DEFAULT_RANDOM_SPLITS;

	pub fn new(threshold: Option<&str>, filter: Option<(&str, Vec<f64>)>, n_random: usize, drift_block_size: usize) -> Self
	{
		Self
		{
			threshold: threshold.map(|s| s.to_string()),
			filter: filter.map(|(s, p)| (s.to_string(), p)),
			n_random,
			drift_block_size
		}
	}

	pub fn empty() -> Self
	{
		Self::new(None, None, DEFAULT_RANDOM_SPLITS, DEFAULT_DRIFT_BLOCK_SIZE)
	}

	pub fn threshold_str(&self) -> Option<&str>
	{
		self.threshold.as_ref().map(|s| s.as_str())
	}

	pub fn threshold(&self) -> Result<Threshold, Error>
	{
		match self.threshold.as_ref()
		{
			Some(s) => Threshold::try_from(s.as_str()).map_err(Error::parse),
			None => Ok(Threshold::default())
		}
	}

	pub fn filter(&self) -> Option<(&str, &[f64])>
	{
		self.filter.as_ref().map(|(s, p)| (s.as_str(), p.as_slice()))
	}

	pub fn n_random_splits(&self) -> usize
	{
		self.n_random
	}

	pub fn drift_block_size(&self) -> usize
	{
		self.drift_block_size
	}
}

impl Default for Frc
{
	fn default() -> Self 
	{
		let (f, p) = frc::filters::default_tukey_filter();
		Self
		{
			threshold: Some(String::from("17")),
			filter : Some((f.to_string(), p.to_vec())),
			n_random: DEFAULT_RANDOM_SPLITS,
			drift_block_size: DEFAULT_DRIFT_BLOCK_SIZE
		}    
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn default_threshold() 
	{
		assert_eq!(Frc::default().threshold().unwrap(), Threshold::OneSeventh);
	}

	#[test]
	fn default_filter() 
	{
		assert_eq!(Frc::default().filter(), Some(("tukey", [0.25].as_slice())));
	}

	#[test]
	fn can_have_none_filter() // dont need to specify one for frc but usually do want one
	{
		assert_eq!(Frc::empty().filter(), None);
	}

	#[test]
	fn default_drift_block_size()
	{
		assert_eq!(Frc::empty().drift_block_size(), 10);
		assert_eq!(Frc::default().drift_block_size(), 10)
	}
}