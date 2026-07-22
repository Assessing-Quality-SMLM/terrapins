use super::{Thresholder};

use crate::utils;
use crate::Error;

fn _snr_threshold(snr: f64, n_voxels: f64) -> f64
{
	let half_snr = 0.5 * snr;
    let factor = 2.0 * half_snr.sqrt();
    let sqrt_n = n_voxels.sqrt();
    let num = half_snr + ((factor + 1.0) / sqrt_n);
    let denom = ((snr + 2.0) * 0.5) + (factor / sqrt_n);
    // println!("half_snr: {half_snr}");
    // println!("2 * sqrt(half_snr): {factor}");
    // println!("{num}/{factor}");
    return num / denom
}

fn _get_bit_snr(bit: f64) -> f64
{
	2.0f64.powf(bit) - 1.0
}

// pub fn bit(bit: f64, n_voxels: usize) -> Result<f64, Error>
// {
// 	let snr = _get_bit_snr(bit);
//     utils::to_f64(n_voxels).map(|n| _snr_threshold(snr, n))
// }

pub fn half_bit(n_voxels: usize) -> Result<f64, Error>
{
	let snr = 2.0f64.sqrt() - 1.0;
	utils::to_f64(n_voxels).map(|n| _snr_threshold(snr, n))
}

#[derive(Debug)]
pub struct HalfBit;

impl HalfBit
{
	pub fn new() -> Self
	{
		Self{}
	}
}

impl Thresholder for HalfBit
{
	type Error = Error;

	fn get_threshold(&self, _radius: usize, n_pixels: usize) -> Result<f64, Self::Error> 
	{ 
		half_bit(n_pixels)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn test_half_bit_one_voxel() 
	{
    	assert_eq!(half_bit(1).unwrap(), 1.0)
	}

	// #[test]
	// fn test_half_bit_generic_one_voxel() 
	// {
    // 	assert_eq!(bit(0.5, 1).unwrap(), 1.0)
	// }

	#[test]
	fn test_two_voxels() 
	{
    	assert_eq!(half_bit(2).unwrap(), 0.8417393120671525)
	}

	// #[test]
	// fn test_half_bit_generic_two_voxel() 
	// {
    // 	assert_eq!(bit(0.5, 2).unwrap(), 0.8417393120671525)
	// }
}