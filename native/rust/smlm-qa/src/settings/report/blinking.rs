use super::AssessmentSettings;

const DEFAULT_CORRELATION_VALUE: f64 = 0.5;

#[derive(Debug, Clone, PartialEq)]
pub struct Settings
{
	value: f64,
	assessment_settings: AssessmentSettings,
}

impl Settings
{
	pub fn new(value: f64, assessment_settings: AssessmentSettings) -> Self
	{
		Self{value, assessment_settings}
	}

	pub fn target(&self) -> f64
	{
		self.value
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
		Self::new(DEFAULT_CORRELATION_VALUE, AssessmentSettings::default_blink_settings())
	}
}