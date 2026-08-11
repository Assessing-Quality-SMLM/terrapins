mod box_muller;

use super::{Distribution};

use rand::Rng;

use std::f64::consts::PI;

pub fn pdf(x: f64, mu: f64, sigma: f64) -> f64
{
	//println!("N({mu},{sigma}) x: {x}");
	let sigma_sqr = sigma * sigma;
	let sigma_sqr_2 = 2.0 * sigma_sqr;
	let diff = x - mu;
	let e_num = diff * diff;
	let e = e_num / sigma_sqr_2;
	let exp = (- e).exp();
	let denom = (PI * sigma_sqr_2).sqrt();
	let value = exp / denom;
	// println!("{value}");
	value
}

#[derive(Debug)]
pub enum SampleMethod 
{
	BoxMuller,
}

#[derive(Debug)]
pub struct Gaussian
{
	mu: f64, //mean
	sigma: f64 //std //var = sigma^2
}

impl Gaussian
{
	pub fn new(mu: f64, sigma: f64) -> Self
	{
		Self{mu, sigma}
	}

	pub fn sample(&self, n: usize, method: SampleMethod) -> Vec<f64>
	{
		match method
		{
			SampleMethod::BoxMuller => self.sample_box_muller(n)
		}
	}
	
	pub fn sample_box_muller(&self, n: usize) -> Vec<f64>
	{
		box_muller::box_muller(self.mu, self.sigma, n)
	}

	pub fn sample_box_muller_with<R: Rng>(&self, n: usize, rng: R) -> Vec<f64>
	{
		box_muller::box_muller_with(self.mu, self.sigma, n, rng)
	}
}

impl Distribution for Gaussian
{
	type Dist = Self;
	fn n_dim() -> usize 
	{
	    2
	}

	fn from(theta: &[f64]) -> Self 
	{
		Self::new(theta[0], theta[1])    
	}

	fn params(&self, theta: &mut [f64]) -> ()
	{
		theta[0] = self.mu;
		theta[1] = self.sigma;
	}

	fn pdf(&self, x: f64) -> f64
	{
		pdf(x, self.mu, self.sigma)
	}
}

// #[cfg(feature="fit")]
// #[cfg(test)]
// mod tests 
// {
// 	use super::*;

// 	use rand::rngs::StdRng;
// 	use rand::SeedableRng;

// 	#[test]
// 	fn round_trip() 
// 	{
// 		let mu = 0.0;
// 		let sigma = 1.0;
// 		let dist = Gaussian::new(mu, sigma);

// 	    let rng = StdRng::seed_from_u64(42);
// 		let data = dist.sample_box_muller_with(10, rng);
// 		let expected = [-1.2368442265385122, -0.34024418320728517, -0.7502583716520492, 0.5036846336751698, -5.802815688176105, 3.433899665522036, 0.3557475454560333, -0.4945179653801638, 4.060014853320537, 0.08297195908865453];
// 		assert_eq!(data, expected);
// 		let new_dist = Gaussian::new(100.0, 2.0).fit(&data, Some((&[-100.0, -100.0], &[100.0, 100.0]))).unwrap();
// 		assert_eq!(new_dist.mu, -0.018836143882874017);
// 		assert_eq!(new_dist.sigma, 2.545282576293948);
// 	}
// }