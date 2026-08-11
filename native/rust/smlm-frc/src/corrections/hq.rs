use crate::corrections::hq;
use crate::corrections::{CorrectionData, Config, ParseError};

use std::str::FromStr;

const PI: f64 = std::f64::consts::PI;
const PI_SQR : f64 = PI * PI;

pub fn h_q(q: f64, sigma_mean: f64, delta_sigma: f64) -> f64
{
    let term =  PI * delta_sigma * q;
    let term_sqr = term * term;
    let factor = 1.0 + (8.0 * term_sqr);
    let denom = factor.sqrt();
    let num_term = PI * sigma_mean * q;
    let exp_num = 4.0 * (num_term * num_term);
    let exp_frac = exp_num / factor;
    let numerator = (- exp_frac).exp();
    numerator / denom
}

pub fn h_q_estimation(q: f64, uncertainties: &[f64]) -> f64
{
	let f = |sigma: &f64| {let t = PI * sigma * q; (-4.0 * (t * t)).exp()};
	let total : f64 = uncertainties.iter().map(f).sum();
	total / uncertainties.len() as f64
}

pub fn beta_sigma_estimations(beta: f64, uncertainties: &[f64]) -> (f64, f64)
{
    let f = |sigma: &f64| (sigma * sigma) - ( beta /  (4.0 * PI_SQR) );
    let total : f64 = uncertainties.iter().map(f).sum();
    let sigma_mean = (total / uncertainties.len() as f64).sqrt();
    let sigma_std = sig_proc::stats::standard_deviation(uncertainties.iter().copied());
    (sigma_mean, sigma_std)
}


#[derive(Debug, Default, Clone, PartialEq)]
pub enum HqMethod
{
    #[default]
    Theory, // use specified values
    Estimate // Estimate from uncertainty sample
}

impl FromStr for HqMethod
{
    type Err = ParseError;
    fn from_str(value: &str) -> Result<Self, Self::Err>
    { 
        match value
        {
            "t" => Ok(Self::Theory),
            "e" => Ok(Self::Estimate),
            _ => Err(ParseError { value: value.to_string(), type_name: "HqMethod".to_string() })
        }
    }
}

impl From<&str> for HqMethod
{
    fn from(value: &str) -> Self
    {
        Self::from_str(value).unwrap_or(Self::default())
    }
}

pub fn calculate_theoretical_hq(q_s: &[f64], sigma_mean: f64, delta_sigma: f64) -> Vec<f64>
{
    let f = |&q| h_q(q, sigma_mean, delta_sigma);
    q_s.iter().map(f).collect()
}

fn fit_histogram(_uncertainties: &[f64]) -> (f64, f64)
{
    (0.0, 0.0)
}

pub fn get_hq(data: &mut CorrectionData, uncertainties: Option<&[f64]>, config: &Config) -> Result<(), String>
{
    let sigma_mean;
    let delta_sigma;
    let h_q;
    match config.hq_method()
    {
        HqMethod::Theory => 
        {
            println!("Calculating Theoretical Hq");
            match config.sigma_specified()
            {
                false => return Err(format!("sigma parameters need to be set")),
                true => 
                {
                    sigma_mean = config.sigma_mean().unwrap();
                    delta_sigma = config.delta_sigma().unwrap();
                    h_q = calculate_theoretical_hq(data.qs(), sigma_mean, delta_sigma);
                }
            }

        },
        HqMethod::Estimate =>
        {
            println!("Calculating Empirical Hq");
            match uncertainties
            {
                None => return Err(format!("Uncertainties must be provided")),
                Some(u) => 
                {
                    let (mu, sigma) = fit_histogram(u);
                    h_q = data.qs().iter().map(|q| hq::h_q_estimation(*q, u)).collect();
                    sigma_mean = mu;
                    delta_sigma = sigma;

                }
            }
        }
    }
    data.set_hq(h_q);
    data.set_uncertainty_parameters(sigma_mean, delta_sigma);
    Ok(())
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn basic_hq() 
    {
        let q = 1.0;
        let sigma_mean = 1.0;
        let delta_sigma = 1.0;
        let result = h_q(q, sigma_mean, delta_sigma);
        assert_eq!(result, 0.06825598950079666);
    }

    #[test]
    fn basic_hq_from_uncertainties() 
    {
        let q = 1.0;
        let uncertainties = [1.0];
        let result = h_q_estimation(q, &uncertainties);
        assert_eq!(result, 7.157165835186059e-18);
    }

    #[test]
    fn basic_hq_beta_estimation() 
    {
        let beta = 1.0;
        let uncertainties = [1.0];
        let (mean, std) = beta_sigma_estimations(beta, &uncertainties);
        assert_eq!(mean, 0.9872536169036888);
        assert_eq!(std, 0.0);
    }
}