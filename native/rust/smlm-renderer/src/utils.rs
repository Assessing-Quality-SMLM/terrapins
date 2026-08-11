// use crate::data_properties::{ImageFrame, NmFrame};

pub fn super_res_pixel_size(camera_pixel_size: f64, magnification: f64) -> f64
{
	camera_pixel_size / magnification
}

pub fn rendering_pixel_size(pixel_size: f64, zoom_level: usize) -> f64
{
	pixel_size / zoom_level as f64
}

pub fn distance(x: f64, y: f64) -> f64
{
	let mag = x - y;
	(mag * mag).sqrt()
}

pub fn n_pixels_required(length_nm: f64, pixel_size_nm: f64) -> usize
{
	(length_nm / pixel_size_nm).ceil() as usize
}

pub fn pixel_location(location: f64, pixel_size: f64) -> usize
{
	(location / pixel_size).floor() as usize
}

// pub fn image_size_from_pixels(size_pixels: usize, pixel_size: f64) -> f64
// {
// 	(size_pixels as f64) * pixel_size
// }

pub fn shifted_pixel_location(location: f64, min_offset: f64, pixel_size: f64) -> usize
{
	// println!("location: {location}, min_offset: {min_offset}, pixel_size: {pixel_size}");
    let shift = location - min_offset;
    pixel_location(shift, pixel_size)
}

pub fn get_pixel_centre_nm(pixel: usize, pixel_size: f64) -> f64
{
	let start = (pixel as f64) * pixel_size;
	start + (pixel_size / 2.0)
}

pub fn get_global_pixel_centre_nm(pixel: usize, offset: f64, pixel_size: f64) -> f64
{
	// println!("pixel: {pixel}, offset: {offset}, pixel_size: {pixel_size}");
	get_pixel_centre_nm(pixel, pixel_size) + offset
}

pub fn pixel_boundaries(pixel: (usize, usize), pixel_size: f64) -> ((f64, f64), (f64, f64))
{
	let (row, col) = pixel;
	let y_lower = (row as f64) * pixel_size;
	let y_upper = y_lower + pixel_size;

	let x_lower = (col as f64) * pixel_size;
	let x_upper = x_lower + pixel_size;

	( (x_lower, x_upper), (y_lower, y_upper))
}

// pub fn downstep(buffer: &mut [f64]) -> &mut [f32]
// {
// 	for idx in 0..buffer.len()
// 	{
// 		let down_step = buffer[idx] as f32;
// 		let f32_buffer = unsafe {std::mem::transmute::<&mut [f64], &mut [f32]>(buffer)};
// 		f32_buffer[idx] = down_step;
// 	}
// 	unsafe {std::mem::transmute::<&mut [f64], &mut [f32]>(buffer)}
// }

// pub fn pixel_frame_to_nm_frame(pixel_frame: &ImageFrame, pixel_size: f64) -> NmFrame
// {
// 	let x_start = pixel_frame.start_col() as f64 * pixel_size;
// 	let y_start = pixel_frame.start_row() as f64 * pixel_size;

// 	let width = pixel_frame.width() as f64 * pixel_size;
// 	let height = pixel_frame.height() as f64 * pixel_size;

// 	NmFrame::from(x_start, y_start, width, height)
// }


#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn super_res_pixel_size_test() 
	{
		assert_eq!(super_res_pixel_size(2.0, 10.0), 0.2)
	}

	// #[test]
	// fn test_downstep()
	// {
	// 	let mut buffer = [1.2345678901, 1.2345678901];
	// 	assert_eq!(downstep(&mut buffer), [1.23456789, 1.23456789]);

	// }

	#[test]
	fn test_rendering_pixel_size() 
	{
		assert_eq!(rendering_pixel_size(12.3, 5), 2.46);
	}

	#[test]
	fn n_pixels_required_test() 
	{
		assert_eq!(n_pixels_required(23.0, 10.0), 3);
	}

	#[test]
	fn pixel_location_test() 
	{
		assert_eq!(pixel_location(0.1, 1.0), 0);
		assert_eq!(pixel_location(3.8, 1.2), 3)
	}

	#[test]
	fn shifted_pixel_location_test() 
	{
		assert_eq!(shifted_pixel_location(3.9, 1.4, 1.2), 2)
	}

	#[test]
	fn pixel_centre_test() 
	{
		assert_eq!(get_pixel_centre_nm(2, 1.2), 3.0);
	}

	#[test]
	fn pixel_centre_offset_test() 
	{
		assert_eq!(get_global_pixel_centre_nm(2, 3.4, 1.2), 6.4);
	}

	// #[test]
	// fn image_size_from_pixels_test() 
	// {
	// 	assert_eq!(image_size_from_pixels(512, 10.2), 5222.4);
	// }

	// #[test]
	// fn pixel_frame_to_nm_frame_test() 
	// {
	// 	let pixel_frame = ImageFrame::from(10, 20, 100, 50);
	// 	let pixel_size = 10.0;
	// 	let nm_frame = pixel_frame_to_nm_frame(&pixel_frame, pixel_size);
	// 	let expected = NmFrame::from(100.0, 200.0, 1000.0, 500.0);
	// 	assert_eq!(nm_frame, expected);
	// }
}