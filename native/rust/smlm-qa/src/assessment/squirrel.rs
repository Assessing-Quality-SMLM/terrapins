use super::{Assessment, Outcome};

use crate::{constants, settings::report::{AssessmentSettings, SquirrelSettings}, tools::squirrel::SquirrelResult};

const SHARPENING: &str = "Compare the generated widefield error map with the HAWKMAN error map. If errors are in the same place then SQUIRREL is detecting sharpening. If they are in different places or on areas of weak sharpening then SQUIRREL is detecting under counting. This could be due to fine structure that has not been resolved, poor blinking, dim emitters, high local background or non-linearity.";
const MISSING_STRUCTURE: &str = "Missing structure due to high background / bleaching or labelling. Differences are due to non-blinking issues.";

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

fn get_message_aof(passed: bool) -> &'static str
{
	if !passed {SHARPENING} else {""}
}

fn get_message_wf(passed: bool) -> &'static str
{
	if !passed {MISSING_STRUCTURE} else {""}
}


fn assess_wf(wf_score: f64, settings: &AssessmentSettings) -> Assessment
{
	let outcome = get_outcome(wf_score, settings);
	let message = get_message_wf(outcome == Outcome::Pass);
	let mut assessment = Assessment::new(constants::SQUIRREL, outcome, message);
	assessment.set_score(wf_score);
	assessment
}

fn assess_aof(score: f64, settings: &AssessmentSettings) -> Assessment
{
	let outcome = get_outcome(score, settings);
	let message = get_message_aof(outcome == Outcome::Pass);
	let mut assessment = Assessment::new(constants::SQUIRREL, outcome, message);
	assessment.set_score(score);
	assessment
}

fn get_joint_message(wf_outcome: Outcome, aof_outcome: Outcome) -> String
{
	let mut message = String::new();
	let wf_message = get_message_wf(wf_outcome == Outcome::Pass);
	if !wf_message.is_empty()
	{
		message.push_str(format!("Because the true widefield assessment did not pass - {wf_message}").as_str());
	}
	let aof_message = get_message_aof(aof_outcome == Outcome::Pass);
	if !aof_message.is_empty()
	{
		let prefix = if message.is_empty() {""} else {" "};
		message.push_str(format!("{prefix}Because the average of frames assessment did not pass - {aof_message}").as_str());
	}
	message
}

fn assess_both(wf_score: f64, aof_score: f64, settings: &AssessmentSettings) -> Assessment
{
	let wf_outcome = get_outcome(wf_score, settings);
	let aof_outcome = get_outcome(aof_score, settings);
	let outcome = std::cmp::min(wf_outcome, aof_outcome);
	let message = get_joint_message(wf_outcome, aof_outcome);
	let mut assessment = Assessment::new(constants::SQUIRREL, outcome, &message);
	assessment.set_score(wf_score);
	assessment
}

fn assess_(wf_score: Option<f64>, aof_score: Option<f64>, settings: &AssessmentSettings) -> Assessment
{
	// filtered before so can't both be none
	if wf_score.is_some() && aof_score.is_some()
	{
		return assess_both(wf_score.unwrap(), aof_score.unwrap(), settings)
	}

	if wf_score.is_some()
	{
		return assess_wf(wf_score.unwrap(), settings)
	}

	return assess_aof(aof_score.unwrap(), settings)
}

fn get_score_from(squirrel_result: &SquirrelResult) -> Result<f64, String>
{
	squirrel_result.metrics().map(|m| m.pearsons()).map_err(|e| format!("Error creating metrics for generated widefield: {e}"))
}


pub fn assess(settings: &SquirrelSettings, generated_wf: Option<&SquirrelResult>, true_wf: Option<&SquirrelResult>) -> Result<Assessment, String>
{
	if generated_wf.is_none() && true_wf.is_none()
	{
		return Err("Both generated widefield and true widefield squirrel results are missing".to_string())
	}

	let wf_score = true_wf.map(get_score_from).transpose()?;
	let aof_score = generated_wf.map(get_score_from).transpose()?;

	Ok(assess_(wf_score, aof_score, settings.assessment_settings()))
}

#[cfg(test)]
mod tests 
{
	use super::*;

	// n, p, i, f = None, Pass, Inderterminate, Fail 
	// 4 x 4 inputs = 16

	#[test] // n, n
	fn missing_data_test() 
	{
		let expected = "Both generated widefield and true widefield squirrel results are missing";
		assert_eq!(assess(&SquirrelSettings::default(), None, None).unwrap_err(), expected)
	}

	#[test] // n, p
	fn no_true_generated_pass()
	{
		let true_wf_result = None;
		let generated_wf_result = Some(1.0);
		let assessment = assess_(true_wf_result, generated_wf_result, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), true);
		assert_eq!(assessment.score(), Some(1.0));
		assert_eq!(assessment.message(), "");
	}

	#[test] // n, i
	fn no_true_generated_indeterminate()
	{
		let aof_score = 0.92;
		let aof = Some(aof_score);
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let assessment = assess_(None, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.score(), aof);
		assert_eq!(assessment.message(), SHARPENING);
	}

	#[test] // n, f
	fn no_true_generated_failed()
	{
		let aof_score = 0.8;
		let aof = Some(aof_score);
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let assessment = assess_(None, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score(), aof);
		assert_eq!(assessment.message(), SHARPENING);
	}

	#[test] // p, n
	fn true_passed_no_generated()
	{
		let true_score = 1.0;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Pass);
		let assessment = assess_(Some(true_score), None, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), true);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), false);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), "");
	}

	#[test] // p, p
	fn true_passed_generated_passed()
	{
		let true_score = 1.0;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Pass);
		let aof_score = 1.0;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Pass);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), true);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), false);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), "");
	}

	#[test] // p, i
	fn true_passed_generated_indeterminate()
	{
		let true_score = 1.0;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Pass);
		let aof_score = 0.92;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.failed(), false);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the average of frames assessment did not pass - {SHARPENING}"));
	}

	#[test] // p, f
	fn true_passed_generated_fail()
	{
		let true_score = 1.0;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Pass);
		let aof_score = 0.8;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the average of frames assessment did not pass - {SHARPENING}"));
	}

	#[test] // i, n
	fn true_indeterminate_no_generated()
	{
		let true_score = 0.92;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let assessment = assess_(Some(true_score), None, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.failed(), false);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), MISSING_STRUCTURE);
	}

	#[test] // i, p
	fn true_indeterminate_generated_passed()
	{
		let true_score = 0.92;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let aof_score = 1.0;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Pass);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.failed(), false);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the true widefield assessment did not pass - {MISSING_STRUCTURE}"));
	}

	#[test] // i, i
	fn true_indeterminate_generated_indeterminate()
	{
		let true_score = 0.92;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let aof_score = true_score;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), true);
		assert_eq!(assessment.failed(), false);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the true widefield assessment did not pass - {MISSING_STRUCTURE} Because the average of frames assessment did not pass - {SHARPENING}"));
	}

	#[test] // i, f
	fn true_indeterminate_generated_failed()
	{
		let true_score = 0.92;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let aof_score = 0.8;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the true widefield assessment did not pass - {MISSING_STRUCTURE} Because the average of frames assessment did not pass - {SHARPENING}"));
	}

	#[test] // f, n
	fn true_fail_generated_missing()
	{
		let true_score = 0.8;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let true_ = Some(true_score);
		let assessment = assess_(true_, None, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), MISSING_STRUCTURE);
	}

	#[test] // f, p
	fn true_fail_generated_passed()
	{
		let true_score = 0.8;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let aof_score = 1.0;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Pass);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the true widefield assessment did not pass - {MISSING_STRUCTURE}"));
	}

	#[test] // f, i
	fn true_fail_generated_indeterminate()
	{
		let true_score = 0.8;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let aof_score = 0.92;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Indeterminate);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the true widefield assessment did not pass - {MISSING_STRUCTURE} Because the average of frames assessment did not pass - {SHARPENING}"));
	}

	#[test] // f, f
	fn true_fail_generated_failed()
	{
		let true_score = 0.8;
		assert_eq!(get_outcome(true_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let aof_score = 0.8;
		assert_eq!(get_outcome(aof_score, &AssessmentSettings::default_squirrel_settings()), Outcome::Fail);
		let true_ = Some(true_score);
		let aof = Some(aof_score);
		let assessment = assess_(true_, aof, &AssessmentSettings::default_squirrel_settings());
		assert_eq!(assessment.passed(), false);
		assert_eq!(assessment.indeterminate(), false);
		assert_eq!(assessment.failed(), true);
		assert_eq!(assessment.score(), Some(true_score));
		assert_eq!(assessment.message(), format!("Because the true widefield assessment did not pass - {MISSING_STRUCTURE} Because the average of frames assessment did not pass - {SHARPENING}"));
	}
}