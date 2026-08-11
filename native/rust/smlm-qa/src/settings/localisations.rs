use super::
{
	Context,
	LocalisationData,
	DataSettings,
	EquipmentSettings,
	ReconSettings
};

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Localisations 
{
	data: DataSettings,
	equipment: EquipmentSettings,
	recon_settings: ReconSettings,
}

impl Localisations
{
	pub fn from_data(data: DataSettings) -> Self
	{
		Self
		{
			data,
			equipment: EquipmentSettings::default(),
			recon_settings: ReconSettings::default(),
		}
	}

	// for the io layer
	pub fn data_settings(&self) -> &DataSettings
	{
		&self.data
	}
	
	pub fn data_settings_mut(&mut self) -> &mut DataSettings
	{
		&mut self.data
	}

	pub fn with_data_settings(mut self, value: DataSettings) -> Self
	{
		self.data = value;
		self
	}

	pub fn widefield(&self) -> Option<&str>
	{
		self.data.widefield()
	}

	pub fn image_stack(&self) -> Option<&str>
	{
		self.data.image_stack()
	}

	pub fn localisation_data(&self) -> Option<&LocalisationData>
	{
		self.data.localisation_data()
	}

	pub fn hawk_localisation_data(&self) -> Option<&LocalisationData>
	{
		self.data.hawk_localisation_data()
	}

	pub fn equipment_settings(&self) -> &EquipmentSettings
	{
		&self.equipment
	}

	pub fn equipment_settings_mut(&mut self) -> &mut EquipmentSettings
	{
		&mut self.equipment
	}

	pub fn with_equipment_settings(mut self, value: EquipmentSettings) -> Self
	{
		self.equipment = value;
		self
	}

	// for the io layer
	pub fn recon_settings(&self) -> &ReconSettings
	{
		&self.recon_settings
	}

	pub fn recon_settings_mut(&mut self) -> &mut ReconSettings
	{
		&mut self.recon_settings
	}

	pub fn with_recon_settings(mut self, value: ReconSettings) -> Self
	{
		self.recon_settings = value;
		self
	}

	pub fn widefield_pixel_size_nm(&self) -> f64
	{
		self.camera_pixel_size_nm()
	}
	
	pub fn camera_pixel_size_nm(&self) -> f64
	{
		self.equipment.camera_pixel_size_nm()
	}

	pub fn set_camera_pixel_size_nm(&mut self, value: f64) -> ()
	{
		self.equipment.set_camera_pixel_size_nm(value)
	}

	pub fn instrument_psf_fwhm_nm(&self) -> f64
	{
		self.equipment.instrument_psf_fwhm_nm()
	}

	pub fn set_instrument_psf_fwhm_nm(&mut self, value: f64) -> ()
	{
		self.equipment.set_instrument_psf_fwhm_nm(value)
	}

	pub fn set_magnification(&mut self, value: f64) -> ()
	{
		self.recon_settings.set_magnification(value)
	}

	pub fn recon_pixel_size_nm(&self) -> f64
	{
		self.context().super_res_pixel_size_nm()
	}

	pub fn super_res_psf_px(&self) -> f64
	{
		self.context().super_res_psf_px()
	}

	fn context(&self) -> Context
	{
		Context::new(&self.equipment, self.recon_settings().magnification())
	}

	// pub fn to_images<FS: FileSystem, P: AsRef<Path>>(&self, fs: FS, output_directory: P) -> Images
	// {
	// 	Images::from_output_directory(fs, output_directory, self.image_stack().map(|s| s.to_string()), self.pixel_size_nm(), self.psf_px())
	// }
}

// #[cfg(test)]
// mod tests 
// {
// 	use super::*;

	
// }