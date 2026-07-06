pub use self::setup::Setup;
pub use self::context::Context;
pub use self::data_generation::Settings as DataSettings;
pub use self::equipment_settings::EquipmentSettings;
pub use self::frc_data::FRCData;
pub use self::frc_settings::Frc as FrcSettings;
pub use self::hawkman_settings::{Settings as HawkmanSettings, ThresholdSettings as HawkmanThresholdSettings};
pub use self::images::Images;
pub use self::localisation_data::{DEFAULT_HAWK_PSF_SIGMA_FILTER, LocalisationData};
pub use self::localisations::Localisations;
pub use self::recon_settings::ReconSettings;
pub use self::squirrel_settings::SquirrelSettings;
pub use self::report::Settings as ReportSettings;
pub use self::workflow::Workflow;

mod context;
mod data_generation;
mod equipment_settings;
mod frc_data;
mod frc_settings;
mod hawkman_settings;
mod images;
mod localisation_data;
mod localisations;
pub mod report;
mod recon_settings;
mod setup;
mod squirrel_settings;
mod workflow;

use crate::{io, utils, Error};
use crate::filesystem::{FileSystem, SystemFileSystem};
use crate::tools::{hawkman::Settings as HawkmanSettingsInternal, squirrel::Settings as SquirrelSettingsInternal};

use renderer::{config::{Config as RenderConfig}, NmFrame};

use std::path::{Path, PathBuf};

pub const DEFAULT_N_THREADS: u32 = 4;

#[derive(Debug, Clone, PartialEq)]
pub struct _Settings<FS>
{
	filesystem: FS,	
	output_directory: String, 
	workflow: Workflow,
	setup: Setup,
	report: ReportSettings,
	extract: bool
}

impl _Settings<SystemFileSystem>
{
	pub fn from_workflow(workflow: Workflow) -> Self
	{
		Self::new(SystemFileSystem, workflow)
	}

	pub fn from_data(data_settings: DataSettings) -> Self
	{
		Self::from_workflow(Workflow::from_data(data_settings))
	}

	// IO
	pub fn from_disk<P: AsRef<Path>>(filename: P) -> Result<Self, Error>
	{
		io::settings::read_settings_from_disk(filename)
	}
}

impl<FS: FileSystem> _Settings<FS>
{
	pub fn new(filesystem: FS, workflow: Workflow) -> Self
	{
		Self
		{
			filesystem,
			output_directory: ".".to_string(),
			workflow : workflow,
			setup: Setup::default(),
			report: ReportSettings::default(),
			extract: false
		}
	}

	fn filesystem(&self) -> &FS
	{
		&self.filesystem
	}

	pub fn output_directory(&self) -> PathBuf
	{
		PathBuf::from(&self.output_directory)
	}

	pub fn set_output_directory(&mut self, value: &str) -> ()
	{
		self.output_directory = value.to_string();
	}

	pub fn with_output_directory(mut self, value: &str) -> Self
	{
		self.set_output_directory(value);
		self
	}

	// for the io layer
	pub fn workflow(&self) -> &Workflow
	{
		&self.workflow
	}

	pub fn set_workflow(&mut self, value: Workflow) -> ()
	{
		self.workflow = value;
	}

	pub fn with_workflow(mut self, value: Workflow) -> Self
	{
		self.workflow = value;
		self
	}

	// pub fn to_images_workflow(&self) -> Self
	// where FS: Clone
	// {
	// 	let mut base = self.clone();
	// 	base.set_workflow(self.workflow.to_images(self.filesystem(), &self.output_directory()));
	// 	base
	// }

	pub fn use_image_based_workflow(&self) -> bool
	{
		self.workflow.is_image_based()
	}

	pub fn images_mut(&mut self) -> Option<&mut Images>
	{
		self.workflow.images_mut()
	}

	pub fn localisation_workflow_mut(&mut self) -> Option<&mut Localisations>
	{
		self.workflow.localisations_mut()
	}

	// for the io layer
	pub fn setup(&self) -> &Setup
	{
		&self.setup
	}

	pub fn with_setup(mut self, value: Setup) -> Self
	{
		self.setup = value;
		self
	}

	fn recon_settings(&self) -> Option<&ReconSettings>
	{
		self.workflow.recon_settings()
	}

// image properties

	pub fn camera_pixel_size_nm(&self) -> f64
	{
		self.workflow.camera_pixel_size_nm()
	}

	pub fn set_camera_pixel_size_nm(&mut self, value: f64) -> ()
	{
		self.workflow.set_camera_pixel_size_nm(value)
	}
	
	pub fn instrument_psf_fwhm_nm(&self) -> f64
	{
		self.workflow.instrument_psf_fwhm_nm()
	}
	
	pub fn set_instrument_psf_fwhm_nm(&mut self, value: f64) -> ()
	{
		self.workflow.set_instrument_psf_fwhm_nm(value)
	}
	
	pub fn super_res_pixel_size_nm(&self) -> f64
	{
		self.workflow.super_res_pixel_size_nm()
	}

	pub fn magnification(&self) -> f64
	{
		utils::magnification(self.widefield_pixel_size_nm(), self.super_res_pixel_size_nm())
	}

	pub fn set_magnification(&mut self, value: f64) -> ()
	{
		self.workflow.set_magnification(value)
	}

// FRC
	pub fn frc_settings(&self) -> &FrcSettings
	{
		self.setup.frc_settings()
	}

	pub fn frc_n_random_splits(&self) -> usize
	{
		self.frc_settings(). n_random_splits()
	}

	pub fn frc_drift_split_block_size(&self) -> usize
	{
		self.frc_settings().drift_block_size()
	}

	pub fn frc_render_config<P: AsRef<Path>>(&self, output_directory: P, label: &str, render_space: Option<&NmFrame>) -> Result<RenderConfig, Error>
	{
		self.to_render_settings(output_directory, label, render_space)
	}

	fn hawkman_settings(&self) -> &HawkmanSettings
	{
		self.setup.hawkman_settings()
	}

	pub fn n_threads(&self) -> u32
	{
		self.setup.n_threads()
	}

	pub fn set_n_threads(&mut self, n_threads: u32) -> ()
	{
		self.setup.set_n_threads(n_threads)
	}

	pub fn report_settings(&self) -> &ReportSettings
	{
		&self.report
	}

	pub fn with_report_settings(mut self, value: ReportSettings) -> Self
	{
		self.report = value;
		self
	}

	pub fn extract(&self) -> bool
	{
		self.extract
	}

	pub fn set_extract(&mut self, value: bool) -> ()
	{
		self.extract = value;
	}

	pub fn with_extract(mut self, value: bool) -> Self
	{
		self.extract = value;
		self
	}

	fn output_path(&self) -> PathBuf
	{
		Path::new(&self.output_directory()).to_path_buf()
	}

// WORKFLOW

// Shared

	pub fn image_stack(&self) -> Option<&str>
	{
		self.workflow.image_stack()
	}

	pub fn specified_widefield(&self) -> Option<&str>
	{
		self.workflow.widefield()
	}

	fn actual_specified_widefield(&self) -> Result<&str, String>
	{
		self.specified_widefield()
			.ok_or_else(|| format!("Widefield is not specified"))
	}

// Localisations
	pub fn localisation_data(&self) -> Option<&LocalisationData>
	{
		self.workflow.localisation_data()
	}

	pub fn hawk_localisation_data(&self) -> Option<&LocalisationData>
	{
		self.workflow.hawk_localisation_data()
	}

// Images
	pub fn specified_reference_image(&self) -> Option<PathBuf>
	{
		self.workflow.reference_image().map(PathBuf::from)
	}

	fn actual_specified_reference_image(&self) -> Option<PathBuf>
	{
		self.specified_reference_image().and_then(|p| io::real_path(self.filesystem(), p))
	}

	fn specified_hawked_image(&self) -> Option<PathBuf>
	{
		self.workflow.hawk_image().map(PathBuf::from)
	}

	pub fn half_split_data(&self) -> Option<&FRCData>
	{
		self.workflow.half_split_data()
	}

	pub fn zip_split_data(&self) -> Option<&FRCData>
	{
		self.workflow.zip_split_data()
	}

	pub fn drift_split_data(&self) -> Option<&FRCData>
	{
		self.workflow.drift_split_data()
	}

	pub fn reference_recon_directory(&self) -> PathBuf
	{
		io::reference_recon_directory(self.output_path())
	}

	fn reference_recon_image(&self) -> PathBuf
	{
		io::recon_image_filename(self.reference_recon_directory())
	}

	fn actual_recon_image(&self) -> Option<PathBuf>
	{
		io::real_path(self.filesystem(), self.reference_recon_image())
	}

	pub fn reference_recon_setting(&self, render_space: Option<&NmFrame>) -> Result<RenderConfig, Error>
	{
		self.create_render_settings(self.reference_recon_directory(), None, render_space)
	}

	fn widefield_pixel_size_nm(&self) -> f64
	{
		self.workflow.widefield_pixel_size_nm()
	}

	// fn recon_pixel_size_nm(&self) -> f64
	// {
	// 	self.workflow.pixel_size_nm()
	// }

// HAWK	
	pub fn hawk_recon_directory(&self) -> PathBuf
	{
		io::hawk_recon_directory(self.output_path())
	}

	// fn hawk_image(&self) -> PathBuf
	// {
	// 	io::hawk_recon_image_name(self.output_path())
	// }

	// fn actual_hawk_image(&self) -> Option<PathBuf>
	// {
	// 	io::real_path(self.filesystem(), self.hawk_image())
	// }

	pub fn hawk_recon_setting(&self, render_space: Option<&NmFrame>) -> Result<RenderConfig, Error>
	{
		self.create_render_settings(self.hawk_recon_directory(), None, render_space)
	}

// HAWKMAN
	pub fn hawkman_reference_image(&self) -> PathBuf
	{
		io::recon_image_filename(self.hawk_recon_directory())
	}	

	pub fn hawkman_test_image(&self) -> PathBuf
	{
		self.reference_recon_image()
	}	

	pub fn hawkman_specified_hawked_image(&self) -> Option<PathBuf>
	{
		self.specified_hawked_image()
	}

	pub fn set_hawkman_n_levels(&mut self, value: u32)
	{
		self.setup.set_hawkman_n_levels(value)
	}

	fn hawkman_output_location(&self) -> PathBuf
	{
		io::hawkman_data_location(&self.output_directory())
	}

	pub fn to_hawkman_settings(&self) -> HawkmanSettingsInternal
	{
		let ref_image_hawk = io::path_to_string(&self.hawkman_reference_image());
		let test_image_ho_hawk = io::path_to_string(&self.hawkman_test_image());

		let hawkman_output = io::path_to_string(&self.hawkman_output_location());
		let hawkman_settings = self.hawkman_settings();
		let psf = self.workflow.super_res_psf_px();
		HawkmanSettingsInternal::new(&ref_image_hawk, &test_image_ho_hawk).with_output_directory(&hawkman_output)
															 .with_start_level(hawkman_settings.start_level())
															 .with_n_levels(hawkman_settings.n_levels())
															 .with_psf(psf)
															 .with_fwhm(hawkman_settings.fwhm())
															 .with_skel(hawkman_settings.skel())
												   			 .with_n_threads(self.n_threads())
	}

// SQUIRREL

	// fn squirrel_super_res_image(&self) -> PathBuf
	// {
	// 	self.hawkman_hawked_image()
	// }

	pub fn set_squirrel_wf_border_size(&mut self, value: u32)
	{
		self.setup.set_border_in_wf_px(value)
	}

	pub fn set_squirrel_registration(&mut self, value: bool)
	{
		self.setup.set_squirrel_registration(value)
	}

	pub fn squirrel_reference_image(&self) -> Option<PathBuf>
	{
		self.actual_recon_image().or_else(|| self.actual_specified_reference_image())
	}

	pub fn squirrel_specified_widefield(&self) -> Result<PathBuf, String>
	{
		self.actual_specified_widefield().map(PathBuf::from)
	}

	pub fn squirrel_true_widefield_location(&self) -> PathBuf
	{
		io::true_widefield_location(self.output_directory())
	}
	
	fn squirrel_actual_true_widefield(&self) -> Result<PathBuf, String>
	{
		io::real_path_result(self.filesystem(), self.squirrel_true_widefield_location())
	}

	fn average_of_frames_widefield_image_name(&self) -> PathBuf
	{
		io::average_of_frames_image_name(self.output_directory())
	}
	
	// fn average_of_frames_widefield(&self) -> String
	// {
	// 	io::path_to_string(&self.average_of_frames_widefield_image_name())
	// }

	fn actual_average_of_frames_widefield(&self) -> Result<PathBuf, String>
	{
		io::real_path_result(self.filesystem(), self.average_of_frames_widefield_image_name())
	}

	fn non_linear_squirrel_output_locattion(&self) -> PathBuf
	{
		io::non_linear_squirrel_data_location(&self.output_directory())
	}

	fn squirrel_output_location(&self) -> PathBuf
	{
		 io::squirrel_data_location(&self.output_directory())
	}

	pub fn non_linearity_squirrel_settings(&self) -> Result<SquirrelSettingsInternal, String>
	{
		let output_location = io::path_to_string(&self.non_linear_squirrel_output_locattion());
		self.actual_average_of_frames_widefield().and_then(|p| self.generate_squirrel_settings(&io::path_to_string(&p), &output_location))
	}

	pub fn everything_else_squirrel_settings(&self) -> Result<SquirrelSettingsInternal, String>
	{
		let output_location = io::path_to_string(&self.squirrel_output_location());
		self.squirrel_actual_true_widefield().and_then(|p| self.generate_squirrel_settings(&io::path_to_string(&p), &output_location))
	}

	fn generate_squirrel_settings(&self, widefield: &str, output_location: &str) -> Result<SquirrelSettingsInternal, String>
	{
		let wf_pixel_size = self.widefield_pixel_size_nm();
    	let super_res = self.squirrel_reference_image()
    						.ok_or_else(|| format!("Neither HAWK nor reference image exists"))
    						.map(|p| io::path_to_string(&p))?;
    	let squirrel_settings = self.setup().squirrel_settings();
    	Ok(
    		SquirrelSettingsInternal::new(widefield, &super_res, wf_pixel_size, self.instrument_psf_fwhm_nm())
    									.with_squirrel_settings(squirrel_settings.clone())
    									.with_output_directory(output_location)
    									.with_n_threads(self.n_threads())
		)
	}

// RENDERING
	fn to_render_settings<P: AsRef<Path>>(&self, output_directory: P, image_name: &str, render_soace: Option<&NmFrame>) -> Result<RenderConfig, Error>
	{
		self.create_render_settings(output_directory, Some(image_name), render_soace)
	}

	fn create_render_settings<P: AsRef<Path>>(&self, output_directory: P, label: Option<&str>, render_space: Option<&NmFrame>) -> Result<RenderConfig, Error>
	{
		let od = output_directory.as_ref();
		let recon_settings = self.recon_settings().ok_or(Error::rendering("Recon settings not specified".to_string()))?;
		
		let image_name = label.map(|l| od.join(format!("{l}.tiff"))).unwrap_or(io::recon_image_filename(od));
		let image_filename = io::path_to_string(&image_name);

		let data_name = label.map(|l| od.join(format!("{l}_data"))).unwrap_or(io::recon_data_filename(od));
		let data_filename = io::path_to_string(&data_name);
		let config = RenderConfig::default().with_magnification_factor(recon_settings.magnification())
											.with_camera_pixel_size(self.camera_pixel_size_nm())
											.with_zoom_level(1)
											.with_image_filename(Some(image_filename))
											.with_data_filename(Some(data_filename))
											.with_sigma_scale(recon_settings.sigma_scale())		   
											.with_write_as_f32(true)
											.with_n_threads(self.n_threads() as usize);
		match Self::get_data_frame(recon_settings, render_space)?
		{
			None => Ok(config),
			Some(f) => Ok(config.with_global_frame_nm(f.clone()))
		}
	}

	fn get_data_frame(recon_settings: &ReconSettings, render_space: Option<&NmFrame>) -> Result<Option<NmFrame>, Error>
	{
		match recon_settings.global_frame()
		{
			Some(f) => renderer::io::parse_global_frame_nm(f).map_err(Error::parse).map(Some),
			None => Ok(render_space.map(|frame| frame.clone()))
		}
	}
}

impl<FS: FileSystem + Default> Default for _Settings<FS>
{
	fn default() -> Self 
	{
	    Self::new(FS::default(), Workflow::default())
	}
}

pub type Settings = _Settings<SystemFileSystem>;

#[cfg(test)]
mod tests 
{
	use super::*;	

	use crate::{filesystem::FakeFileSystem, fs_extra::to_path, io};

	#[test]
	fn camera_pixel_size_set_on_rendering()
	{
		let mut settings = Settings::default();
		settings.set_camera_pixel_size_nm(200.0);
		let render_settings = settings.create_render_settings("here", None, None).unwrap();
		let config = RenderConfig::default().with_camera_pixel_size(200.0);
		assert_eq!(render_settings.camera_pixel_size_nm(), config.camera_pixel_size_nm())
	}

	#[test]
	fn hawkman_test_image_path()
	{
		let settings = Settings::default();
		let expected = to_path(&[".", "recon", "image.tiff"]);
		assert_eq!(settings.hawkman_test_image(), expected)
	}

	#[test]
	fn hawkman_reference_path()
	{
		let settings = Settings::default();
		let expected = to_path(&[".", "hawk", "image.tiff"]);
		assert_eq!(settings.hawkman_reference_image(), expected)
	}

	#[test]
	fn hawkman_output_location()
	{
		let settings = Settings::default();
		let expected = to_path(&[".", "hawkman"]);
		assert_eq!(settings.hawkman_output_location(), expected)
	}

	#[test]
	fn squirrel_specified_widefield_set()
	{
		let workflow = Workflow::images(Images::default().with_widefield("wf"));
		let settings = Settings::default().with_workflow(workflow);
		assert_eq!(settings.squirrel_specified_widefield().unwrap(), Path::new("wf"))	
	}

	// THIS IS DISK LINKED NOW SO CANT TEST WO FS ABSTRACTION
	#[test]
	fn squirrel_reference_image_is_recon()
	{
		let recon_image = PathBuf::from(".").join("recon").join("image.tiff");
		let filesystem = FakeFileSystem::new(vec![recon_image.clone()]);

		let settings = _Settings::new(filesystem, Workflow::default());
		let expected = to_path(&[".", "recon", "image.tiff"]);
		assert_eq!(settings.squirrel_reference_image().unwrap(), expected)
	}

	#[test]
	fn squirrel_output_location_is_sensible()
	{
		let settings = Settings::default();
		let expected = to_path(&[".", "squirrel"]);
		assert_eq!(settings.squirrel_output_location(), expected)
	}

	#[test]
	fn squirrel_widefield_is_output_location()
	{
		let wf_path = PathBuf::from(".").join("widefield.tiff");
		let filesystem = FakeFileSystem::default();
		let settings = _Settings::new(filesystem, Workflow::default());
		assert_eq!(settings.squirrel_true_widefield_location(), wf_path)
	}

	#[test]
	fn squirrel_actual_true_widefield_tests_is_oj_disk()
	{
		let wf_path = PathBuf::from(".").join("widefield.tiff");
		let filesystem = FakeFileSystem::new(vec![wf_path.clone()]);
		let settings = _Settings::new(filesystem, Workflow::default());
		assert_eq!(settings.squirrel_true_widefield_location(), wf_path)
	}

	#[test]
	fn squirrel_runs_with_recon_image() 
	{
		let od = Path::new(".").join("data");
		let wf_path = od.join("widefield.tiff");
		let recon_image = od.join("recon").join("image.tiff");
		let filesystem = FakeFileSystem::new(vec![wf_path.clone(), recon_image.clone()]);
		
		let data = DataSettings::default().with_widefield("some").with_hawk_localisation_data(LocalisationData::from_filepath("thing"));
		let workflow = Workflow::from_data(data);

		let settings = _Settings::new(filesystem, workflow).with_output_directory(&io::path_to_string(&od));
		let sq_settings = settings.everything_else_squirrel_settings().unwrap();
		
		let expected_wf = io::path_to_string(&io::true_widefield_location(&od));
		let expected_sr = io::path_to_string(&recon_image);
		let expected_output_directory = io::path_to_string(&od.join("squirrel"));
		let expected = SquirrelSettingsInternal::new(&expected_wf, &expected_sr, settings.camera_pixel_size_nm(), settings.instrument_psf_fwhm_nm())
												.with_output_directory(&expected_output_directory)
												.with_n_threads(settings.n_threads());
		assert_eq!(sq_settings, expected);
	}

	#[test]
	fn image_stack_not_set_no_non_linear_squirrel_settings() 
	{
		let data = DataSettings::default();
		assert_eq!(data.image_stack(), None);
		let settings = Settings::from_data(data);
		assert_eq!(settings.image_stack(), None);
		let error = format!("{} does not exist", io::path_to_string(&to_path(&[".", "aof_widefield.tiff"])));
		assert_eq!(settings.non_linearity_squirrel_settings().unwrap_err(), error);

		let data = DataSettings::default().with_image_stack("something");
		assert_eq!(data.image_stack(), Some("something"));

		let workflow = Workflow::from_data(data);

		let output_directory = PathBuf::from(".");
		let wf_path = output_directory.join("widefield.tiff");
		let recon_image = output_directory.join("recon").join("image.tiff");
		let sof = output_directory.join("aof_widefield.tiff");
		let filesystem = FakeFileSystem::new(vec![wf_path.clone(), recon_image.clone(), sof.clone()]);
				
		let settings = _Settings::new(filesystem, workflow);
		assert_eq!(settings.image_stack(), Some("something"));

		let sq_settings = settings.non_linearity_squirrel_settings().unwrap();
		let expected_widefield = io::path_to_string(&io::average_of_frames_image_name(&output_directory));
		let expected_sr = io::path_to_string(&recon_image);
		let squirrel_output_directory = output_directory.join("non_linear_squirrel");
		let expected_output_directory = io::path_to_string(&squirrel_output_directory);
		let expected = SquirrelSettingsInternal::new(&expected_widefield, &expected_sr, settings.camera_pixel_size_nm(), settings.instrument_psf_fwhm_nm())
												.with_output_directory(&expected_output_directory)
												.with_n_threads(settings.n_threads());
		assert_eq!(sq_settings, expected);	
	}	

	#[test]
	fn use_localisation_flow_by_default()
	{
		let data_settings = DataSettings::default();
		let settings = Settings::from_data(data_settings);
		assert_eq!(settings.use_image_based_workflow(), false)
	}

	#[test]
	fn can_set_n_threads()
	{
		let mut settings = Settings::default();
		assert_eq!(settings.n_threads(), 4);
		settings.set_n_threads(10);
		assert_eq!(settings.n_threads(), 10);
	}
}