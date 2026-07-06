const DEFAULT_REGISTRATION: bool = true;
const DEFAULT_WF_BORDER_PX: u32 = 2;
const DEFAULT_SHOW_POSITIVE_NEGATIVE: bool = false;
const DEFAULT_OPTIMISER : &str = "LN_NELDERMEAD";
const DEFAULT_PACTHWISE: bool = false;
const DEFAULT_PATCH_SIZE: u8 = 32;
const DEFAULT_STEP_SIZE: u8 = 16;

#[derive(Debug, Clone, PartialEq)]
pub struct SquirrelSettings 
{
	registration: bool,
	registration_method: Option<String>,
	wf_border_px: u32,
	optimiser_algorithm: Option<String>,
	three_parameter_solver: bool,
	show_positive_negative: bool,
	patchwise : bool,
	patch_size : u8,
	step_size : u8,
}

impl SquirrelSettings
{
	pub fn new() -> Self
	{
		Self
		{
			registration: DEFAULT_REGISTRATION,
			registration_method: None,
			wf_border_px: DEFAULT_WF_BORDER_PX,
			optimiser_algorithm: None,
			three_parameter_solver: false,
			show_positive_negative: DEFAULT_SHOW_POSITIVE_NEGATIVE,
			patchwise: DEFAULT_PACTHWISE,
			patch_size: DEFAULT_PATCH_SIZE,
			step_size: DEFAULT_STEP_SIZE
		}
	}

	pub fn registration(&self) -> bool
	{
		self.registration
	}

	pub fn set_registration(&mut self, value: bool) -> ()
	{
		self.registration = value;
	}

	pub fn registration_method(&self) -> Option<&str>
	{
		self.registration_method.as_ref().map(|s| s.as_str())
	}

	pub fn set_registration_method(&mut self, value: &str) -> ()
	{
		self.registration_method = Some(value.to_string())
	}

	pub fn border_wf_px(&self) -> u32
	{
		self.wf_border_px
	}

	pub fn set_border_in_wf_px(&mut self, value: u32) -> ()
	{
		self.wf_border_px = value;
	}

	pub fn optimiser_algorithm(&self) -> &str
	{
		self.optimiser_algorithm.as_ref().map(|s| s.as_str()).unwrap_or(DEFAULT_OPTIMISER)
	}

	pub fn set_optimisation_algorithm(&mut self, value: &str)
	{
		self.optimiser_algorithm = Some(value.to_string());
	}

	pub fn three_parameter_solve(&self) -> bool
	{
		self.three_parameter_solver
	}

	pub fn set_three_parameter_solve(&mut self, value: bool) -> ()
	{
		self.three_parameter_solver = value;
	}

	pub fn show_positive_negative(&self) -> bool
	{
		self.show_positive_negative
	}

	pub fn set_show_positive_negative(&mut self, value: bool) -> ()
	{
		self.show_positive_negative = value;
	}

	pub fn patchwise(&self) -> bool
	{
		self.patchwise
	}

	pub fn set_patchwise(&mut self, value: bool) -> ()
	{
		self.patchwise = value;
	}

	pub fn patch_size(&self) -> u8
	{
		self.patch_size
	}

	pub fn set_patch_size(&mut self, value: u8) -> ()
	{
		self.patch_size = value;
	}

	pub fn step_size(&self) -> u8
	{
		self.step_size
	}

	pub fn set_step_size(&mut self, value: u8) -> ()
	{
		self.step_size = value;
	}
}

impl Default for SquirrelSettings
{
	fn default() -> Self 
	{
		Self::new()
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn can_set_registration_method() 
	{
		let mut settings = SquirrelSettings::default();
		assert_eq!(settings.registration_method(), None);
		settings.set_registration_method("something");
		assert_eq!(settings.registration_method(), Some("something"));
	}
}