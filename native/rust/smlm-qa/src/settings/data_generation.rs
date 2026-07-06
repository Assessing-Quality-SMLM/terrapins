use super::{LocalisationData};

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Settings
{	
	// organised in acquisition order
	widefield: Option<String>,
	image_stack: Option<String>,

	localisation_file: Option<LocalisationData>,
	hawk_localisation_file: Option<LocalisationData>,	
}

impl Settings
{	
	pub fn is_empty(&self) -> bool
	{		

		self.widefield.is_none() &&
		self.image_stack.is_none() &&

		self.localisation_file.is_none() &&
		self.hawk_localisation_file.is_none()
	}

	pub fn widefield(&self) -> Option<&str>
	{
		self.widefield.as_ref().map(|s| s.as_str())
	}

	pub fn set_widefield(&mut self, value: &str) -> ()
	{
		self.widefield = Some(value.to_string());
	}

	pub fn with_widefield(mut self, value: &str) -> Self
	{
		self.widefield = Some(value.to_string());
		self
	}

	pub fn image_stack(&self) -> Option<&str>
	{
		self.image_stack.as_ref().map(|s| s.as_str())
	}

	pub fn set_image_stack(&mut self, value: &str) -> ()
	{
		self.image_stack = Some(value.to_string());
	}

	pub fn with_image_stack(mut self, value: &str) -> Self
	{
		self.image_stack = Some(value.to_string());
		self
	}

	pub fn localisation_data(&self) -> Option<&LocalisationData>
	{
		self.localisation_file.as_ref()
	}

	pub fn set_localisation_data(&mut self, value: LocalisationData) -> ()
	{
		self.localisation_file = Some(value);
	}

	pub fn localisation_file(&self) -> Option<&str>
	{
		self.localisation_file.as_ref().map(|l| l.filepath())
	}

	pub fn with_localisation_data(mut self, value: LocalisationData) -> Self
	{
		self.localisation_file = Some(value);
		self
	}

	pub fn hawk_localisation_data(&self) -> Option<&LocalisationData>
	{
		self.hawk_localisation_file.as_ref()
	}

	pub fn set_hawk_localisation_data(&mut self, value: LocalisationData) -> ()
	{
		self.hawk_localisation_file = Some(value);
	}

	pub fn hawk_localisation_file(&self) -> Option<&str>
	{
		self.hawk_localisation_file.as_ref().map(|l| l.filepath())
	}

	pub fn with_hawk_localisation_data(mut self, value: LocalisationData) -> Self
	{
		self.hawk_localisation_file = Some(value);
		self
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn is_empty() 
	{
		assert_eq!(Settings::default().is_empty(), true);
	}

	#[test]
	fn not_empty() 
	{
		assert_eq!(Settings::default().with_localisation_data(LocalisationData::from_filepath("")).is_empty(), false);
		assert_eq!(Settings::default().with_hawk_localisation_data(LocalisationData::from_filepath("")).is_empty(), false);
		assert_eq!(Settings::default().with_image_stack("").is_empty(), false);
		assert_eq!(Settings::default().with_widefield("").is_empty(), false);
	}
}