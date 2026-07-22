use super::AssessmentSettings;

#[derive(Debug, Clone, PartialEq)]
pub struct Settings
{
	assessment_settings: AssessmentSettings
}

impl Settings
{
	pub fn new(assessment_settings: AssessmentSettings) -> Self
	{
		Self{assessment_settings}
	}

	pub fn assessment_settings(&self) -> &AssessmentSettings
	{
		&self.assessment_settings
	}
}

impl Default for Settings
{
	fn default() -> Self
	{
		Self::new(AssessmentSettings::default_squirrel_settings())
	}
}