#[derive(Debug, Clone, PartialEq)]
pub struct ReconSettings 
{
	sigma_scale: f64,
    magnification: f64,
    global_position: Option<String>
}

impl ReconSettings
{
	pub fn new(sigma_scale: f64, magnification: f64) -> Self
	{
		Self
		{
			sigma_scale, 
			magnification, 
			global_position: None
		}
	}

	pub fn sigma_scale(&self) -> f64
	{
		self.sigma_scale
	}

	pub fn magnification(&self) -> f64
	{
		self.magnification
	}

	pub fn set_magnification(&mut self, value: f64) -> ()
	{
		self.magnification = value
	}

	pub fn global_frame(&self) -> Option<&str>
	{
		self.global_position.as_ref().map(|s| s.as_str())
	}

	pub fn with_global_frame(mut self, value: &str) -> Self
	{
		self.global_position = Some(value.to_string());
		self
	}
}

impl Default for ReconSettings
{
	fn default() -> Self 
	{
		Self::new(3.0, 10.0)
	}
}
