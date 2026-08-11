pub use self::bias::{Settings as BiasSettings};
pub use self::blinking::{Settings as BlinkingSettings};
pub use self::ratio::{Settings as RatioSettings};
pub use self::sampling::{Settings as SamplingSettings};
pub use self::squirrel::{Settings as SquirrelSettings};

mod bias;
mod blinking;
mod sampling;
mod squirrel;
mod ratio;

const PASS: usize = 0;
const INDERTERMINATE: usize = 1;

#[derive(Debug, Clone, PartialEq)]
pub struct AssessmentSettings
{
	thresholds: [f64; 2],
	labels: [String; 3],
}

impl AssessmentSettings
{
	pub fn new(thresholds: [f64; 2], labels: [String; 3]) -> Self
	{
		Self
		{
			thresholds,
			labels
		}
	}

	pub fn default_drift_settings() -> Self
	{
		Self
		{
			thresholds : [0.9, 0.8],
			labels: ["Pass".to_string(), "Partial".to_string(), "Fail".to_string()]
		}
	}

	pub fn default_blink_settings() -> Self
	{
		Self
		{
			thresholds : [0.75, 0.5],
			labels: ["Pass".to_string(), "Partial".to_string(), "Fail".to_string()]
		}
	}

	pub fn default_sampling_settings() -> Self
	{
		Self
		{
			thresholds : [0.3, 0.1],
			labels: ["High".to_string(), "Medium".to_string(), "Low".to_string()]
		}
	}

	pub fn default_magnification_settings() -> Self
	{
		Self
		{
			thresholds: [0.2, 0.4],
			labels: ["Pass".to_string(), "Too High".to_string(), "Too Low".to_string()]
		}
	}

	pub fn default_bias_settings() -> Self
	{
		Self
		{
			thresholds: [2.0, 4.0],
			labels: ["Pass".to_string(), "Partial".to_string(), "Fail".to_string()]
		}
	}

	pub fn default_squirrel_settings() -> Self
	{
		Self
		{
			thresholds: [0.93, 0.85],
			labels: ["Pass".to_string(), "Partial".to_string(), "Fail".to_string()]
		}
	}

	pub fn default_localisation_precision_settings() -> Self
	{
		Self
		{
			thresholds: [2.0, 0.0],
			labels: ["Pass".to_string(), "Partial".to_string(), "Fail".to_string()]
		}
	}

	pub fn default_frc_resolution_assessment() -> Self
	{
		Self
		{
			thresholds: [0.0, 0.0],
			labels: ["Accurate".to_string(), "Suspect".to_string(), "Biased".to_string()]
		}
	}

	pub fn default_limiting_precision_settings() -> Self
	{
		Self
		{
			thresholds: [0.66, 0.33],
			labels: ["Good".to_string(), "Fair".to_string(), "Poor".to_string()]
		}
	}

	pub fn thresholds(&self) -> &[f64; 2]
	{
		&self.thresholds
	}

	pub fn labels(&self) -> &[String; 3]
	{
		&self.labels
	}

	pub fn pass_threshold(&self) -> f64
	{
		self.get_threshold(PASS)
	}

	pub fn inderterminate_threshold(&self) -> f64
	{
		self.get_threshold(INDERTERMINATE)
	}

	fn get_threshold(&self, idx: usize) -> f64
	{
		self.thresholds[idx]
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct Settings
{
	drift_settings: AssessmentSettings,
	blinking: BlinkingSettings,
	sampling_settings: SamplingSettings,
	magnification: AssessmentSettings,
	bias: BiasSettings,
	squirrel: SquirrelSettings,
	localisation_precision: AssessmentSettings,
	limiting_resolution: AssessmentSettings,
}

impl Settings
{
	pub fn drift_settings(&self) -> &AssessmentSettings
	{
		&self.drift_settings
	}

	pub fn with_drift_settings(mut self, value: AssessmentSettings) -> Self
	{
		self.drift_settings = value;
		self
	}

	pub fn sampling_settings(&self) -> &SamplingSettings
	{
		&self.sampling_settings
	}

	pub fn with_sampling_settings(mut self, value: SamplingSettings) -> Self
	{
		self.sampling_settings = value;
		self
	}

	pub fn magnification(&self) -> &AssessmentSettings
	{
		&self.magnification
	}

	pub fn with_magnification(mut self, value: AssessmentSettings) -> Self
	{
		self.magnification = value;
		self
	}
	
	pub fn blinking(&self) -> &BlinkingSettings
	{
		&self.blinking
	}

	pub fn with_blinking_settings(mut self, value: BlinkingSettings) -> Self
	{
		self.blinking = value;
		self
	}

	pub fn bias(&self) -> &BiasSettings
	{
		&self.bias
	}

	pub fn with_bias(mut self, value: BiasSettings) -> Self
	{
		self.bias = value;
		self
	}

	pub fn squirrel(&self) -> &SquirrelSettings
	{
		&self.squirrel
	}

	pub fn with_squirrel(mut self, value: SquirrelSettings) -> Self
	{
		self.squirrel = value;
		self
	}

	pub fn localisation_precision_settings(&self) -> &AssessmentSettings
	{
		&self.localisation_precision
	}

	pub fn with_localisation_precision(mut self, value: AssessmentSettings) -> Self
	{
		self.localisation_precision = value;
		self
	}

	pub fn limiting_resolution_settings(&self) -> &AssessmentSettings
	{
		&self.limiting_resolution
	}

	pub fn with_limiting_resolution(mut self, value: AssessmentSettings) -> Self
	{
		self.limiting_resolution = value;
		self
	}
}

impl Default for Settings
{
	fn default() -> Self 
	{
		Settings 
		{
			drift_settings: AssessmentSettings::default_drift_settings(),
			blinking: BlinkingSettings::default(),
			sampling_settings: SamplingSettings::default(),
			magnification: AssessmentSettings::default_magnification_settings(),
			bias: BiasSettings::default(),
			squirrel: SquirrelSettings::default(),
			localisation_precision: AssessmentSettings::default_localisation_precision_settings(),
			limiting_resolution: AssessmentSettings::default_limiting_precision_settings()
		}
	}
}