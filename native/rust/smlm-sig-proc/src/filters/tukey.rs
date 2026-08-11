use crate::filters::{Window, Error};

//w1 = 0.5 * (1 + np.cos(np.pi * (-1 + 2.0*n1/alpha/(M-1))))

const PI : f64 = std::f64::consts::PI;

#[cfg(test)]
#[allow(non_snake_case)]
fn lower_tukey_scipy(alpha: f64, N: f64, n: f64) -> f64
{
	let f = 2.0 * n / alpha / N;
	let g = -1.0 + f;
	let h = PI * g;
	0.5 * (1.0 + h.cos())
}

#[cfg(test)]
#[allow(non_snake_case)]
fn lower_tukey_matlab(r: f64, x: f64) -> f64
{
	let f = ((2.0 * PI) / r) * (x - (r/2.0));
	0.5 * (1.0 + f.cos())
}

#[allow(non_snake_case)]
fn lower_tukey(alpha: f64, N: f64, n: f64) -> f64
{
	let num = 2.0 * PI * n;
	let denom = alpha * N;
	let f = num / denom;
	0.5 * (1.0 - f.cos())
}

fn convert_to_usize(value: f64) -> Result<usize, String>
{
	let usize_max_as_float = std::usize::MAX as f64;
	if value < usize_max_as_float 
	{
		Ok(value as usize)
	}
	else 
	{
		Err(format!("Cannot represent {value} as usize"))
	}
}

#[allow(non_snake_case)]
fn tukey_edges(alpha: f64, N: usize, output: &mut [f64]) -> Result<usize, String>
{
	let adjusted_N = (N - 1) as f64; // this relates to scipy sym parameter -> sym=True remove adjustment means sym=False
	let factor = convert_to_usize((alpha * (adjusted_N)).floor())?;
	let width =  factor / 2;
	for idx in 0..(width + 1) // this +1 is part of sym adjustment
	{
		let value = lower_tukey(alpha, adjusted_N, idx as f64);
		output[idx] = value;
		//this minus 1 is for indexing
		output[(N - 1) - idx] = value;
	}	
	Ok(width)
}

#[allow(non_snake_case)]
fn tukey_middle(N: usize, width: usize, output: &mut [f64])
{
	let mid_point_end = N / 2;
	for idx in width..mid_point_end
	{
		output[idx] = 1.0;
	}
}

#[allow(non_snake_case)]
pub fn tukey(alpha: f64, N: usize, output: &mut [f64]) -> Result<(), String>
{
	let width = tukey_edges(alpha, N, output)?;
	if N % 2 != 0 // if symetrical then 1.0 will only happen on odd N
	{
		tukey_middle(N, width, output);
	}
	Ok(())
}

#[derive(Debug)]
pub struct Tukey
{
	alpha: f64,
}

fn valid_parameter_message(alpha: f64) -> String
{
	format!("alpha ({alpha}) should be greater than 0 and less than 1")
}

impl Tukey
{
	pub fn new(alpha: f64) -> Self
	{
		Self { alpha }
	}

	pub fn checked_new(alpha: f64) -> Result<Self, Error>
	{
		if alpha <= 0.0 || alpha >= 1.0
		{
			Err(Error::Parameter(valid_parameter_message(alpha)))
		}
		else 
		{
			Ok(Self::new(alpha))
		}
	}

	#[allow(non_snake_case)]
	pub fn get(&self, N: usize) -> Result<Vec<f64>, String>
	{
		let mut output = vec![1.0; N];
		tukey_edges(self.alpha, N, &mut output).map(|_| output)
	}

	#[allow(non_snake_case)]
	pub fn get_with(&self, N: usize, output: &mut [f64]) -> Result<(), String>
	{
		tukey(self.alpha, N, output)
	}
}

impl Window for Tukey
{
	type Error = Error;
	
	fn create(theta: &[f64]) -> Result<Self, Self::Error> 
	{
		theta.get(0)
			 .ok_or_else(|| Error::Parameter(format!("expected 1 parameter in {:?}", theta)))
			 .and_then(|alpha| Self::checked_new(*alpha))
	}

	fn generate(&self, n: usize) -> Result<Vec<f64>, Self::Error> 
	{ 
		self.get(n).map_err(Error::Custom)
	}

	fn generate_into(&self, n: usize, data: &mut [f64]) -> Result<(), Self::Error>
	{ 
		self.get_with(n, data).map_err(Error::Custom)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	fn data_sets() -> Vec<(usize, f64, Vec<f64>)>
	{
		vec![
		(4, 0.5, vec![0.0, 1.0, 1.0, 0.0]),
	    (4, 0.9, vec![0.0, 0.8431208189343667, 0.8431208189343667, 0.0]),
	    //(4, 1.0, vec![0.0, 0.75, 0.75, 0.0]),
	    //(5, 0.0, vec![1.0, 1.0, 1.0, 1.0, 1.0]),
	    (5, 0.8, vec![0.0, 0.6913417161825448, 1.0, 0.6913417161825448, 0.0]),
	    //(5, 1.0, vec![0.0, 0.5, 1.0, 0.5, 0.0]),
	    //(6, 0.0, vec![1.0, 1.0, 1.0, 1.0, 1.0, 1.0]),
	    //(7, 0.0, vec![1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]),
	    (6, 0.25, vec![0.0, 1.0, 1.0, 1.0, 1.0, 0.0]),
	    (7, 0.25, vec![0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0]),
	    (6, 0.5, vec![0.0, 0.9045084971874737, 1.0, 1.0, 0.9045084971874737, 0.0]),
	    (7, 0.5, vec![0.0, 0.7499999999999999, 1.0, 1.0, 1.0, 0.7499999999999999, 0.0]),
	    (6, 0.75, vec![0.0, 0.5522642316338267, 1.0, 1.0, 0.5522642316338267, 0.0]),
	    (7, 0.75, vec![0.0, 0.4131759111665348, 0.9698463103929542, 1.0, 0.9698463103929542, 0.4131759111665348, 0.0]),
	    //(6, 1.0, vec![0.0, 0.3454915028125263, 0.9045084971874737, 0.9045084971874737, 0.3454915028125263, 0.0]),
	    //(7, 1.0, vec![0.0, 0.25, 0.75, 1.0, 0.75, 0.25, 0.0])
	    ]
	}

	#[test]
	fn test_data() 
	{
		#[allow(non_snake_case)]		
		for (n, alpha, expected) in data_sets()
		{
			println!("N: {n} alpha: {alpha}");
			let window = Tukey::new(alpha).get(n).unwrap();
			assert_eq!(window, expected)
		}
	}

	#[test]
	fn wiki() 
	{
		let alpha = 0.9;
		#[allow(non_snake_case)]
		let N = 4;
		let window : Vec<f64> = (0..N).map(|n| lower_tukey(alpha, N as f64,  n as f64)).collect();
		let expected =  [0.0, 0.5868240888334652, 0.9698463103929542, 0.24999999999999994];
		assert_eq!(window, expected)
	}

	#[test]
	fn matlab() 
	{
		let alpha = 0.9;
		let n = 4;
		let dn = 1.0 / (n as f64);
		#[allow(non_snake_case)]
		let X = (0..n).map(|x| (x as f64) * dn);
		let window : Vec<f64> = X.map(|x| lower_tukey_matlab(alpha, x)).collect();
		let expected =  [0.0, 0.5868240888334652, 0.9698463103929542, 0.2500000000000001];
		assert_eq!(window, expected)
	}

	#[cfg(target_os = "macos")]
	#[test]
	fn scipy() 
	{
		let expected =  [0.0, 0.5868240888334653, 0.9698463103929542, 0.2500000000000003];
		scipy_test(&expected)
	}

	#[cfg(not(target_os = "macos"))]
	#[test]
	fn scipy() 
	{
		let expected =  [0.0, 0.5868240888334653, 0.9698463103929542, 0.25000000000000033];
		scipy_test(&expected);
	}

	fn scipy_test(expected: &[f64])
	{
		let alpha = 0.9;
		#[allow(non_snake_case)]
		let N = 4;
		let window : Vec<f64> = (0..N).map(|n| lower_tukey_scipy(alpha, N as f64,  n as f64)).collect();
		assert_eq!(window, expected)
	}
}

