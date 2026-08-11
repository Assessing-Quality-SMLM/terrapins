use super::CorrectionData;

const PI: f64 = std::f64::consts::PI;

// L is pixel size 
// q is 1 / L, 2/L ...
///2𝜋𝑞𝐿 is the circumference of the Fourier circle
#[allow(non_snake_case)]
fn nu_q(q: f64, L: f64, frc_num: f64) -> f64
{
    let n_pixels = 2.0 * PI * q * L; // n_pixels in fourier circle
    println!("n_pixels: {n_pixels}");
    frc_num / n_pixels
}

// L is pixel size 
// q is 1 / L, 2/L ...
// (idx L)/ L = idx
fn nu_q_idx(q_idx: usize, frc_num: f64) -> f64
{
    let n_pixels = 2.0 * PI * (q_idx as f64);
    frc_num / n_pixels
}

/// 2𝜋𝑞𝐿 is the circumference of the Fourier circle
/// this is a property of the data
pub fn nu_q_emp(n_pixels: usize, frc_num: f64) -> f64
{
    frc_num / (n_pixels as f64)
}

pub fn set_nuq(data: &mut CorrectionData, frc_num: &[f64], ring_sizes: &[usize]) -> ()
{
	for idx in 0..frc_num.len()
	{
		data.nu_mut()[idx] = nu_q_emp(ring_sizes[idx], frc_num[idx])
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn empirical() 
	{
		let frc_num = 0.5;
		let n_pixels = 10;
		assert_eq!(nu_q_emp(n_pixels, frc_num), 0.05)
	}

	#[test]
	fn idx() 
	{
		let frc_num = 0.5;
		assert_eq!(nu_q_idx(2, frc_num), 0.5 / (4.0 * PI))
	}

	#[test]
	#[allow(non_snake_case)]
	fn analytic_redundency() 
	{
		let L = 1.0;
		let q = 2.0 / L;
		let frc_num = 0.5;
		assert_eq!(nu_q(q, L, frc_num), 0.5 / (4.0 * PI));
		let L = 100.0;
		let q = 2.0 / L;
		let expected = 0.03978873577297383;
		// 0.5 / (4.0 * PI) = 0.039788735772973836; small difference
		assert_eq!(nu_q(q, L, frc_num), expected)
	}
}