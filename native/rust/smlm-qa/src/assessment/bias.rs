use super::{Assessment, Outcome};

use crate::{constants::{DEFAULT_LOCALISATION_PRECISION}, settings::{Settings, report::{AssessmentSettings, BiasSettings}}, tools::hawkman::HawkmanResult};

use std::path::{Path};


type Level = u32;

type Score = (Level, f64);


const ASSESSMENT_NAME: &str = "Bias";

#[derive(Debug, PartialEq)]
enum Crossing 
{
	AllUnder,
	Level(Level),
	AllOver,
}

impl Crossing
{
	fn to_score(&self, sr_pixel_size: f64) -> Option<f64>
	{
		match self
		{
			Self::AllUnder => None,
			Self::Level(level) => Some(*level as f64 * sr_pixel_size),
			Self::AllOver => None
		}
	}
}

fn get_crossing(threshold: f64, level_ordered_scores: &[Score]) -> Crossing
{
	if level_ordered_scores.is_empty()
	{
		return Crossing::AllUnder
	}

	let (_level, score) = level_ordered_scores[0];
	if score >= threshold
	{
		return Crossing::AllOver
	}
	for (_level, score) in &level_ordered_scores[1..]
	{
		if *score >= threshold
		{
			return Crossing::Level(*_level)
		}
	}
	return Crossing::AllUnder
}

fn get_outcome_from_score(score: f64, localisation_precision: f64, assessment_settings: &AssessmentSettings) -> Outcome
{	
	if score <= (assessment_settings.pass_threshold() * localisation_precision)
	{
		Outcome::Pass
	}
	else if score <= (assessment_settings.inderterminate_threshold() * localisation_precision)
	{
		Outcome::Indeterminate
	}
	else 
	{
		Outcome::Fail	
	}
}

fn get_outcome(crossing: &Crossing, sr_pixel_size: f64, localisation_precision: f64, assessment_settings: &AssessmentSettings) -> Outcome
{
	match crossing
	{
		Crossing::AllUnder => Outcome::Fail,
		Crossing::Level(l) => 
		{
			let score = *l as f64 * sr_pixel_size;
			get_outcome_from_score(score, localisation_precision, assessment_settings)
		}
		Crossing::AllOver => Outcome::Pass
	}
}

fn get_crossing_part(crossing: &Crossing) -> String
{
	match crossing
	{
		Crossing::AllUnder => "Threshold has not been crossed".to_string(),
		Crossing::Level(level) => format!("Threshold crossed at level {level}"),
		Crossing::AllOver => "First level was over the threshold. This means no sharpening has been detected and resolution will be limited by localisation precision or sampling".to_string()
	}
}

fn precision(localisation_precision: Option<f64>) -> f64
{
	localisation_precision.unwrap_or(DEFAULT_LOCALISATION_PRECISION)
}

fn get_outcome_part(outcome: &Outcome, localisation_precision: Option<f64>, settings: &AssessmentSettings) -> String
{
	let precision_part = 
		match localisation_precision
		{
			None => "(assumed to be)",
			Some(_) => "of"
		};
	let p = precision(localisation_precision);
	let f = |scalar| format!("Precision is worse than {scalar}x max precision {precision_part} {p}nm.");
	match outcome
	{
		Outcome::Pass => if localisation_precision.is_some() {String::new()} else {format!("Precision assumed to be {p}nm")},
		Outcome::Indeterminate => f(settings.pass_threshold()),
		Outcome::Fail => f(settings.inderterminate_threshold())
	}	
}

fn get_message(crossing: &Crossing, outcome: &Outcome, localisation_precision: Option<f64>, settings: &AssessmentSettings) -> String
{
	format!("{}. {}", get_crossing_part(crossing), get_outcome_part(outcome, localisation_precision, settings))
}

fn assess_(crossing: Crossing, sr_pixel_size: f64, localisation_precision: Option<f64>, assessment_settings: &AssessmentSettings) -> Assessment
{	
	let score = crossing.to_score(sr_pixel_size);
	let outcome = get_outcome(&crossing, sr_pixel_size, precision(localisation_precision), assessment_settings);
	let message = get_message(&crossing, &outcome, localisation_precision, assessment_settings);
	let mut assessment = Assessment::new(ASSESSMENT_NAME, outcome, &message);
	match score
	{
		None => {},
		Some(s) => assessment.set_score(s)
	}
	assessment
}

pub fn assess<T: AsRef<Path>>(report_settings: &BiasSettings, settings: &Settings, hawkman_results: &HawkmanResult<T>, mean_precision: Option<f64>) -> Result<Assessment, String>
{
	let scores = hawkman_results.global_scores()?;	
	let threshold = report_settings.threshold();
	let sr_pixel_size = settings.super_res_pixel_size_nm();
	let crossing = get_crossing(threshold, &scores);
	Ok(assess_(crossing, sr_pixel_size, mean_precision, report_settings.assessment_settings()))
}


#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn crossing_with_empty_scores()
	{
		assert_eq!(get_crossing(0.1, &[]), Crossing::AllUnder)
	}

	#[test]
	fn crossing_if_first_then_all_over()
	{
		let scores = [(1, 0.2)];
		assert_eq!(get_crossing(0.1, &scores), Crossing::AllOver)
	}

	#[test]
	fn crossing_is_level_at_which_crossed()
	{
		let scores = [(1, 0.099), (2, 0.2)];
		assert_eq!(get_crossing(0.1, &scores), Crossing::Level(2))
	}

	#[test]
	fn crossing_all_under()
	{
		let scores = [(1, 0.099), (2, 0.09)];
		assert_eq!(get_crossing(0.1, &scores), Crossing::AllUnder)
	}

	#[test]
	fn levels_can_have_gaps()
	{
		let scores = [(1, 0.09), (2, 0.099), (3, 0.2)];
		assert_eq!(get_crossing(0.1, &scores), Crossing::Level(3));

		let scores = [(1, 0.09), (3, 0.2)];
		assert_eq!(get_crossing(0.1, &scores), Crossing::Level(3))
	}


	#[test]
	fn sanity_test()
	{
		let scores = [
		(1,0.6590445489009311),
		(2,0.7314350276597177),
		(3,0.8130247167006368),
		(4,0.9162437387781279),
		(5,0.8388043677597564),
		(6,0.7960298048221524),
		(7,0.8367995902692034),
		(8,0.7741977758515581),
		(9,0.7724654386314691),
		(10,0.7912862761743579)
		];
		assert_eq!(get_crossing(0.95, &scores), Crossing::AllUnder);
	}

	#[test]
	fn outcome_tests()
	{
		assert_eq!(get_outcome_from_score(2.0, 1.0, &AssessmentSettings::default_bias_settings()), Outcome::Pass); // 2x boundary
		assert_eq!(get_outcome_from_score(2.00000001, 1.0, &AssessmentSettings::default_bias_settings()), Outcome::Indeterminate); //4x boundary
		assert_eq!(get_outcome_from_score(4.0, 1.0, &AssessmentSettings::default_bias_settings()), Outcome::Indeterminate);
		assert_eq!(get_outcome_from_score(5.00000001, 1.0, &AssessmentSettings::default_bias_settings()), Outcome::Fail);
	}

	#[test]
	fn get_message_with_level_crossing_and_pass()
	{
		assert_eq!(get_message(&Crossing::Level(1), &Outcome::Pass, Some(1.0), &AssessmentSettings::default_bias_settings()), "Threshold crossed at level 1. ");
	}

	#[test]
	fn get_message_with_level_crossing_and_pass_and_no_precision()
	{
		assert_eq!(get_message(&Crossing::Level(1), &Outcome::Pass, None, &AssessmentSettings::default_bias_settings()), "Threshold crossed at level 1. Precision assumed to be 20nm");
	}

	#[test]
	fn get_message_with_level_crossing_and_indeterminate()
	{
		assert_eq!(get_message(&Crossing::Level(1), &Outcome::Indeterminate, Some(1.0), &AssessmentSettings::default_bias_settings()), "Threshold crossed at level 1. Precision is worse than 2x max precision of 1nm.");
	}

	#[test]
	fn get_message_with_level_crossing_and_fail()
	{
		assert_eq!(get_message(&Crossing::Level(1), &Outcome::Fail, Some(2.0), &AssessmentSettings::default_bias_settings()), "Threshold crossed at level 1. Precision is worse than 4x max precision of 2nm.");
	}

	#[test]
	fn get_message_with_level_crossing_and_fail_and_no_precision()
	{
		assert_eq!(get_message(&Crossing::Level(1), &Outcome::Fail, None, &AssessmentSettings::default_bias_settings()), "Threshold crossed at level 1. Precision is worse than 4x max precision (assumed to be) 20nm.");
	}

	#[test]
	fn get_message_with_all_over_and_pass()
	{
		assert_eq!(get_message(&Crossing::AllOver, &Outcome::Pass, Some(1.0), &AssessmentSettings::default_bias_settings()), "First level was over the threshold. This means no sharpening has been detected and resolution will be limited by localisation precision or sampling. ");
	}

	#[test]
	fn get_message_with_all_over_and_pass_and_no_precion()
	{
		assert_eq!(get_message(&Crossing::AllOver, &Outcome::Pass, None, &AssessmentSettings::default_bias_settings()), "First level was over the threshold. This means no sharpening has been detected and resolution will be limited by localisation precision or sampling. Precision assumed to be 20nm");
	}

	#[test]
	fn get_message_with_all_over_and_indeterminate()
	{
		assert_eq!(get_message(&Crossing::AllOver, &Outcome::Indeterminate, Some(1.0), &AssessmentSettings::default_bias_settings()), "First level was over the threshold. This means no sharpening has been detected and resolution will be limited by localisation precision or sampling. Precision is worse than 2x max precision of 1nm.");
	}

	#[test]
	fn get_message_with_all_over_and_fail()
	{
		assert_eq!(get_message(&Crossing::AllOver, &Outcome::Fail, Some(1.0), &AssessmentSettings::default_bias_settings()), "First level was over the threshold. This means no sharpening has been detected and resolution will be limited by localisation precision or sampling. Precision is worse than 4x max precision of 1nm.");
	}

	#[test]
	fn get_message_with_all_over_and_fail_and_no_precision()
	{
		assert_eq!(get_message(&Crossing::AllOver, &Outcome::Fail, None, &AssessmentSettings::default_bias_settings()), "First level was over the threshold. This means no sharpening has been detected and resolution will be limited by localisation precision or sampling. Precision is worse than 4x max precision (assumed to be) 20nm.");
	}

	#[test]
	fn get_message_with_all_under_and_pass()
	{
		assert_eq!(get_message(&Crossing::AllUnder, &Outcome::Pass, Some(1.0), &AssessmentSettings::default_bias_settings()), "Threshold has not been crossed. ");
	}

	#[test]
	fn get_message_with_all_under_and_pass_and_no_precion()
	{
		assert_eq!(get_message(&Crossing::AllUnder, &Outcome::Pass, None, &AssessmentSettings::default_bias_settings()), "Threshold has not been crossed. Precision assumed to be 20nm");
	}

	#[test]
	fn get_message_with_all_under_and_indeterminate()
	{
		assert_eq!(get_message(&Crossing::AllUnder, &Outcome::Indeterminate, Some(1.0), &AssessmentSettings::default_bias_settings()), "Threshold has not been crossed. Precision is worse than 2x max precision of 1nm.");
	}

	#[test]
	fn get_message_with_all_under_and_fail()
	{
		assert_eq!(get_message(&Crossing::AllUnder, &Outcome::Fail, Some(1.0), &AssessmentSettings::default_bias_settings()), "Threshold has not been crossed. Precision is worse than 4x max precision of 1nm.");
	}

	#[test]
	fn get_message_with_all_under_and_fail_and_no_precision()
	{
		assert_eq!(get_message(&Crossing::AllUnder, &Outcome::Fail, None, &AssessmentSettings::default_bias_settings()), "Threshold has not been crossed. Precision is worse than 4x max precision (assumed to be) 20nm.");
	}
}