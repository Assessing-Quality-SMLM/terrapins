use super::{Assessment, Outcome, spatial_freq_for_correlation_from};

use crate::{constants, settings::report::{BlinkingSettings, AssessmentSettings}};

use frc::{Resolution as FrcResolution};


fn get_score(target: f64, drift: &FrcResolution, zip: &FrcResolution) -> Result<f64, String>
{
	let error = |name| format!("Could not calculate spatial frequency at {target} correlation for {name}");
	let drift_value = spatial_freq_for_correlation_from(target, drift).ok_or_else(|| error(constants::DRIFT_SPLIT))?;
	let zip_value = spatial_freq_for_correlation_from(target, zip).ok_or_else(|| error(constants::ZIP_SPLIT))?;
	let score = drift_value / zip_value;
	Ok(score)
}

fn get_outcome(score: f64, settings: &AssessmentSettings) -> Outcome
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

fn get_details(outcome: &Outcome) -> &'static str
{
	if *outcome == Outcome::Pass
	{
		""
	}
	else 
	{
		"Multiple repeat localisations of the same flurophores are present, reducing maximum performance. Consider merging repeat localisations (or increase distance if already merging). Aim to increase flurophore blinking rate with higher illumination intensity, buffer changes or increasing exposure time (if emitter density allows)"
	}
}

fn assess_(score: f64, settings: &AssessmentSettings) -> Assessment
{
	let outcome = get_outcome(score, settings);
	let message = get_details(&outcome);
	let mut assessment = Assessment::new("Blinking", outcome, message);
	assessment.set_score(score);
	assessment
}

pub fn assess(settings: &BlinkingSettings, drift: &FrcResolution, zip: &FrcResolution) -> Result<Assessment, String>
{
	let f = |s| assess_(s, settings.assessment_settings());
	get_score(settings.target(), drift, zip).map(f)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn score_test_basic()
	{
		let drift_correlation = vec![0.5];
		let drift_qs = vec![1.0];
		let drift_resolution = FrcResolution::new(Ok(0.0), drift_qs, drift_correlation, Vec::new());
		let zip_correlation = vec![0.5];
		let zip_qs = vec![0.5];
		let zip_resolution = FrcResolution::new(Ok(0.0), zip_qs, zip_correlation, Vec::new());
		let score = get_score(0.5, &drift_resolution, &zip_resolution).unwrap();
		assert_eq!(score, 2.0);
	}

	#[test]
	fn score_drift_error()
	{
		let drift_correlation = vec![];
		let drift_qs = vec![];
		let drift_resolution = FrcResolution::new(Ok(0.0), drift_qs, drift_correlation, Vec::new());
		let zip_correlation = vec![];
		let zip_qs = vec![];
		let zip_resolution = FrcResolution::new(Ok(0.0), zip_qs, zip_correlation, Vec::new());
		let result = get_score(0.5, &drift_resolution, &zip_resolution).unwrap_err();
		assert_eq!(result, "Could not calculate spatial frequency at 0.5 correlation for drift");
	}

	#[test]
	fn score_zip_error()
	{
		let drift_correlation = vec![0.5];
		let drift_qs = vec![1.0];
		let drift_resolution = FrcResolution::new(Ok(0.0), drift_qs, drift_correlation, Vec::new());
		let zip_correlation = vec![];
		let zip_qs = vec![];
		let zip_resolution = FrcResolution::new(Ok(0.0), zip_qs, zip_correlation, Vec::new());
		let result = get_score(0.5, &drift_resolution, &zip_resolution).unwrap_err();
		assert_eq!(result, "Could not calculate spatial frequency at 0.5 correlation for zip");
	}

	#[test]
	fn outcome_tests()
	{
		assert_eq!(get_outcome(0.750001, &AssessmentSettings::default_blink_settings()), Outcome::Pass);
		assert_eq!(get_outcome(0.75, &AssessmentSettings::default_blink_settings()), Outcome::Pass);
		assert_eq!(get_outcome(0.7499999999, &AssessmentSettings::default_blink_settings()), Outcome::Indeterminate);
		assert_eq!(get_outcome(0.5, &AssessmentSettings::default_blink_settings()), Outcome::Indeterminate);
		assert_eq!(get_outcome(0.499999999, &AssessmentSettings::default_blink_settings()), Outcome::Fail);
	}

	#[test]
	fn details_on_pass()
	{
		assert_eq!(get_details(&Outcome::Pass), "")
	}

	#[test]
	fn details_on_fail()
	{
		let expected = "Multiple repeat localisations of the same flurophores are present, reducing maximum performance. Consider merging repeat localisations (or increase distance if already merging). Aim to increase flurophore blinking rate with higher illumination intensity, buffer changes or increasing exposure time (if emitter density allows)";
		assert_eq!(get_details(&Outcome::Indeterminate), expected);
		assert_eq!(get_details(&Outcome::Fail), expected);
	}

	#[test]
	fn test_pass()
	{
		let assessment = assess_(0.75, &AssessmentSettings::default_blink_settings());
		assert_eq!(assessment.passed(), true);
		assert_eq!(assessment.score().unwrap(), 0.75);
		assert_eq!(assessment.message(), "");
	}

	#[test]
	fn test_indeterminate()
	{
		let assessment = assess_(0.5, &AssessmentSettings::default_blink_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.score().unwrap(), 0.5);
		let expected_message = "Multiple repeat localisations of the same flurophores are present, reducing maximum performance. Consider merging repeat localisations (or increase distance if already merging). Aim to increase flurophore blinking rate with higher illumination intensity, buffer changes or increasing exposure time (if emitter density allows)";
		assert_eq!(assessment.message(), expected_message);
	}

	#[test]
	fn test_fail()
	{
		let assessment = assess_(0.4, &AssessmentSettings::default_blink_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score().unwrap(), 0.4);
		let expected_message = "Multiple repeat localisations of the same flurophores are present, reducing maximum performance. Consider merging repeat localisations (or increase distance if already merging). Aim to increase flurophore blinking rate with higher illumination intensity, buffer changes or increasing exposure time (if emitter density allows)";
		assert_eq!(assessment.message(), expected_message);
	}
}