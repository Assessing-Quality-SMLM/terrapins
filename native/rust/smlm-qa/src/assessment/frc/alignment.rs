use super::calibration::{PIXEL_SIZE_NM};

fn scale_factor(reference_pixel_size: f64, measured_pixel_size: f64) -> f64
{
	reference_pixel_size / measured_pixel_size
}

pub fn align_to_calibration(qs: &[f64], pixel_size_nm: f64) -> Vec<f64>
{	
	let sf = scale_factor(PIXEL_SIZE_NM, pixel_size_nm);
	qs.iter().map(|x| x * sf).collect()
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn scale_factor_basic() 
	{		
		assert_eq!(scale_factor(PIXEL_SIZE_NM, PIXEL_SIZE_NM), 1.0)
	}

	#[test]
	fn scale_factor_adjusts_for_pixel_size() 
	{
		let reference = PIXEL_SIZE_NM;
		let test = 6.25;
		assert_eq!(scale_factor(reference, test), 2.0)
	}	
}