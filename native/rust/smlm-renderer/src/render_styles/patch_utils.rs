fn get_start_end_(location: usize, boundary: usize, patch_size: usize) -> (usize, usize)
{
	let half_patch = patch_size / 2;
	let end = location + half_patch;
    let start = location.checked_sub(half_patch);
	let clipped_start = if start.is_some() {start.unwrap()} else{0};
    let clipped_end = std::cmp::min(end, boundary);
    (clipped_start, clipped_end)
}

pub fn get_start_end(location: usize, n_pixels: usize, patch_size: usize) -> (usize, usize)
{
	get_start_end_(location, n_pixels - 1, patch_size)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn start_end_basic() 
	{
		let location = 10;
		let boundary = 20;
		let patch_size = 5;
		let (start, end) = get_start_end_(location, boundary, patch_size);
		assert_eq!(start, 8);
		assert_eq!(end, 12);
	}

	#[test]
	fn start_clipped_to_zero() 
	{
		let location = 1;
		let boundary = 20;
		let patch_size = 5;
		let (start, end) = get_start_end_(location, boundary, patch_size);
		assert_eq!(start, 0);
		assert_eq!(end, 3);
	}

	#[test]
	fn end_clipped_to_limit()
	{
		let location = 10;
		let boundary = 11;
		let patch_size = 5;
		let (start, end) = get_start_end_(location, boundary, patch_size);
		assert_eq!(start, 8);
		assert_eq!(end, 11);
	}

	#[test]
	fn api_clips_with_pixels()
	{
		let location = 3;
		let n_pixels = 5;
		let patch_size = 5;
		let (start, end) = get_start_end(location, n_pixels, patch_size);
		assert_eq!(start, 1);
		assert_eq!(end, 4);
	}
}
/// Fallback Gaussian width in nanometres for a localisation whose uncertainty is not a
/// measurement.
///
/// Matches `locs::constants::DEFAULT_UNCERTAINTY`, which the CSV reader already substitutes when
/// a file has no uncertainty column at all, so a ThunderSTORM-format file whose column is a
/// placeholder now renders the same as the equivalent CSV with the column omitted.
pub const FALLBACK_UNCERTAINTY_NM: f64 = 20.0;

/// The Gaussian width to blur one localisation by, in nanometres.
///
/// This is its *uncertainty*, not its fitted peak width - the two are both called sigma in
/// places and only the uncertainty is used for rendering.
///
/// A placeholder is substituted rather than rendered. Zero is the value a fitter with no
/// uncertainty writes into the column, and blurring by it is not merely inaccurate: `blur_2d`
/// evaluates `exp(-d^2 / 0) / 0`, which is `0/0` at every pixel of the patch, so a single such
/// localisation turns the entire reconstruction into NaN. Falling back keeps the image usable;
/// the fact that the values are not measurements is reported separately, through the mean
/// localisation precision being absent.
pub fn blur_sigma_nm<L: locs::UncertainLocalisation>(localisation: &L) -> f64
{
    if localisation.has_measured_uncertainty()
    {
        localisation.uncertainty()
    }
    else
    {
        FALLBACK_UNCERTAINTY_NM
    }
}

#[cfg(test)]
mod blur_sigma_tests
{
    use super::*;
    use locs::AllocatedLocalisation;

    fn with_uncertainty(uncertainty: f64) -> AllocatedLocalisation
    {
        AllocatedLocalisation::new(1, 0.0, 0.0, 100.0, 1000.0, uncertainty)
    }

    #[test]
    fn a_measured_uncertainty_is_used()
    {
        assert_eq!(blur_sigma_nm(&with_uncertainty(12.5)), 12.5);
    }

    #[test]
    fn placeholders_fall_back()
    {
        for value in [0.0, -1.0, f64::NAN, f64::INFINITY]
        {
            assert_eq!(blur_sigma_nm(&with_uncertainty(value)), FALLBACK_UNCERTAINTY_NM);
        }
    }
}
