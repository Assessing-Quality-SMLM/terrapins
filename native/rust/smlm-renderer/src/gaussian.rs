use statrs::function::erf::erf;

use std::f64::consts::PI;

// fn blur_1d(x: f64, sigma: f64) -> f64
// {
// 	let sigma_sqr_2 = 2.0 * sigma * sigma;
// 	let e = (-x / sigma_sqr_2).exp();
// 	let denom = (PI * sigma_sqr_2).sqrt();
// 	e / denom
// }

pub fn blur_2d(x: f64, y: f64, sigma: f64) -> f64
{
	// println!("x: {x}, y: {y}, sigma: {sigma}");
	let xy_sqr = (x * x) + (y * y);
	// println!("xy_sqr: {xy_sqr}");
	let sigma_sqr = sigma * sigma;
	// println!("sigma_sqr: {sigma_sqr}");
	let sigma_sqr_2 = 2.0 * sigma_sqr;
	// println!("sigma_sqr_2: {sigma_sqr_2}");
	let e = (-xy_sqr / sigma_sqr_2).exp();
	// println!("e: {e}");
	let denom = PI * sigma_sqr_2;
	// println!("denom: {denom}");
	e / denom
}

// pub fn blur_2d_naive(x: f64, y: f64, sigma_x: f64, sigma_y: f64) -> f64
// {
// 	blur_1d(x, sigma_x) * blur_1d(y, sigma_y)
// }

pub fn integral_over(x_lower: f64, x_upper: f64, y_lower: f64, y_upper: f64, sigma: f64) -> f64
{
	let denom = 2.0_f64.sqrt() * sigma;
	let x_u = erf(x_upper / denom);
	let x_l = erf(x_lower / denom);
	
	let y_u = erf(y_upper / denom);
	let y_l = erf(y_lower / denom);

	let num = (x_u - x_l) * (y_u - y_l);

	num / 4.0
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn basic_blur_2d() 
	{
		let value = blur_2d(1.0, 1.0, 1.0);
		assert_eq!(value, 0.05854983152431917);
	}

	#[test]
	fn point_blur_2d() 
	{
		let value = blur_2d(0.0, 0.0, 1.0);
		assert_eq!(value, 1.0 / (2.0 * PI));
	}

	#[test]
	fn basic_integral_over_test()
	{
		let value = integral_over(0.0, 1.0, 0.0, 1.0, 1.0 / (2.0_f64).sqrt());
		let x = (0.8427007929427149  * 0.8427007929427149) / 4.0;
		assert_eq!(value, x)
	}
}