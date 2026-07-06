use locs::{AllocatedLocalisation, io::{Settings, ParseMethod, read_file_to_memory}};

const DEFAULT_PSF_SIGMA_FILTER : (f64, f64) = (20.0, 2000.0);
pub const DEFAULT_HAWK_PSF_SIGMA_FILTER : (f64, f64) = (60.0, 200.0);
const DEFAULT_UNCERTAINTY_FILTER : (f64, f64) = (0.0, 1000.0);

fn to_parse_method(format: Option<&str>) -> Result<ParseMethod, String>
{
    match format
    {
        None => Ok(ParseMethod::default()),
        Some(f) => ParseMethod::try_from(f)
    }
}

#[derive(Debug, Clone, PartialEq)]
pub struct LocalisationData
{
	filepath: String,
	format: Option<String>,
	psf_sigma_filter: Option<(f64, f64)>,
	uncertainty_filter: Option<(f64, f64)>
}

impl LocalisationData
{
	pub fn new(filepath: &str, format: Option<String>, psf_sigma_filter: Option<(f64, f64)>, uncertainty_filter: Option<(f64, f64)>) -> Self
	{
		Self
		{
			filepath: filepath.to_string(), 
			format,
			psf_sigma_filter : psf_sigma_filter,
			uncertainty_filter: uncertainty_filter
		}
	}

	pub fn from(filepath: &str, format: &str) -> Self
	{
		Self::new(filepath, Some(format.to_string()), Some(DEFAULT_PSF_SIGMA_FILTER), Some(DEFAULT_UNCERTAINTY_FILTER))
	}

	pub fn from_filepath(filepath: &str) -> Self
	{
		Self::new(filepath, None, Some(DEFAULT_PSF_SIGMA_FILTER), Some(DEFAULT_UNCERTAINTY_FILTER))
	}

	pub fn with(filepath: &str, format: Option<&str>) -> Self
	{
		match format
		{
			None => Self::from_filepath(filepath),
			Some(f) => Self::from(filepath, f)
		}
	}

	pub fn filepath(&self) -> &str
	{
		&self.filepath
	}

	pub fn format(&self) -> Option<&str>
	{
		self.format.as_ref().map(|s| s.as_str())
	}

	pub fn psf_sigma_filter(&self) -> Option<(f64, f64)>
	{
		self.psf_sigma_filter
	}

	pub fn set_psf_sigma_filter(&mut self, value: (f64, f64)) -> ()
	{
		self.psf_sigma_filter = Some(value);
	}

	pub fn uncertainty_filter(&self) -> Option<(f64, f64)>
	{
		self.uncertainty_filter
	}

	pub fn parse_method(&self) -> Result<ParseMethod, String>
	{
		to_parse_method(self.format())
	}

	fn localisation_settings(&self) -> Result<Settings, String>
	{
		let mut settings = self.parse_method().map(Settings::new)?;
		match self.uncertainty_filter()
		{
			None => {},
			Some((min, max)) => 
			{
				settings = settings.with_uncertainty_filter(min, max);
			}
		}

		match self.psf_sigma_filter()
		{
			None => {},
			Some((min, max)) => 
			{
				settings = settings.with_psf_sigma_filter(min, max);
			}
		}
		Ok(settings)
	}

	pub fn to_localisations(&self) -> Result<Vec<AllocatedLocalisation>, String>
	{
		self.localisation_settings().and_then(|s| read_file_to_memory(self.filepath(), &s))
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;
	
	use locs::io::CsvSettings;

	#[test]
	fn ts_parse_method_by_default() 
	{
		assert_eq!(LocalisationData::from_filepath("").parse_method().unwrap(), ParseMethod::ThunderStorm)
	}

	#[test]
	fn ts_parse_method_specified() 
	{
		assert_eq!(LocalisationData::from("", "ts").parse_method().unwrap(), ParseMethod::ThunderStorm)
	}

	#[test]
	fn csv_parse_method_specified() 
	{
		let expected = ParseMethod::Csv(CsvSettings::default());
		assert_eq!(LocalisationData::from("", "csv=0;,;0;1").parse_method().unwrap(), expected)
	}

	#[test]
	fn parse_method_error() 
	{
		assert_eq!(LocalisationData::from("", "junk").parse_method().unwrap_err(), "Cannot create parse method from junk")
	}

	#[test]
	fn default_uncertainty_filter()
	{
		assert_eq!(LocalisationData::from_filepath("").uncertainty_filter(), Some((0.0, 1000.0)));
		assert_eq!(LocalisationData::from("", "").uncertainty_filter(), Some((0.0, 1000.0)))
	}

	#[test]
	fn make_sure_settings_includes_uncertainty_filter()
	{
		let data = LocalisationData::from_filepath("");
		assert_eq!(data.uncertainty_filter(), Some((0.0, 1000.0)));
		assert_eq!(data.localisation_settings().unwrap().uncertainty_filter(), Some((0.0, 1000.0)))
	}

	#[test]
	fn uncertainty_filter_can_be_missing()
	{
		let data = LocalisationData::new("", None, None, None);
		assert_eq!(data.uncertainty_filter(), None);
		assert_eq!(data.localisation_settings().unwrap().uncertainty_filter(), None)
	}
}