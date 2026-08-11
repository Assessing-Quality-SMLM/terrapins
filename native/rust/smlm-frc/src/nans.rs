fn zero_nans(data: &mut [f64]) -> ()
{
	for value in data.iter_mut()
	{
		if value.is_nan()
		{
			*value = 0.0;
		}
	}
}

fn fail_on_nan(data: &[f64]) -> Result<(), String>
{
	for value in data
	{
		if value.is_nan()
		{
			return Err("NaN Found".to_string())
		}
	}
	Ok(())
}

#[derive(Debug)]
pub enum Method 
{

	None,
	Zero,
	Fail,
}

impl Method
{
	pub fn filter(&self, data: &mut [f64]) -> Result<(), String>
	{
		match self
		{
			Self::None => Ok(()),
			Self::Zero => {zero_nans(data); Ok(())},
			Self::Fail => fail_on_nan(data)
		}
	}
}

pub fn filter_nans(data: &mut [f64], method: Method) -> Result<(), String>
{
	method.filter(data)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn fail_nan() 
	{
		let data = [1.0, std::f64::NAN];
		assert_eq!(fail_on_nan(&data), Err(String::from("NaN Found")));
 	}

 	#[test]
	fn zero_nan() 
	{
		let mut data = [1.0, std::f64::NAN];
		zero_nans(&mut data);
		assert_eq!(data, [1.0, 0.0]);
 	}

 	#[test]
 	fn none_interface() 
 	{
		let mut data = [1.0, std::f64::NAN];
 		assert_eq!(filter_nans(&mut data, Method::None), Ok(()));
 	}

 	#[test]
 	fn fail_nan_interface() 
 	{
		let mut data = [1.0, std::f64::NAN];
 		assert_eq!(filter_nans(&mut data, Method::Fail), Err(String::from("NaN Found")));
 	}

 	#[test]
 	fn zero_nan_interface() 
 	{
		let mut data = [1.0, std::f64::NAN];
 		assert_eq!(filter_nans(&mut data, Method::Zero), Ok(()));
		assert_eq!(data, [1.0, 0.0]);
 	}
}