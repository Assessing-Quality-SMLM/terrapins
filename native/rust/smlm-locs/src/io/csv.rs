use super::{parse_u32, parse_f64, Parser, ParserFactory};

use crate::{AllocatedLocalisation, LocalisationBuilder as Builder};

const DEFAULT_DELIM : char = ',';

#[derive(Debug, Clone, PartialEq)]
pub struct Settings 
{
	n_header_lines : usize,
	delim: char,
	x: u8,
	y: u8,
	frame_number: Option<u8>,
	sigma: Option<u8>,
	uncertainty: Option<u8>
}

impl Settings
{
	fn new(n_header_lines: usize, delim: char, x_pos: u8, y_pos: u8, frame_number: Option<u8>, sigma_pos: Option<u8>, uncertainty_pos: Option<u8>) -> Self
	{
		Self
		{
			n_header_lines,
			delim,
			x: x_pos,
			y: y_pos,
			frame_number,
			sigma: sigma_pos,
			uncertainty: uncertainty_pos
		}
	}

	pub fn n_header_lines(&self) -> usize
	{
		self.n_header_lines
	}

	pub fn delim(&self) -> char
	{
		self.delim
	}

	pub fn x_position(&self) -> usize
	{
		self.x as usize
	}

	pub fn y_position(&self) -> usize
	{
		self.y as usize
	}

	pub fn frame_number_position(&self) -> Option<usize>
	{
		self.frame_number.map(|v| v as usize)
	}

	pub fn sigma_position(&self) -> Option<usize>
	{
		self.sigma.map(|v| v as usize)
	}

	pub fn uncertainty_position(&self) -> Option<usize>
	{
		self.uncertainty.map(|v| v as usize) // upscaling
	}

	pub fn max_column(&self) -> usize
	{
		match self.uncertainty_position()
		{
			None => std::cmp::max(self.x_position(), self.y_position()),
			Some(v) => *[self.x_position(), self.y_position(), v].iter().max().unwrap() // iterator never empty so safe to unwrap
		}
	}

	pub fn with_delimeter(mut self, value: char) -> Self
	{
		self.delim = value;
		self
	}

	pub fn with_n_headers(mut self, value: usize) -> Self
	{
		self.n_header_lines = value;
		self
	}

	pub fn with_x_pos(mut self, value: u8) -> Self
	{
		self.x = value;
		self
	}

	pub fn with_y_pos(mut self, value: u8) -> Self
	{
		self.y = value;
		self
	}

	pub fn with_frame_number_pos(mut self, value: Option<u8>) -> Self
	{
		self.frame_number = value;
		self
	}

	pub fn with_sigma_pos(mut self, value: Option<u8>) -> Self
	{
		self.sigma = value;
		self
	}

	pub fn with_uncertainty_pos(mut self, value: Option<u8>) -> Self
	{
		self.uncertainty = value;
		self
	}
}

impl Default for Settings
{
	fn default() -> Self
	{
		Self::new(0, DEFAULT_DELIM, 0, 1, None, None, None)
	}
}


impl ParserFactory for Settings
{
	type Parser = CsvParser;
	type Error = String;
	
	fn create(&self) -> Result<Self::Parser, Self::Error>
	{
		Ok(CsvParser::new(self.clone()))
	}

	fn from_header(&self, _header: &str) -> Result<Self::Parser, Self::Error>
	{
		self.create()
	}

	fn n_header_lines(&self) -> usize
	{
		self.n_header_lines()
	}
}


pub fn parse_line(line: &str, settings: &Settings) -> Result<AllocatedLocalisation, String>
{
	let splits : Vec<&str> = line.split(settings.delim()).collect();
	if splits.len() < settings.max_column()
	{
		let error = format!("{:?} does not contain enough columns: got {} required {} indicies", splits, splits.len(), settings.max_column());
		Err(error)
	}
	else
	{
		let parse = |idx: usize| parse_f64(splits[idx]);
		let x = parse(settings.x_position())?;
		let y = parse(settings.y_position())?;
		let mut builder = Builder::new(x, y);
		builder = 
			match settings.frame_number_position()
			{
				Some(v) => builder.with_frame_number(parse_u32(splits[v])?),
				None => builder
			};
		builder = 
			match settings.sigma_position()
			{
				Some(v) => builder.with_sigma(parse(v)?),
				None => builder
			};
		builder = 
			match settings.uncertainty_position()
			{
				Some(v) => builder.with_uncertainty(parse(v)?),
				None => builder
			};
		Ok(builder.build())
	}
}

#[derive(Debug, Clone)]
pub struct CsvParser 
{
	settings: Settings
}

impl CsvParser
{
	pub fn new(settings: Settings) -> Self
	{
		Self{settings}
	}
}

impl Parser for CsvParser
{
	type Localisation = AllocatedLocalisation;
	type Error = String;

	fn parse_line(&self, line: &str) -> Result<Self::Localisation, Self::Error>
	{
		parse_line(line, &self.settings)
	}
}

impl TryFrom<&str> for CsvParser
{
	type Error = String;
	fn try_from(_value: &str) -> Result<Self, Self::Error>
	{
		Err(format!("Cannot construct csv parser from string"))
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;
	
	#[test]
	fn default_settings_parse_test() 
	{
		let data = "1,2,3";
		let settings = Settings::default();
		let localisation = parse_line(data, &settings).unwrap();
		assert_eq!(localisation.x(), 1.0);
		assert_eq!(localisation.y(), 2.0);
		assert_eq!(localisation.sigma(), 20.0);
		assert_eq!(localisation.uncertainty(), 20.0);
	}

	#[test]
	fn basic_parse_test() 
	{
		let data = "1,2,3";
		let settings = Settings::default().with_uncertainty_pos(Some(2));
		let localisation = parse_line(data, &settings).unwrap();
		assert_eq!(localisation.x(), 1.0);
		assert_eq!(localisation.y(), 2.0);
		assert_eq!(localisation.uncertainty(), 3.0);
	}

	#[test]
	fn not_enough_columns() 
	{
		let data = "1,2,3";
		let settings = Settings::default().with_x_pos(10);
		let error = parse_line(data, &settings).unwrap_err();
		assert_eq!(error, "[\"1\", \"2\", \"3\"] does not contain enough columns: got 3 required 10 indicies")
	}

	#[test]
	fn configured_for_not_enough_columns() 
	{
		let data = "1,2,3";
		let settings = Settings::default().with_uncertainty_pos(None);
		let localisation = parse_line(data, &settings).unwrap();
		assert_eq!(localisation.x(), 1.0);
		assert_eq!(localisation.y(), 2.0);
		assert_eq!(localisation.uncertainty(), 20.0);
	}

	#[test]
	fn sigma_has_default_value() 
	{
		let data = "1,2";
		let settings = Settings::default().with_sigma_pos(None).with_uncertainty_pos(None);
		let localisation = parse_line(data, &settings).unwrap();
		assert_eq!(localisation.x(), 1.0);
		assert_eq!(localisation.y(), 2.0);
		assert_eq!(localisation.sigma(), 20.0);
		assert_eq!(localisation.uncertainty(), 20.0);
	}

	#[test]
	fn uncertainty_has_default_value() 
	{
		let data = "1,2";
		let settings = Settings::default().with_sigma_pos(None).with_uncertainty_pos(None);
		let localisation = parse_line(data, &settings).unwrap();
		assert_eq!(localisation.x(), 1.0);
		assert_eq!(localisation.y(), 2.0);
		assert_eq!(localisation.sigma(), 20.0);
		assert_eq!(localisation.uncertainty(), 20.0);
	}

	#[test]
	fn frame_number_has_default_value() 
	{
		let data = "1,2";
		let settings = Settings::default().with_frame_number_pos(None).with_sigma_pos(None).with_uncertainty_pos(None);
		let localisation = parse_line(data, &settings).unwrap();
		assert_eq!(localisation.frame_number(), 0);
		assert_eq!(localisation.x(), 1.0);
		assert_eq!(localisation.y(), 2.0);
		assert_eq!(localisation.sigma(), 20.0);
		assert_eq!(localisation.uncertainty(), 20.0);
	}

	#[test]
	fn parsing_junk() 
	{
		let data = "junk,2,3";
		let error = parse_line(data, &Settings::default()).unwrap_err();
		assert_eq!(error, "cannot parse junk as f64 - invalid float literal");
	}

	#[test]
	fn out_of_range() 
	{
		let input = "1.18973e+4932,2145.33857,104.22976";
		assert_eq!(parse_line(input, &Settings::default()).unwrap_err(), "cannot parse 1.18973e+4932 to f64 - out of range")
	}
}