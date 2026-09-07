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
		self.sigma.unwrap_or(constants::MISSING)
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
		self.uncertainty.unwrap_or(constants::MISSING)
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

	use crate::{FitLocalisation, UncertainLocalisation};

	// AllocatedLocalisation derives PartialEq, and MISSING is NaN, so a built localisation with
	// an absent quantity is not equal even to itself. These check the fields instead.

	#[test]
	fn missing_everything() 
	{
		let l = Builder::new(1.0, 2.0).build();
		assert_eq!(l.frame_number(), 0);
		assert_eq!(l.x(), 1.0);
		assert_eq!(l.y(), 2.0);
		assert_eq!(l.intensity(), 0.0);
		assert!(!l.has_measured_psf_sigma(), "an unset sigma must not become a plausible number");
		assert!(!l.has_measured_uncertainty(), "an unset uncertainty must not become a plausible number");
	}

	#[test]
	fn missing_can_set_sigma() 
	{
		let l = Builder::new(1.0, 2.0).with_sigma(3.0).build();
		assert_eq!(l.psf_sigma(), 3.0);
		assert!(l.has_measured_psf_sigma());
		assert!(!l.has_measured_uncertainty());
	}

	#[test]
	fn missing_can_set_intensity() 
	{
		let l = Builder::new(1.0, 2.0).with_intensity(4.0).build();
		assert_eq!(l.intensity(), 4.0);
		assert!(!l.has_measured_psf_sigma());
		assert!(!l.has_measured_uncertainty());
	}

	#[test]
	fn missing_can_set_uncertainty() 
	{
		let l = Builder::new(1.0, 2.0).with_uncertainty(5.0).build();
		assert_eq!(l.uncertainty(), 5.0);
		assert!(l.has_measured_uncertainty());
		assert!(!l.has_measured_psf_sigma());
	}

	#[test]
	fn missing_can_set_frame_number() 
	{
		let l = Builder::new(1.0, 2.0).with_frame_number(10).build();
		assert_eq!(l.frame_number(), 10);
		assert!(!l.has_measured_psf_sigma());
		assert!(!l.has_measured_uncertainty());
	}

	#[test]
	fn a_set_value_is_still_equatable()
	{
		// Everything present, so the derived PartialEq still works as before.
		let l = Builder::new(1.0, 2.0).with_frame_number(7).with_sigma(3.0)
			.with_intensity(4.0).with_uncertainty(5.0).build();
		assert_eq!(l, AllocatedLocalisation::new(7, 1.0, 2.0, 3.0, 4.0, 5.0));
	}
}
