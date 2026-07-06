const DEFAULT_LOWER: f64 = 1.0 / 7.0;
const DEFAULT_UPPER: f64 = 6.0 / 7.0;

const DEFAULT_LOWER_MAGNIFICATION: f64 = 0.2;
const DEFAULT_UPPER_MAGNIFICATION: f64 = 0.4;

// 6/7 vs 1/7 ratio of 
#[derive(Debug, Clone, PartialEq)]
pub struct Settings
{
	lower: f64,
	upper: f64
}

impl Settings
{
	pub fn new(lower: f64, upper: f64) -> Self
	{
		Self{lower, upper}
	}

	pub fn sampling() -> Self
	{
		Self::new(DEFAULT_LOWER, DEFAULT_UPPER)
	}

	pub fn magnification() -> Self
	{
		Self::new(DEFAULT_LOWER_MAGNIFICATION, DEFAULT_UPPER_MAGNIFICATION)
	}

	pub fn lower(&self) -> f64
	{
		self.lower
	}

	pub fn upper(&self) -> f64
	{
		self.upper
	}
}