pub use self::report::{Report, ReportItem};
use self::assessment::{Assessment}; 
use self::outcome::{Outcome};

mod assessment;
mod bias;
mod frc;
mod limiting_resolution;
mod outcome;
mod report;
mod sampling;
mod squirrel;

use crate::settings::report::AssessmentSettings;
use crate::{constants, io};
use crate::settings::{Settings, ReportSettings};
use crate::results::Results;

use std::path::{Path};

fn error_f(name: &str) -> String
{
	format!("Data for {name} split is missing")
}

fn generate_localisation_precision_assessment<T>(results: &Results<T>, assessment_settngs: &AssessmentSettings) -> Result<ReportItem, String>
{
	let mean_precision = results.mean_precision().ok_or_else(|| format!("Mean precision not calculated"))?;
	let mut assessment = Assessment::pass_with("Localisation", "Mean precision of localisations.");
	assessment.set_score(assessment_settngs.pass_threshold() * mean_precision);
	Ok((assessment, assessment_settngs.clone()))
}

fn generate_blinking_assessment<T>(results: &Results<T>, settings: &ReportSettings) -> Result<ReportItem, String>
{
	let drift = results.drift_split_frc().ok_or_else(|| error_f(constants::DRIFT_SPLIT))?;
	let zip = results.zip_split_frc().ok_or_else(|| error_f(constants::ZIP_SPLIT))?;
	let blinking_settings = settings.blinking();
	frc::assess_blinking(blinking_settings, drift, zip).map(|a| (a, blinking_settings.assessment_settings().clone()))
}

fn generate_sampling_report<T, P: AsRef<Path>>(results: &Results<T>, settings: &Settings, output_directory: P) -> Result<ReportItem, String>
{
	let drift = results.drift_split_frc().ok_or_else(|| error_f(constants::DRIFT_SPLIT))?;
	sampling::assess(drift, settings, output_directory).map(|a| (a, settings.report_settings().sampling_settings().assessment().clone()))
}

fn generate_drift_assessment<T>(results: &Results<T>, settings: &ReportSettings) -> Result<ReportItem, String>
{
	let drift = results.drift_split_frc().ok_or_else(|| error_f(constants::DRIFT_SPLIT))?;
	let half = results.half_split_frc().ok_or_else(|| error_f(constants::HALF_SPLIT))?;
	let drift_settings = settings.drift_settings();
	frc::assess_drift(drift, half, drift_settings).map(|a| (a, drift_settings.clone()))
}

fn generate_frc_resolution_assessment<T>(results: &Results<T>, settings: &Settings, report: &Report) -> Result<ReportItem, String>
{
	let drift = results.drift_split_frc().ok_or_else(|| error_f(constants::DRIFT_SPLIT))?;	
	let pixel_size = settings.super_res_pixel_size_nm();
	let drift_assessment = report.drift_assessment();
	let mag_assessment = report.magnification_assessment();
	let bias_assessment = report.bias_assessment();
	let squirrel_assessment = report.squirrel_assessment();
	let assessment = frc::assess_resolution(drift, pixel_size, drift_assessment, mag_assessment, bias_assessment, squirrel_assessment);
	Ok((assessment, AssessmentSettings::default_frc_resolution_assessment()))
}

fn generate_frc_magnification_assessment<T>(results: &Results<T>, settings: &ReportSettings) -> Result<ReportItem, String>
{
	let drift = results.drift_split_frc().ok_or_else(|| error_f(constants::DRIFT_SPLIT))?;
	let assessment_settings = settings.magnification();
	let assessment = frc::assess_magnification(drift, assessment_settings);
	Ok((assessment, assessment_settings.clone()))
}

fn generate_bias_assessment<T: AsRef<Path>>(settings: &Settings, results: &Results<T>, report_settings: &ReportSettings) -> Result<ReportItem, String>
{
	let hawkman = results.hawkman().ok_or_else(|| "HAWKMAN data is missing.")?;
	let bias_settings = report_settings.bias();
	bias::assess(bias_settings, settings, hawkman, results.mean_precision()).map(|a| (a, bias_settings.assessment_settings().clone()))
}

fn generate_squirrel_report<T>(settings: &ReportSettings, results: &Results<T>) -> Result<ReportItem, String>
{
	let generated_squirrel = results.generated_wf_squirrel();
	let true_squirrel = results.true_wf_squirrel();
	let squirrel_settings = settings.squirrel();
	squirrel::assess(squirrel_settings, generated_squirrel, true_squirrel).map(|a| (a, squirrel_settings.assessment_settings().clone()))
}

fn generate_limiting_resolution_assessment<T>(results: &Results<T>, report: &Report, assessment_settngs: &AssessmentSettings) -> Result<ReportItem, String>
{
	let localisation_precision = results.mean_precision();
	let frc_res = report.frc_resolution_assessment();
	let drift_assessment = report.drift_assessment();
	let bias_assessment = report.bias_assessment();
	let squirrel_assessment = report.squirrel_assessment();
	let assessment = limiting_resolution::assess(localisation_precision, frc_res, drift_assessment, bias_assessment, squirrel_assessment, assessment_settngs);
	assessment.map(|a| (a, assessment_settngs.clone()))
}

pub fn assess_results<T: AsRef<Path>>(settings: &Settings, results: &Results<T>) -> Report
{
	let report_settings = settings.report_settings();
	let output_directory = settings.output_directory();
	
	let mut report = Report::new();
	let mut errors = Vec::new();

	let report_location = io::report_location(&output_directory);
	println!("writing report to: {}", report_location.display());
	match std::fs::create_dir_all(&report_location)
	{
		Ok(_) => {},
		Err(e) => 
		{
			println!("Error creating report directory {}: {e}", report_location.display());
			return report
		}
	}

	let localisation_precision = generate_localisation_precision_assessment(results, report_settings.localisation_precision_settings());
	if localisation_precision.is_ok()
	{
		report.add_localisation_precision_assessment(localisation_precision.unwrap());
	}
	else 
	{
		errors.push(localisation_precision.unwrap_err());
	}
	
	let drift = generate_drift_assessment(results, report_settings);
	if drift.is_ok()
	{
		report.add_drift_assessment(drift.unwrap());
	}
	else 
	{
		errors.push(drift.unwrap_err());
	}

	let blinking = generate_blinking_assessment(results, report_settings);
	if blinking.is_ok()
	{
		report.add_blinking_assessment(blinking.unwrap())
	}
	else 
	{
		errors.push(blinking.unwrap_err());
	}

	let sampling = generate_sampling_report(results, settings, &report_location);
	if sampling.is_ok()
	{
		report.add_frc_sampling_assessment(sampling.unwrap())
	}
	else 
	{
		errors.push(sampling.unwrap_err());
	}

	let magnification = generate_frc_magnification_assessment(results, report_settings);
	if magnification.is_ok()
	{
		report.add_magnification_assessment(magnification.unwrap())
	}
	else 
	{
		errors.push(magnification.unwrap_err());
	}

	let bias = generate_bias_assessment(settings, results, report_settings);
	if bias.is_ok()
	{
		report.add_bias_assessment(bias.unwrap())
	}
	else 
	{
		errors.push(bias.unwrap_err())
	}

	let squirrel = generate_squirrel_report(report_settings, results);
	if squirrel.is_ok()
	{
		report.add_squirrel_assessment(squirrel.unwrap());
	}
	else 
	{
		errors.push(squirrel.unwrap_err())	
	}

	let frc_resolution = generate_frc_resolution_assessment(results, settings, &report);
	if frc_resolution.is_ok()
	{
		report.add_frc_resolution_assessment(frc_resolution.unwrap());
	}
	else 
	{
		errors.push(frc_resolution.unwrap_err());
	}

	let limiting_resolution = generate_limiting_resolution_assessment(results, &report, report_settings.limiting_resolution_settings());
	if limiting_resolution.is_ok()
	{
		report.add_limiting_resolution_assessment(limiting_resolution.unwrap());
	}
	else 
	{
		errors.push(limiting_resolution.unwrap_err());
	}

	match report.write_to_directory(&report_location)
	{
		Ok(_) => {},
		Err(e) => 
		{
			println!("Could not write report to {}: {}", report_location.display(), e.to_string())
		}
	}

	let calibration_curves = frc::write_calibration_curves_to(&report_location);
	if calibration_curves.is_err()
	{
		errors.push(calibration_curves.unwrap_err().to_string());
	}

	for error in errors
	{
		println!("{error}");
	}
	
	report
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn assessment_writing()
	{
		assert_eq!(Assessment::pass_with("some", "thing").to_string(), "some: passed\nthing");
		assert_eq!(Assessment::fail_with("some", "thing").to_string(), "some: failed\nthing");
	}

	#[test]
	fn write_report()
	{
		let mut report = Report::default();
		let item = (Assessment::pass_with("some", "thing"), AssessmentSettings::default_sampling_settings());
		report.add_frc_sampling_assessment(item);
		let item = (Assessment::fail_with("something", "else"), AssessmentSettings::default_limiting_precision_settings());
		report.add_frc_resolution_assessment(item);
		assert_eq!(report.to_string(), "\nsome: passed\nthing\n\nsomething: failed\nelse\n");
	}

}