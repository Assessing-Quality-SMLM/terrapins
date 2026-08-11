mod fit_uncertainties;
mod fq;
mod hq;
mod q_estimation;
mod q_refinement;
mod nu;

pub use self::hq::{HqMethod};
pub use self::q_refinement::{refine_Q_and_sigmas, RefineQData};

use self::q_refinement::{refine_Q, DEFAULT_NOISE};

use crate::{plotting, OptimiserOptions};

use imp::{Image, Stats};

use std::fmt::{Display, Formatter, Error as FmtError};
use std::str::FromStr;

#[allow(non_snake_case)]
fn correction_exp_decay(Q: f64, Q_norm: f64, exp_decay: f64) -> f64
{
	(Q / Q_norm) * exp_decay
}

#[allow(non_snake_case)]
pub fn correction_q(q: f64, Q: f64, Q_norm: f64, hq: f64) -> f64
{
	let exp_decay = fq::exp_decay(q, hq);
	correction_exp_decay(Q, Q_norm, exp_decay)
}

#[allow(non_snake_case)]
pub fn get_corrections(q_s: &[f64], hqs:  &[f64], Q_norm: f64, Q: f64) -> Vec<f64>
{
	(0..q_s.len()).map(|idx| correction_q(q_s[idx], Q, Q_norm, hqs[idx]))
				  .collect()
}

#[allow(non_snake_case)]
pub fn diff_from_fq_num(Q: f64, fq_num: f64, exp_decay: f64) -> f64
{
	let p = fq_num / exp_decay;
	(p / Q) - 1.0
}

#[allow(non_snake_case)]
pub fn diff_from_hq(Q: f64, Q_norm: f64, q: f64, nu_q: f64, hq: f64) -> f64
{
	let corr_q = correction_q(q, Q, Q_norm, hq);
	diff(nu_q, corr_q)
}

pub fn diff(nu_q: f64, corr_q: f64) -> f64
{
	(nu_q / corr_q) - 1.0
}

pub fn cost(diff: f64, noise: f64) -> f64
{
	let x = (diff * diff) / (noise * noise);
	1.0 - (-x).exp()
}

pub struct ParseError
{
	value: String,
	type_name: String
}

impl Display for ParseError
{

	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), std::fmt::Error>
	{
		write!(f, "{} not recognised as {}", self.value, self.type_name)
	}
}

#[derive(Debug, Default, Clone, PartialEq)]
pub enum UncertaintyMethod 
{
	#[default]
	None, // None provided - No localisation data - use uncertainty parameters specified
	SampleAndEstimate, //sample from list of uncertainties but fill estimate sigma values from full data
	SampleAndFit, //sample from list of uncertainties but fit sigma parameters
	Specified // Load uncertainty data for other reasons but use parameters specified
}

impl UncertaintyMethod
{
	pub fn to_hq(&self) -> HqMethod
	{
		match self
		{
			Self::Specified | Self::None => HqMethod::Theory,
			Self::SampleAndEstimate | Self::SampleAndFit => HqMethod::Estimate
		}
	}
}

impl FromStr for UncertaintyMethod
{
	type Err = ParseError;
	fn from_str(value: &str) -> Result<Self, Self::Err>
	{ 
		match value
		{
			"-" => Ok(Self::None),
			"se" => Ok(Self::SampleAndEstimate),
			"sf" => Ok(Self::SampleAndFit),
			"sp" => Ok(Self::Specified),
			_ => Err(ParseError { value: value.to_string(), type_name: "UncertaintyMethod".to_string() })
		}
	}
}

impl From<&str> for UncertaintyMethod
{
	fn from(value: &str) -> Self
	{
		Self::from_str(value).unwrap_or(Self::None)
	}
}

#[derive(Debug, Default, Clone, PartialEq)]
pub enum Filter
{
	None,
	#[default]
	Loess,
	Median
}

impl FromStr for Filter
{
	type Err = ParseError;
	fn from_str(value: &str) -> Result<Self, Self::Err>
	{ 
		match value
		{
			"-" => Ok(Self::None),
			"l" => Ok(Self::Loess),
			"m" => Ok(Self::Median),
			_ => Err(ParseError { value: value.to_string(), type_name: "Filter".to_string() })
		}
	}
}

impl From<&str> for Filter
{
	fn from(value: &str) -> Self
	{
		Self::from_str(value).unwrap_or(Self::default())
	}
}

#[derive(Debug, Default, Clone, PartialEq)]
pub enum Estimation
{
	QOnly,
	#[default]
	QAndSigma
}

impl FromStr for Estimation
{
	type Err = ParseError;
	fn from_str(value: &str) -> Result<Self, Self::Err>
	{ 
		let lower = value.to_lowercase();
		match lower.as_str()
		{
			"q" => Ok(Self::QOnly),
			"q+" => Ok(Self::QAndSigma),
			_ => Err(ParseError { value: value.to_string(), type_name: "Estimation".to_string() })
		}
	}
}

impl From<&str> for Estimation
{
	fn from(value: &str) -> Self
	{
		Self::from_str(value).unwrap_or(Self::default())
	}
}

#[allow(non_snake_case)]
pub struct Config
{
	nm_per_pix : f64,
	q_min: f64,
	q_max: f64,
	q_data_min_percent: f64,
	sigma_mean: Option<f64>,
	delta_sigma: Option<f64>,
	uncertainty_method: UncertaintyMethod,
	hq_method: HqMethod,
	// sample_uncertainties: Option<usize>, // sample the uncertainties to estimate hq
	// fit_uncertainty_parameters: bool, // fit gaussian to provided uncertainties
	smooth_exp_filter : Filter, // filter for smoothing exp estimate
	initial_Q_optimiser_options: OptimiserOptions,
	use_beta_to_estimate_sigma_values: bool, // override the logic based on fit method
	Q_estimation_noise: f64,
	estimate: Estimation,  // estimate Q or Q & sigma paramerters
	direct_search: bool // if only have frc result can still search it might not be valid
}

#[allow(non_snake_case)]
impl Config
{
	pub fn new(nm_per_pix: f64, q_min: f64, q_max: f64) -> Self
	{
		Self
		{
			nm_per_pix, 
			q_min, 
			q_max,
			q_data_min_percent: 10.0,
			sigma_mean: None,
			delta_sigma: None,
			uncertainty_method: UncertaintyMethod::SampleAndEstimate,
			hq_method: HqMethod::Estimate,
			smooth_exp_filter : Filter::default(),
			initial_Q_optimiser_options: OptimiserOptions::default(),
			use_beta_to_estimate_sigma_values: false,
			Q_estimation_noise: DEFAULT_NOISE,
			estimate : Estimation::default(),
			direct_search: false
		}
	}

	pub fn nm_per_pix(&self) -> f64
	{
		self.nm_per_pix
	}

	pub fn pixel_size(&self) -> f64
	{
		self.nm_per_pix()
	}

	// pixel size of input images
	// 1 / L is pixel size in fourier space
	pub fn L(&self) -> f64
	{
		self.pixel_size()
	}

	pub fn q_min(&self) -> f64
	{
		self.q_min
	}

	pub fn q_max(&self) -> f64
	{
		self.q_max
	}

	pub fn q_data_min_percent(&self) -> f64
	{
		self.q_data_min_percent
	}

	pub fn sigma_mean(&self) ->  Option<f64>
	{
		self.sigma_mean.clone()
	}

	pub fn set_sigma_mean(&mut self, value: f64) -> ()
	{
		self.sigma_mean = Some(value);
	}

	pub fn delta_sigma(&self) ->  Option<f64>
	{
		self.delta_sigma.clone()
	}

	pub fn set_delta_sigma(&mut self, value: f64) -> ()
	{
		self.delta_sigma = Some(value);
	}

	pub fn sigma_specified(&self) -> bool
	{
		self.sigma_mean.is_some() && self.delta_sigma.is_some()
	}

	pub fn uncertainty_method(&self) -> UncertaintyMethod
	{
		self.uncertainty_method.clone()
	}

	pub fn with_uncertainty_method(mut self, method: UncertaintyMethod) -> Self
	{
		self.uncertainty_method = method;
		self
	}

	pub fn hq_method(&self) -> HqMethod
	{
		self.hq_method.clone()
	}

	pub fn with_hq_method(mut self, method: HqMethod) -> Self
	{
		self.hq_method = method;
		self
	}

	// /// sample uncertainties to estimate hq
	// pub fn sample_uncertainties(&self) -> bool
	// {
	// 	self.sample_uncertainties.is_some()
	// }

	// /// number of samples to use
	// /// can unwrap if sample uncertainties is true
	// pub fn sample_size(&self) -> Option<usize>
	// {
	// 	self.sample_uncertainties
	// }

	// pub fn fit_uncertainty_parameters(&self) -> bool
	// {
	// 	self.fit_uncertainty_parameters
	// }

	/// use loess filter to smooth the exp estimate
	pub fn smooth_exp_filter(&self) -> Filter
	{
		self.smooth_exp_filter.clone()
	}

	pub fn with_exp_filter(mut self, filter: Filter) -> Self
	{
		self.smooth_exp_filter = filter;
		self
	}

	pub fn initial_Q_optimiser_options(&self) -> OptimiserOptions
	{
		self.initial_Q_optimiser_options.clone()
	}

	pub fn Q_estimation_noise(&self) -> f64
	{
		self.Q_estimation_noise
	}

	pub fn set_Q_estimation_noise(&mut self, noise: f64) -> ()
	{
		self.Q_estimation_noise = noise;
	}

	pub fn use_beta_to_estimate_sigma_values(&self) -> bool
	{
		self.use_beta_to_estimate_sigma_values ||{
		match self.uncertainty_method
		{
			UncertaintyMethod::SampleAndFit => true,
			_ => false
		}}
	}

	pub fn set_beta_estimation(&mut self, value: bool) -> ()
	{
		self.use_beta_to_estimate_sigma_values = value;
	}

	pub fn estimate(&self) -> Estimation
	{
		self.estimate.clone()
	}

	pub fn with_estimate(mut self, estimate: Estimation) -> Self
	{
		self.estimate = estimate;
		self
	}

	pub fn direct_search(&self) -> bool
	{
		self.direct_search
	}

	pub fn with_direct_search(mut self, value: bool) -> Self
	{
		self.direct_search = value;
		self
	}
}

struct EstimateResults
{
	// Q: f64,
	alpha: f64,
	beta: f64
}

#[allow(non_snake_case)]
impl EstimateResults
{
	// pub fn new(Q: f64, params: UncertaintyParameters, alpha: f64, beta: f64) -> Self
	pub fn new(alpha: f64, beta: f64) -> Self
	{
		Self
		{
			// Q,
			alpha,
			beta
		}
	}

	pub fn initial_Q(&self) -> f64
	{
		self.alpha.exp()
	}

	pub fn alpha(&self) -> f64
	{
		self.alpha
	}

	pub fn beta(&self) -> f64
	{
		self.beta
	}
}

impl Display for EstimateResults
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError>
	{
		write!(f, " alpha: {}, beta: {}, Q: {}", self.alpha(), self.beta(), self.initial_Q())
	}
}


#[allow(non_snake_case)]
pub fn Q_norm<I: Image<Data=f64>, J: Image<Data=f64>>(image_1: I, image_2: J) -> f64
{
	(1.0 / image_1.mean()) + (1.0 / image_2.mean())
}



pub fn corrected_frc(frc_num: f64, frc_denom: f64, correction: f64) -> f64
{
	(frc_num - correction) / (frc_denom + correction)
}

pub fn get_estimation_range(q_s: &[f64], config: &Config) -> Result<(usize, usize), String>
{
	let mut lower = 0;
	let mut upper = q_s.len();
	while upper > 0 && q_s[upper - 1] > config.q_max()
	{
		upper -= 1;
	}
	while lower < q_s.len() && q_s[lower] < config.q_min()
	{
		lower += 1;
	}
	let range = upper - lower;
	let pcent = config.q_data_min_percent();
	let min_data = q_s.len() as f64 * (pcent / 100.0);
	let min_data_size = min_data.ceil() as usize;
	if range < min_data_size
	{
		let error = format!("Not enough data for Q estimation: range is {range}, {}% of {} is {}", pcent, q_s.len(), min_data_size);
		Err(error)
	}
    else
    {
    	Ok((lower, upper))
    }
}

#[allow(non_snake_case)]
fn initial_estimate(data: &mut CorrectionData, config: &Config) -> Result<EstimateResults, String>
{
	let (alpha, beta) = q_estimation::estimate_initial_Q(data.qs(), data.fq(), &config.initial_Q_optimiser_options())?;
	Ok(EstimateResults::new(alpha, beta))
}

#[allow(non_snake_case)]
fn initial_Qs() -> Vec<f64>
{
	(0..50).map(|x| (x as f64) * 0.02).collect()
}

#[allow(non_snake_case)]
fn initial_sigma_means(config: &Config) -> Vec<f64>
{
	match config.sigma_mean()
	{
		None => (0..50).map(|x| (x as f64)).collect(), // what is sensible mean uncertainty?
		Some(sm) => vec![sm]
	}
}

#[allow(non_snake_case)]
fn initial_delta_sigmas(config: &Config) -> Vec<f64>
{
	match config.delta_sigma()
	{
		None => (0..10).map(|x| (x as f64)).collect(), // 10 std is large 5 probably plenty
		Some(ds) => vec![ds]
	}
}

#[allow(non_snake_case)]
fn direct_search(data: &CorrectionData, Q_norm: f64, config: &Config) -> Result<QEstimationResult, String>
{
	println!("Searching directly");
	let Qs = initial_Qs();
	let means = initial_sigma_means(config);
	let deltas = initial_delta_sigmas(config);
	let noise = DEFAULT_NOISE;
	let starting_points = Qs.iter().map(|Q| means.iter().map(|sm| deltas.iter().map(|ds| (*Q, *sm , *ds))).flatten()).flatten();
	let mut options = OptimiserOptions::default();
	let lower_Q = 0.0;
	let upper_Q = 1.0;

	let lower_sigma_mean = 0.0;
	let upper_sigma_mean = 100.0;
	
	let lower_delta_sigma = 0.0;
	let upper_delta_sigma = 5.0;
	let _ = options.set_bounds(&[lower_Q, lower_sigma_mean, lower_delta_sigma], &[upper_Q, upper_sigma_mean, upper_delta_sigma])?;

	let mut best: (f64, f64, f64, f64) = (std::f64::MAX, std::f64::MAX, std::f64::MAX, std::f64::MAX);
	for (initial_Q, sigma_mean, delta_sigma) in starting_points
	{
		println!("Starting from Q: {} sigma_m: {} \u{0394}sigma: {}", initial_Q, sigma_mean, delta_sigma);
		let refine_q_data = RefineQData::new(data, Q_norm, initial_Q, sigma_mean, delta_sigma, noise);
		let (Q, sigma_mean, delta_sigma, cost) = refine_Q_and_sigmas(refine_q_data, &options)?;
		if cost.is_finite() && cost < best.3
		{
			best = (Q, sigma_mean, delta_sigma, cost)
		}
	}
	let (Q, sigma_mean, delta_sigma, cost) = best;
	println!("!!!BEST!!!\nQ: {} sigma_m: {} \u{0394}sigma: {} cost: {}", Q, sigma_mean, delta_sigma, cost);
	Ok(QEstimationResult::new(Q, sigma_mean, delta_sigma))
}

#[allow(non_snake_case)]
pub fn estimate_Q(data: &mut CorrectionData, frc_num: &[f64], ring_sizes: &[usize], Q_norm: f64, uncertainties: Option<&[f64]>, config: &Config) -> Result<QEstimationResult, String>
{
	if config.direct_search()
	{
		let _ = hq::get_hq(data, uncertainties, config)?;
		return direct_search(data, Q_norm, config)
	}
	nu::set_nuq(data, frc_num, ring_sizes);
	println!("nu: {:?}", data.nu());
	plotting::plot_data(&data.nu(), "NU");
	fq::populate_numerator(data, Q_norm);
	println!("fq_num: {:?}", data.fq_num());
	plotting::plot_data(&data.fq_num(), "FQ NUM");
	let _ = hq::get_hq(data, uncertainties, config)?;
	println!("hq: {:?}", data.hq());
	plotting::plot_data(&data.hq(), "HQ");
	let _ = fq::populate_exp_decay(data).map_err(|e| format!("Error estimating exp: {e}"))?;
	println!("exp_decay: {:?}", data.exp());
	plotting::plot_data(&data.exp(), "EXP");
	let f_q = fq::get_fq(data, config)?;
	data.set_fq(f_q);
	plotting::plot_data(data.fq(), "FQ");
	let estimation = initial_estimate(data, config).map_err(|e| format!("Error obtaining initial Q estimate: {e}"))?;
	println!("Initial estimation: {estimation}");
	if config.use_beta_to_estimate_sigma_values()
	{
		println!("Using beta to estimate parameters");
		match uncertainties
		{
			None => return Err("Uncertainties must be supplied to estimate parameters from beta".to_string()),
			Some(u) => 
			{
				let (sigma_mean, delta_sigma) = hq::beta_sigma_estimations(estimation.beta(), u);
				data.set_uncertainty_parameters(sigma_mean, delta_sigma);
			}
		}
	}

	let noise = config.Q_estimation_noise();
	let mut options = OptimiserOptions::default();
	let lower_Q = 0.0;
	let upper_Q = 1.0;
	match config.estimate()
	{
		Estimation::QOnly => 
		{
			println!("Estimating Q");
			let refine_q_data = RefineQData::new(data, Q_norm, estimation.initial_Q(), data.sigma_mean(), data.delta_sigma(), noise);
			let _ = options.set_bounds(&[lower_Q], &[upper_Q])?;
			let Q_estimation = refine_Q(refine_q_data, &options)?;
			let q_estimation = QEstimationResult::new(Q_estimation, data.sigma_mean(), data.delta_sigma());
			Ok(q_estimation)
		},
		Estimation::QAndSigma => 
		{
			println!("Estimating Q and parameters");
			let lower_sigma_mean = 0.0;
			let upper_sigma_mean = 100.0;
			
			let lower_delta_sigma = 0.0;
			let upper_delta_sigma = 5.0;
			let _ = options.set_bounds(&[lower_Q, lower_sigma_mean, lower_delta_sigma], &[upper_Q, upper_sigma_mean, upper_delta_sigma])?;
			let refine_q_data = RefineQData::new(data, Q_norm, estimation.initial_Q(), data.sigma_mean(), data.delta_sigma(), noise);
			let (Q_estimation, sigma_mean, delta_sigma, _cost) = refine_Q_and_sigmas(refine_q_data, &options)?;
			let q_estimation = QEstimationResult::new(Q_estimation, sigma_mean, delta_sigma);
			Ok(q_estimation)
		}
	}
}

#[derive(Debug)]
pub struct CorrectionData<'a>
{
	qs: &'a [f64],
	nu: Vec<f64>,
	hq: Vec<f64>,
	exp: Vec<f64>,
	fq_num: Vec<f64>,
	fq: Vec<f64>,
	sigma_mean: f64,
	delta_sigma: f64,
}

impl<'a> CorrectionData<'a>
{
	pub fn new(qs: &'a[f64]) -> Self
	{
		let n = qs.len();
		Self
		{
			qs,
			nu: vec![0.0; n],
			hq: vec![0.0; n],
			exp: vec![0.0; n],
			fq_num: vec![0.0; n],
			fq: vec![0.0; n],
			sigma_mean: 0.0,
			delta_sigma: 0.0
		}
	}

	pub fn n(&self) -> usize
	{
		self.qs.len()
	}

	pub fn qs(&self) -> &[f64]
	{
		&self.qs
	}

	pub fn nu(&self) -> &[f64]
	{
		&self.nu
	}

	pub fn nu_mut(&mut self) -> &mut [f64]
	{
		&mut self.nu
	}

	pub fn hq(&self) -> &[f64]
	{
		&self.hq
	}

	pub fn set_hq(&mut self, hq: Vec<f64>) -> ()
	{
		self.hq = hq;
	}

	/// H(q)*sinc(PI * q)^2;
	// fq denominator
	pub fn exp(&self) -> &[f64]
	{
		&self.exp
	}

	pub fn set_exp(&mut self, exp: Vec<f64>) -> ()
	{
		self.exp = exp;
	}

	pub fn fq_num(&self) -> &[f64]
	{
		&self.fq_num
	}

	pub fn fq_num_mut(&mut self) -> &mut [f64]
	{
		&mut self.fq_num
	}

	pub fn fq(&self) -> &[f64]
	{
		&self.fq
	}

	pub fn set_fq(&mut self, fq: Vec<f64>) -> ()
	{
		self.fq = fq;
	}

	pub fn sigma_mean(&self) -> f64
	{
		self.sigma_mean
	}

	pub fn delta_sigma(&self) -> f64
	{
		self.delta_sigma
	}

	pub fn set_uncertainty_parameters(&mut self, sigma_mean: f64, delta_sigma: f64) -> ()
	{
		self.sigma_mean = sigma_mean;
		self.delta_sigma = delta_sigma;
	}
}

#[allow(non_snake_case)]
#[derive(Debug)]
pub struct QEstimationResult
{
	Q: f64,
	sigma_mean: f64,
	delta_sigma: f64
}

#[allow(non_snake_case)]
impl QEstimationResult
{
	pub fn new(Q: f64, sigma_mean: f64, delta_sigma: f64) -> Self
	{
		Self{Q, sigma_mean, delta_sigma}
	}

	pub fn Q(&self) -> f64
	{
		self.Q
	}

	pub fn get_corrections(&self, q_s: &[f64], Q_norm: f64) -> Vec<f64>
	{
		let hqs = hq::calculate_theoretical_hq(&q_s, self.sigma_mean, self.delta_sigma);
		get_corrections(q_s, &hqs, Q_norm, self.Q())
	}
}

impl Display for QEstimationResult
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError> 
	{ 
		write!(f, "Q: {}, sigma mean: {}, delta sigma: {}", self.Q, self.sigma_mean, self.delta_sigma)
	}
}

#[cfg(test)]
mod tests 
{
	use imp::images::BorrowedImage;

use super::*;

	#[test]
	fn basic_q_norm() 
	{
		let data : [f64; 4] = [1.0, 2.0, 3.0, 4.0];
		let image_1 = BorrowedImage::new((1, 4), &data);
		let image_2 = BorrowedImage::new((1, 4), &data);
		assert_eq!(Q_norm(image_1, image_2), 0.8);
	}

	#[test]
	fn corrected_frc() 
	{
		let num = 8.0;
		let denom = 12.0;
		let correction = 3.0;
		assert_eq!(super::corrected_frc(num, denom, correction), 1.0 / 3.0);
	}

	#[test]
	fn uncertainty_method_default() 
	{
		assert_eq!(UncertaintyMethod::default(), UncertaintyMethod::None)
	}

	#[test]
	fn hq_method_default() 
	{
		assert_eq!(HqMethod::default(), HqMethod::Theory)
	}

	#[test]
	fn filter_default() 
	{
		assert_eq!(Filter::default(), Filter::Loess)
	}
}