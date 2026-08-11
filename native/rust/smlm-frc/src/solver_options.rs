use nv_nlopt::{Optimiser, ObjectiveFunction};

#[derive(Debug, Clone)]
struct Bounds
{
	lower: Vec<f64>,
	upper: Vec<f64>,
}

impl Bounds
{
	pub fn new(lower: Vec<f64>, upper: Vec<f64>) -> Self
	{
		Self{lower, upper}
	}

	pub fn lower(&self) -> &[f64]
	{
		&self.lower
	}

	pub fn upper(&self) -> &[f64]
	{
		&self.upper
	}
}

#[derive(Debug, Clone)]
pub struct Options 
{
	max_iterations: i32,
	max_seconds: Option<f64>,
	bounds: Option<Bounds>,
}

impl Options
{
	pub fn max_iterations(&self) -> i32
	{
		self.max_iterations
	}

	pub fn max_seconds(&self) -> Option<f64>
	{
		self.max_seconds
	}

	pub fn lower_bounds(&self) -> Option<&[f64]>
	{
		self.bounds.as_ref().map(Bounds::lower)
	}

	pub fn upper_bounds(&self) -> Option<&[f64]>
	{
		self.bounds.as_ref().map(Bounds::upper)
	}

	pub fn set_bounds(&mut self, lower: &[f64], upper: &[f64]) -> Result<(), String>
	{
		if lower.len() != upper.len()
		{
			Err(format!("Bound lengths do not match"))
		}
		else
		{
			let _ = self.bounds.replace(Bounds::new(lower.to_vec(), upper.to_vec()));
			Ok(())
		}
	}

	pub fn configure<T, OF: ObjectiveFunction<T>>(&self, optimiser: &mut Optimiser<T, OF>) -> Result<(), String>
	{
		let _r = match self.lower_bounds()
	    {
	    	None => Ok(()),
	    	Some(bounds) => optimiser.set_lower_bounds(bounds).to_error_string()
	    }?;
	    let _r = match self.upper_bounds()
	    {
	    	None => Ok(()),
	    	Some(bounds) => optimiser.set_upper_bounds(bounds).to_error_string(),
	    }?;
	    let _r = optimiser.set_max_eval(self.max_iterations());
	    let _r = match self.max_seconds()
	    {
	    	None => Ok(()),
	    	Some(max) => optimiser.set_max_time(max).to_error_string()
	    };
	    Ok(())
	}
}

impl Default for Options
{
	fn default() -> Self 
	{
		Self
		{
			max_iterations: 1500,
			max_seconds: Some(15.0),
			bounds: None
		}    
	}
}

#[cfg(test)]
mod tests {
	use super::*;

	#[test]
	fn set_bounds_replaces_bounds() 
	{
		let mut options = Options::default();
		let _ = options.set_bounds(&[1.0], &[2.0]).unwrap();
		assert_eq!(options.lower_bounds().unwrap(), &[1.0]);
		assert_eq!(options.upper_bounds().unwrap(), &[2.0]);

		let _ = options.set_bounds(&[10.0], &[20.0]).unwrap();
		assert_eq!(options.lower_bounds().unwrap(), &[10.0]);
		assert_eq!(options.upper_bounds().unwrap(), &[20.0]);
	}
}