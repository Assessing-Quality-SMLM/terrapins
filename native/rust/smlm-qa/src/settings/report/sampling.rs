use super::{AssessmentSettings, RatioSettings};

#[derive(Debug, Clone, PartialEq)]
pub struct Settings
{
	ratio: RatioSettings,
	assessment_settings: AssessmentSettings,

}

impl Settings
{
	pub fn new(ratio: RatioSettings, assessment_settings: AssessmentSettings) -> Self
	{
		Self 
		{ 
			ratio, 
			assessment_settings 
		}
	}

	pub fn ratio(&self) -> &RatioSettings
	{
		&self.ratio
	}

	pub fn assessment(&self) -> &AssessmentSettings
	{
		&self.assessment_settings
	}
}

impl Default for Settings
{
	fn default() -> Self 
	{
	    Self::new(RatioSettings::sampling(), AssessmentSettings::default_sampling_settings())
	}
}