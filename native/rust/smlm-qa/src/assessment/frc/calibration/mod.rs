mod sampling_ratio_0_005;
mod sampling_ratio_0_05;
mod sampling_ratio_0_5;
mod sampling_ratio_5;
mod sampling_ratio_25;

use crate::{Error, io};

use frc::{Resolution as FrcResolution};

use std::fs::{File};
use std::path::{Path};

// was used for calibration calc but not needed now - keep anyway just for reference
// pub const MAGNIFICATION: f64 = 8.0;
pub const PIXEL_SIZE_NM: f64 = 12.5;

// (spatial_frc (q), frc, smoothed frc, threshold curve)
fn to_frc_and_threshold(data: &[(f64, f64, f64, f64)]) -> (Vec<f64>, Vec<f64>, Vec<f64>)
{
	let mut qs = Vec::with_capacity(data.len());
	let mut frcs = Vec::with_capacity(data.len());
	let mut threshold = Vec::with_capacity(data.len());
	for (q, _frc, smoothed_frc, threshold_curve) in data
	{
		qs.push(*q);
		frcs.push(*smoothed_frc);
		threshold.push(*threshold_curve);
	}
	(qs, frcs, threshold)
}

fn to_resolution(data: &[(f64, f64, f64, f64)]) -> FrcResolution
{
	let (qs, frcs, threshold) = to_frc_and_threshold(data);
	FrcResolution::from(qs, frcs, threshold)
}

fn write_report_to<P: AsRef<Path>>(filename: P, data: &[(f64, f64, f64, f64)]) -> Result<(), Error>
{
	let resolution = to_resolution(data);	
    File::create(filename).and_then(|f| resolution.write_to(PIXEL_SIZE_NM, f)).map_err(Error::from)
}

pub fn write_calibration_data<P: AsRef<Path>>(output_directory: P) -> Result<(), Error>
{
	let dir = output_directory.as_ref();
	let sr_0_005_filename = io::calibration_sampling_ratio_0_005(dir);
	let sr_0_005_ok = write_report_to(sr_0_005_filename, sampling_ratio_0_005::DATA);
	
	let sr_0_05_filename = io::calibration_sampling_ratio_0_05(dir);
	let sr_0_05_ok = write_report_to(sr_0_05_filename, sampling_ratio_0_05::DATA);

	let sr_0_5_filename = io::calibration_sampling_ratio_0_5(dir);
	let sr_0_5_ok = write_report_to(sr_0_5_filename, sampling_ratio_0_5::DATA);

	let sr_5_filename = io::calibration_sampling_ratio_5(dir);
	let sr_5_ok = write_report_to(sr_5_filename, sampling_ratio_5::DATA);

	let sr_25_filename = io::calibration_sampling_ratio_25(dir);
	let sr_25_ok = write_report_to(sr_25_filename, sampling_ratio_25::DATA);

	sr_0_005_ok.and(sr_0_05_ok).and(sr_0_5_ok).and(sr_5_ok).and(sr_25_ok)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn sr_0_005_fire_number() 
	{
		let res = to_resolution(sampling_ratio_0_005::DATA);
		assert_eq!(res.as_value(), 11.252770994857483) // stated 11.493
	}

	#[test]
	fn sr_0_05_fire_number() 
	{
		let res = to_resolution(sampling_ratio_0_05::DATA);
		assert_eq!(res.as_value(), 6.243912185619021) // stated 6.306
	}

	#[test]
	fn sr_0_5_fire_number() 
	{
		let res = to_resolution(sampling_ratio_0_5::DATA);
		assert_eq!(res.as_value(), 4.675803186092291) // stated 4.709
	}

	#[test]
	fn sr_5_fire_number() 
	{
		let res = to_resolution(sampling_ratio_5::DATA);
		assert_eq!(res.as_value(), 3.923368761353248) // stated 3.951
	}

	#[test]
	fn sr_25_fire_number() 
	{
		let res = to_resolution(sampling_ratio_25::DATA);
		assert_eq!(res.as_value(), 3.6056305526710513) // stated 3.625
	}
}