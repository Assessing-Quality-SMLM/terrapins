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