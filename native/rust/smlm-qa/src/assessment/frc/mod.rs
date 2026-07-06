mod alignment;
mod blinking;
mod calibration;
mod resolution;

pub use self::blinking::assess as assess_blinking;
pub use self::resolution::assess as assess_resolution;

use super::{Assessment, Outcome};

use crate::{Error};
use crate::settings::{report::{RatioSettings, AssessmentSettings, SamplingSettings}};

use frc::{Resolution as FrcResolution};

use std::path::{Path};

const SAMPLING_ASSESSMENT_NAME : &str = "Sampling";
const DRIFT_ASSESSMENT_NAME : &str = "Drift";
const FRC_MAGNIFICATION_NAME : &str = "FRC Magnification";

fn get_index_closest_to<I: Iterator<Item=f64>>(target: f64, values: I) -> Option<usize>
{
	values.enumerate()
		  .filter(|(_, x)| x.is_finite())
		  .map(|(idx, x)| (idx, (x - target).powi(2).sqrt()))
		  .min_by(|(_, x), (_, y)| x.total_cmp(y))
		  .map(|(idx, _)| idx)
}

fn spatial_freq_for_correlation(target: f64, qs: &[f64], correlations: &[f64]) -> Option<f64>
{
	get_index_closest_to(target, correlations.iter().copied()).and_then(|idx| qs.get(idx)).copied()
}

fn spatial_freq_for_correlation_from(target: f64, resolution: &FrcResolution) -> Option<f64>
{
	spatial_freq_for_correlation(target, resolution.qs(), resolution.correlations())
}

fn error_message(target: f64, name: &str) -> String
{
	format!("Could not calculate spatial frequency at {target} correlation for {name}")
}

fn drift_outcome(score: f64, assessment_settings: &AssessmentSettings) -> Outcome
{
	// println!("pass threshold: {}", assessment_settings.pass_threshold());
	if score >= assessment_settings.pass_threshold()
	{
		return Outcome::Pass
	}
	else if score >= assessment_settings.inderterminate_threshold()
	{
		return Outcome::Indeterminate
	}
	return Outcome::Fail
}

fn assess_drift_(drift_res: f64, half_res: f64, settings: &AssessmentSettings) -> Assessment
{
	let score = drift_res / half_res;
	let outcome = drift_outcome(score, settings);
	let core = format!("drift split: {drift_res} half split: {half_res}");
	let message = if outcome == Outcome::Pass {core} else{ format!("{core}. Your data still contains drift or photobleaching")};
	let mut assessment = Assessment::new(DRIFT_ASSESSMENT_NAME, outcome, &message);
	assessment.set_score(score);
	assessment
}

pub fn assess_drift(drift_split: &FrcResolution, half_split: &FrcResolution, assessment_settings: &AssessmentSettings) -> Result<Assessment, String>
{
	let drift_res = drift_split.as_value();
	let half_res = half_split.as_value();
	Ok(assess_drift_(drift_res, half_res, assessment_settings))
}

fn get_sampling_ratio(lower: f64, upper: f64) -> f64
{
	upper / lower
}

fn sampling_ratio(settings: &RatioSettings, curve: &FrcResolution) -> Result<(f64, f64), String>
{
	// lower is 1/7 and upper is 6/7
	// we want 6/7 / 1/7 for sampling report
	let lower = spatial_freq_for_correlation_from(settings.lower(), curve).ok_or_else(|| error_message(settings.lower(), "lower ratio"))?;
	let upper = spatial_freq_for_correlation_from(settings.upper(), curve).ok_or_else(|| error_message(settings.upper(), "upper ratio"))?;
	Ok((lower, upper))
}

fn sampling_outcome(score: f64, settings: &AssessmentSettings) -> Outcome
{
	if score >= settings.pass_threshold()
	{
		Outcome::Pass
	}
	else if score >= settings.inderterminate_threshold()
	{
		Outcome::Indeterminate
	}
	else 
	{
		Outcome::Fail
	}
}

const SAMPLING_FAILURE_MESSAGE : &str = "There are insufficient independent localisations to fully sample the structure. This is reducing the resolution below the localisation precision limit";

fn assess_sampling_(lower: f64, upper: f64, settings: &AssessmentSettings) -> Assessment
{
	let score = get_sampling_ratio(lower, upper);
	let outcome = sampling_outcome(score, settings);
	let core = format!("lower bound spatial frequency: {lower} upper bound spatial frequency: {upper}");
	let message = if outcome == Outcome::Pass {core} else {format!("{core}. {}", SAMPLING_FAILURE_MESSAGE)};
	let mut assessment = Assessment::new(SAMPLING_ASSESSMENT_NAME, outcome, &message);
	assessment.set_score(score);
	assessment
}

pub fn assess_sampling(settings: &SamplingSettings, drift_split: &FrcResolution) -> Result<Assessment, String>
{
	sampling_ratio(settings.ratio(), drift_split).map(|(lower, upper)| assess_sampling_(lower, upper, settings.assessment()))
}

const HIGHER_THRESHOLD_IDX : usize = 0;
const LOWER_THRESHOLD_IDX : usize = 1;

fn magnfication_too_high(score: f64, thresholds: &[f64; 2]) -> bool
{
	score < thresholds[HIGHER_THRESHOLD_IDX]
}

fn magnification_too_low(score: f64, thresholds: &[f64; 2]) -> bool
{
	score > thresholds[LOWER_THRESHOLD_IDX]
}

fn get_magnification_outcome(score: f64, thresholds: &[f64; 2]) -> Outcome
{
	// we'll setup like this for the dictionary ovverride in the output file
	// see the colour string
	if score < thresholds[HIGHER_THRESHOLD_IDX]
	{
		Outcome::Indeterminate
	}
	else if thresholds[LOWER_THRESHOLD_IDX] < score
	{
		Outcome::Fail
	}
	else 
	{
		Outcome::Pass
	}	
}

fn get_magnification_message(score: f64, thresholds: &[f64; 2]) -> &'static str
{
	if magnfication_too_high(score, thresholds)
	{
		"The reconstruction magnification is set too high for the resolution achieved. This will lead to an excessively sparse/noisy reconstruction and will reduce the sampling score"
	}
	else if magnification_too_low(score, thresholds)
	{
		"The reconstruction magnification is too low for the resolution achieved. This may result in some fine structure being missed that is at or below the scale of the reconstruction pixel size"
	}
	else 
	{
		"The reconstruction magnification is appropriate for the resolution achieved"
	}
}

fn get_magnification_colour(outcome: &Outcome) -> Outcome
{
	match outcome
	{
		Outcome::Pass => Outcome::Pass,
		_ => Outcome::Indeterminate
	}
}

fn assess_magnification_(score: f64, settings: &AssessmentSettings) -> Assessment
{
	let thresholds = settings.thresholds();
	let outcome = get_magnification_outcome(score, thresholds);
	let colour = get_magnification_colour(&outcome);
	let message = get_magnification_message(score, thresholds);
	let mut assessment = Assessment::new(FRC_MAGNIFICATION_NAME, outcome, message);
	assessment.set_score(score);
	assessment.set_colour(colour);
	assessment
}

pub fn assess_magnification(drift_split: &FrcResolution, settings: &AssessmentSettings) -> Assessment
{
	let value = drift_split.to_spatial_frequency();
	assess_magnification_(value, settings)
}

pub fn align_to_calibration_space(frc: &FrcResolution, pixel_size: f64) -> FrcResolution
{
	let new_qs = alignment::align_to_calibration(frc.qs(), pixel_size);
	frc.realign_with(new_qs)
}

pub fn write_calibration_curves_to<P: AsRef<Path>>(output_directory: P) -> Result<(), Error>
{
	calibration::write_calibration_data(output_directory)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn sampling_ratio_is_upper_over_lower() // i.e f(6/7) / f(1/7)
	{
		assert_eq!(get_sampling_ratio(1.0, 2.0), 2.0)
	}

	#[test]
	fn magnification_outcomes()
	{
		assert_eq!(get_magnification_outcome(0.19999999, AssessmentSettings::default_magnification_settings().thresholds()), Outcome::Indeterminate);
		assert_eq!(get_magnification_outcome(0.2, AssessmentSettings::default_magnification_settings().thresholds()), Outcome::Pass);
		assert_eq!(get_magnification_outcome(0.4, AssessmentSettings::default_magnification_settings().thresholds()), Outcome::Pass);
		assert_eq!(get_magnification_outcome(0.4000000001, AssessmentSettings::default_magnification_settings().thresholds()), Outcome::Fail);
	}

	#[test]
	fn magnification_ok()
	{
		let assessment = assess_magnification_(0.3, &AssessmentSettings::default_magnification_settings());
		assert_eq!(assessment.score(), Some(0.3));
		assert_eq!(assessment.passed(), true);
		assert_eq!(assessment.message(), "The reconstruction magnification is appropriate for the resolution achieved");
	}

	fn magnification_dictionary() -> [String; 3] 
	{
		["Pass".to_string(), "Too High".to_string(), "Too Low".to_string()]
	}

	#[test]
	fn magnification_too_low()
	{
		let assessment = assess_magnification_(0.41, &AssessmentSettings::default_magnification_settings());
		assert_eq!(assessment.score(), Some(0.41));
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.message(), "The reconstruction magnification is too low for the resolution achieved. This may result in some fine structure being missed that is at or below the scale of the reconstruction pixel size");
		let expected = "name,FRC Magnification\nscore,0.41\nresult,failed,Too Low\ncolour,indeterminate\nmessage,The reconstruction magnification is too low for the resolution achieved. This may result in some fine structure being missed that is at or below the scale of the reconstruction pixel size";
		assert_eq!(assessment.section_string(&magnification_dictionary()), expected)
	}

	#[test]
	fn magnification_too_high()
	{
		let assessment = assess_magnification_(0.19999, &AssessmentSettings::default_magnification_settings());
		assert_eq!(assessment.score(), Some(0.19999));
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.failed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.message(), "The reconstruction magnification is set too high for the resolution achieved. This will lead to an excessively sparse/noisy reconstruction and will reduce the sampling score");
		let expected = "name,FRC Magnification\nscore,0.19999\nresult,indeterminate,Too High\ncolour,indeterminate\nmessage,The reconstruction magnification is set too high for the resolution achieved. This will lead to an excessively sparse/noisy reconstruction and will reduce the sampling score";
		assert_eq!(assessment.section_string(&magnification_dictionary()), expected)
	}

	#[test]
	fn drift_outcome_tests()
	{
		assert_eq!(drift_outcome(0.9000001, &AssessmentSettings::default_drift_settings()), Outcome::Pass);
		assert_eq!(drift_outcome(0.9, &AssessmentSettings::default_drift_settings()), Outcome::Pass);
		assert_eq!(drift_outcome(0.8999999999, &AssessmentSettings::default_drift_settings()), Outcome::Indeterminate);
		assert_eq!(drift_outcome(0.80000001, &AssessmentSettings::default_drift_settings()), Outcome::Indeterminate);
		assert_eq!(drift_outcome(0.8, &AssessmentSettings::default_drift_settings()), Outcome::Indeterminate);
		assert_eq!(drift_outcome(0.79999999999, &AssessmentSettings::default_drift_settings()), Outcome::Fail);
		assert_eq!(drift_outcome(0.5, &AssessmentSettings::default_drift_settings()), Outcome::Fail);
	}

	#[test]
	fn drift_assessment_pass_test()
	{
		let assessment = assess_drift_(0.9, 1.0, &AssessmentSettings::default_drift_settings());
		assert_eq!(assessment.score().unwrap(), 0.9);
		assert_eq!(assessment.passed(), true);
		assert_eq!(assessment.message(), "drift split: 0.9 half split: 1");
	}

	#[test]
	fn drift_assessment_indeterminate()
	{
		let assessment = assess_drift_(0.8, 1.0, &AssessmentSettings::default_drift_settings());
		assert_eq!(assessment.score().unwrap(), 0.8);
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.message(), "drift split: 0.8 half split: 1. Your data still contains drift or photobleaching");
	}

	#[test]
	fn drift_assessment_fail()
	{
		let assessment = assess_drift_(0.7, 1.0, &AssessmentSettings::default_drift_settings());
		assert_eq!(assessment.score().unwrap(), 0.7);
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.message(), "drift split: 0.7 half split: 1. Your data still contains drift or photobleaching");
	}

	#[test]
	fn sampling_outcome_tests()
	{
		assert_eq!(sampling_outcome(0.3000001, &AssessmentSettings::default_sampling_settings()), Outcome::Pass);
		assert_eq!(sampling_outcome(0.3, &AssessmentSettings::default_sampling_settings()), Outcome::Pass);
		assert_eq!(sampling_outcome(0.2999999999, &AssessmentSettings::default_sampling_settings()), Outcome::Indeterminate);
		assert_eq!(sampling_outcome(0.10000001, &AssessmentSettings::default_sampling_settings()), Outcome::Indeterminate);
		assert_eq!(sampling_outcome(0.1, &AssessmentSettings::default_sampling_settings()), Outcome::Indeterminate);
		assert_eq!(sampling_outcome(0.09999999999, &AssessmentSettings::default_sampling_settings()), Outcome::Fail);
	}
	
	#[test]
	fn sampling_assessment_pass_test()
	{
		let assessment = assess_sampling_(1.0, 0.3, &AssessmentSettings::default_sampling_settings());
		assert_eq!(assessment.score().unwrap(), 0.3);
		assert_eq!(assessment.passed(), true);
		assert_eq!(assessment.message(), "lower bound spatial frequency: 1 upper bound spatial frequency: 0.3");
	}

	#[test]
	fn sampling_assessment_indeterminate()
	{
		let assessment = assess_sampling_(1.0, 0.1, &AssessmentSettings::default_sampling_settings());
		assert_eq!(assessment.score().unwrap(), 0.1);
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		let expected_message = "lower bound spatial frequency: 1 upper bound spatial frequency: 0.1. There are insufficient independent localisations to fully sample the structure. This is reducing the resolution below the localisation precision limit";
		assert_eq!(assessment.message(), expected_message);
	}

	#[test]
	fn sampling_assessment_fail()
	{
		let assessment = assess_sampling_(1.0, 0.09, &AssessmentSettings::default_sampling_settings());
		assert_eq!(assessment.score().unwrap(), 0.09);
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		let expected_message = "lower bound spatial frequency: 1 upper bound spatial frequency: 0.09. There are insufficient independent localisations to fully sample the structure. This is reducing the resolution below the localisation precision limit";
		assert_eq!(assessment.message(), expected_message);
	}

	#[test]
	fn can_get_index_basic() 
	{
		let n = 10;
		let result = get_index_closest_to(0.5,  (0..n).map(|idx| idx as f64 / n as f64)).unwrap();
		assert_eq!(result, 5)
	}

	#[test]
	fn can_get_index_backwards() 
	{
		let n = 10;
		let values = (0..n).map(|idx| idx as f64 / n as f64).rev().collect::<Vec<f64>>();
		let result = get_index_closest_to(0.5,  values.iter().copied()).unwrap();
		assert_eq!(result, 4)
	}

	#[test]
	fn resolves_to_first() 
	{
		let result = get_index_closest_to(0.5, [0.4, 0.6].iter().copied()).unwrap();
		assert_eq!(result, 0);
		let result = get_index_closest_to(0.5, [0.6, 0.4].iter().copied()).unwrap();
		assert_eq!(result, 0)
	}

	#[test]
	fn can_handle_nans() 
	{
		let result = get_index_closest_to(0.5, [std::f64::NAN, -std::f64::NAN, 0.6].iter().copied()).unwrap();
		assert_eq!(result, 2);
	}	

	#[test]
	fn can_handle_infs() 
	{
		let result = get_index_closest_to(0.5, [std::f64::INFINITY, -std::f64::INFINITY, 0.6].iter().copied()).unwrap();
		assert_eq!(result, 2);
	}

	#[test]
	fn can_get_half_correlation_basic()
	{
		let qs = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5];
		let correlations = [1.0, 0.9, 0.5, 0.3, 0.2, 0.1];
		assert_eq!(spatial_freq_for_correlation(0.5, &qs, &correlations), Some(0.2))
	}

	#[test]
	fn half_correlation_can_handle_different_size_arrays()
	{
		let qs = [0.0];
		let correlations = [];
		assert_eq!(spatial_freq_for_correlation(0.5, &qs, &correlations), None)
	}
}