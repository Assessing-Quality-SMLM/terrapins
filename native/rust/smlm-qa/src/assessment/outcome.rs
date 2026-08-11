#[derive(Debug, PartialEq, PartialOrd, Eq, Ord, Copy, Clone)]
pub enum Outcome
{
	Pass = 1,
	Fail = -1,
	Indeterminate = 0
}

impl Outcome
{
	pub fn to_str(&self) -> &str
	{
		match self
		{
			Self::Pass => "passed",
			Self::Fail => "failed",
			Self::Indeterminate => "indeterminate",
		}
	}
}


#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn pass_str()
	{
		assert_eq!(Outcome::Pass.to_str(), "passed");
	}

	#[test]
	fn indeterminate_str()
	{
		assert_eq!(Outcome::Indeterminate.to_str(), "indeterminate");
	}

	#[test]
	fn fail_str()
	{
		assert_eq!(Outcome::Fail.to_str(), "failed");
	}

	#[test]
	fn pass_greater_than_indeterminate() 
	{
		assert_eq!(Outcome::Pass > Outcome::Indeterminate, true)
	}

	#[test]
	fn indeterminate_greater_than_fail() 
	{
		assert_eq!(Outcome::Indeterminate > Outcome::Fail, true)
	}

	#[test]
	fn pass_greater_than_fail() 
	{
		assert_eq!(Outcome::Pass > Outcome::Fail, true)
	}

	#[test]
	fn at_least_as_good()
	{
		let outcomes = [Outcome::Indeterminate, Outcome::Pass];
		assert_eq!(outcomes.iter().all(|o| *o >= Outcome::Indeterminate), true);
	}
}