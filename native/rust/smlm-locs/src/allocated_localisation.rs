use crate::{Localisation, FitLocalisation, UncertainLocalisation};

#[derive(Debug, Clone, Copy, PartialEq)]
pub struct AllocatedLocalisation
{
	frame_number: u32,
	x: f64,
	y: f64,
	sigma: f64,
	intensity: f64,
	uncertainty: f64
}

impl AllocatedLocalisation
{
	pub fn new(frame_number: u32, x: f64, y: f64, sigma: f64, intensity: f64, uncertainty: f64) -> Self
	{
		Self{frame_number, x, y, sigma, intensity, uncertainty}
	}    
}

impl AllocatedLocalisation
{
	pub fn frame_number(&self) -> u32
	{
		self.frame_number
	}

	pub fn x(&self) -> f64
	{
		self.x
	}

	pub fn y(&self) -> f64
	{
		self.y
	}	

	pub fn sigma(&self) -> f64
	{
		self.sigma
	}

	pub fn intensity(&self) -> f64
	{
		self.intensity
	}

	pub fn uncertainty(&self) -> f64
	{
		self.uncertainty
	}
}

impl Localisation for AllocatedLocalisation
{
	fn x(&self) -> f64
	{
		self.x
	}

	fn y(&self) -> f64
	{
		self.y
	}
}

impl FitLocalisation for AllocatedLocalisation
{
	fn psf_sigma(&self) -> f64
	{
		self.sigma()
	}
}

impl UncertainLocalisation for AllocatedLocalisation
{
	fn uncertainty(&self) -> f64
	{
		self.uncertainty
	}
}