use crate::{tools, settings::{EquipmentSettings}, utils};

#[derive(Debug)]
pub struct Context<'a>
{
	equipment: &'a EquipmentSettings,
	magnification: f64
}

impl<'a> Context<'a>
{
	pub fn new(equipment: &'a EquipmentSettings, magnification: f64) -> Self
	{
		Self{equipment, magnification}
	}

	fn camera_pixel_size_nm(&self) -> f64
	{
		self.equipment.camera_pixel_size_nm()
	}

	pub fn widefield_pixel_size_nm(&self) -> f64
	{
		self.equipment.camera_pixel_size_nm()
	}	

	pub fn super_res_psf_px(&self) -> f64
	{
		let magnification = self.magnification as f64;
		let equipment_settings = self.equipment;
		tools::psf_in_sr_pixels(equipment_settings.instrument_psf_fwhm_nm(), equipment_settings.camera_pixel_size_nm(), magnification)
	}

	pub fn super_res_pixel_size_nm(&self) -> f64
	{
		utils::sr_pixel_size(self.camera_pixel_size_nm(), self.magnification)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn psf_px_test() 
	{
		let equipment = EquipmentSettings::new(270.0, 100.0);
		let magnification = 5.0;
		assert_eq!(Context::new(&equipment, magnification).super_res_psf_px(), 13.5);
	}
}