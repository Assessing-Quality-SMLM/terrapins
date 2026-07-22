pub use self::tukey::{Tukey};

mod tukey;

use crate::{utils};

use std::fmt::{Display, Formatter, Error as FmtError};

#[derive(Debug, PartialEq)]
pub enum Error
{
	Parameter(String),
	Custom(String)
}

impl Error
{
	fn message(&self) -> String
	{
		match self
		{
			Self::Parameter(invalid_message) => invalid_message.clone(),
			Self::Custom(error) => error.to_string()
		}
	}
}

impl Display for Error
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError> 
	{
		write!(f, "{}", self.message())
	}
}

pub trait Window
{
	type Error;
	fn create(theta: &[f64]) -> Result<Self, Self::Error> where Self: Sized;
	fn generate(&self, n: usize) -> Result<Vec<f64>, Self::Error>;
	fn generate_into(&self, n: usize, data: &mut [f64]) -> Result<(), Self::Error>;
}

pub fn create_2d_window_from<W: Window>(window: W, n_rows: usize, n_cols: usize) -> Result<Vec<f64>, W::Error>
{
	let mut output = vec![1.0; n_rows * n_cols];
	let w1 = window.generate(n_rows)?;
	let w2 = window.generate(n_cols)?;
	for (r, c) in (0..n_rows).flat_map(|r| (0..n_cols).map(move |c| (r, c)))
	{	
		let idx = utils::get_index(r, c, n_cols);
		output[idx] = w1[r] * w2[c];
	}
	Ok(output)
}

fn create_2d_window<W: Window>(n_rows: usize, n_cols: usize, theta: &[f64]) -> Result<Vec<f64>, W::Error>
{
	W::create(theta).and_then(|w| create_2d_window_from(w, n_rows, n_cols))
}

pub fn create_square_window_from<W: Window>(window: W, n: usize) -> Result<Vec<f64>, W::Error>
{
	create_2d_window_from(window, n, n)
}

pub fn create_square_window<W: Window>(n: usize, theta: &[f64]) -> Result<Vec<f64>, W::Error>
{
	create_2d_window::<W>(n, n, theta)
}

pub fn window_iter<'a>(data: &'a [f64], window: &'a [f64]) -> impl Iterator<Item=f64> + 'a
{
	data.iter().zip(window).map(|(d, w)| d * w)
}

pub fn apply_window(data: &[f64], window: &[f64]) -> Vec<f64>
{
	window_iter(data, window).collect()	
}


#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn get_square_window_test()
	{
		let window = create_square_window::<Tukey>(10, &[0.25]).unwrap();
		let expected = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.9406018657828265, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9406018657828265, 0.0, 0.0, 0.9698463103929542, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9698463103929542, 0.0, 0.0, 0.9698463103929542, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9698463103929542, 0.0, 0.0, 0.9698463103929542, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9698463103929542, 0.0, 0.0, 0.9698463103929542, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9698463103929542, 0.0, 0.0, 0.9698463103929542, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9698463103929542, 0.0, 0.0, 0.9698463103929542, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9698463103929542, 0.0, 0.0, 0.9406018657828265, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9698463103929542, 0.9406018657828265, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
		assert_eq!(window, expected)
	}

	#[test]
	fn apply_window_test() 
	{
		let data = (0..100).map(|x| x as f64).collect::<Vec<f64>>();
		let window = create_square_window::<Tukey>(10, &[0.25]).unwrap();
		println!("{:?}", window);
		let new_data = apply_window(&data, &window);
		let expected = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.34662052361109, 11.63815572471545, 12.608002035108404, 13.577848345501359, 14.547694655894313, 15.517540966287267, 16.48738727668022, 16.930833584090877, 0.0, 0.0, 20.366772518252038, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 27.155696691002717, 0.0, 0.0, 30.065235622181582, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 36.85415979493226, 0.0, 0.0, 39.76369872611112, 42.0, 43.0, 44.0, 45.0, 46.0, 47.0, 46.5526228988618, 0.0, 0.0, 49.462161830040664, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 56.25108600279135, 0.0, 0.0, 59.160624933970205, 62.0, 63.0, 64.0, 65.0, 66.0, 67.0, 65.94954910672088, 0.0, 0.0, 68.85908803789975, 72.0, 73.0, 74.0, 75.0, 76.0, 77.0, 75.64801221065044, 0.0, 0.0, 76.18875112840894, 79.52739745222225, 80.4972437626152, 81.46709007300815, 82.4369363834011, 83.40678269379406, 84.37662900418702, 82.77296418888872, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
		assert_eq!(new_data, expected);
	}

		#[test]
	fn square_window() 
	{
		let n = 5;
		let theta = [];
		let data = create_square_window::<FakeWindow>(n, &theta).unwrap();
		let expected = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 3.0, 4.0, 0.0, 2.0, 4.0, 6.0, 8.0, 0.0, 3.0, 6.0, 9.0, 12.0, 0.0, 4.0, 8.0, 12.0, 16.0];
		assert_eq!(data, expected)
	}

	#[derive(Debug)]
	struct FakeWindow;
	impl Window for FakeWindow
	{

		type Error = ();
		fn create(_theta: &[f64]) -> Result<Self, Self::Error> 
		{ 
			Ok(Self)
		}

		fn generate(&self, n: usize) -> Result<Vec<f64>, Self::Error> 
		{ 
			Ok((0..n).map(|x| x as f64).collect())
		}
		
		fn generate_into(&self, n: usize, data: &mut [f64]) -> Result<(), Self::Error> 
		{ 
			for idx in 0..n
			{
				data[idx] = idx as f64
			}
			Ok(())
		}
	}
}