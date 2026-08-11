use super::{parse_u32, parse_f64, Parser, ParserFactory};

use crate::{AllocatedLocalisation};

const FRAME_NUMBER : &str ="frame";
const X : &str ="x";
const Y : &str ="y";
const SIGMA : &str ="sigma";
const INTENSITY : &str ="intensity";
const UNCERTAINTY_XY : &str ="uncertainty_xy";
const UNCERTAINTY : &str ="uncertainty";
// const THUNDERSTORM_HEADER : &str ="\"id\",\"frame\",\"x [nm]\",\"y [nm]\",\"sigma [nm]\",\"intensity [photon]\",\"offset [photon]\",\"bkgstd [photon]\",\"chi2\",\"uncertainty_xy [nm]\"";
const N_HEADER_LINES: usize = 1;

#[derive(Debug)]
struct Header
{
	frame_number_idx: usize,
	x_idx: usize,
	y_idx: usize,
	sigma_idx: usize,
	intensity_idx: usize,
	uncertainty_idx: usize
}

impl Header
{
	pub fn new(frame_number_idx: usize, x_idx: usize, y_idx: usize, sigma_idx: usize, intensity_idx: usize, uncertainty_idx: usize) -> Self
	{
		Self{frame_number_idx, x_idx, y_idx, sigma_idx, intensity_idx, uncertainty_idx}
	}

	pub fn frame_number_idx(&self) -> usize
	{
		self.frame_number_idx
	}

	pub fn x_idx(&self) -> usize
	{
		self.x_idx
	}

	pub fn y_idx(&self) -> usize
	{
		self.y_idx
	}

	pub fn sigma_idx(&self) -> usize
	{
		self.sigma_idx
	}

	pub fn intensity_idx(&self) -> usize
	{
		self.intensity_idx
	}

	pub fn uncertainty_idx(&self) -> usize
	{
		self.uncertainty_idx
	}

}

fn matches(field: &str, label: &str) -> bool
{
	let mut words = field.split(" ");	
	words.any(|w| w.trim_matches('\"') == label)
}

fn get_column(splits: &Vec<&str>, label: &str, header: &str) -> Result<usize, String>
{
	splits.iter().position(|x| matches(x,label)).ok_or_else(|| format!("Cannot find {label} in {header}"))
}

fn get_columns(header: &str) -> Result<Header, String>
{
	let splits = header.split(",").collect::<Vec<&str>>();
	let frame_number = get_column(&splits, FRAME_NUMBER, header)?;
	let x_nm = get_column(&splits, X, header)?;
	let y_nm = get_column(&splits, Y, header)?;
	let sigma_nm = get_column(&splits, SIGMA, header)?;
	let intensity_photon = get_column(&splits, INTENSITY, header)?;
	let uncertainty_nm = get_column(&splits, UNCERTAINTY, header).or_else(|e_1| get_column(&splits, UNCERTAINTY_XY, header).map_err(|e_2| format!("{e_1} or {e_2}")))?;

	Ok(Header::new(frame_number, x_nm, y_nm, sigma_nm, intensity_photon, uncertainty_nm))
}



fn parse_line(line: &str, header: &Header) -> Result<AllocatedLocalisation, String>
{	
	let splits = line.split(",").collect::<Vec<&str>>();
	let parse = |idx: usize| parse_f64(splits[idx]);
	let frame_number = parse_u32(splits[header.frame_number_idx()])?;
	let x = parse(header.x_idx())?;
	let y = parse(header.y_idx())?;
	let sigma = parse(header.sigma_idx())?;
	let intensity = parse(header.intensity_idx())?;
	let uncertainty = parse(header.uncertainty_idx())?;
	Ok(AllocatedLocalisation::new(frame_number, x, y, sigma, intensity, uncertainty))
}

#[derive(Debug)]
pub struct ThunderStormParserFactory;

impl ParserFactory for ThunderStormParserFactory
{
	type Parser = ThunderStormParser;
	type Error = String;
	fn create(&self) -> Result<Self::Parser, Self::Error>
	{
		Err(format!("Cannot create Thunderstorm Parser without a header"))
	}

	fn from_header(&self, header: &str) -> Result<Self::Parser, Self::Error>
	{
		get_columns(header).map(ThunderStormParser::new)
	}

	fn n_header_lines(&self) -> usize
	{
		1
	}
}

#[derive(Debug)]
pub struct ThunderStormParser
{
	header: Header
}

impl ThunderStormParser
{
	fn new(header: Header) -> Self
	{
		Self{header}
	}

	pub fn n_header_lines() -> usize
	{
		N_HEADER_LINES
	}
}

impl Parser for ThunderStormParser
{
	type Localisation = AllocatedLocalisation;
	type Error = String;

	fn parse_line(&self, line: &str) -> Result<Self::Localisation, Self::Error>
	{
		parse_line(line, &self.header)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;
	use crate::io;

	#[test]
	fn match_ignores_units()
	{
		let field = "something [unit]";
		assert!(matches(field, "something"));
	}

	#[test]
	fn match_ignores_wrapping_quotes()
	{
		let field = "\"something [unit]\"";
		assert!(matches(field, "something"));
	}

	#[test]
	fn match_is_word_based()
	{
		let field = "\"something [unit]\"";
		assert_eq!(matches(field, "some"), false);
		assert_eq!(matches(field, "some "), false);
	}

	#[test]
	fn collision_test()
	{
		let field = "\"uncertainty_xy [nm]\"";
		assert_eq!(matches(field, X), false);
		assert_eq!(matches(field, Y), false);
	}

	#[test]
	fn can_parse_uncertainty_xy()
	{
		let header = "frame,x, y,\"uncertainty_xy [nm]\", sigma, intensity";
		let header = get_columns(header).unwrap();
		assert_eq!(header.frame_number_idx(), 0);
		assert_eq!(header.x_idx(), 1);
		assert_eq!(header.y_idx(), 2);
		assert_eq!(header.uncertainty_idx(), 3);
		assert_eq!(header.sigma_idx(), 4);
		assert_eq!(header.intensity_idx(), 5);
	}

	#[test]
	fn can_parse_uncertainty()
	{
		let header = "frame,x, y,\"uncertainty [nm]\", sigma, intensity";
		let header = get_columns(header).unwrap();
		assert_eq!(header.frame_number_idx(), 0);
		assert_eq!(header.x_idx(), 1);
		assert_eq!(header.y_idx(), 2);
		assert_eq!(header.uncertainty_idx(), 3);
		assert_eq!(header.sigma_idx(), 4);
		assert_eq!(header.intensity_idx(), 5);
	}

	#[test]
	fn can_parse_header_sigma_with_unit()
	{
		let header = "x, y,\"uncertainty [nm]\", sigma [nm], intensity, frame";
		let header = get_columns(header).unwrap();
		assert_eq!(header.x_idx(), 0);
		assert_eq!(header.y_idx(), 1);
		assert_eq!(header.uncertainty_idx(), 2);
		assert_eq!(header.sigma_idx(), 3);
		assert_eq!(header.intensity_idx(), 4);
		assert_eq!(header.frame_number_idx(), 5);
	}

	#[test]
	fn line_ok() 
	{
		let input = "1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
		let expected = AllocatedLocalisation::new(1, 685.44715, 2145.33857, 104.22976, 1496790.54948, 9.81696);
		assert_eq!(parse_line(input, &Header::new(0, 1, 2, 3, 4, 8)).unwrap(), expected)
	}

	#[test]
	fn frame_number_written_as_float() 
	{
		let input = "1.0,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
		let expected = AllocatedLocalisation::new(1, 685.44715, 2145.33857, 104.22976, 1496790.54948, 9.81696);
		assert_eq!(parse_line(input, &Header::new(0, 1, 2, 3, 4, 8)).unwrap(), expected)
	}	

	#[test]
	fn out_of_range() 
	{
		let input = "1,1.18973e+4932,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
		assert_eq!(parse_line(input, &Header::new(0, 1, 2, 3, 4, 8)).unwrap_err(), "cannot parse 1.18973e+4932 to f64 - out of range")
	}

	#[test]
	fn invalid_parameter() 
	{
		let input = "1,junk,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
		assert_eq!(parse_line(input, &Header::new(0, 1, 2, 3, 4, 8)).unwrap_err(), "cannot parse junk as f64 - invalid float literal")
	}

	#[test]
	fn parse_ok() 
	{
		let input = r#""frame","x [nm]","y [nm]","sigma [nm]","intensity [photon]","offset [photon]","bkgstd [photon]","chi2","uncertainty_xy [nm]"
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696"#;
		let localisations : Vec<Result<AllocatedLocalisation, String>> = io::localisations_from(input.as_bytes(), ThunderStormParserFactory).unwrap().collect();
		let expected = AllocatedLocalisation::new(1, 685.44715, 2145.33857, 104.22976, 1496790.54948, 9.81696);
		assert_eq!(localisations[0].as_ref().unwrap(), &expected);
		assert_eq!(localisations[1].as_ref().unwrap(), &expected);
	}

	#[test]
	fn bad_header()
	{
		let input = r#"header
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696"#;
		match io::localisations_from(input.as_bytes(), ThunderStormParserFactory)
		{
			Err(e) => assert_eq!(e, "Cannot find frame in header"),
			Ok(_) => assert_eq!(true, false) // fail the test
		}
	}

	#[test]
	fn bad_stream()
	{
		let input = r#""frame","x [nm]","y [nm]","sigma [nm]","intensity [photon]","offset [photon]","bkgstd [photon]","chi2","uncertainty_xy [nm]"
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
1,junk,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696"#;
		let result = io::localisations_from(input.as_bytes(), ThunderStormParserFactory).unwrap().skip(1).next().unwrap().unwrap_err();
		assert_eq!(result, "error parsing line 2 - cannot parse junk as f64 - invalid float literal")
	}

	#[test]
	fn no_trailing_new_lines()
	{
		let input = "1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
		let mut buffer = Vec::new();
		let header = "\"frame\",\"x [nm]\",\"y [nm]\",\"sigma [nm]\",\"intensity [photon]\",\"offset [photon]\",\"bkgstd [photon]\",\"chi2\",\"uncertainty_xy [nm]\"";
		let _ = crate::io::write_stream(&mut buffer, &[header], std::iter::once(input)).unwrap();
		let text = std::str::from_utf8(&buffer).unwrap();
		let expected = r#""frame","x [nm]","y [nm]","sigma [nm]","intensity [photon]","offset [photon]","bkgstd [photon]","chi2","uncertainty_xy [nm]"
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696"#;
		assert_eq!(text, expected)
	}

}