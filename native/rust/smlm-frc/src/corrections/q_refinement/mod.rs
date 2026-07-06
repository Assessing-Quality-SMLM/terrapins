use super::CorrectionData;
use crate::OptimiserOptions;
use crate::corrections;
use crate::corrections::hq;

use nv_nlopt::{Optimiser, Algorithm};

pub const DEFAULT_NOISE: f64 = 0.1;

#[allow(non_snake_case)]
#[derive(Debug)]
pub struct RefineQData<'a>
{
	data: &'a CorrectionData<'a>,
	Q_norm: f64,
	initial_Q: f64,
	sigma_mean: f64,
	delta_sigma: f64,
	noise : f64
}

#[allow(non_snake_case)]
impl<'a> RefineQData<'a>
{
	pub fn new(data: &'a CorrectionData, Q_norm: f64, initial_Q: f64, sigma_mean: f64, delta_sigma: f64, noise: f64) -> Self
	{
		Self
		{
			data,
			Q_norm,
			initial_Q,
			sigma_mean,
			delta_sigma,
			noise
		}
	}

	pub fn n(&self) -> usize
	{
		self.data.n()
	}

	pub fn correction_data(&self) -> &CorrectionData
	{
		&self.data
	}

	#[allow(non_snake_case)]
	pub fn Q_norm(&self) -> f64
	{
		self.Q_norm
	}

	#[allow(non_snake_case)]
	pub fn initial_Q(&self) -> f64
	{
		self.initial_Q
	}

	pub fn sigma_mean(&self) -> f64
	{
		self.sigma_mean
	}

	pub fn delta_sigma(&self) -> f64
	{
		self.delta_sigma
	}

	pub fn noise(&self) -> f64
	{
		self.noise
	}
}

#[allow(non_snake_case)]
fn q_cost(data: &RefineQData, Q: f64) -> f64
{
	let correction_data = data.correction_data();
	let noise = data.noise();
	let mut total_cost = 0.0;
	for idx in 0..data.n()
	{
		let fq_num = correction_data.fq_num()[idx];
		let exp_decay = correction_data.exp()[idx];
		let diff = corrections::diff_from_fq_num(Q, fq_num, exp_decay);
		let value = corrections::cost(diff, noise);
		total_cost += value;
	}
	println!("total cost: {total_cost} (Q: {Q})");
	total_cost
}

#[allow(non_snake_case)]
fn refine_Q_of(theta: &[f64], _gradient: Option<&mut [f64]>, data: &RefineQData) -> f64
{
	let Q = theta[0];
	q_cost(data, Q)
}

#[allow(non_snake_case)]
pub fn refine_Q(data: RefineQData, options: &OptimiserOptions) -> Result<f64, String>
{
	let n_dim = 1;

	let mut guess = [data.initial_Q()];
    let mut value = 0.0;
    // let alg =  Algorithm::LN_COBYLA;
    let alg = Algorithm::LN_NELDERMEAD;
    let mut optimiser = Optimiser::minimise(refine_Q_of, data, alg, n_dim);
    // let _ = options.configure(&mut optimiser)?;
    let _r = optimiser.optimise(&mut guess, &mut value).to_error_string();
    println!("{:?}", _r);
    let Q = guess[0];
    println!("Estimated Q: {Q}");
    Ok(Q)
}

#[allow(non_snake_case)]
fn refine_Q_and_sigmas_of(theta: &[f64], _gradient: Option<&mut [f64]>, data: &RefineQData) -> f64
{
	let Q = theta[0];
	let sigma_mean = theta[1];
	let delta_sigma = theta[2];
	qplus_cost(data, Q, sigma_mean, delta_sigma)
}


#[allow(non_snake_case)]
fn qplus_cost(data: &RefineQData, Q: f64, sigma_mean: f64, delta_sigma: f64) -> f64
{
	let correction_data = data.correction_data();
	let Q_norm = data.Q_norm();
	let noise = data.noise();
	let mut total_cost = 0.0;
	for idx in 0..data.n()
	{
		let q = correction_data.qs()[idx];
		let nu_q = correction_data.nu()[idx];
		let hq = hq::h_q(q, sigma_mean, delta_sigma);
		// println!("hq: {hq}");
		let diff =  corrections::diff_from_hq(Q, Q_norm, q, nu_q, hq);
		// println!("diff: {diff}");
		let value = corrections::cost(diff, noise);
		total_cost += value;
	}
	println!("total cost: {total_cost} (Q: {Q}, sm: {sigma_mean}, ds: {delta_sigma})");
	total_cost
}

#[allow(non_snake_case)]
pub fn refine_Q_and_sigmas(data: RefineQData, options: &OptimiserOptions) -> Result<(f64, f64, f64, f64), String>
{
	let n_dim = 3;

    let mut guess = [data.initial_Q(), data.sigma_mean(), data.delta_sigma()];
    println!("{:?}", guess);
    let mut value = 0.0;
    // let alg =  Algorithm::LN_COBYLA;
    let alg = Algorithm::LN_NELDERMEAD;
    let mut optimiser = Optimiser::minimise(refine_Q_and_sigmas_of, data, alg, n_dim);
    // let _ = options.configure(&mut optimiser)?;
	println!("here");
    let _r = optimiser.optimise(&mut guess, &mut value);
    println!("{:?}", guess);
    let Q = guess[0];
    let sigma_mean = guess[1];
    let delta_sigma = guess[2];
    println!("Estimated Q: {Q}, sigma_mean: {sigma_mean}, delta_sigma: {delta_sigma}, cost: {value}");
    Ok((Q, sigma_mean, delta_sigma, value))
}