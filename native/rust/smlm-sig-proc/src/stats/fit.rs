use super::{Distribution};

use nv_nlopt::{Optimiser, Algorithm};

struct Data<'a>
{
	data: &'a [f64]
}

fn of<D: Distribution>(theta: &[f64], _gradient: Option<&mut [f64]>, data: &Data) -> f64
{
	let cost = D::from(theta).ln_likelihood(data.data);
	// println!("{cost}");
	cost
}

pub fn fit<D: Distribution>(dist: D, data: &[f64], bounds: Option<(&[f64], &[f64])>) -> Result<D::Dist, String>
{
	let n_dim = D::n_dim();
	let alg = Algorithm::LN_NELDERMEAD;
    let mut optimiser = Optimiser::maximise(of::<D>, Data{data}, alg, n_dim as u32);
    optimiser.set_max_eval(10000);
    match bounds
    {
    	None => {},
    	Some((lower, upper)) => 
    	{
    		let r1 = optimiser.set_lower_bounds(lower);
    		let r2 = optimiser.set_upper_bounds(upper);
    		let _ = r1.to_error_string().and_then(|_| r2.to_error_string())?;
    	}
    }
    let mut value = 0.0;
    let mut theta = vec![0.0; n_dim];
    dist.params(&mut theta);
    let _ = optimiser.optimise(&mut theta, &mut value).to_error_string()?;
    // println!("{value}");
    Ok(D::from(&theta))
}