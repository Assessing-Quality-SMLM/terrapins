use crate::{fft::{self, hermitian_extend, normalise_round_trip}};
use fft::c64;

use std::fmt::{Display, Formatter, Error as FmtError};

#[derive(Debug)]
pub enum ConvolutionError 
{
	DimensionsDoNotMatch
}

impl Display for ConvolutionError
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError> 
	{
		write!(f, "Dimensions do not match")
	}
}

#[allow(dead_code)]
fn fft_convolve_fast(data: &[f64], window: &[f64], n_rows: usize, n_cols: usize) -> Vec<f64>
{
	let data_fft = fft::fft_real_to_complex(&data);
	let window_fft = fft::fft_real_to_complex(&window);
	let convolution_r2c = data_fft.iter().zip(window_fft.iter()).map(|(d, w)| d * w).collect::<Vec<c64>>();
	let hermitian = hermitian_extend(&convolution_r2c, n_rows, n_cols);
	let mut r = fft::fft_complex_to_real(&hermitian);

	// let extended_real = hermitian_extend(&data_fft, n_rows, n_cols);
	// let data_r2c = hermitian_extend(&data_fft, n_rows, n_cols);
	// let c_data = data.iter().map(|x| c64::new(*x, 0.0)).collect::<Vec<c64>>();
	// let data_fft = fft::fft_complex_to_complex(&c_data);

	// for (a, b) in extended_real.iter().zip(data_fft.iter())
    // {
    //     println!("{}", a - b);
    // }

	// let difference = data_fft.iter().zip(data_r2c.iter()).map(|(a, b)| a - b).collect::<Vec<c64>>();
	// println!("{:?}", difference);
	
	// let window_r2c = hermitian_extend(&window_fft, n_rows, n_cols);
	// let window_c_data = window.iter().map(|x| c64::new(*x, 0.0)).collect::<Vec<c64>>();
	// let window_fft = fft::fft_complex_to_complex(&window_c_data);

	// let difference = window_fft.iter().zip(window_r2c.iter()).map(|(a, b)| a - b).collect::<Vec<c64>>();
	// println!("{:?}", difference);
	

	// let convolution = data_fft.into_iter().zip(window_fft.into_iter()).map(|(d, w)| d * w).collect::<Vec<c64>>();

	// let difference = convolution.iter().zip(convolution_r2c.iter()).map(|(a, b)| a - b).collect::<Vec<c64>>();
	// println!("{:?}", difference);
	
	// let mut r = fft::ifft_complex_to_complex(&convolution).into_iter().map(|c| c.re).collect::<Vec<f64>>();
	normalise_round_trip(&mut r);
	r
}

fn fft_convolve_slow(data: &[f64], window: &[f64]) -> Vec<f64>
{
	let c_data = data.iter().map(|x| c64::new(*x, 0.0)).collect::<Vec<c64>>();
	let data_fft = fft::fft_complex_to_complex(&c_data);

	let window_c_data = window.iter().map(|x| c64::new(*x, 0.0)).collect::<Vec<c64>>();
	let window_fft = fft::fft_complex_to_complex(&window_c_data);

	let m = data_fft.into_iter().zip(window_fft.into_iter()).map(|(d, w)| d * w).collect::<Vec<c64>>();
	let c = fft::ifft_complex_to_complex(&m);
	
	let mut real_part = c.into_iter().map(|c| c.re).collect::<Vec<f64>>();
	normalise_round_trip(&mut real_part);
	real_part
}

pub fn fft_convolve(data: &[f64], window: &[f64], _n_rows: usize, _n_cols: usize) -> Vec<f64>
{
	// fft_convolve_fast(data, window, _n_rows, _n_cols)
	fft_convolve_slow(data, window)
}

pub fn checked_fft_convolve(data: &[f64], window: &[f64], n_rows: usize, n_cols: usize) -> Result<Vec<f64>, ConvolutionError>
{
	match data.len() == window.len()
	{
		true => Ok(fft_convolve(data, window, n_rows, n_cols)),
		false => Err(ConvolutionError::DimensionsDoNotMatch)
	}
}

// #[cfg(test)]
// mod tests 
// {
// 	use super::*;

// 	use crate::filters::{Window, create_square_window};

// }