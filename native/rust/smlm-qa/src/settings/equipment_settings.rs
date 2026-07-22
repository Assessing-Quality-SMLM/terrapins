#[derive(Debug, Clone, PartialEq)]
pub struct EquipmentSettings 
{
	instrument_psf_fwhm_nm: f64,
	camera_pixel_size_nm: f64
}

impl EquipmentSettings
{
	pub fn new(instrument_psf_fwhm_nm: f64, camera_pixel_size_nm: f64) -> Self
	{
		Self
		{
			instrument_psf_fwhm_nm, 
			camera_pixel_size_nm
		}
	}

	pub fn instrument_psf_fwhm_nm(&self) -> f64
	{
		self.instrument_psf_fwhm_nm
	}

	pub fn set_instrument_psf_fwhm_nm(&mut self, value: f64) -> ()
	{
		self.instrument_psf_fwhm_nm = value;
	}

	pub fn with_instrument_psf_fwhm_nm(mut self, value: f64) -> Self
	{
		self.instrument_psf_fwhm_nm = value;
		self
	}

	pub fn camera_pixel_size_nm(&self) -> f64
	{
		self.camera_pixel_size_nm
	}

	pub fn set_camera_pixel_size_nm(&mut self, value: f64) -> ()
	{
		self.camera_pixel_size_nm = value;
	}

	pub fn with_camera_pixel_size_nm(mut self, value: f64) -> Self
	{
		self.camera_pixel_size_nm = value;
		self	
	}
}

impl Default for EquipmentSettings
{
	fn default() -> Self 
	{
		Self::new(270.0, 160.0)
	}
}