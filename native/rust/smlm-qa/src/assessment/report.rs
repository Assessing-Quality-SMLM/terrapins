use crate::settings::report::AssessmentSettings;

use super::Assessment;

use std::fmt::{Display, Formatter};
use std::fs::{File};
use std::io::{BufWriter, Error as IoError, Write};
use std::ops::{Deref, DerefMut};
use std::path::{Path};

fn write_possible_assessment<W: Write>(mut writer: W, assessement: Option<&Assessment>) -> Result<(), IoError>
{
	match assessement
	{
		None => Ok(()),
		Some(a) => 
		{
			writer.write(&[b'\n']).and_then(|_| a.write_to(&mut writer).and_then(|_| write!(writer, "\n")))
		}
	}
}

#[allow(dead_code)]
fn fidge_resolution_results_based_on_worst_score<T: Deref<Target=Assessment> + DerefMut<Target=Assessment>>(assessments: &mut [T]) -> ()
{
	let all_passed = assessments.iter().all(|a| a.deref().passed());
	if !all_passed
	{
		for assessment in assessments
		{
			assessment.set_failed();
			assessment.append_message(" Failed due to one or more other resolution assessments failing.");
		}
		return
	}

	let get_score = |a: &Assessment| a.score().unwrap_or(std::f64::MIN);
	assessments.sort_by(|a, b| get_score(a).total_cmp(&get_score(b)));
	let n = assessments.len();
	assessments[n - 1].set_passed();
	for assessment in assessments.iter_mut().take(n - 1)
	{
		assessment.set_failed();
		assessment.append_message(" Failed due to other resolution having a worse score.");
	}
}

pub type ReportItem = (Assessment, AssessmentSettings);

#[derive(Debug, Default, PartialEq)]
pub struct Report 
{
	localisation_precision: Option<ReportItem>,
	blinking: Option<ReportItem>,
	bias: Option<ReportItem>,
	squirrel: Option<ReportItem>,
	frc_sampling_assessment: Option<ReportItem>,
	frc_resolution_assessment: Option<ReportItem>,
	drift: Option<ReportItem>,
	magnification: Option<ReportItem>,
	limiting_resolution: Option<ReportItem>
}

impl Report
{
	pub fn new() -> Self
	{
		Self::default()
	}
	
	pub fn add_localisation_precision_assessment(&mut self, item : ReportItem) -> ()
	{
		self.localisation_precision = Some(item)
	}

	pub fn add_blinking_assessment(&mut self, item : ReportItem) -> ()
	{
		self.blinking = Some(item);
	}

	pub fn drift_assessment(&self) -> Option<&Assessment>
	{
		self.drift.as_ref().map(|i| &i.0)
	}

	pub fn add_drift_assessment(&mut self, item : ReportItem) -> ()
	{
		self.drift = Some(item);
	}

	pub fn bias_assessment(&self) -> Option<&Assessment>
	{
		self.bias.as_ref().map(|i| &i.0)
	}

	pub fn add_bias_assessment(&mut self, item : ReportItem) -> ()
	{
		self.bias = Some(item);
	}

	// pub fn fidge_precision_assessments(&mut self) -> ()
	// {
	// 	let mut precision_assessments = Vec::new();
	// 	match self.bias.as_ref()
	// 	{
	// 		None => {},
	// 		Some(a) => precision_assessments.push(a.borrow_mut()),
	// 	}
	// 	match self.localisation_precision.as_ref()
	// 	{
	// 		None => {},
	// 		Some(a) => precision_assessments.push(a.borrow_mut())
	// 	}
	// 	match self.frc_resolution_assessment.as_ref()
	// 	{
	// 		None => {},
	// 		Some(a) =>  precision_assessments.push(a.borrow_mut())
	// 	}
	// 	if precision_assessments.is_empty()
	// 	{
	// 		return
	// 	}
	// 	fidge_resolution_results_based_on_worst_score(precision_assessments.as_mut_slice())
	// }

	pub fn squirrel_assessment(&self) -> Option<&Assessment>
	{
		self.squirrel.as_ref().map(|i| &i.0)
	}

	pub fn add_squirrel_assessment(&mut self, item : ReportItem) -> ()
	{
		self.squirrel = Some(item);
	}

	pub fn add_frc_sampling_assessment(&mut self, item : ReportItem) -> ()
	{
		self.frc_sampling_assessment = Some(item);
	}

	pub fn frc_resolution_assessment(&self) -> Option<&Assessment>
	{
		self.frc_resolution_assessment.as_ref().map(|i| &i.0)
	}

	pub fn add_frc_resolution_assessment(&mut self, item : ReportItem) -> ()
	{
		self.frc_resolution_assessment = Some(item);
	}

	pub fn magnification_assessment(&self) -> Option<&Assessment>
	{
		self.magnification.as_ref().map(|i| &i.0)
	}

	pub fn add_magnification_assessment(&mut self, item : ReportItem) -> ()
	{
		self.magnification = Some(item)
	}

	pub fn add_limiting_resolution_assessment(&mut self, item: ReportItem) -> ()
	{
		self.limiting_resolution = Some(item)
	}

	fn assessments(&self) -> Vec<(&str, Option<&ReportItem>)>
	{
		vec![
				("localisation_precision", self.localisation_precision.as_ref()),
				("drift", self.drift.as_ref()),
				("blinking", self.blinking.as_ref()),
				("sampling", self.frc_sampling_assessment.as_ref()),
				("magnification", self.magnification.as_ref()),
				("bias", self.bias.as_ref()),
				("squirrel", self.squirrel.as_ref()),
				("frc_resolution", self.frc_resolution_assessment.as_ref()),
				("limiting_resolution", self.limiting_resolution.as_ref())
			]
	}

	fn write_to<W: Write>(&self, mut writer: W) -> Result<(), IoError>
	{
		let mut wa = |a| write_possible_assessment(&mut writer, a);

		let mut ok = Ok(());
		for a in self.assessments()
		{
			ok = ok.and(wa(a.1.map(|i| &i.0)));
		}
		ok
	}

	pub fn write_to_directory<P: AsRef<Path>>(&self, directory: P) -> Result<(), IoError>
	{
		let dir = directory.as_ref();
		if !dir.exists()
		{
			let _ = std::fs::create_dir(&directory)?;
		}
		let mut ok = Ok(());
		for a in self.assessments()
		{
			ok = ok.and(self.write_assessment(&dir.join(a.0), a.1));
		}
		ok
	}

	fn write_assessment<P: AsRef<Path>>(&self, filename: P, item: Option<&ReportItem>) -> Result<(), IoError>
	{		
		item.map_or(Ok(()), |i| File::create(filename).map(BufWriter::new).and_then(|w| i.0.write_as_sections(w, i.1.labels())))
	}
}

impl Display for Report
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), std::fmt::Error>
	{
		let mut buffer = Vec::new();
		let _ = self.write_to(&mut buffer);
		let message = match std::str::from_utf8(&buffer)
		{
			Err(e) => e.to_string(),
			Ok(s) => s.to_string()
		};
		write!(f, "{}", message)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn if_not_all_resolutions_pass_there_is_no_correct_resolution()
	{
		let mut a = Assessment::pass_with("a", "");
		a.set_score(1.0);
		let mut b = Assessment::fail_with("b", "");
		b.set_score(2.0);
		
		assert_eq!(a.passed(), true);
		assert_eq!(b.passed(), false);
		{
			let mut data = [&mut a, &mut b];
			fidge_resolution_results_based_on_worst_score(&mut data);
		}
		
		assert_eq!(a.passed(), false);
		assert_eq!(a.message(), " Failed due to one or more other resolution assessments failing.");
		assert_eq!(b.passed(), false);
		assert_eq!(b.message(), " Failed due to one or more other resolution assessments failing.");
	}

	#[test]
	fn if_all_resolutions_pass_only_have_worst_score_as_passed()
	{
		let mut a = Assessment::pass_with("a", "");
		a.set_score(1.0);
		let mut b = Assessment::pass_with("b", "");
		b.set_score(2.0);
		
		{
			let mut data = [&mut a, &mut b];
			fidge_resolution_results_based_on_worst_score(&mut data);
		}
		assert_eq!(a.passed(), false);
		assert_eq!(a.message(), " Failed due to other resolution having a worse score.");
		assert_eq!(b.passed(), true);
	}

	#[test]
	fn if_all_resolutions_pass_but_score_not_set_ignore_it()
	{
		let mut a = Assessment::pass_with("a", "");
		a.set_score(1.0);
		let mut b = Assessment::pass_with("b", "");
		
		{
			let mut data = [&mut a, &mut b];
			fidge_resolution_results_based_on_worst_score(&mut data);
		}

		assert_eq!(a.passed(), true);
		assert_eq!(a.message(), "");
		assert_eq!(b.passed(), false);
		assert_eq!(b.message(), " Failed due to other resolution having a worse score.");
	}


	// #[test]
	// fn can_fidge_an_empty_report()
	// {
	// 	let mut report = Report::new();
	// 	report.fidge_precision_assessments();
	// 	assert_eq!(true, true)
	// }
}