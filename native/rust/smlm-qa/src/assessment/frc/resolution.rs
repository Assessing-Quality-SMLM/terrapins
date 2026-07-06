use super::{Assessment, Outcome};

use frc::{Resolution as FrcResolution};

const FRC_RESOLUTION_NAME : &str = "FRC Resolution";

#[derive(Debug)]
struct Components<'a>
{
	drift: Option<&'a Assessment>,
	mag: Option<&'a Assessment>,
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

	fn magnification_outcome(&self) -> Outcome
	{
		outcome(self.mag)
	}

	fn bias_outcome(&self) -> Outcome
	{
		outcome(self.bias)
	}

	fn squirrel_outcome(&self) -> Outcome
	{
		outcome(self.squirrel)
	}

	fn outcomes(&self) -> [Outcome; 4]
	{
		[self.drift_outcome(), self.magnification_outcome(), self.bias_outcome(), self.squirrel_outcome()]
	}

	pub fn outcome(&self) -> Outcome
	{
		get_outcome(&self.outcomes())		
	}
}

fn assess_(drift_score: f64, components: &Components) -> Assessment
{
	let outcome = components.outcome();
	let message = "Score is in nm";
	let mut assessment = Assessment::new(FRC_RESOLUTION_NAME, outcome, message);
	assessment.set_score(drift_score);
	assessment
}

pub fn assess(drift_split: &FrcResolution, pixel_size: f64, drift: Option<&Assessment>, mag: Option<&Assessment>, bias: Option<&Assessment>, squirrel: Option<&Assessment>) -> Assessment
{
	let score = drift_split.to_nm(pixel_size);
	let components = Components{drift, mag, bias, squirrel};	
	assess_(score, &components)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn empty_outcomes_is_fail() 
	{
		assert_eq!(get_outcome(&[]), Outcome::Fail);
	}

	#[test]
	fn any_intermediate_is_intermediate()
	{
		assert_eq!(get_outcome(&[Outcome::Pass, Outcome::Indeterminate]), Outcome::Indeterminate);
	}

	#[test]
	fn any_fail_is_fail()
	{
		assert_eq!(get_outcome(&[Outcome::Pass, Outcome::Indeterminate, Outcome::Fail]), Outcome::Fail);
	}

	#[test]
	fn empty_assessment_outcome_is_pass()
	{
		assert_eq!(outcome(None), Outcome::Pass)
	}

	#[test]
	fn basic_test()
	{
		let drift = Assessment::new("a", Outcome::Pass, "thing");
		let components = &Components { drift: Some(&drift), mag: None, bias: None, squirrel: None };
		let assessment = assess_(1.0, components);
		assert_eq!(assessment.score().unwrap(), 1.0);
		assert_eq!(assessment.outcome(), Outcome::Pass);
		assert_eq!(assessment.message(), "Score is in nm");
	}
}