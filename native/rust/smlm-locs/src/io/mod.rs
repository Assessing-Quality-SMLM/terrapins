pub use self::settings::{Settings};

mod csv;
mod parse_method;
mod settings;
pub mod thunderstorm;


use crate::filters::{ValidFilter};
use crate::{AllocatedLocalisation, FitLocalisation, UncertainLocalisation};

pub use self::parse_method::{ParseMethod};
pub use self::thunderstorm::{ThunderStormParser, ThunderStormParserFactory};
pub use self::csv::{CsvParser, Settings as CsvSettings};

use std::fs::File;
use std::fmt::{Display};
use std::io::{BufRead, BufReader, Error as IoError, Read, Write};
use std::path::Path;

pub trait Parser
{
	type Localisation;
	type Error;
	fn parse_line(&self, line: &str) -> Result<Self::Localisation, Self::Error>;
}

impl<P: Parser<Error=String>> Parser for &P
{
	type Localisation = P::Localisation;
	type Error = P::Error;	
	
	fn parse_line(&self, line: &str) -> Result<Self::Localisation, Self::Error>
	{
		(*self).parse_line(line)
	}
}

pub trait ParserFactory
{
	type Parser : Parser;
	type Error;
	fn create(&self) -> Result<Self::Parser, Self::Error>;
	fn from_header(&self, header: &str) -> Result<Self::Parser, Self::Error>;
	fn n_header_lines(&self) -> usize;
}

impl<T: ParserFactory> ParserFactory for &T
{
	type Parser = T::Parser;
	type Error = T::Error;
	fn create(&self) -> Result<Self::Parser, Self::Error>
	{
		T::create(self)
	}

	fn from_header(&self, header: &str) -> Result<Self::Parser, Self::Error>
	{
		T::from_header(self, header)
	}

	fn n_header_lines(&self) -> usize
	{
		T::n_header_lines(self)
	}
}

pub fn parse_u32(value: &str) -> Result<u32, String>
{
	value.trim().parse::<f64>()
		 .map_err(|e| format!("cannot parse {value} as u32 - {e}"))
		 .map(|f| f.round() as u32)
}

pub fn parse_f64(value: &str) -> Result<f64, String>
{
	value.trim().parse::<f64>().map_err(|e| format!("cannot parse {value} as f64 - {e}"))
						.and_then(|x| if x.is_infinite() {Err(format!("cannot parse {value} to f64 - out of range"))}else{Ok(x)})
}


fn localisations_from_lines<L : Iterator<Item=Result<String, IoError>>, P: Parser + 'static>(lines: L, parser: P, n_headers_lines: usize) -> impl Iterator<Item=Result<P::Localisation, String>>
where P::Error : Display
{
	lines.enumerate().map(move |(idx, r)| 
		{
			let line_number = idx + n_headers_lines;
			match r
			{
				Err(e) => Err(format!("error reading line {} - {e}", line_number)),
				Ok(line) => parser.parse_line(&line).map_err(|e| format!("error parsing line {} - {e}", line_number))
			}
		})
}

type FactoryLocalisation<F> = <<F as ParserFactory>::Parser as Parser>::Localisation;

pub fn read_localisations<F: ParserFactory + 'static, R: BufRead>(reader: R, factory: F) -> Result<impl Iterator<Item=Result<FactoryLocalisation<F>, String>>, String>
where <<F as ParserFactory>::Parser as Parser>::Error : Display, 
	  String: From<<F as ParserFactory>::Error>,
{
	let mut lines = reader.lines();
	let n_headers = factory.n_header_lines();
	match n_headers
	{
		0 => 
		{
			let p = factory.create()?;
			let iter = lines.skip(0);
			Ok(localisations_from_lines(iter, p, n_headers))
		}
		n @ _x =>
		{
			match lines.next()
			{
				None => return Err(format!("Reader has no data")),
				Some(header) =>
				{
					let p = factory.from_header(&header.map_err(|e| e.to_string())?)?;
					let iter = lines.skip(n - 1);
					Ok(localisations_from_lines(iter, p, n_headers))
				}
			}
		}
	}
}

pub fn localisations_from<R: Read, F: ParserFactory + 'static>(reader: R, factory: F) -> Result<impl Iterator<Item=Result<FactoryLocalisation<F>, String>>, String>
where <<F as ParserFactory>::Parser as Parser>::Error : Display, 
	  String: From<<F as ParserFactory>::Error> 
{
	read_localisations(BufReader::new(reader), factory)
}

pub fn read_file<P: AsRef<Path>, F: ParserFactory + 'static>(filename: P, factory: F) -> Result<impl Iterator<Item=Result<FactoryLocalisation<F>, String>>, String>
where <<F as ParserFactory>::Parser as Parser>::Error : Display, 
	  String: From<<F as ParserFactory>::Error> 

{
	File::open(filename).map_err(|e| e.to_string()).and_then(|f| localisations_from(f, factory))
}

fn filter_localisations_to_memory<T: UncertainLocalisation + FitLocalisation, E, I: Iterator<Item=Result<T, E>>>(iter: I, settings: &Settings) -> Vec<T>
{
	let i = iter.filter_map(Result::ok);
	let psf_bounds = settings.psf_sigma_filter();
	let uncertainty_bounds = settings.uncertainty_filter();
	i.valid(psf_bounds, uncertainty_bounds)
}

pub fn read_file_to_memory_with<P: AsRef<Path>, F: ParserFactory + 'static>(filename: P, factory: F) -> Result<Vec<FactoryLocalisation<F>>, String>
where <<F as ParserFactory>::Parser as Parser>::Error : Display, 
	  String: From<<F as ParserFactory>::Error> 
{
	read_file(filename, factory).map(|i| i.filter_map(Result::ok).collect())
}

pub fn read_file_to_memory<P: AsRef<Path>>(filename: P, settings: &Settings) -> Result<Vec<AllocatedLocalisation>, String>
{
	match settings.parse_method()
	{
		ParseMethod::ThunderStorm => read_file(filename, ThunderStormParserFactory).map(|i| filter_localisations_to_memory(i, settings)),
		ParseMethod::Csv(csv_settings) => read_file(filename, csv_settings.clone()).map(|i| filter_localisations_to_memory(i, settings))
	}
}

pub fn write_headers<W: Write, T: Display>(mut writer: W, headers: &[T]) -> Result<(), IoError>
{
    for header in headers
    {
        let _ = write!(writer, "{}", header)?;
    }
    Ok(())
}

pub fn write_item<T: Display, W: Write>(mut stream: W, item: T) -> Result<(), IoError>
{
	write!(stream, "\n{item}")
}

pub fn write_stream<W: Write, T: Display, U: Display, I: Iterator<Item=T>>(mut stream: W, headers: &[U], items: I) -> Result<(), IoError>
{
	let _ = write_headers(&mut stream, headers)?;
	for item in items
	{
		let _ = write_item(&mut stream, item)?;
	}
	Ok(())
}

pub fn write<W: Write, T: Display, U: Display>(stream: W, headers: &[U], items: &[T]) -> Result<(), IoError>
{
	write_stream(stream, headers, items.iter())
}

#[derive(Debug)]
pub struct StreamData
{
	headers: Vec<String>,
	data: Vec<String>,
}

impl StreamData
{
	pub fn new() -> Self
	{
		Self
		{
			headers: Vec::new(), 
			data: Vec::new()
		}
	}

	pub fn headers(&self) -> &Vec<String>
	{
		&self.headers
	}

	pub fn headers_mut(&mut self) -> &mut Vec<String>
	{
		&mut self.headers
	}

	pub fn data(&self) -> &[String]
	{
		&self.data
	}

	pub fn take(self) -> (Vec<String>, Vec<String>)
	{
		(self.headers, self.data)
	}

	pub fn add_line(&mut self, line: String) -> ()
	{
		self.data.push(line)
	}
}

fn generate_error<T: Display>(line: usize, error: T) -> String
{
	format!("error on line {} - {}", line, error)
}

pub fn collect_headers<I: Iterator<Item=Result<String, IoError>>>(mut iterator: I, headers: &mut Vec<String>, n_headers: usize) -> Result<(), String>
{
	for line_number in 0..n_headers
	{
		match iterator.next()
		{
			None => return Ok(()),
			Some(line) => 
			{
				let header = line.map_err(|e| generate_error(line_number, e))?;
				headers.push(header)
			}
		}
	}
	Ok(())
}

pub fn collect_stream<R: BufRead>(reader: R, n_headers: usize) -> Result<StreamData, String>
{	
	let mut data = StreamData::new();
	let mut lines = reader.lines();
	let _ = collect_headers(&mut lines, data.headers_mut(), n_headers)?;
	for (line, result) in lines.enumerate()
	{
		let line_number = line + n_headers;
		let line_data = result.map_err(|e| generate_error(line_number, e))?;
		data.add_line(line_data);
	}
	Ok(data)
}

#[cfg(test)]
mod tests 
{
	use super::*;
	
	use thunderstorm::ThunderStormParserFactory;
	
	use crate::AllocatedLocalisation;

	use std::io::{ErrorKind};

	const DATA: &str = r#"header
0,1,2,3
4,5,6,7"#;

	const MULTIPLE_HEADERS: &str = r#"header1
header2
0,1,2,3
4,5,6,7"#;

	const ZERO_HEADERS: &str = r#"0,1,2,3
4,5,6,7"#;

	#[test]
	fn no_header_test() 
	{
		let settings = csv::Settings::default().with_n_headers(0).with_x_pos(1).with_y_pos(0).with_sigma_pos(Some(3)).with_uncertainty_pos(Some(2));
		let localisations: Vec<AllocatedLocalisation> = localisations_from(ZERO_HEADERS.as_bytes(), settings).unwrap().filter_map(Result::ok).collect();
		assert_eq!(localisations.len(), 2);
		assert_eq!(localisations[0], AllocatedLocalisation::new(0, 1.0, 0.0, 3., 0.0, 2.));
		assert_eq!(localisations[1], AllocatedLocalisation::new(0, 5.0, 4.0, 7., 0.0, 6.));
	}

	#[test]
	fn header_not_supplied_test() 
	{
		let result = localisations_from(ZERO_HEADERS.as_bytes(), ThunderStormParserFactory);
		match result
		{
			Err(e) => assert_eq!(e, "Cannot find frame in 0,1,2,3"),
			Ok(_) => assert_eq!(true, false) // fail the test
		}
	}

	#[test]
	fn out_order_test() 
	{
		let settings = csv::Settings::default().with_n_headers(1).with_x_pos(1).with_y_pos(0).with_sigma_pos(Some(3)).with_uncertainty_pos(Some(2));
		let localisations: Vec<AllocatedLocalisation> = localisations_from(DATA.as_bytes(), settings).unwrap().filter_map(Result::ok).collect();
		assert_eq!(localisations.len(), 2);
		assert_eq!(localisations[0], AllocatedLocalisation::new(0, 1.0, 0.0, 3.0, 0.0, 2.));
		assert_eq!(localisations[1], AllocatedLocalisation::new(0, 5.0, 4.0, 7.0, 0.0, 6.));
	}

	#[test]
	fn can_collect_stream_data()
	{
		let stream_data = collect_stream(MULTIPLE_HEADERS.as_bytes(), 2).unwrap();
		let expected_headers = ["header1", "header2"];
		let expected_data = ["0,1,2,3", "4,5,6,7"];
		assert_eq!(*stream_data.headers(), expected_headers);
		assert_eq!(stream_data.data(), expected_data)
	}

	#[test]
	fn collection_tolerant_to_insufficient_headers()
	{
		let stream_data = collect_stream(BufReader::new("header1".as_bytes()), 2).unwrap();
		let expected_headers = ["header1"];
		let expected_data = ["";0];
		assert_eq!(*stream_data.headers(), expected_headers);
		assert_eq!(stream_data.data(), expected_data)
	}

	#[test]
	fn line_numbers_on_errors()
	{
		let stream_data = collect_stream(FakeStream, 0).unwrap_err();
		assert_eq!(stream_data, "error on line 0 - error filling buffer")

	}

	#[test]
	fn parse_can_handle_white_space() 
	{
		let value = " 12.34";
		assert_eq!(parse_f64(value).unwrap(), 12.34)
	}

	#[test]
	fn filter_localisations_to_memory_can_handle_errors()
	{
		let data = [Err("something")];
		let settings = Settings::default();
		assert_eq!(settings.uncertainty_filter(), None);
		let result = filter_localisations_to_memory::<AllocatedLocalisation, _, _>(data.into_iter(), &settings);
		assert_eq!(result.is_empty(), true)
	}

	#[test]
	fn filter_localisations_to_memory_can_filter_unertainty()
	{
		let lower = 1.0;
		let upper = 2.0;
		let data: Vec<Result<_, String>> = vec![Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0)),
												Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5))];
		let settings = Settings::default().with_uncertainty_filter(lower, upper);
		assert_eq!(settings.uncertainty_filter(), Some((lower, upper)));
		let result = filter_localisations_to_memory(data.into_iter(), &settings);
		assert_eq!(result[0], AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5))
	}

	#[test]
	fn filter_localisations_to_memory_can_filter_sigma()
	{
		let lower = 1.0;
		let upper = 2.0;
		let data: Vec<Result<_, String>> = vec![Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0)),
												Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 5.0))];
		let settings = Settings::default().with_psf_sigma_filter(lower, upper);
		assert_eq!(settings.psf_sigma_filter(), Some((lower, upper)));
		let result = filter_localisations_to_memory(data.into_iter(), &settings);
		assert_eq!(result[0], AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 5.0))
	}

	#[test]
	fn filter_localisations_to_memory_can_filter_both()
	{
		let lower = 1.0;
		let upper = 2.0;
		let data: Vec<Result<_, String>> = vec![Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 5.0)),
												Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 5.0)),
												Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 3.0, 4.0, 1.5)),
												Ok(AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 1.5))];
		let settings = Settings::default().with_psf_sigma_filter(lower, upper).with_uncertainty_filter(lower, upper);
		assert_eq!(settings.psf_sigma_filter(), Some((lower, upper)));
		assert_eq!(settings.uncertainty_filter(), Some((lower, upper)));
		let result = filter_localisations_to_memory(data.into_iter(), &settings);
		assert_eq!(result[0], AllocatedLocalisation::new(0, 1.0, 2.0, 1.5, 4.0, 1.5))
	}

	struct FakeStream;

	impl Read for FakeStream
	{

		fn read(&mut self, _: &mut [u8]) -> Result<usize, IoError> 
		{
			Err(IoError::new(ErrorKind::Other, "error reading")) 
		}
	}

	impl BufRead for FakeStream
	{

		fn fill_buf(&mut self) -> Result<&[u8], IoError> 
		{
			Err(IoError::new(ErrorKind::Other, "error filling buffer"))
		}

		fn consume(&mut self, _: usize)
		{

		}
	}
}