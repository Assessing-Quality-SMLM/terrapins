use super::outcome::{Outcome};

use std::fmt::{Display, Formatter};
use std::io::{Write, Error as IoError};

#[derive(Debug, PartialEq, Clone)]
pub struct Assessment
{
	name: String,
	score: Option<f64>,
	outcome: Outcome,
	message: String,
	colour: Option<Outcome>,
}

impl Assessment
{
	pub fn new(name: &str, outcome: Outcome, message: &str) -> Self
	{
		Self
		{
			name: name.to_string(), 
			score: None,
			outcome, 
			message: message.to_string(),
			colour: None,
		}
	}

	pub fn pass_with(name: &str, message: &str) -> Self
	{
		Self::new(name, Outcome::Pass, message)
	}

	pub fn fail_with(name: &str, message: &str) -> Self
	{
		Self::new(name, Outcome::Fail, message)
	}

	pub fn inderterminate_with(name: &str, message: &str) -> Self
	{
		Self::new(name, Outcome::Indeterminate, message)
	}

	pub fn pass_fail(name: &str, pass: bool, message: &str) -> Self
	{
		let outcome = if pass{Outcome::Pass} else {Outcome::Fail};
		Self::new(name, outcome, message)
	}

	fn name(&self) -> &str
	{
		&self.name
	}

	pub fn score(&self) -> Option<f64>
	{
		self.score
	}

	pub fn set_score(&mut self, value: f64) -> ()
	{
		self.score = Some(value);
	}

	pub fn outcome(&self) -> Outcome
	{
		self.outcome
	}

	pub fn passed(&self) -> bool
	{
		self.outcome == Outcome::Pass
	}

	pub fn failed(&self) -> bool
	{
		self.outcome == Outcome::Fail
	}

	pub fn indeterminate(&self) -> bool
	{
		self.outcome == Outcome::Indeterminate
	}

	pub fn set_passed(&mut self) -> ()
	{
		self.set_outcome(Outcome::Pass);
	}

	pub fn set_failed(&mut self) -> ()
	{
		self.set_outcome(Outcome::Fail);
	}

	fn set_outcome(&mut self, value: Outcome) -> ()
	{
		self.outcome = value
	}

	fn outcome_string<'a>(&self, dictionary: &[String; 3]) -> String
	{
		let label = match self.outcome
		{
			Outcome::Pass => &dictionary[0],
			Outcome::Indeterminate => &dictionary[1],
			Outcome::Fail => &dictionary[2],
		};
		format!("{},{label}", self.outcome.to_str())
	}

	pub fn message(&self) -> &str
	{
		&self.message
	}

	pub fn append_message(&mut self, value: &str)
	{
		self.message.push_str(value);
	}

	fn colour_string(&self) -> Option<String>
	{
		self.colour.map(|c| format!("colour,{}", c.to_str()))
	}

	pub fn set_colour(&mut self, value: Outcome) -> ()
	{
		self.colour = Some(value);
	}

	pub fn section_string(&self, dictionary: &[String; 3]) -> String
	{
		let string_score = self.score().map(|s| s.to_string()).unwrap_or("-".to_string());
		let outcome_string = self.outcome_string(dictionary);
		match self.colour_string()
		{
			None => format!("name,{}\nscore,{string_score}\nresult,{outcome_string}\nmessage,{}", self.name(), self.message()),
			Some(colour_string) => 
			{
				format!("name,{}\nscore,{string_score}\nresult,{outcome_string}\n{colour_string}\nmessage,{}", self.name(), self.message())
			}
		}
	}

	pub fn write_as_sections<W: Write>(&self, mut writer: W, dictionary: &[String; 3]) -> Result<(), IoError>
	{
		write!(writer, "{}", self.section_string(dictionary))
	}

	pub fn write_to<W: Write>(&self, mut writer: W) -> Result<(), IoError>
	{
		write!(writer, "{}", self.to_string())
	}
}

impl Display for Assessment
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), std::fmt::Error>
	{
		let outcome = self.outcome.to_str();
		match self.score
		{
			None => 
			{
				write!(f, "{}: {outcome}\n{}", self.name(), self.message())
			}
			Some(score) => 
			{
				write!(f, "{} ({score}): {outcome}\n{}", self.name(), self.message())

			}
		}
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;
	
	fn standard_dictionary() -> [String; 3] 
	{
		["Pass".to_string(), "Partial".to_string(), "Fail".to_string()]
	}

	#[test]
	fn score_written_if_present() 
	{
		let mut assessment = Assessment::pass_with("something", "a thing");
		assert_eq!(assessment.to_string(), "something: passed\na thing");
		assessment.set_score(1.23);
		assert_eq!(assessment.to_string(), "something (1.23): passed\na thing");
	}

	#[test]
	fn write_in_sections()
	{
		let mut assessment = Assessment::pass_with("something","a thing");
		assert_eq!(assessment.section_string(&standard_dictionary()), "name,something\nscore,-\nresult,passed,Pass\nmessage,a thing");
		assessment.set_score(1.23);
		assert_eq!(assessment.section_string(&standard_dictionary()), "name,something\nscore,1.23\nresult,passed,Pass\nmessage,a thing");
	}

	#[test]
	fn pass_is_written_as_pass() 
	{
		let mut assessment = Assessment::pass_with("something", "a thing");
		assert_eq!(assessment.to_string(), "something: passed\na thing");
		assessment.set_score(1.23);
		assert_eq!(assessment.to_string(), "something (1.23): passed\na thing");
	}

	#[test]
	fn fail_is_written_as_fail() 
	{
		let mut assessment = Assessment::fail_with("something", "a thing");
		assert_eq!(assessment.to_string(), "something: failed\na thing");
		assessment.set_score(1.23);
		assert_eq!(assessment.to_string(), "something (1.23): failed\na thing");
	}

	#[test]
	fn indeterminate_is_written_as_inderterminate() 
	{
		let mut assessment = Assessment::inderterminate_with("something", "a thing");
		assert_eq!(assessment.to_string(), "something: indeterminate\na thing");
		assessment.set_score(1.23);
		assert_eq!(assessment.to_string(), "something (1.23): indeterminate\na thing");
	}

	#[test]
	fn append_message_test()
	{
		let mut assessment = Assessment::pass_with("something", "a thing");
		assert_eq!(assessment.message(), "a thing");
		assessment.append_message(" more stuff");
		assert_eq!(assessment.message(), "a thing more stuff");
	}

	#[test]
	fn colour_string_output_if_have_it()
	{
		let mut assessment = Assessment::pass_with("something", "a thing");
		assessment.set_colour(Outcome::Pass);
		assert_eq!(assessment.section_string(&standard_dictionary()), "name,something\nscore,-\nresult,passed,Pass\ncolour,passed\nmessage,a thing");
	}

	#[test]
	fn magnification_report_too_high_test()
	{
		let magnification_dictionary = ["Pass".to_string(), "Too High".to_string(), "Too Low".to_string()];
		let outcome = Outcome::Indeterminate;
		let mut assessment = Assessment::new("something", outcome, "else");
		assessment.set_colour(Outcome::Indeterminate);
		let expected = "name,something\nscore,-\nresult,indeterminate,Too High\ncolour,indeterminate\nmessage,else";
		assert_eq!(assessment.section_string(&magnification_dictionary), expected);
	}

	#[test]
	fn magnification_report_too_low_test()
	{
		let magnification_dictionary = ["Pass".to_string(), "Too High".to_string(), "Too Low".to_string()];
		let outcome = Outcome::Fail;
		let mut assessment = Assessment::new("something", outcome, "else");
		assessment.set_colour(Outcome::Indeterminate);
		let expected = "name,something\nscore,-\nresult,failed,Too Low\ncolour,indeterminate\nmessage,else";
		assert_eq!(assessment.section_string(&magnification_dictionary), expected);
	}
}