pub use one_seventh::{OneSeventh, ONESEVENTH};
pub use half_bit::{HalfBit};
pub use sigma_factor::{Sigma, DEFAULT_SIGMA};

mod half_bit;
mod one_seventh;
mod sigma_factor;

pub trait Thresholder
{
	type Error;
	fn get_threshold(&self, radius: usize, n_pixels: usize) -> Result<f64, Self::Error>;
}

impl<T: Thresholder> Thresholder for &T
{
	type Error = T::Error;
	fn get_threshold(&self, radius: usize, n_pixels: usize) -> Result<f64, Self::Error> 
	{
		(*self).get_threshold(radius, n_pixels)
	}
}

use crate::Error;

#[derive(Debug, Default, PartialEq)]
pub enum Threshold 
{
	#[default]
    HalfBit,
    OneSeventh,
    Sigma(f64),
}

impl TryFrom<&str> for Threshold
{
    type Error = String;
    fn try_from(value: &str) -> Result<Self, Self::Error>
    {
    	//half=half bit; 17=1/7; sigma_x = sigma factor (x = factor)
    	match value
    	{
    		"half" => Ok(Self::HalfBit),
    		"17" => Ok(Self::OneSeventh),
    		sigma @ _ => 
    		{
    			let splits = sigma.split("_").collect::<Vec<&str>>();
    			if splits.len() != 2
    			{
    				Err(format!("cannot parse {value} as threshold: {sigma} not recognised as sigma threshold"))
    			}
    			else 
    			{
    				let factor_str = splits[1];
    				let factor = factor_str.parse().map_err(|e| format!("cannot parse {value} as threshold: cannot parse {factor_str}: {e}"))?;
    				Ok(Self::Sigma(factor))
    			}
    		}
    	}
    }
}

impl TryFrom<u8> for Threshold
{
    type Error = Error;
    fn try_from(value: u8) -> Result<Self, Self::Error>
    {
        match value
        {
            0 => Ok(Self::HalfBit),
            1 => Ok(Self::OneSeventh),
            2 => Ok(Self::Sigma(DEFAULT_SIGMA)),
            _ => Err(Error::Threshold(value))
        }
    }
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn half_bit_test() 
	{
		assert_eq!(Threshold::try_from(0), Ok(Threshold::HalfBit))
	}

	#[test]
	fn one_seventh_test() 
	{
		assert_eq!(Threshold::try_from(1), Ok(Threshold::OneSeventh))
	}

	#[test]
	fn sigma_test() 
	{
		assert_eq!(Threshold::try_from(2), Ok(Threshold::Sigma(DEFAULT_SIGMA)))
	}

	#[test]
	fn fails_test() 
	{
		assert_eq!(Threshold::try_from(3), Err(Error::Threshold(3)))
	}

	#[test]
	fn can_parse_half_bit()
	{
		assert_eq!(Threshold::try_from("half"), Ok(Threshold::HalfBit))
	}

	#[test]
	fn can_parse_one_seventh()
	{
		assert_eq!(Threshold::try_from("17"), Ok(Threshold::OneSeventh))
	}

	#[test]
	fn can_parse_sigma_factor()
	{
		assert_eq!(Threshold::try_from("sigma_2"), Ok(Threshold::Sigma(2.0)))
	}

	#[test]
	fn errors_on_bad_sigma_factor()
	{
		assert_eq!(Threshold::try_from("sigma_blah").unwrap_err(), "cannot parse sigma_blah as threshold: cannot parse blah: invalid float literal")
	}

	#[test]
	fn errors_on_other_things()
	{
		assert_eq!(Threshold::try_from("blah").unwrap_err(), "cannot parse blah as threshold: blah not recognised as sigma threshold")
	}
}