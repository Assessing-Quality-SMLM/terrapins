use super::AssessmentSettings;

const DEFAULT_SCORE_THRESHOLD: f64 = 0.95;

#[derive(Debug, Clone, PartialEq)]
pub struct Settings
{
	global_score_threshold: f64,
	assessment_settings: AssessmentSettings
}

impl Settings
{
	pub fn new(global_score_threshold: f64, assessment_settings: AssessmentSettings) -> Self
	{
		Self
		{
			global_score_threshold, 
			assessment_settings
		}
	}

	pub fn threshold(&self) -> f64
	{
		self.global_score_threshold
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
		Self::new(DEFAULT_SCORE_THRESHOLD, AssessmentSettings::default_bias_settings())
	}
}