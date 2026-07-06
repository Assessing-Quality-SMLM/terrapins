use super::{FRCData};

use crate::{io, filesystem::FileSystem, settings::{Context, EquipmentSettings}};

use std::path::{Path, PathBuf};

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Images
{
	// organised in acquisition order
	widefield: Option<String>,
	image_stack: Option<String>,

	reference_image: Option<String>,
	hawk_image: Option<String>,	

	half_split: Option<FRCData>,
	zip_split: Option<FRCData>,
	drift_split: Option<FRCData>,
	// what about random splits

	equipment: EquipmentSettings,
	magnification: f64,
}

impl Images
{
	pub fn from_output_directory<FS: FileSystem, P: AsRef<Path>>(fs: FS, output_directory: P, image_stack: Option<String>, equipment: EquipmentSettings, magnification: f64) -> Self
	{
		// println!("reading from {}", output_directory.as_ref().display());
		let f = |p| io::real_path(&fs, p).map(|pb: PathBuf| format!("{}", pb.display()));
		let frc_data = |p| 
		{
			let a = io::frc_image_location_a(&p);
			let b = io::frc_image_location_b(p);
			f(a).and_then(|a| f(b).map(|b| (a, b))).map(|(a, b)| FRCData::new(&a, &b))
		};
		let p = output_directory.as_ref();
		Self
		{
			widefield: f(io::true_widefield_location(p)),
			image_stack,
			reference_image: f(io::recon_image_filename_in_output_directory(p)),
			hawk_image: f(io::hawk_recon_image_name(p)),
			half_split: frc_data(io::frc_half_split_directory(p)),
			zip_split: frc_data(io::frc_zip_split_directory(p)),
			drift_split: frc_data(io::frc_drift_split_directory(p)),
			
			equipment,
			magnification,
		}
	}

	pub fn is_empty(&self) -> bool
	{
		self.widefield.is_none() &&
		self.image_stack.is_none() &&

		self.reference_image.is_none() &&
		self.hawk_image.is_none() &&

		self.half_split.is_none() && 
		self.zip_split.is_none() &&
		self.drift_split.is_none()
	}

	pub fn widefield(&self) -> Option<&str>
	{
		self.widefield.as_ref().map(|s| s.as_str())
	}

	pub fn set_widefield(&mut self, value: &str) -> ()
	{
		self.widefield = Some(value.to_string());
	}

	pub fn with_widefield(mut self, value: &str) -> Self
	{
		self.widefield = Some(value.to_string());
		self
	}

	pub fn image_stack(&self) -> Option<&str>
	{
		self.image_stack.as_ref().map(|s| s.as_str())
	}

	pub fn set_image_stack(&mut self, value: &str) -> ()
	{
		self.image_stack = Some(value.to_string());
	}

	pub fn with_image_stack(mut self, value: &str) -> Self
	{
		self.image_stack = Some(value.to_string());
		self
	}

	pub fn reference_image(&self) -> Option<&str>
	{
		self.reference_image.as_ref().map(|s| s.as_str())
	}

	pub fn set_reference_image(&mut self, value: &str) -> ()
	{
		self.reference_image = Some(value.to_string());
	}

	pub fn with_reference_image(mut self, value: &str) -> Self
	{
		self.reference_image = Some(value.to_string());
		self
	}

	pub fn hawk_image(&self) -> Option<&str>
	{
		self.hawk_image.as_ref().map(|s| s.as_str())
	}

	pub fn set_hawk_image(&mut self, value: &str) -> ()
	{
		self.hawk_image = Some(value.to_string());
	}

	pub fn with_hawk_image(mut self, value: &str) -> Self
	{
		self.hawk_image = Some(value.to_string());
		self
	}

	pub fn half_split_data(&self) -> Option<&FRCData>
	{
		self.half_split.as_ref()
	}

	pub fn set_half_split_a_image(&mut self, value: &str) -> ()
	{
		match self.half_split.as_mut()
		{
			None => 
			{
				let mut data = FRCData::default();
				data.set_image_a(value);
				self.half_split = Some(data);
			},
			Some(frc) => frc.set_image_a(value)
		}
	}

	pub fn set_half_split_b_image(&mut self, value: &str) -> ()
	{
		match self.half_split.as_mut()
		{
			None =>
			{
				let mut data = FRCData::default();
				data.set_image_b(value);
				self.half_split = Some(data);
			}
			Some(frc) => frc.set_image_b(value)
		}
	}

	pub fn with_half_split(mut self, value: FRCData) -> Self
	{
		self.half_split = Some(value);
		self
	}

	pub fn zip_split_data(&self) -> Option<&FRCData>
	{
		self.zip_split.as_ref()
	}

	pub fn set_zip_split_a_image(&mut self, value: &str) -> ()
	{
		match self.zip_split.as_mut()
		{
			None =>
			{
				let mut data = FRCData::default();
				data.set_image_a(value);
				self.zip_split = Some(data);
			}
			Some(frc) => frc.set_image_a(value)
		}
	}

	pub fn set_zip_split_b_image(&mut self, value: &str) -> ()
	{
		match self.zip_split.as_mut()
		{
			None =>
			{
				let mut data = FRCData::default();
				data.set_image_b(value);
				self.zip_split = Some(data);
			},
			Some(frc) => frc.set_image_b(value)
		}
	}

	pub fn with_zip_split(mut self, value: FRCData) -> Self
	{
		self.zip_split = Some(value);
		self
	}

	pub fn drift_split_data(&self) -> Option<&FRCData>
	{
		self.drift_split.as_ref()
	}

	pub fn set_drift_split_a_image(&mut self, value: &str) -> ()
	{
		match self.drift_split.as_mut()
		{
			None =>
			{
				let mut data = FRCData::default();
				data.set_image_a(value);
				self.drift_split = Some(data);
			}
			Some(frc) => frc.set_image_a(value)
		}
	}

	pub fn set_drift_split_b_image(&mut self, value: &str) -> ()
	{
		match self.drift_split.as_mut()
		{
			None =>
			{
				let mut data = FRCData::default();
				data.set_image_b(value);
				self.drift_split = Some(data);
			},
			Some(frc) => frc.set_image_b(value)
		}
	}

	pub fn with_drift_split(mut self, value: FRCData) -> Self
	{
		self.drift_split = Some(value);
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

	pub fn with_wf_pixel_size_nm(mut self, value: f64) -> Self
	{
		self.set_camera_pixel_size_nm(value);
		self
	}

	pub fn instrument_psf_fwhm_nm(&self) -> f64
	{
		self.equipment.instrument_psf_fwhm_nm()
	}

	pub fn set_instrument_psf_fwhm_nm(&mut self, value: f64) -> ()
	{
		self.equipment.set_instrument_psf_fwhm_nm(value)
	}

	pub fn equipment_settings(&self) -> &EquipmentSettings
	{
		&self.equipment
	}

	pub fn magnification(&self) -> f64
	{
		self.magnification
	}

	pub fn set_magnification(&mut self, value: f64) -> ()
	{
		self.magnification = value;
	}

	pub fn with_equipment_settings(mut self, value: EquipmentSettings) -> Self
	{
		self.equipment = value;
		self
	}

	pub fn with_magnification(mut self, value: f64) -> Self
	{
		self.magnification = value;
		self
	}

	pub fn super_res_psf_px(&self) -> f64
	{
		self.context().super_res_psf_px()
	}
	
	pub fn super_res_pixel_size_nm(&self) -> f64
	{
		self.context().super_res_pixel_size_nm()
	}

	fn context(&self) -> Context
	{
		Context::new(&self.equipment, self.magnification)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	use crate::filesystem::{FakeFileSystem};

	#[test]
	fn is_empty() 
	{
		assert_eq!(Images::default().is_empty(), true);
	}

	#[test]
	fn not_empty() 
	{
		assert_eq!(Images::default().with_half_split(FRCData::new("a", "b")).is_empty(), false);
		assert_eq!(Images::default().with_zip_split(FRCData::new("a", "b")).is_empty(), false);
		assert_eq!(Images::default().with_drift_split(FRCData::new("a", "b")).is_empty(), false);
		assert_eq!(Images::default().with_image_stack("").is_empty(), false);
		assert_eq!(Images::default().with_widefield("").is_empty(), false);
	}

	#[test]
	fn set_half_split_a()
	{
		let mut images = Images::default();
		assert_eq!(images.half_split_data(), None);
		images.set_half_split_a_image("something");
		assert_eq!(images.half_split_data().unwrap().image_a(), "something");
	}

	#[test]
	fn set_half_split_b()
	{
		let mut images = Images::default();
		assert_eq!(images.half_split_data(), None);
		images.set_half_split_b_image("something");
		assert_eq!(images.half_split_data().unwrap().image_b(), "something");
	}

	#[test]
	fn set_zip_split_a()
	{
		let mut images = Images::default();
		assert_eq!(images.zip_split_data(), None);
		images.set_zip_split_a_image("something");
		assert_eq!(images.zip_split_data().unwrap().image_a(), "something");
	}

	#[test]
	fn set_zip_split_b()
	{
		let mut images = Images::default();
		assert_eq!(images.zip_split_data(), None);
		images.set_zip_split_b_image("something");
		assert_eq!(images.zip_split_data().unwrap().image_b(), "something");
	}

	#[test]
	fn set_drift_split_a()
	{
		let mut images = Images::default();
		assert_eq!(images.drift_split_data(), None);
		images.set_drift_split_a_image("something");
		assert_eq!(images.drift_split_data().unwrap().image_a(), "something");
	}

	#[test]
	fn set_drift_split_b()
	{
		let mut images = Images::default();
		assert_eq!(images.drift_split_data(), None);
		images.set_drift_split_b_image("something");
		assert_eq!(images.drift_split_data().unwrap().image_b(), "something");
	}

	#[test]
	fn can_create_images_from_output_directory()
	{
		let output_directory = ".";
		let widefield = PathBuf::from(output_directory).join("widefield.tiff");
		
		let reference = PathBuf::from(output_directory).join("recon").join("image.tiff");
		let hawk = PathBuf::from(output_directory).join("hawk").join("image.tiff");
		
		let half_split_a = PathBuf::from(output_directory).join("frc_half_split").join("image").join("a.tiff");
		let half_split_b = PathBuf::from(output_directory).join("frc_half_split").join("image").join("b.tiff");

		let zip_split_a = PathBuf::from(output_directory).join("frc_zip_split").join("image").join("a.tiff");
		let zip_split_b = PathBuf::from(output_directory).join("frc_zip_split").join("image").join("b.tiff");
		
		let files = 
		[
			widefield.clone(), 
			reference.clone(), 
			hawk.clone(), 
			half_split_a.clone(),
			half_split_b.clone(),
			zip_split_a.clone(),
			zip_split_b.clone()
		];
		let fs = FakeFileSystem::from(files.as_slice());
		
		let images = Images::from_output_directory(fs, output_directory, None, EquipmentSettings::new(270.0, 100.0), 10.0);
		assert_eq!(images.widefield().unwrap(), io::path_to_string(&widefield));
		assert_eq!(images.image_stack(), None);
		assert_eq!(images.reference_image().unwrap(), io::path_to_string(&reference));
		assert_eq!(images.hawk_image().unwrap(), io::path_to_string(&hawk));
		assert_eq!(images.half_split_data().map(|d| d.image_a()).unwrap(), io::path_to_string(&half_split_a));
		assert_eq!(images.half_split_data().map(|d| d.image_b()).unwrap(), io::path_to_string(&half_split_b));
		assert_eq!(images.zip_split_data().map(|d| d.image_a()).unwrap(), io::path_to_string(&zip_split_a));
		assert_eq!(images.zip_split_data().map(|d| d.image_b()).unwrap(), io::path_to_string(&zip_split_b));
		assert_eq!(images.widefield_pixel_size_nm(), 100.0);
		assert_eq!(images.super_res_psf_px(), 27.0);
		assert_eq!(images.super_res_pixel_size_nm(), 10.0);
	}

	#[test]
	fn if_image_not_on_disk_its_not_added()
	{
		let output_directory = ".";
		let fs = FakeFileSystem::default();
		
		let images = Images::from_output_directory(fs, output_directory, None, EquipmentSettings::new(270.0, 100.0), 10.0);
		assert_eq!(images.widefield(), None);
	}
}