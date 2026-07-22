use rand;
use rand::Rng;
use rand::distributions::{Distribution, Uniform};

use std::f64::consts::PI;

fn sample(mu: f64, sigma: f64, u: f64, v: f64) -> (f64, f64)
{
	let factor_1 = sigma * -2.0 * u.ln();
	let factor_2 = 2.0 * PI * v;
	let z_0 = (factor_1 * factor_2.cos()) + mu;
	let z_1 = (factor_1 * factor_2.sin()) + mu;
	(z_0, z_1)
}

pub fn box_muller_with<R: Rng>(mu: f64, sigma: f64, n: usize, mut rng: R) -> Vec<f64>
{
	let dist = Uniform::new_inclusive(0.0, 1.0);
	let mut samples = Vec::with_capacity(n);
	for _ in 0..(n/2)
	{
		let u = dist.sample(&mut rng);
		let v = dist.sample(&mut rng);
		let (z_0, z_1) = sample(mu, sigma, u, v);
		samples.push(z_0);
		samples.push(z_1);
	}
	samples
}

pub fn box_muller(mu: f64, sigma: f64, n: usize) -> Vec<f64>
{
	let rng = rand::thread_rng();
	box_muller_with(mu, sigma, n, rng)
}


#[cfg(test)]
mod tests 
{
	use super::*;

	use rand::rngs::StdRng;
	use rand::SeedableRng;

	#[test]
	fn basic() 
	{
		let mu = 0.0;
		let sigma = 1.0;
	    let rng = StdRng::seed_from_u64(42);
		let data = box_muller_with(mu, sigma, 10, rng);
		let expected = [-1.2368442265385122, -0.34024418320728517, -0.7502583716520492, 0.5036846336751698, -5.802815688176105, 3.433899665522036, 0.3557475454560333, -0.4945179653801638, 4.060014853320537, 0.08297195908865453];
		assert_eq!(data.len(), 10);
		assert_eq!(data, expected);
	}
}