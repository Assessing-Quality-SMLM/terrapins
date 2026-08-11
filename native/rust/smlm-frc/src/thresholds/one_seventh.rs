use super::{Thresholder};

use crate::Error;

pub const ONESEVENTH : f64 = 1.0 / 7.0;

#[derive(Debug)]
pub struct OneSeventh;

impl Thresholder for OneSeventh
{

	type Error = Error;
	fn get_threshold(&self, _radius: usize, _n_pixels: usize) -> Result<f64, Self::Error>
	{
		Ok(ONESEVENTH)
	}
}