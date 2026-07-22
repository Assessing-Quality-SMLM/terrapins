use super::Thresholder;

use crate::Error;

pub const DEFAULT_SIGMA: f64 = 3.0;

pub fn sigma_factor(sigma: f64, n_pixels: usize) -> f64
{
	let denom = ((n_pixels as f64) / 2.0).sqrt();
	sigma / denom
}

// pub fn sigma_factor_asymmetry_adjustment(sigma: f64, n_pixels: usize, n_asm: usize) -> f64
// {
// 	sigma_factor(sigma, n_pixels) * (n_asm as f64).sqrt()
// }

#[derive(Debug)]
pub struct Sigma
{
	sigma: f64,
}

impl Sigma
{
	pub fn new(sigma: f64) -> Self
	{
		Self{sigma}
	}
}

impl Thresholder for Sigma
{
	type Error = Error;
	fn get_threshold(&self, _radius: usize, n_pixels: usize) -> Result<f64, Self::Error>
	{
		Ok(sigma_factor(self.sigma, n_pixels))
	}
}

impl Default for Sigma
{
	fn default() -> Self
	{
		Self::new(DEFAULT_SIGMA)
	}
}