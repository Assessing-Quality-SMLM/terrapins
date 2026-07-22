pub mod settings;
mod zip;

pub use self::zip::{extract_data, ZipError};

use crate::{Error, Settings};
use crate::filesystem::{FileSystem};
use crate::results::{Results, HawkmanResult};

use frc::{Resolution as FrcResolution};

use time::{UtcDateTime};

use std::fs;
use std::fs::{File};
use std::path::{Path, PathBuf};

pub const SETTINGS_FILENAME: &str = "settings.json";

pub const RESOLUTION_FILENAME: &str = "resolution";

pub const REFERENCE_RECON_DIRECTORY: &str = "recon";
pub const HAWK_RECON_DIRECTORY: &str = "hawk";
pub const RECON_IMAGE_NAME: &str = "image.tiff";
pub const RECON_DATA_FILENAME: &str = "data";

pub const HALF_SPLIT_DATA: &str = "frc_half_split";
pub const ZIP_SPLIT_DATA: &str = "frc_zip_split";
pub const DRIFT_SPLIT_DATA: &str = "frc_drift_split";
pub const RANDOM_SPLITS_DATA: &str = "frc_random_splits";
pub const FRC_A: &str = "a";
pub const FRC_B: &str = "b";
pub const FRC_IMAGE_LOCATION: &str = "image";
#[allow(dead_code)]
pub const FRC_RESOLUTION_DATA: &str = "resolution";

pub const HAWKMAN_DATA : &str = "hawkman";
#[allow(dead_code)]
pub const HAWKMAN_CONFIDENCE_MAP: &str = "confidence_map";
#[allow(dead_code)]
pub const HAWKMAN_SHARPENING_MAP: &str = "sharpening_map";
#[allow(dead_code)]
pub const HAWKMAN_STRUCTURE_MAP: &str = "structure_map";
#[allow(dead_code)]
pub const HAWKMAN_RESOLUTION_IMAGE: &str = "resolution_map.tiff";
#[allow(dead_code)]
pub const HAWKMAN_SCALE_IMAGE: &str = "resolution_scale_map.tiff";

pub const AVERAGE_OF_FRAMES_WIDEFIELD: &str = "aof_widefield.tiff";

pub const NON_LINEAR_SQUIRREL_DATA : &str = "non_linear_squirrel";
pub const SQUIRREL_DATA : &str = "squirrel";
pub const SQUIRREL_WIDEFIELD_IMAGE_NAME: &str = "widefield.tiff";
#[allow(dead_code)]
pub const SQUIRREL_ERROR_MAP : &str = "error_map.tiff";
pub const SQUIRREL_METRICS : &str = "metrics";

pub const CALIBRATION_SR_0_005 : &str = "sr_0_005";
pub const CALIBRATION_SR_0_05 : &str = "sr_0_05";
pub const CALIBRATION_SR_0_5 : &str = "sr_0_5";
pub const CALIBRATION_SR_5 : &str = "sr_5";
pub const CALIBRATION_SR_25 : &str = "sr_25";
pub const CALIBRATION_ALIGNED_FRC : &str = "frc_calibration_space";

pub const REPORT : &str = "report";


fn time_as_path(date_time: UtcDateTime) -> Result<String, String>
{
	let format = time::format_description::parse("[year]_[month]_[day]_[hour]_[minute]_[second]").map_err(|e| e.to_string())?;
	date_time.format(&format).map_err(|e| e.to_string())
}

pub fn current_time_as_path() -> Result<String, String>
{
    time_as_path(UtcDateTime::now())
}

pub fn output_directory<P: AsRef<Path>>(working_directory: P, data_name: Option<&str>) -> PathBuf
{
	match data_name
	{
		Some(d) => working_directory.as_ref().join(d),
		None => 
		{
			match current_time_as_path()
			{
				Ok(p) => working_directory.as_ref().join(p),
				Err(_) => 
				{
					working_directory.as_ref().join("data")
				}
			}
		}
	}
}

pub fn path_to_string(path: &Path) -> String
{
	format!("{}", path.display())
}

pub fn settings_filename<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(SETTINGS_FILENAME)
}

pub fn reference_recon_directory<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(REFERENCE_RECON_DIRECTORY)
}

pub fn recon_image_filename<P: AsRef<Path>>(directory: P) -> PathBuf
{
	directory.as_ref().join(RECON_IMAGE_NAME)
}

pub fn recon_data_filename<P: AsRef<Path>>(directory: P) -> PathBuf
{
	directory.as_ref().join(RECON_DATA_FILENAME)
}

pub fn recon_image_filename_in_output_directory<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	recon_image_filename(reference_recon_directory(output_directory))
}

pub fn hawk_recon_directory<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(HAWK_RECON_DIRECTORY)
}

pub fn hawk_recon_image_name<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	recon_image_filename(hawk_recon_directory(output_directory))
}

pub fn frc_half_split_directory<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(HALF_SPLIT_DATA)
}

pub fn frc_zip_split_directory<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(ZIP_SPLIT_DATA)
}

pub fn frc_drift_split_directory<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(DRIFT_SPLIT_DATA)
}

pub fn frc_random_split_directory<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(RANDOM_SPLITS_DATA)
}

pub fn frc_random_split_directory_for<P: AsRef<Path>>(output_directory: P, idx: usize) -> PathBuf
{
	frc_random_split_directory(output_directory).join(idx.to_string())
}

pub fn frc_resolution_data<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(RESOLUTION_FILENAME)
}

fn frc_image_location<P: AsRef<Path>>(output_directory: P, label: &str) -> PathBuf
{
	output_directory.as_ref().join(FRC_IMAGE_LOCATION).join(format!("{label}.tiff"))
}

pub fn frc_image_location_a<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	frc_image_location(output_directory, FRC_A)
}

pub fn frc_image_location_b<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	frc_image_location(output_directory, FRC_B)
}

pub fn hawkman_data_location<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(HAWKMAN_DATA)
}

pub fn average_of_frames_image_name<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(AVERAGE_OF_FRAMES_WIDEFIELD)
}

pub fn non_linear_squirrel_data_location<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(NON_LINEAR_SQUIRREL_DATA)
}

pub fn squirrel_data_location<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(SQUIRREL_DATA)
}

pub fn true_widefield_location<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(SQUIRREL_WIDEFIELD_IMAGE_NAME)
}

pub fn calibration_sampling_ratio_0_005<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(CALIBRATION_SR_0_005)
}

pub fn calibration_sampling_ratio_0_05<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(CALIBRATION_SR_0_05)
}

pub fn calibration_sampling_ratio_0_5<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(CALIBRATION_SR_0_5)
}

pub fn calibration_sampling_ratio_5<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(CALIBRATION_SR_5)
}

pub fn calibration_sampling_ratio_25<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(CALIBRATION_SR_25)
}

pub fn calibration_aligned_frc<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(CALIBRATION_ALIGNED_FRC)
}

pub fn report_location<P: AsRef<Path>>(output_directory: P) -> PathBuf
{
	output_directory.as_ref().join(REPORT)
}

pub fn real_path<FS: FileSystem, P: AsRef<Path>>(filesystem: FS, path: P) -> Option<P>
{
	if filesystem.exists(&path)
	{
		Some(path)
	}
	else 
	{
		None
	}
}

pub fn real_path_result<FS: FileSystem, P: AsRef<Path>>(filesystem: FS, path: P) -> Result<P, String>
{
	if filesystem.exists(&path)
	{
		Ok(path)
	}
	else 
	{
		Err(format!("{} does not exist", path.as_ref().display()))		
	}
}

// RESULTS
fn parse_frc_data<P: AsRef<Path>>(directory: P) -> Result<FrcResolution, Error>
{
	let filename = frc_resolution_data(directory);
	// println!("{}", filename.display());
	File::open(filename).and_then(FrcResolution::read_from).map_err(Error::from)
}

fn parse_frc_random_split_data<P: AsRef<Path>>(directory: P) -> Result<Vec<FrcResolution>, Error>
{
	// println!("parsing: {}", directory.as_ref().display());
	let mut results = Vec::new();
	for entry in fs::read_dir(&directory)?
	{
		let entry = entry?.path();
		if entry.is_dir()
		{
			continue;
		}
		let data = parse_frc_data(entry)?;
		results.push(data);
	}

	Ok(results)
}

fn parse_to_results<P: AsRef<Path>>(directory: P) -> Result<Results<PathBuf>, Error>
{
	let mut results = Results::default();
	let directory_path = directory.as_ref();
	// println!("data in {} {}", directory_path.display(), directory_path.exists());
	let half_split_data = frc_half_split_directory(directory_path);
	// println!("looking for: {}", half_split_data.display());
	if half_split_data.exists()
	{
		let half_split_results = parse_frc_data(half_split_data)?;
		results.add_frc_half_split_results(half_split_results);
	}
	let zip_split_data = frc_zip_split_directory(directory_path);
	if zip_split_data.exists()
	{
		let zip_split_results = parse_frc_data(zip_split_data)?;
		results.add_frc_zip_split_results(zip_split_results);
	}
	let random_data = frc_random_split_directory(directory_path);
	if random_data.exists()
	{
		let random_results = parse_frc_random_split_data(random_data)?;
		// println!("{:?}", random_results);
		// println!("parsing: {}", directory.as_ref().display());
		results.add_frc_random_split_results(random_results);
	}
	let hawkman_data = directory_path.join(HAWKMAN_DATA);
	if hawkman_data.exists()
	{
		results.add_hawkman_results(HawkmanResult::new(hawkman_data));
	}
	Ok(results)
}

#[allow(dead_code)]
pub fn read_results(data_file: &str) -> Result<(PathBuf, Results<PathBuf>, Settings), Error>
{	
	let data_file_path = Path::new(data_file).canonicalize()?;
	let output_directory = data_file_path.parent().ok_or(Error::from(ZipError::InvalidFileName(data_file.to_string())))?;
	let settings = Settings::from_disk(settings_filename(output_directory))?;
	let extracted_zip_directory = output_directory.join(data_file_path.file_stem().ok_or(Error::ZipReading(ZipError::InvalidFileName(format!("{} has not file stem", data_file_path.display()))))?);
	// println!("extracting to {}", extracted_zip_directory.display());
	let _ = zip::extract_data(data_file)?;
	parse_to_results(&extracted_zip_directory).map(|r| (extracted_zip_directory, r, settings))
						   // .map(|r| {fs::remove_dir_all(extracted_zip_directory); r})
}

pub fn create_zip<P: AsRef<Path>>(directory: P) -> Result<PathBuf, Error>
{
	zip::create(directory)
}

fn to_data_directory<P: AsRef<Path>>(directory: P) -> Result<PathBuf, Error>
{
	let mut d = directory.as_ref().to_path_buf();
	let f_name = d.file_name()
     			  .ok_or_else(|| format!("{} has no parent", d.display()))
				  .and_then(|os_str| os_str.to_str().ok_or_else(|| format!("Cannot convert {:?} to str", os_str)))
	 			  .map(|name| format!("{name}_data"))
     			  .map_err(Error::extraction)?;
    d.set_file_name(f_name);
    Ok(d.to_path_buf())
}

pub fn finalise_data<P: AsRef<Path>>(directory: P, cleanup_directory: bool) -> Result<(), Error>
{
	// println!("creating zip of: {}", directory.as_ref().display());
	let _ = create_zip(&directory)?;
	if cleanup_directory
	{
		// println!("removing dir: {}", directory.as_ref().display());
		fs::remove_dir_all(directory).map_err(Error::from)
	}
	else 
	{

		// println!("copying: {}", directory.as_ref().display());
		to_data_directory(&directory).and_then(|new_name| 
			{
				// println!("{} to {}", directory.as_ref().display(), new_name.display());
				fs::rename(directory, new_name).map_err(Error::from)
			})
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;
	
	use crate::filesystem::{FakeFileSystem};
	use crate::fs_extra::to_path;

	use time::{Date, Month, Time};


	#[test]
	fn settings_filename_test() 
	{
		assert_eq!(settings_filename("somewhere"), Path::new("somewhere").join("settings.json"))
	}

	#[test]
	fn frc_image_location_has_extension_on_it()
	{
		assert_eq!(frc_image_location("a", "thing"), PathBuf::from(to_path(&["a", "image", "thing.tiff"])))
	}

	#[test]
	fn recon_output_location() 
	{
		assert_eq!(reference_recon_directory("somewhere"), Path::new("somewhere").join("recon"))
	}

	#[test]
	fn recon_image_location() 
	{
		assert_eq!(recon_image_filename("somewhere"), Path::new("somewhere").join("image.tiff"))
	}

	#[test]
	fn recon_data_location() 
	{
		assert_eq!(recon_data_filename("somewhere"), Path::new("somewhere").join("data"))
	}

	#[test]
	fn hawk_recon_image_name_test()
	{
		assert_eq!(hawk_recon_image_name("somewhere"), Path::new("somewhere").join("hawk").join("image.tiff"))
	}

	#[test]
	fn frc_half_split_location() 
	{
		assert_eq!(frc_half_split_directory("somewhere"), Path::new("somewhere").join("frc_half_split"))
	}

	#[test]
	fn frc_zip_split_location() 
	{
		assert_eq!(frc_zip_split_directory("somewhere"), Path::new("somewhere").join("frc_zip_split"))
	}

	#[test]
	fn frc_random_split_location() 
	{
		assert_eq!(frc_random_split_directory("somewhere"), Path::new("somewhere").join("frc_random_splits"))
	}

	#[test]
	fn frc_random_split_location_for_idx() 
	{
		assert_eq!(frc_random_split_directory_for("somewhere", 1), Path::new("somewhere").join("frc_random_splits").join("1"))
	}

	#[test]
	fn hawkman_output_location() 
	{
		assert_eq!(hawkman_data_location("somewhere"), Path::new("somewhere").join("hawkman"))
	}

	#[test]
	fn non_linaer_squirrel_output_location() 
	{
		assert_eq!(non_linear_squirrel_data_location("somewhere"), Path::new("somewhere").join("non_linear_squirrel"))
	}

	#[test]
	fn squirrel_output_location() 
	{
		assert_eq!(squirrel_data_location("somewhere"), Path::new("somewhere").join("squirrel"))
	}

	#[test]
	fn test_average_of_frames_widefield_image_name()
	{
		assert_eq!(average_of_frames_image_name("somewhere"), Path::new("somewhere").join("aof_widefield.tiff"))
	}

	#[test]
	fn junk_path_returns_none()
	{
		assert_eq!(real_path(FakeFileSystem::default(), "junk"), None);
	}

	#[test]
	fn junk_path_returns_error()
	{
		assert_eq!(real_path_result(FakeFileSystem::default(), "junk").unwrap_err(), "junk does not exist");
	}

	#[test]
	fn time_to_path()
	{
		let date = Date::from_calendar_date(2025, Month::November, 04).unwrap();
		let time = Time::from_hms(12, 7, 32).unwrap();
		let date_time = UtcDateTime::new(date, time);
		let path = time_as_path(date_time).unwrap();
		assert_eq!(path, "2025_11_04_12_07_32")
	}

	#[test]
	fn output_directory_uses_name_if_supplied()
	{
		let working_directory = "some";
		let result = output_directory(working_directory, Some("thing"));
		assert_eq!(result, PathBuf::from("some").join("thing"))

	}
}