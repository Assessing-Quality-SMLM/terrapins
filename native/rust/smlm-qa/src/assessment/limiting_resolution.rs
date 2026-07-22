use super::{Assessment, Outcome};

use crate::{constants::DEFAULT_LOCALISATION_PRECISION, settings::report::AssessmentSettings};

#[derive(Debug)]
struct Components<'a>
{
	drift: Option<&'a Assessment>,
	bias: Option<&'a Assessment>,
	squirrel: Option<&'a Assessment>
}

fn outcome(assessment: Option<&Assessment>) -> Outcome
{
	assessment.map(Assessment::outcome).unwrap_or(Outcome::Pass)
}

fn get_outcome(outcomes: &[Outcome]) -> Outcome
{
	outcomes.iter().min().map(|o| *o).unwrap_or(Outcome::Fail)
}

impl Components<'_>
{
	fn drift_outcome(&self) -> Outcome
	{
		outcome(self.drift)
	}

	fn bias_outcome(&self) -> Outcome
	{
		outcome(self.bias)
	}

	fn squirrel_outcome(&self) -> Outcome
	{
		outcome(self.squirrel)
	}

	fn outcomes(&self) -> [Outcome; 3]
	{
		[self.drift_outcome(), self.bias_outcome(), self.squirrel_outcome()]
	}

	pub fn outcome(&self) -> Outcome
	{
		get_outcome(&self.outcomes())		
	}
}

fn get_score(localisation_precision: f64, bias: Option<f64>, frc: f64) -> f64
{
	let num = 2.0 * localisation_precision;
	let denom = bias.map(|b| if b < frc {frc} else {b}).unwrap_or(frc);
	let value = num / denom;
	value * 100.0
}

fn get_outcome_from_score(score: f64, settings: &AssessmentSettings) -> Outcome
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

fn get_message(localisation_precision: Option<f64>) -> String
{
	let core = "% of max possible resolution.";
	if localisation_precision.is_none()
	{
		format!("{core} Localisation precision is assumed to be 20nm.")
	}
	else
	{
		core.to_string()
	}
}

pub fn assess_(localisation_precision: Option<f64>, bias: Option<f64>, frc: f64, settings: &AssessmentSettings) -> Assessment
{
	let precision = localisation_precision.unwrap_or(DEFAULT_LOCALISATION_PRECISION);
	let score = get_score(precision, bias, frc);
	let outcome = get_outcome_from_score(score, settings);
	let message = get_message(localisation_precision);
	let mut assessment = Assessment::new("Limiting Resolution", outcome, &message);
	assessment.set_score(score);
	assessment
}

pub fn assess(localisation_precision: Option<f64>, frc_res: Option<&Assessment>, drift: Option<&Assessment>, bias: Option<&Assessment>, squirrel: Option<&Assessment>, settings: &AssessmentSettings) -> Result<Assessment, String>
{
	if bias.is_none()
	{
		Err(format!("Missing bias report so ignoring"))
	}
	else if frc_res.is_none()
	{
		Err(format!("Missing frc report so ignoring"))
	}
	else 
	{
		let bias_score = bias.unwrap().score();
		let frc_score = frc_res.unwrap().score().unwrap();
		let mut assessment = assess_(localisation_precision, bias_score, frc_score, settings);
		let components = Components{drift, bias, squirrel};
		let colour_outcome = components.outcome();
		assessment.set_colour(colour_outcome);
		Ok(assessment)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn score_with_bias_max() 
	{
		assert_eq!(get_score(1.0, Some(2.0), 1.0), 100.0); // 2 / 2
	}

	#[test]
	fn score_with_frc_max() 
	{
		assert_eq!(get_score(1.0, Some(1.0), 2.0), 100.0); // 2 / 2
	}

	#[test]
	fn score_without_bias() 
	{
		assert_eq!(get_score(1.0, None, 1.0), 200.0); // 2 / 1
	}
}