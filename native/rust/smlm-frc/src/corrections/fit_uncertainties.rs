use nv_nlopt::{Optimiser, Algorithm};

const PI: f64 = std::f64::consts::PI;

fn pdf(x: f64, mu: f64, sigma: f64) -> f64
{
	let variance = sigma * sigma;
	let var2 = 2.0 * variance;
	let x_take_mu = x - mu;
	let exp_num = x_take_mu * x_take_mu;
	let exp = (- (exp_num / var2)).exp();
	let denom = (PI * var2).sqrt();
	exp / denom
}

fn log_likelihood(data: &[f64], mu: f64, sigma: f64) -> f64
{
	let mut ll = 0.0;
	for x in data
	{
		ll += pdf(*x, mu, sigma).ln();
	}
	ll
}

#[derive(Debug)]
pub struct FitResults
{
	mu: f64,
	sigma: f64
}

impl FitResults
{
	pub fn new(mu: f64, sigma: f64) -> Self
	{
		Self{mu, sigma}
	}

	pub fn mu(&self) -> f64
	{
		self.mu
	}

	pub fn sigma(&self) -> f64
	{
		self.sigma
	}
}

fn estimation_of(theta: &[f64], _gradient: Option<&mut [f64]>, data: &(*const f64, usize)) -> f64
{
	let mu = theta[0];
	let sigma = theta[1];
	let data_array = unsafe{std::slice::from_raw_parts(data.0, data.1)};
	log_likelihood(data_array, mu, sigma)
}

pub fn estimate(data: &[f64]) -> Result<FitResults, String>
{
	let n_dim = 2;
    let alg =  Algorithm::LN_COBYLA;
    let mut optimiser = Optimiser::minimise(estimation_of, (data.as_ptr(), data.len()), alg, n_dim);
    let _r = optimiser.set_max_eval(1500);
    let _r = optimiser.set_max_time(15.0);
    let mut guess = [0.0, 1.0];
    let mut value = 0.0;
    let _r = optimiser.optimise(&mut guess, &mut value);
    let mu = guess[0];
    let sigma = guess[1];
    Ok(FitResults::new(mu, sigma))
}

#[cfg(test)]
mod tests 
{
	use super::*;
}