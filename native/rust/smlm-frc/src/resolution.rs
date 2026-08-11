use crate::frc;
use crate::IntersectionError;
use crate::plotting;

use std::fmt::{Display, Formatter, Error as FmtError};
use std::io::{BufRead, BufReader, Read, Write, ErrorKind, Error as IoError};
use std::num::ParseFloatError;

const INFINITY: f64 = std::f64::INFINITY;

fn create_error(error: &str) -> IoError
{
	IoError::new(ErrorKind::Other, error)
}

#[derive(Debug)]
enum Value 
{
	Value(f64),
	Inf(IntersectionError),
}

impl Value
{
	pub fn as_f64(&self) -> f64
	{
		match self
		{
			Self::Value(v) => *v,
			Self::Inf(_) => INFINITY
		}
	}

	pub fn to_nm(&self, nm_per_pix: f64) -> f64
	{
		match self
		{
			Self::Value(v) => v * nm_per_pix,
			Self::Inf(_) => INFINITY
		}	
	}

	
	fn as_string(&self) -> String
	{
		match self
		{
			Self::Value(v) => v.to_string(),
			Self::Inf(e) => format!("{},{}", INFINITY, e.to_string())
		}	
	}
}

impl From<Result<f64, IntersectionError>> for Value
{
	fn from(value: Result<f64, IntersectionError>) -> Self
	{
		match value
		{
			Ok(v) => Self::Value(v),
			Err(e) => Self::Inf(e)
		}
	}
}

impl Display for Value
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError> 
	{
		write!(f, "{}", self.as_string())
	}
}

#[derive(Debug)]
pub struct Resolution 
{
	value: Value,
	qs: Vec<f64>,
	correlations: Vec<f64>,
	threshold_curve: Vec<f64>
}

impl Resolution
{
	pub fn new(intersection_result: Result<f64, IntersectionError>, qs: Vec<f64>, correlations: Vec<f64>, threshold_curve: Vec<f64>) -> Self
	{
		Self
		{
			value : Value::from(intersection_result), 
			qs, 
			correlations, 
			threshold_curve
		}
	}
	
	pub fn from(qs: Vec<f64>, correlations: Vec<f64>, threshold_curve: Vec<f64>) -> Self
	{
		let intersection = frc::get_intersection_resolution(&qs, &correlations, &threshold_curve);
		Self::new(intersection, qs, correlations, threshold_curve)
	}

	pub fn realign_with(&self, qs: Vec<f64>) -> Self
	{
		Self::from(qs, self.correlations.clone(), self.threshold_curve.clone())
	}

	pub fn qs(&self) -> &[f64]
	{
		&self.qs
	}

	pub fn correlations(&self) -> &[f64]
	{
		&self.correlations
	}

	pub fn threshold_curve(&self) -> &[f64]
	{
		&self.threshold_curve
	}

	// FIRE number
	pub fn as_value(&self) -> f64
	{
		self.value.as_f64()
	}

	pub fn to_spatial_frequency(&self) -> f64
	{
		let value = self.as_value();
		if value.is_finite() {1.0 / value} else {INFINITY}
	}

	pub fn to_nm(&self, nm_per_pix: f64) -> f64
	{
		self.value.to_nm(nm_per_pix)
	}

	pub fn plot(&self) -> ()
	{
		plotting::plot_frc(&self.qs, &self.correlations, &self.threshold_curve);
	}

	pub fn write_to<W: Write>(&self, nm_per_pix: f64, mut writer: W) -> Result<(), IoError>
	{
		let _ = write!(writer, "{},{}\n", self.as_value(), self.to_nm(nm_per_pix));
		for ((q, cor), t) in self.qs.iter().zip(self.correlations.iter()).zip(self.threshold_curve.iter())
		{
			let _ = write!(writer, "{q},{cor},{t}\n")?;
		}
		Ok(())
	}

	fn parse_intersection_error(line: &str) -> IntersectionError
	{
		let splits = line.split(",").collect::<Vec<&str>>();
		if splits.len() < 2
		{
			IntersectionError::ParseError
		}
		else
		{
			IntersectionError::from(splits[1])
		}
	}

	fn parse_intersection(line: &str) -> Result<f64, IntersectionError>
	{
		line.parse().map_err(|_| Self::parse_intersection_error(line))
	}

	fn parse_intersection_from(line: Result<String, IoError>) -> Result<Result<f64, IntersectionError>, IoError>
	{
		line.map(|s| Self::parse_intersection(s.as_str()))
	}	

	pub fn read_from<R: Read>(reader: R) -> Result<Self, IoError>
	{
		let buf_reader = BufReader::new(reader);
		let mut lines = buf_reader.lines();
		let intersection_line = lines.next().ok_or_else(|| create_error("Intersection line missing"))?;
		let intersection = Self::parse_intersection_from(intersection_line)?;
		let mut qs = Vec::new();
		let mut corrs = Vec::new();
		let mut t_curve = Vec::new();
		for line in lines
		{
			let line = line?;
			let splits = line.split(",").collect::<Vec<&str>>();
			if splits.len() < 3
			{
				return Err(create_error(&format!("{:?} cannot be split into 3 with ,", splits)))
			}
			let q = splits[0].trim().parse::<f64>().map_err(|e: ParseFloatError| create_error(&e.to_string()))?;
			qs.push(q);
			let corr = splits[1].trim().parse::<f64>().map_err(|e: ParseFloatError| create_error(&e.to_string()))?;
			corrs.push(corr);
			let t = splits[2].trim().parse::<f64>().map_err(|e: ParseFloatError| create_error(&e.to_string()))?;
			t_curve.push(t);
		}
		Ok(Self::new(intersection, qs, corrs, t_curve))
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	use crate::{IntersectionError};

	#[test]
	fn value_to_string() 
	{
		assert_eq!(Value::from(Ok(1.1)).to_string(), "1.1")
	}

	#[test]
	fn value_as_value() 
	{
		assert_eq!(Value::from(Ok(1.1)).as_f64(), 1.1)
	}

	#[test]
	fn value_to_nm() 
	{
		assert_eq!(Value::from(Ok(1.1)).to_nm(10.0), 11.0)
	}

	#[test]
	fn value_error_to_string() 
	{
		assert_eq!(Value::from(Err(IntersectionError::NoCrossing)).to_string(), "inf,No Crossing")
	}

	#[test]
	fn value_error_as_value() 
	{
		assert_eq!(Value::from(Err(IntersectionError::NoCrossing)).as_f64(), std::f64::INFINITY)
	}

	#[test]
	fn value_error_to_nm() 
	{
		assert_eq!(Value::from(Err(IntersectionError::NoCrossing)).to_nm(10.0), std::f64::INFINITY)
	}

	#[test]
	fn write_data() 
	{
		let qs = vec![1.0, 2.0, 3.0];
		let frcs = vec![3.0, 4.0, 5.0];
		let threshold_curve = vec![6.0, 7.0, 8.0];
		let resolution = Resolution::new(Ok(1.0), qs, frcs, threshold_curve);
		let mut buffer = Vec::new();
		let _ = resolution.write_to(1.0, &mut buffer).unwrap();
		let expected = "1,1\n1,3,6\n2,4,7\n3,5,8\n";
		let got = std::str::from_utf8(&buffer).unwrap();
		assert_eq!(got, expected);
	}

	#[test]
	fn read_data_ok()
	{
		let data = r#"1
2, 3,4
5,6,7"#;
		let resolution = Resolution::read_from(data.as_bytes()).unwrap();
		assert_eq!(resolution.as_value(), 1.0);
		assert_eq!(resolution.qs(), vec![2.0, 5.0]);
		assert_eq!(resolution.correlations(), vec![3.0, 6.0]);
		assert_eq!(resolution.threshold_curve(), vec![4.0, 7.0])
	}

	#[test]
	fn read_data_bad_intersection()
	{
		let data = r#"inf,No Crossing
2, 3,4
5,6,7"#;
		let resolution = Resolution::read_from(data.as_bytes()).unwrap();
		assert_eq!(resolution.as_value(), INFINITY);
		assert_eq!(resolution.qs(), vec![2.0, 5.0]);
		assert_eq!(resolution.correlations(), vec![3.0, 6.0]);
		assert_eq!(resolution.threshold_curve(), vec![4.0, 7.0])
	}

	#[test]
	fn read_data_junk_intersection()
	{
		let data = r#"junk
2, 3,4
5,6,7"#;
		let resolution = Resolution::read_from(data.as_bytes()).unwrap();
		assert_eq!(resolution.as_value(), INFINITY);
		assert_eq!(resolution.qs(), vec![2.0, 5.0]);
		assert_eq!(resolution.correlations(), vec![3.0, 6.0]);
		assert_eq!(resolution.threshold_curve(), vec![4.0, 7.0])
	}

	#[test]
	fn read_empty_string()
	{
		let data = r#"1,No Crossing
2,3,,
5,6,7"#;
		let error = Resolution::read_from(data.as_bytes()).unwrap_err();
		assert_eq!(error.to_string(), "cannot parse float from empty string");
	}

	#[test]
	fn read_bad_split()
	{
		let data = r#"1,No Crossing
2,34
5,6,7"#;
		let error = Resolution::read_from(data.as_bytes()).unwrap_err();
		assert_eq!(error.to_string(), "[\"2\", \"34\"] cannot be split into 3 with ,");
	}

	#[test]
	fn read_bad_parse()
	{
		let data = r#"1,No Crossing
2,junk, 4
5,6,7.0"#;
		let error = Resolution::read_from(data.as_bytes()).unwrap_err();
		assert_eq!(error.to_string(), "invalid float literal");
	}

	#[test]
	fn inf_spatial_frequency_test()
	{
		let data = r#"inf,No Crossing
2, 3,4
5,6,7"#;
		let resolution = Resolution::read_from(data.as_bytes()).unwrap();
		assert_eq!(resolution.as_value(), INFINITY);
		assert_eq!(resolution.to_spatial_frequency(), INFINITY);
	}
}