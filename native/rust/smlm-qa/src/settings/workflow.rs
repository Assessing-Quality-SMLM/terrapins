use super::{
	Images,
	Localisations,
	FRCData,
	LocalisationData,
	DataSettings,
	ReconSettings
};

#[derive(Debug, Clone, PartialEq)]
pub enum Workflow 
{
	Images(Images),
	Localisations(Localisations)
}

impl Workflow
{
	pub fn images(images: Images) -> Self
	{
		Self::Images(images)
	}

	pub fn localisations(localisations: Localisations) -> Self
	{
		Self::Localisations(localisations)
	}

	pub fn from_data(data_settings: DataSettings) -> Self
	{
		Self::localisations(Localisations::from_data(data_settings))
	}

	pub fn is_image_based(&self) -> bool
	{
		match self
		{
			Self::Images(_) => true,
			_ => false
		}
	}

	pub fn images_mut(&mut self) -> Option<&mut Images>
	{
		match self
		{
			Self::Images(i) => Some(i),
			_ => None
		}
	}

	pub fn localisations_mut(&mut self) -> Option<&mut Localisations>
	{
		match self
		{
			Self::Localisations(l) => Some(l),
			_ => None
		}
	}

	pub fn widefield(&self) -> Option<&str>
	{
		match self
		{
			Self::Images(images) => images.widefield(),
			Self::Localisations(localisations) => localisations.widefield()
		}
	}

	pub fn image_stack(&self) -> Option<&str>
	{
		match self
		{
			Self::Images(images) => images.image_stack(),
			Self::Localisations(localisations) => localisations.image_stack()
		}
	}

	pub fn reference_image(&self) -> Option<&str>
	{
		match self
		{
			Self::Images(images) => images.reference_image(),
			_ => None
		}
	}

	pub fn hawk_image(&self) -> Option<&str>
	{
		match self
		{
			Self::Images(images) => images.hawk_image(),
			_ => None
		}
	}

	pub fn half_split_data(&self) -> Option<&FRCData>
	{
		match self
		{
			Self::Images(images) => images.half_split_data(),
			_ => None
		}
	}

	pub fn zip_split_data(&self) -> Option<&FRCData>
	{
		match self
		{
			Self::Images(images) => images.zip_split_data(),
			_ => None
		}
	}

	pub fn drift_split_data(&self) -> Option<&FRCData>
	{
		match self
		{
			Self::Images(images) => images.drift_split_data(),
			_ => None
		}
	}

	pub fn localisation_data(&self) -> Option<&LocalisationData>
	{
		match self
		{
			Self::Localisations(localisations) => localisations.localisation_data(),
			_ => None
		}
	}

	pub fn hawk_localisation_data(&self) -> Option<&LocalisationData>
	{
		match self
		{
			Self::Localisations(localisations) => localisations.hawk_localisation_data(),
			_ => None
		}
	}

	pub fn widefield_pixel_size_nm(&self) -> f64
	{
		match self
		{
			Self::Images(images) => images.widefield_pixel_size_nm(),
			Self::Localisations(localisations) => localisations.widefield_pixel_size_nm()
		}
	}

	pub fn camera_pixel_size_nm(&self) -> f64
	{
		match self
		{
			Self::Images(images) => images.camera_pixel_size_nm(),
			Self::Localisations(localisations) => localisations.camera_pixel_size_nm(),
		}
	}

	pub fn set_camera_pixel_size_nm(&mut self, value: f64) -> ()
	{
		match self
		{
			Self::Images(images) => images.set_camera_pixel_size_nm(value),
			Self::Localisations(localisations) => localisations.set_camera_pixel_size_nm(value),
		}
	}

	pub fn instrument_psf_fwhm_nm(&self) -> f64
	{
		match self
		{
			Self::Images(images) => images.instrument_psf_fwhm_nm(),
			Self::Localisations(localisations) => localisations.instrument_psf_fwhm_nm(),
		}
	}

	pub fn set_instrument_psf_fwhm_nm(&mut self, value: f64) -> ()
	{
		match self
		{
			Self::Images(images) => images.set_instrument_psf_fwhm_nm(value),
			Self::Localisations(localisations) => localisations.set_instrument_psf_fwhm_nm(value),
		}
	}	

	pub fn super_res_pixel_size_nm(&self) -> f64
	{
		match self
		{
			Self::Images(images) => images.super_res_pixel_size_nm(),
			Self::Localisations(localisations) => localisations.recon_pixel_size_nm()
		}
	}

	pub fn set_magnification(&mut self, value: f64) -> ()
	{
		match self
		{
			Self::Images(images) => images.set_magnification(value),
			Self::Localisations(localisations) => localisations.set_magnification(value),
		}
	}

	pub fn super_res_psf_px(&self) -> f64
	{
		match self
		{
			Self::Images(images) => images.super_res_psf_px(),
			Self::Localisations(localisations) => localisations.super_res_psf_px()
		}
	}

	pub fn recon_settings(&self) -> Option<&ReconSettings>
	{
		match self
		{
			Self::Localisations(localisations) => Some(localisations.recon_settings()),
			_ => None
		}
	}

	// pub fn to_images<FS: FileSystem, P: AsRef<Path>>(&self, fs: FS, output_directory: P) -> Self
	// {
	// 	let images = match self
	// 	{
	// 		Self::Images(i) => i.clone(),
	// 		Self::Localisations(l) => l.to_images(fs, output_directory)
	// 	};
	// 	Self::Images(images)
	// }
}

impl Default for Workflow
{
	fn default() -> Self
	{
		Self::Localisations(Localisations::default())
	}
}