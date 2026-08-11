use super::ParseMethod;

#[derive(Debug, Default)]
pub struct Settings 
{
	parse_method: ParseMethod,
	psf_sigma_filter: Option<(f64,f64)>,
	uncertainty_filter: Option<(f64,f64)>
}

impl Settings 
{
	pub fn new(parse_method: ParseMethod) -> Self
	{
		Self{parse_method, psf_sigma_filter: None, uncertainty_filter: None}
	}

	pub fn parse_method(&self) -> &ParseMethod
	{
		&self.parse_method
	}

	pub fn psf_sigma_filter(&self) -> Option<(f64, f64)>
	{
		self.psf_sigma_filter.clone()
	}

	pub fn with_psf_sigma_filter(mut self, min: f64, max: f64) -> Self
	{
		self.psf_sigma_filter = Some((min, max));
		self
	}

	pub fn uncertainty_filter(&self) -> Option<(f64, f64)>
	{
		self.uncertainty_filter.clone()
	}

	pub fn with_uncertainty_filter(mut self, min: f64, max: f64) -> Self
	{
		self.uncertainty_filter = Some((min, max));
		self
	}
}

