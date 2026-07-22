use crate::{Image};

use tiff_wrap as tiff;
use tiff::{Tiff, TiffWrapError};

fn to_rgb_data<T: Clone>(data: &[T]) -> impl Iterator<Item=Vec<T>> + use<'_, T>
{
	data.chunks_exact(3).map(|chunk| chunk.to_vec())
}

#[derive(Debug)]
pub struct RGBImage<T>
{
	shape: (usize, usize),
	data: Vec<Vec<T>>
}

impl RGBImage<f64>
{
	pub fn from_opencv_tiff(filename: &str) -> Result<Self, TiffWrapError>
	{
		let mut reader = Tiff::read(filename)?;
		let cols = reader.width()? as usize;
		let rows = reader.height()? as usize;
		let shape = (rows, cols);
		let data = to_rgb_data(&reader.current_image()?.to_f64()?).collect();
		Ok(Self::new(shape, data))
	}
}

impl<T> RGBImage<T>
{
	pub fn new(shape: (usize, usize), data: Vec<Vec<T>>) -> Self
	{
		Self{shape, data}
	}

	pub fn red_channel(&self) -> Vec<&T>
	{
		self.get_channel(0)
	}

	pub fn green_channel(&self) -> Vec<&T>
	{
		self.get_channel(1)
	}

	pub fn blue_channel(&self) -> Vec<&T>
	{
		self.get_channel(2)
	}

	pub fn get_channel(&self, channel: usize) -> Vec<&T>
	{
		self.data.iter().map(|v| &v[channel]).collect()
	}

}

impl<T> Image for RGBImage<T>
{
	type Data = Vec<T>;

	fn n_rows(&self) -> usize 
	{ 
		self.shape.0
	}

	fn n_cols(&self) -> usize 
	{ 
		self.shape.1
	}

	fn data(&self) -> &[Self::Data] 
	{ 
		&self.data
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn test_get_rgb_pixel() 
	{
		let data = RGBImage::new((1, 2), vec![vec![1, 2 ,3], vec![4, 5, 6]]);
		assert_eq!(data.get_at(0, 1), &vec![4, 5, 6])
	}

	#[test]
	fn test_parsing() 
	{
		let data = vec![1, 2, 3, 4, 5, 6];
		let rgb_data: Vec<Vec<u8>> = to_rgb_data(&data).collect();
		assert_eq!(rgb_data, vec![vec![1, 2, 3], vec![4, 5, 6]])
	}

	#[test]
	fn test_parsing_buffer_size_error_last_n_dropped() 
	{
		let data = vec![1, 2, 3, 4, 5, 6, 7]; // 1 too many values - last is stripped
		let rgb_data: Vec<Vec<u8>> = to_rgb_data(&data).collect();
		assert_eq!(rgb_data, vec![vec![1, 2, 3], vec![4, 5, 6]])
	}

	#[test]
	fn test_get_rgb_channel() 
	{
		let image = RGBImage::new((1, 2), vec![vec![1, 2 ,3], vec![4, 5, 6]]);
		assert_eq!(image.red_channel().into_iter().copied().collect::<Vec<u8>>(), vec![1, 4]);
		assert_eq!(image.green_channel().into_iter().copied().collect::<Vec<u8>>(), vec![2, 5]);
		assert_eq!(image.blue_channel().into_iter().copied().collect::<Vec<u8>>(), vec![3, 6])
	}
}