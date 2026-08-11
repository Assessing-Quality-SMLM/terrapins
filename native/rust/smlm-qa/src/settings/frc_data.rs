#[derive(Debug, Clone, Default, PartialEq)]
pub struct FRCData
{
	a_image: String,
	b_image: String
}

impl FRCData
{
	pub fn new(a_image: &str, b_image: &str) -> Self
	{
		Self
		{
			a_image: a_image.to_string(), 
			b_image: b_image.to_string()
		}
	}

	pub fn image_a(&self) -> &str
	{
		&self.a_image
	}

	pub fn set_image_a(&mut self, value: &str) -> ()
	{
		self.a_image = value.to_string();
	}

	pub fn image_b(&self) -> &str
	{
		&self.b_image
	}

	pub fn set_image_b(&mut self, value: &str) -> ()
	{
		self.b_image = value.to_string();
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn set_a()
	{
		let mut data = FRCData::default();
		assert_eq!(data.image_a(), "");
		data.set_image_a("something");
		assert_eq!(data.image_a(), "something");
	}

	#[test]
	fn set_b()
	{
		let mut data = FRCData::default();
		assert_eq!(data.image_b(), "");
		data.set_image_b("something");
		assert_eq!(data.image_b(), "something");
	}
}