pub fn psf_in_pixels(psf_nm : f64, pixel_size_nm: f64) -> f64
{
	psf_nm / pixel_size_nm
}

pub fn psf_in_sr_pixels(instrument_psf_fwhm_nm : f64, instrument_pixel_size: f64, magnification: f64) -> f64
{	
	let sr_pixel_size = instrument_pixel_size / magnification;
	psf_in_pixels(instrument_psf_fwhm_nm, sr_pixel_size)

	// (instrument_psf_fwhm_nm * magnification) / instrument_pixel_size 
	// is equivalent if want to work only in multiplications
}

#[cfg(test)]
mod tests 
{
	use super::*;

	const BASELINE_PSF : f64 = 270.0;
	const BASELINE_MAG : f64 = 10.0;
	const BASELINE_PIXEL_SIZE : f64 = 160.0;
	const BASELINE_RESULT : f64 = 16.875;

	#[test]
	fn raw_test_baseline() 
	{
		assert_eq!(psf_in_sr_pixels(BASELINE_PSF, BASELINE_PIXEL_SIZE, BASELINE_MAG), BASELINE_RESULT);
	}

	#[test]
	fn raw_test_half_mag() 
	{
		assert_eq!(psf_in_sr_pixels(BASELINE_PSF, BASELINE_PIXEL_SIZE, BASELINE_MAG / 2.0), BASELINE_RESULT / 2.0)
	}

	#[test]
	fn raw_test_100nm_ps_mf_5() 
	{
		assert_eq!(psf_in_sr_pixels(BASELINE_PSF, 100.0, 5.0), 13.5)
	}
}