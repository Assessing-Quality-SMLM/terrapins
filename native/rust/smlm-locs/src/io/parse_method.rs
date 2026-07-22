use super::{CsvSettings};
use crate::constants::{THUNDER_STORM};

#[derive(Debug, Default, Clone, PartialEq)]
pub enum ParseMethod 
{
	#[default] 
	ThunderStorm,
	Csv(CsvSettings),
}

impl TryFrom<&str> for ParseMethod
{
	type Error = String;
	fn try_from(description: &str) -> Result<Self, Self::Error>
	{
		parse_method(description)
	}
}

fn parse_method(description: &str) -> Result<ParseMethod, String>
{
    match description
    {
        THUNDER_STORM => Ok(ParseMethod::ThunderStorm),
        desc @ _ =>
        {
            let splits : Vec<&str> = desc.split("=").collect();
            if splits.len() < 2
            {
                return Err(format!("Cannot create parse method from {description}"))
            }
            if splits[0] != "csv"
            {
                Err(format!("{} not recognised as parse method identifier", splits[0]))
            }
            else 
            {
                let settings = parse_settings(splits[1])?;
                Ok(ParseMethod::Csv(settings))
            }
        },
    }
}

fn parse_optional_param(param: &str) -> Result<Option<u8>, String>
{	
	if param.is_empty()
	{
		return Ok(None)
	}
	match param
	{
		"-1" => Ok(None),
		desc @ _ => desc.parse::<u8>().map(Some).map_err(|e| e.to_string())
	}
}

fn get_optional_param(index: usize, splits: &[&str]) -> Result<Option<u8>, String>
{
	match splits.get(index)
	{
		None => Ok(None),
		Some(desc) => parse_optional_param(desc)
	}
}

fn parse_settings(description: &str) -> Result<CsvSettings, String>
{
    let splits : Vec<&str> = description.split(";").collect();
    // println!("{:?}", splits);
    let n_required = 4;
    if splits.len() < n_required || splits.iter().take(n_required).any(|s| s.is_empty())
    {
        return Err(format!("Cannot parse {description} into csv settings"))
    }
    let n_headers = splits[0].parse::<usize>().map_err(|e| e.to_string())?;
    let delim = splits[1].chars().next().unwrap(); // unwrap ok as must be there
    let x_pos = splits[2].parse::<u8>().map_err(|e| e.to_string())?;
    let y_pos = splits[3].parse::<u8>().map_err(|e| e.to_string())?;

    let psf_sigma_pos = get_optional_param(4, &splits)?;
    let uncertainty_pos = get_optional_param(5, &splits)?;
    let frame_number_pos = get_optional_param(6, &splits)?;

    Ok(CsvSettings::default().with_n_headers(n_headers)
                             .with_delimeter(delim)
                             .with_x_pos(x_pos)
                             .with_y_pos(y_pos)
                             .with_sigma_pos(psf_sigma_pos)
                             .with_uncertainty_pos(uncertainty_pos)
    						 .with_frame_number_pos(frame_number_pos))
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn can_parse_thunderstorm() 
	{
		assert_eq!(ParseMethod::try_from("ts").unwrap(), ParseMethod::ThunderStorm)
	}

	#[test]
	fn error_on_unknown() 
	{
		assert_eq!(ParseMethod::try_from("blah").unwrap_err(), "Cannot create parse method from blah")
	}

	#[test]
	fn error_on_unknown_descriptions() 
	{
		assert_eq!(ParseMethod::try_from("blah=").unwrap_err(), "blah not recognised as parse method identifier")
	}

	#[test]
	fn bad_csv_description() 
	{
		assert_eq!(ParseMethod::try_from("csv=stuff;and;things").unwrap_err(), "Cannot parse stuff;and;things into csv settings")
	}

	#[test]
	fn good_minimal_csv_description() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2);
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_minimal_csv_for_sigma() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(Some(3));
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;3").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_minimal_csv_for_uncertainty() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)											 
											 .with_uncertainty_pos(Some(3));
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;;3").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn comma_delimeted_csv_description() 
	{
		assert_eq!(ParseMethod::try_from("csv=1;;;1;2;3").unwrap_err(), "Cannot parse 1;;;1;2;3 into csv settings")
	}

	#[test]
	fn good_csv_description_with_specified_no_sigma_value_and_implied_no_uncertainty() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(None)
											 .with_uncertainty_pos(None);
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;-1").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_csv_description_with_implied_no_sigma_value_and_specified_no_uncertainty()
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(None)
											 .with_uncertainty_pos(None);
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;;-1").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_csv_description_with_specified_no_sigma_and_implied_no_uncertainty_value() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(None)
											 .with_uncertainty_pos(None);
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;-1;").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_csv_description_with_specified_no_sigma_or_uncertainty_value() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(None)
											 .with_uncertainty_pos(None);
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;-1;-1").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_csv_description_with_sigma_but_no_uncertainty() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(Some(4))
											 .with_uncertainty_pos(None);
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;4;-1").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_csv_description_with_no_sigma_and_specified_uncertainty() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(None)
											 .with_uncertainty_pos(Some(4));
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;;4").unwrap(), ParseMethod::Csv(settings))
	}

	#[test]
	fn good_csv_description_with_frame_number() 
	{
		let settings = CsvSettings::default().with_n_headers(1)
											 .with_delimeter(',')
											 .with_x_pos(1)
											 .with_y_pos(2)
											 .with_sigma_pos(None)
											 .with_uncertainty_pos(None)
											 .with_frame_number_pos(Some(5));
		assert_eq!(ParseMethod::try_from("csv=1;,;1;2;;;5").unwrap(), ParseMethod::Csv(settings))
	}
}