use crate::corrections::{Filter, CorrectionData, Config};
use crate::{loess, maths, nans, plotting};

use na::DVector;

const PI: f64 = std::f64::consts::PI;

pub fn exp_decay(q: f64, hq: f64) -> f64
{
	let s = maths::sinc(PI * q);
	hq * s * s
}

// fq denominator
fn get_exp_decay(qs: &[f64], hq: &[f64]) -> Vec<f64>
{
	qs.iter().zip(hq.iter()).map(|(q, hq)| exp_decay(*q, *hq)).collect()
}

pub fn populate_numerator(data: &mut CorrectionData, q_norm: f64) -> ()
{
	for idx in 0..data.n()
	{
		data.fq_num_mut()[idx] = q_norm * data.nu()[idx];
	}
}

fn split_fq(fq_num: f64, exp_decay: f64) -> f64
{
	fq_num.abs().ln() - exp_decay.abs().ln()
}

#[allow(unused)]
fn single_fq(fq_num: f64, exp_decay: f64) -> f64
{
	(fq_num / exp_decay).abs().ln()
}

pub fn get_fq(data: &CorrectionData, config: &Config) -> Result<Vec<f64>, String>
{
	let generate_f_q  = || 
		{
			let raw_fq : Vec<f64> = data.fq_num().iter().zip(data.exp()).map(|(num, exp)| split_fq(*num, *exp)).collect();
			let q_est = data.fq_num().iter().zip(data.exp()).map(|(num, exp)| num / exp).collect::<Vec<f64>>();
			plotting::plot_data(&q_est, "Q Est");
			raw_fq
		};
	match config.smooth_exp_filter()
	{
		Filter::Loess =>
		{
			println!("Smoothing fq with Loess");
			let q_vector = DVector::<f64>::from_vec(data.qs().to_vec());
			let f_q = generate_f_q();
			println!("raw_fq: {:?}" , f_q);
			// let f_q = fq_num.iter().zip(exp).map(|(num, exp)| single_fq(*num, *exp)).collect();
		    let exp_vector = DVector::<f64>::from_vec(f_q);
		    let l = loess::Loess::new(&q_vector, &exp_vector);
		    let mut smoothed_fq = Vec::with_capacity(data.n());
		    for &q in q_vector.iter() 
		    {
		        let y = l.estimate(q, 7, true, 1);
		        smoothed_fq.push(y);
		    }
		    Ok(smoothed_fq)
		},
		Filter::Median => 
		{
			println!("Smoothing fq with Median filter");
			// filter first then transform
			// let norm = fq_num.iter().zip(exp).map(|(num, exp)| num / exp).collect(); //filter this
			// let smooothed = filter(norm)
			// let fq = smoothed.iter().map(|x| x.abs().ln()).collect();
			Err(format!("Not implemented"))
		},
		Filter::None => 
		{
			println!("No filtering applied to fq");
			let f_q = generate_f_q();
			// let f_q = fq_num.iter().zip(exp).map(|(num, exp)| single_fq(*num, *exp)).collect();
			Ok(f_q)
		}
	}
}

pub fn populate_exp_decay(data: &mut CorrectionData) -> Result<(), String>
{
	let exp = get_exp_decay(data.qs(), data.hq());
	data.set_exp(exp);
	Ok(())
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn basic_test_split_fq() 
	{
		assert_eq!(split_fq(1.0, 1.0), 0.0)
	}

	#[test]
	fn basic_test_single() 
	{
		assert_eq!(single_fq(1.0, 1.0), 0.0)
	}
}