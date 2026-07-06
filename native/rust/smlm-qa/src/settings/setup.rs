use super::{FrcSettings, HawkmanSettings, SquirrelSettings};

#[derive(Debug, Clone, PartialEq)]
pub struct Setup 
{
	// equipment: EquipmentSettings,
	// recon_settings: ReconSettings,
	frc: FrcSettings,
	hawkman: HawkmanSettings,
	squirrel: SquirrelSettings,
	n_threads: u32,
}

impl Setup
{
	

	pub fn frc_settings(&self) -> &FrcSettings
	{
		&self.frc
	}

	pub fn with_frc_settings(mut self, value: FrcSettings) -> Self
	{
		self.frc = value;
		self
	}

	pub fn hawkman_settings(&self) -> &HawkmanSettings
	{
		&self.hawkman
	}

	pub fn with_hawkman_settings(mut self, value: HawkmanSettings) -> Self
	{
		self.hawkman = value;
		self
	}

	pub fn set_hawkman_n_levels(&mut self, value: u32)
	{
		self.hawkman.set_n_levels(value)
	}

	pub fn squirrel_settings(&self) -> &SquirrelSettings
	{
		&self.squirrel
	}

	pub fn with_squirrel_settings(mut self, value: SquirrelSettings) -> Self
	{
		self.squirrel = value;
		self
	}

	pub fn set_border_in_wf_px(&mut self, value: u32)
	{
		self.squirrel.set_border_in_wf_px(value)
	}

	pub fn set_squirrel_registration(&mut self, value: bool)
	{
		self.squirrel.set_registration(value)
	}

	pub fn n_threads(&self) -> u32
	{
		self.n_threads
	}

	pub fn set_n_threads(&mut self, value: u32) -> ()
	{
		self.n_threads = value;
	}

	pub fn with_n_threads(mut self, value: u32) -> Self
	{
		self.n_threads = value;
		self
	}
}

impl Default for Setup
{
	fn default() -> Self 
	{
		Self
		{
			// equipment: EquipmentSettings::default(),
			// recon_settings: ReconSettings::default(),
			frc: FrcSettings::default(),
			hawkman: HawkmanSettings::default(),
			squirrel: SquirrelSettings::default(),
			n_threads: super::DEFAULT_N_THREADS
		}    
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn can_set_n_threads() 
	{
		let mut setup = Setup::default();
		assert_eq!(setup.n_threads(), 4);
		setup.set_n_threads(10);
		assert_eq!(setup.n_threads(), 10);
	}

	#[test]
	fn can_set_hawkman_n_levels() 
	{
		let mut setup = Setup::default();
		assert_eq!(setup.hawkman.n_levels(), 10);
		setup.set_hawkman_n_levels(20);
		assert_eq!(setup.hawkman.n_levels(), 20);
	}
}