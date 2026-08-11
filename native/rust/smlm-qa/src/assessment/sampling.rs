use super::frc as frc_assessment;

use crate::assessment::Assessment;
use crate::io;
use crate::settings::{Settings};

use frc::{Resolution as FrcResolution};

use std::fs::File;
use std::path::{Path};

pub fn assess<P: AsRef<Path>>(drift_resolution: &FrcResolution, settings: &Settings, output_directory: P) -> Result<Assessment, String>
{
	let pixel_size_nm = settings.super_res_pixel_size_nm();
	let aligned = frc_assessment::align_to_calibration_space(drift_resolution, pixel_size_nm);
	let report_settings = settings.report_settings();
	let assessment = frc_assessment::assess_sampling(report_settings.sampling_settings(), drift_resolution);
	let filename = io::calibration_aligned_frc(output_directory);
	let write_ok = File::create(&filename).and_then(|f| aligned.write_to(pixel_size_nm, f)).map_err(|e| format!("Could not create {} due to {e}", filename.display()));
	write_ok.and_then(|_| assessment)
}