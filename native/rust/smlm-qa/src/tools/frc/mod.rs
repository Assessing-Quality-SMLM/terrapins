use crate::{fs_extra, io, utils};
use crate::error::{Error};
use crate::results::Results;
use crate::settings::{Settings, LocalisationData, FRCData};

use locs::{UncertainLocalisation};

use frc::{Config as FrcConfig, Resolution};

use renderer::{Crop, Image, NmFrame};

use std::fmt::Debug;
use std::fs::{File};
use std::fs;
use std::path::{Path};

fn frc_with<I: Image<Data = f64> + Crop, J: Image<Data = f64> + Crop, P: AsRef<Path>>(a_image: I, b_image: J, settings: &Settings, output_directory: P) -> Result<Resolution, Error>
{
    let frc_settings = settings.frc_settings();
    let frc_config = FrcConfig::new(a_image.n_cols());
    let threshold = frc_settings.threshold()?;
    let filter = frc_settings.filter();
    let resolution = frc::frc(a_image, b_image, threshold, filter, &frc_config).map_err(Error::from)?;

    let nm_per_pixel = 0.0;
    let filename = io::frc_resolution_data(output_directory);
    let _ = File::create(filename).and_then(|f| resolution.write_to(nm_per_pixel, f)).map_err(Error::from)?;
    Ok(resolution)
}

fn generate_frc<T: UncertainLocalisation + Debug + Clone + Send + 'static, P: AsRef<Path>>(a: &[T], b: &[T], settings: &Settings, output_directory: P, render_space: Option<&NmFrame>) -> Result<Resolution, Error>
{
    let output_directory = output_directory.as_ref();
    let image_output_directory = output_directory.join(io::FRC_IMAGE_LOCATION);
    let _ = fs::create_dir_all(&image_output_directory)?;

    let render_config = settings.frc_render_config(&image_output_directory, "a", render_space)?;
    let a_image = renderer::render_localisations(&render_config, a).map_err(Error::rendering)?.take_image();
    
    let render_config = settings.frc_render_config(&image_output_directory, "b", render_space)?;
    let b_image = renderer::render_localisations(&render_config, b).map_err(Error::rendering)?.take_image();

    frc_with(a_image, b_image, settings, output_directory)
}

fn calculate_block_size(n_localisations: usize, n_blocks: usize) -> usize
{
    n_localisations / n_blocks
}

fn generate_drift_split_frc<T : UncertainLocalisation + Debug + Copy + Clone + Send + 'static>(localisations: &[T], settings: &Settings, render_space: Option<&NmFrame>) -> Result<Resolution, Error>
{
    let n_blocks = settings.frc_drift_split_block_size();
    let block_size = calculate_block_size(localisations.len(), n_blocks);
    println!("block_size: {block_size}");

    let (a, b) = locs::split::block_split_copy(localisations, block_size);
    let output_directory = io::frc_drift_split_directory(settings.output_directory());
    generate_frc(&a, &b, settings, output_directory, render_space)
}

fn generate_zip_split_frc<T : UncertainLocalisation + Debug + Copy + Clone + Send + 'static>(localisations: &[T], settings: &Settings, render_space: Option<&NmFrame>) -> Result<Resolution, Error>
{
    let (a, b) = locs::split::zip(localisations);
    let a : Vec<T> = a.into_iter().copied().collect();
    let b : Vec<T> = b.into_iter().copied().collect();
    let output_directory = io::frc_zip_split_directory(settings.output_directory());
    generate_frc(&a, &b, settings, output_directory, render_space)
}

fn generate_half_frc<T: UncertainLocalisation + Debug + Clone + Send + 'static>(localisations: &[T], settings: &Settings, render_space: Option<&NmFrame>) -> Result<Resolution, Error>
{
    let (a, b) = locs::split::half(localisations);
    let output_directory = io::frc_half_split_directory(settings.output_directory());
    generate_frc(a, b, settings, output_directory, render_space)
}

fn generate_random<T: UncertainLocalisation + Debug + Clone + Send + 'static>(localisations: &[T], settings: &Settings, render_space: Option<&NmFrame>, idx: usize) -> Result<Resolution, Error>
{
    let (a, b) = locs::split::half(localisations);
    let output_directory = io::frc_random_split_directory_for(settings.output_directory(), idx);
    generate_frc(a, b, settings, output_directory, render_space)
}

fn generate_random_frc<T: UncertainLocalisation + Debug + Clone + Send + 'static, U>(localisations: &[T], settings: &Settings, render_space: Option<&NmFrame>, results: &mut Results<U>) -> Result<(), Error>
{
    let n = settings.frc_n_random_splits();
    for idx in 0..n
    {
        let resolution = generate_random(localisations, settings, render_space, idx)?;
        results.add_frc_random_result(resolution);
    }
    Ok(())
}

fn _generate_frc_data<T>(localisation_data: &LocalisationData, settings: &Settings, render_space: Option<&NmFrame>, results: &mut Results<T>) -> Result<(), Error>
{
    let localisations = localisation_data.to_localisations().map_err(Error::parse)?;
    let half_resolution = generate_half_frc(&localisations, settings, render_space)?;
    results.add_frc_half_split_results(half_resolution);
    let zip_resolution = generate_zip_split_frc(&localisations, settings, render_space)?;
    results.add_frc_zip_split_results(zip_resolution);
    let drift_resolution = generate_drift_split_frc(&localisations, settings, render_space)?;
    results.add_frc_drift_split_results(drift_resolution);
    generate_random_frc(&localisations, settings, render_space, results)
}

pub fn generate_frc_data<T>(settings: &Settings, render_space: Option<&NmFrame>, results: &mut Results<T>) -> Result<(), Error>
{
    let localisation_data = settings.localisation_data();
    match localisation_data
    {
        None => Ok(()),
        Some(l) => 
        {
            let localisation_file = l.filepath();
            println!("Generating FRC data with {localisation_file}");
            _generate_frc_data(l, settings, render_space, results)
        }
    }
}

fn frc_from_data<P: AsRef<Path>>(frc_data: &FRCData, settings: &Settings, output_directory: P) -> Result<Resolution, Error>
{
    let a_image = frc_data.image_a();    
    let _ = fs_extra::copy_file(a_image, io::frc_image_location_a(&output_directory))?;

    let b_image = frc_data.image_b();
    let _ = fs_extra::copy_file(b_image, io::frc_image_location_b(&output_directory))?;

    println!("Generating FRC data with {} and {}", a_image, b_image);
    let a_image = utils::read_image(a_image)?;
    let b_image = utils::read_image(b_image)?;
    let _ = fs::create_dir_all(&output_directory)?;
    frc_with(a_image, b_image, settings, output_directory)
}

pub fn generate_non_linear_frc_data<T>(settings: &Settings, results: &mut Results<T>) -> Result<(), Error>
{
    match settings.half_split_data()
    {
        None => {},
        Some(image_data) => 
        {
            let output_directory = io::frc_half_split_directory(settings.output_directory());
            let resolution = frc_from_data(image_data, settings, output_directory)?;
            let _ = results.add_frc_half_split_results(resolution);
        }
    }
    match settings.zip_split_data()
    {
        None => {},
        Some(image_data) => 
        {
            let output_directory = io::frc_zip_split_directory(settings.output_directory());
            let resolution = frc_from_data(image_data, settings, output_directory)?;
            let _ = results.add_frc_zip_split_results(resolution);
        }
    }
    match settings.drift_split_data()
    {
        None => {},
        Some(image_data) => 
        {
            let output_directory = io::frc_drift_split_directory(settings.output_directory());
            let resolution = frc_from_data(image_data, settings, output_directory)?;
            let _ = results.add_frc_drift_split_results(resolution);
        }
    }
    Ok(())
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn calculate_block_size_test() 
    {
        let block_size = calculate_block_size(100, 10);
        assert_eq!(block_size, 10);
    }

    #[test]
    fn calculate_block_size_test_20() 
    {
        let block_size = calculate_block_size(100, 20);
        assert_eq!(block_size, 5);
    }

    #[test]
    fn calculate_block_size_test_bad_division() 
    {
        let block_size = calculate_block_size(10, 3);
        assert_eq!(block_size, 3);
    }
}