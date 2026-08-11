use crate::OptimiserOptions;

use nv_nlopt::{Optimiser, Algorithm};

// sum(y - (a + bx²))
fn f(theta: &[f64], gradient: Option<&mut [f64]>, data: &Vec<(f64, f64)>) -> f64
{
	let alpha = theta[0];
	let beta = theta[1];
	match gradient
	{
		Some(g) => 
		{
			let mut da = 0.0;
			let mut db = 0.0;
			for (q, y) in data
			{
				let q_2 = q * q;
				let u = y - (alpha + (beta * q_2));
				da += u;
				db += u * q_2; 
			}
			da = -2.0 * da;
			db = 2.0 * db;
			g[0] = da;
			g[1] = db;
			println!("gradient ({:.64}, {:.64}): {:.64}, {:.64}", alpha, beta, da, db);
		},
		None => {}
	};
	let mut total_error = 0.0;
	for (q, y) in data
	{
		let diff = y - (alpha + (beta * q * q));
		// if diff.is_nan()
		// {
		// 	println!("diff {diff} = {y} + ({beta} * {q} * {q})");
		// 	total_error += std::f64::MAX;
		// 	break;
		// }
		let error = diff * diff;
		total_error += error;
	}
	println!("total error: {:.64} ({:.64},{:.64})", total_error, alpha, beta);
	total_error
}

//y = a + bx²,
#[allow(non_snake_case)]
pub fn estimate_initial_Q(x_s: &[f64], y_s: &[f64], options: &OptimiserOptions) -> Result<(f64, f64), String>
{
	let n_dim = 2;
    let data : Vec<(f64, f64)> = x_s.iter().copied().zip(y_s.iter().copied()).collect();
    // println!("{:?}", data);

    // let alg =  Algorithm::LN_COBYLA;
    // let alg = Algorithm::LD_MMA;
    // let alg = Algorithm::LD_CCSAQ;
    let alg = Algorithm::LN_NELDERMEAD;
    let mut optimiser = Optimiser::minimise(f, data, alg, n_dim);
    let _ = options.configure(&mut optimiser)?;
    let mut guess = [1.0, 1.0];
    let mut value = 0.0;
    let _r = optimiser.optimise(&mut guess, &mut value);
    let alpha = guess[0];
    let beta = guess[1];
    println!("alpha: {}, beta: {}, value: {}", alpha, beta, value);
    if value.is_infinite() || value.is_nan()
    {
    	return Err(format!("Optimisation resulted in a value of: {value}"))
    }
    else 
    {
    	Ok((guess[0], guess[1]))	
    }
}

//y = a + bx + cx²,
fn fit_quadratic(x_s: &[f64], y_s: &[f64]) -> (f64, f64, f64)
{
	let x_mean = sig_proc::stats::mean(x_s.iter().copied());
	let x_sq_mean = sig_proc::stats::mean(x_s.iter().map(|x| x * x));
	let y_mean = sig_proc::stats::mean(y_s.iter().copied());
	let mut s_xx = 0.0;
	let mut s_xy = 0.0;
	let mut s_x_x2 = 0.0;
	let mut s_x2_x2 = 0.0;
	let mut s_x2_y = 0.0;
	for idx in 0..x_s.len()
	{
		let x = x_s[idx];
		let y = y_s[idx];
		let x_x_bar = x - x_mean;
		let y_y_bar = y - y_mean;
		let x2_x2_bar = (x*x) - x_sq_mean;
		s_xx += x_x_bar * x_x_bar;
		s_xy += x_x_bar * y_y_bar;
		s_x_x2 += x_x_bar * x2_x2_bar;
		s_x2_x2 += x2_x2_bar * x2_x2_bar;
		s_x2_y += x2_x2_bar * y_y_bar;		
	}
	let b_num = (s_xy * s_x2_x2 ) - (s_x2_y * s_x_x2);
	let b_denom = (s_xx * s_x2_x2 ) - (s_x_x2 * s_x_x2);
	let b = b_num / b_denom;

	let c_num = (s_x2_y * s_xx) - (s_xy * s_x_x2);
	let c = c_num / b_denom; //shared denom

	let a = y_mean - (b * x_mean) - (c * x_sq_mean);
	(a, b, c)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn basic_quad_fit() 
	{
		let x = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0];
		let y = [1.0, 5.0, 15.0, 24.0, 37.0, 50.0];
		let (a, b, c) = fit_quadratic(&x, &y);
		assert_eq!(a, -3.500000000000112);
		assert_eq!(b, 2.875000000000049);
		assert_eq!(c, 1.017857142857139);
	}

	// need to figure out some determinism;
	// #[test]
	// fn basic_quad_solve() 
	// {
	// 	let x = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0];
	// 	let y = [1.0, 5.0, 15.0, 24.0, 37.0, 50.0];
	// 	let (a, b) = estimate_initial_Q(&x, &y).unwrap();
	// 	assert_eq!(a, 0.5932203470657478);
	// 	assert_eq!(b, 1.4114360219856272);
	// }
}