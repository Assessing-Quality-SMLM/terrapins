use crate::{constants, AllocatedLocalisation};

#[derive(Debug)]
pub struct Builder 
{
	frame_number: Option<u32>,
	x: f64,
	y: f64,
	sigma: Option<f64>,
	intensity: Option<f64>,
	uncertainty: Option<f64>
}

impl Builder
{
	pub fn new(x: f64, y: f64) -> Self
	{
		Self
		{
			frame_number: None,
			x, 
			y, 
			sigma: None,
			intensity: None,
			uncertainty: None
		}
	}

	fn frame_number(&self) -> u32
	{
		self.frame_number.unwrap_or(constants::DEFAULT_FRAME_NUMBER)
	}

	pub fn with_frame_number(mut self, value: u32) -> Self
	{
		self.frame_number = Some(value);
		self
	}

	fn x(&self) -> f64
	{
		self.x
	}

	fn y(&self) -> f64
	{
		self.y
	}

	fn sigma(&self) -> f64
	{
		self.sigma.unwrap_or(constants::DEFAULT_PSF_SIGMA)
	}

	pub fn with_sigma(mut self, value: f64) -> Self
	{
		self.sigma = Some(value);
		self
	}

	fn intensity(&self) -> f64
	{
		self.intensity.unwrap_or(constants::DEFAULT_INTENSITY)
	}

	pub fn with_intensity(mut self, value: f64) -> Self
	{
		self.intensity = Some(value);
		self
	}

	fn uncertainty(&self) -> f64
	{
		self.uncertainty.unwrap_or(constants::DEFAULT_UNCERTAINTY)
	}

	pub fn with_uncertainty(mut self, value: f64) -> Self
	{
		self.uncertainty = Some(value);
		self
	}


	pub fn build(&self) -> AllocatedLocalisation
	{
		AllocatedLocalisation::new(self.frame_number(), self.x(), self.y(), self.sigma(), self.intensity(), self.uncertainty())
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn missing_everything() 
	{
		assert_eq!(Builder::new(1.0, 2.0).build(), AllocatedLocalisation::new(0, 1.0, 2.0, 20.0, 0.0, 20.0))
	}

	#[test]
	fn missing_can_set_sigma() 
	{
		assert_eq!(Builder::new(1.0, 2.0).with_sigma(3.0).build(), AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 0.0, 20.0))
	}

	#[test]
	fn missing_can_set_intensity() 
	{
		assert_eq!(Builder::new(1.0, 2.0).with_intensity(4.0).build(), AllocatedLocalisation::new(0, 1.0, 2.0, 20.0, 4.0, 20.0))
	}

	#[test]
	fn missing_can_set_uncertainty() 
	{
		assert_eq!(Builder::new(1.0, 2.0).with_uncertainty(5.0).build(), AllocatedLocalisation::new(0, 1.0, 2.0, 20.0, 0.0, 5.0))
	}

	#[test]
	fn missing_can_set_frame_number() 
	{
		assert_eq!(Builder::new(1.0, 2.0).with_frame_number(10).build(), AllocatedLocalisation::new(10, 1.0, 2.0, 20.0, 0.0, 20.0))
	}
}