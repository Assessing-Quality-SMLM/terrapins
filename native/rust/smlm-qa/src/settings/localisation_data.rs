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
		let localisations = self.localisation_settings()
			.and_then(|s| read_file_to_memory(self.filepath(), &s))?;
		// Reporting nothing is never a useful outcome, and import filters discard rows without
		// it being an error, so this used to end in an empty reconstruction and a report with
		// no explanation. Whatever the cause, say so here rather than downstream.
		if localisations.is_empty()
		{
			return Err(format!(
				"No localisations were read from {}. Either the file is empty, or every row was \
				 removed by an import filter - the psf sigma filter is {:?} and the uncertainty \
				 filter is {:?}, both exclusive ranges in nm.",
				self.filepath(), self.psf_sigma_filter(), self.uncertainty_filter()))
		}
		Ok(localisations)
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

	use std::io::Write;

	/// Writes a ThunderSTORM-format file and reads it back through the default settings, which
	/// is the path the assessment binary actually takes.
	fn round_trip(rows: &str) -> Vec<AllocatedLocalisation>
	{
		let path = std::env::temp_dir().join(format!("locs_{}.csv", rows.len()));
		let mut f = std::fs::File::create(&path).unwrap();
		writeln!(f, "\"id\",\"frame\",\"x [nm]\",\"y [nm]\",\"sigma [nm]\",\"intensity [photon]\",\"uncertainty_xy [nm]\"").unwrap();
		write!(f, "{rows}").unwrap();
		drop(f);
		let data = LocalisationData::with(path.to_str().unwrap(), Some("ts"));
		let out = data.to_localisations();
		let _ = std::fs::remove_file(&path);
		match out
		{
			Ok(l) => l,
			// to_localisations turns "everything was filtered out" into an error rather than an
			// empty vector, so that case has to be mapped back to zero here. Any other error is
			// a genuine failure and should surface.
			Err(e) if e.starts_with("No localisations were read") => Vec::new(),
			Err(e) => panic!("{e}")
		}
	}

	#[test]
	fn measured_rows_survive_the_default_filters()
	{
		let rows = "1,1,100,200,110,1000,15\n2,1,300,400,120,2000,18\n";
		assert_eq!(round_trip(rows).len(), 2);
	}

	#[test]
	fn placeholder_uncertainty_is_not_filtered_away()
	{
		// A fitter with no uncertainty writes zero. The default uncertainty filter is the
		// exclusive range (0, 1000), so a zero fails `0 < 0` and every localisation in the file
		// was silently discarded - an empty table, no error, no report.
		let rows = "1,1,100,200,110,1000,0\n2,1,300,400,120,2000,0\n";
		assert_eq!(round_trip(rows).len(), 2,
			"localisations must not be discarded merely for lacking an uncertainty");
	}

	/// CHARACTERISATION TEST - documents current behaviour, does not endorse it.
	///
	/// The filter bounds are exclusive (`min < value && value < max`), so a localisation whose
	/// measured value sits exactly on a bound is discarded. With the default psf sigma filter of
	/// (20, 2000) a measured sigma of exactly 20 nm is dropped.
	///
	/// This is now only reachable for a genuinely measured value - an absent one is kept - so
	/// the practical impact is small, and exact boundary hits are rare in floating point data.
	/// It is left as-is deliberately: whether the bounds should be inclusive is a decision about
	/// filtering policy, not a bug fix, and it needs someone to decide what the filter is for.
	/// See "Known issues" in the README.
	///
	/// If the bounds are made inclusive, this test should be inverted rather than deleted.
	#[test]
	fn characterisation_measured_value_on_the_filter_boundary_is_dropped()
	{
		let rows = "1,1,100,200,20,1000,15\n";
		assert_eq!(round_trip(rows).len(), 0,
			"if this now returns 1 the bounds were made inclusive - update the README note");
	}

	#[test]
	fn an_absent_sigma_is_not_filtered_away()
	{
		// The whole point: a file with no width measurement must not vanish. Empty sigma field.
		let rows = "1,1,100,200,,1000,15\n2,1,300,400,,2000,18\n";
		assert_eq!(round_trip(rows).len(), 2);
	}

	#[test]
	fn a_measured_value_outside_the_range_is_still_dropped()
	{
		// Absence is kept; a real measurement that fails the filter is not. A 5000 nm sigma is
		// a measurement, and outside the default (20, 2000).
		let rows = "1,1,100,200,5000,1000,15\n";
		assert_eq!(round_trip(rows).len(), 0);
	}

	#[test]
	fn an_empty_result_is_an_error_not_a_silent_empty_table()
	{
		// Every row removed by a filter used to yield Ok(vec![]) and an unexplained empty
		// reconstruction downstream.
		let path = std::env::temp_dir().join("locs_all_filtered.csv");
		let mut f = std::fs::File::create(&path).unwrap();
		writeln!(f, "\"frame\",\"x [nm]\",\"y [nm]\",\"sigma [nm]\",\"intensity [photon]\",\"uncertainty_xy [nm]\"").unwrap();
		writeln!(f, "1,100,200,5000,1000,15").unwrap();
		drop(f);
		let err = LocalisationData::with(path.to_str().unwrap(), Some("ts"))
			.to_localisations().unwrap_err();
		let _ = std::fs::remove_file(&path);
		assert!(err.contains("No localisations were read"), "{err}");
		assert!(err.contains("import filter"), "the message must point at the likely cause: {err}");
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