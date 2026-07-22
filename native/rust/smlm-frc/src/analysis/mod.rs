use crate::config::{Config as FrcConfig};
use crate::resolution::{Resolution};
use crate::thresholds::{Threshold};

use imp::{OwnedImage, Image};

use tiff_wrap::{Tiff};

use std::fs::{File};
use std::io::{Write, Error as IoError};
use std::path::PathBuf;

#[derive(Debug)]
pub struct Config 
{
	n_iterations: usize,
	output_directory: PathBuf
}

impl Config
{
	pub fn new(n_iterations: usize, output_directory: PathBuf) -> Self
	{
		Self{n_iterations, output_directory}
	}

	pub fn n_iterations(&self) -> usize
	{
		self.n_iterations
	}

	pub fn output_directory(&self) -> PathBuf
	{
		self.output_directory.clone()
	}
}

fn get_tiff(filename: PathBuf) -> Result<OwnedImage<f64>, String>
{
    Tiff::read(filename).map_err(|e| e.to_string())
                        .and_then(|mut tiff|  tiff.height()
                                                  .map_err(|e| e.to_string())
                                                  .and_then(|n_rows| tiff.width().map_err(|e| e.to_string()).map(|n_cols| (n_rows as usize, n_cols as usize)))
                                                  .map(|shape| (tiff, shape)))
                        .and_then(|(tiff, shape)| tiff.take(1)
                                                      .next()
                                                      .ok_or_else(|| "Tiff file empty".to_string())
                                                      .and_then(|i| i.to_f64().map_err(|e| e.to_string()).map(|data| (shape, data))))
                        .map(|(shape, data)| OwnedImage::new(shape, data))
}

fn render(results_directory: &PathBuf, localisation_data: PathBuf, name: &str) -> Result<OwnedImage<f64>, String>
{
	println!("Rendering {name}");
	let image = results_directory.join(format!("{name}.tiff"));
	let program_location = "/home/nik/Documents/repositories/renderer/cpp/programs_build/f2i";
	let renderer = F2I::new(&program_location, PathBuf::new(), image);
	let options = RenderOptions::default();
	renderer.render_from_data(&options, &localisation_data).and_then(get_tiff)
}

fn get_results(localisations: &str, config: &Config, idx: usize) -> Result<Resolution, String>
{
	let results_directory = config.output_directory.join(format!("{idx}"));
	println!("Creating: {}", results_directory.display());
	let _ = std::fs::create_dir_all(&results_directory).map_err(|e| e.to_string())?;
	let a = results_directory.join(format!("a.out"));
	let b = results_directory.join(format!("b.out"));
	println!("Splitting data");
	let _ = disk_split(localisations, &a, &b)?;
	let a_data = render(&results_directory, a, "a")?;
	let b_data = render(&results_directory, b, "b")?;
	println!("Running FRC");
	let image_width = a_data.n_cols();
	let config = FrcConfig::new(image_width);
	crate::frc(a_data, b_data, Threshold::HalfBit, Some(("tukey", &[0.25])), &config).map_err(|e| e.to_string())
}

fn mean(frcs: &[f64]) -> f64
{
	let mut total = 0.0;
	for frc in frcs
	{
		total += frc;
	}
	total / (frcs.len() as f64)
}

fn _var(frcs: &[f64], mean: f64, offset: f64) -> f64
{
	let mut total = 0.0;
	for frc in frcs
	{
		total += (frc - mean).powi(2);
	}
	total / (frcs.len() as f64 - offset)
}

fn var(frcs: &[f64], mean: f64) -> f64
{
	_var(frcs, mean, 0.0)
}

fn var_unbiased(frcs: &[f64], mean: f64) -> f64
{
	_var(frcs, mean, 1.0)
}

fn write_results<W: Write, I: Iterator<Item=f64>>(mut writer: W, frcs: I, mean : f64, var: f64, unbiased_var: f64) -> Result<(), IoError>
{
	let _ = write!(writer, "mean: {mean}\nvar: {var}\nvar_unbiased: {unbiased_var}")?;
    for frc in frcs
    {
        let _ = write!(writer, "\n{}", frc)?;
    }
    Ok(())
}

pub fn variablity(localisations: &str, config: &Config) -> Result<(), String>
{
	if config.output_directory().exists()
	{
		let _ = std::fs::remove_dir_all(config.output_directory()).map_err(|e| e.to_string())?;
	}
	let mut frcs = Vec::new();
	for iteration in 0..config.n_iterations()
	{
		println!("Iteration: {iteration}");
		let frc = get_results(localisations, config, iteration)?;
		frcs.push(frc.as_value());
	}
    let mean = mean(&frcs);
    let var = var(&frcs, mean);
    let unbiased_var = var_unbiased(&frcs, mean);
	let frc_file = config.output_directory().join("frcs.out");
    let _ = File::create(frc_file).and_then(|f| write_results(f, frcs.into_iter(), mean, var, unbiased_var)).map_err(|e| e.to_string())?;
	Ok(())
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn var_test() 
	{
		let data = [10.0, 15.0, 30.0, 45.0, 57.0, 52.0, 63.0, 72.0, 81.0, 93.0, 102.0, 105.0];
		let mean = mean(&data);
		let var = var(&data, mean);
		let unbiased_var = var_unbiased(&data, mean);
		assert_eq!(mean, 60.416666666666664);
		assert_eq!(var, 932.7430555555557);
		assert_eq!(unbiased_var, 1017.5378787878789);
	}
}