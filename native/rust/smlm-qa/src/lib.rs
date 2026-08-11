extern crate smlm_imp as imp;
extern crate smlm_locs as locs;
// extern crate plotters;
extern crate smlm_renderer as renderer;
extern crate smlm_frc as frc;
extern crate serde;
extern crate serde_json;
extern crate smlm_tiff as tiff_wrap;
extern crate time;
extern crate walkdir;
extern crate zip;

pub use self::locs::constants::{PARSE_INSTRUCTIONS as LOCALISATION_PARSE_INSTRUCTIONS};
pub use self::utils::sr_pixel_size;

mod assessment;
mod constants;
mod error;
mod fs_extra;
mod filesystem;
mod io;
mod results;
pub mod settings;
mod tools;
mod utils;

use crate::assessment::{Report};
use crate::error::{Error};
use crate::results::Results;
use crate::settings::{Settings};
use crate::tools::squirrel::{Settings as SquirrelSettings, SquirrelResult};

use renderer::{NmFrame, Properties as RenderProperties};
use tiff_wrap::{Tiff, TiffWrapError};

use std::path::{Path};

fn get_global_space_from<P: AsRef<Path>>(tiff_file: P, pixel_size: f64) -> Result<NmFrame, TiffWrapError>
{
    let mut tiff = Tiff::read(tiff_file)?;
    let height_nm = tiff.height().map(|x| x as f64 * pixel_size)?;
    let width_nm = tiff.width().map(|x| x as f64 * pixel_size)?;
    Ok(NmFrame::from(0.0, 0.0, width_nm, height_nm))
}

fn get_global_space_with<P: AsRef<Path>>(tiff_file: P, pixel_size: f64) -> Result<NmFrame, String>
{
    let p = tiff_file.as_ref();
    if p.exists()
    {
        get_global_space_from(p, pixel_size).map_err(|e| e.to_string())
    }
    else 
    {
        Err(format!("{} does not exist", p.display()))
    }
}


fn get_camera_space(settings: &Settings) -> Option<NmFrame>
{
    let pixel_size = settings.camera_pixel_size_nm();
    match settings.specified_widefield()
    {
        Some(p) => 
        {
            match get_global_space_with(p, pixel_size)
            {
                Ok(space) => return Some(space),
                Err(e) => 
                {
                    println!("Could not extract space due to {e}");
                }
            }
        }
        None => {},
    }
    match settings.image_stack()
    {
        None => None,
        Some(p) => 
        {
            match get_global_space_from(p, pixel_size)
            {
                Ok(space) => Some(space),
                Err(e) => 
                {
                    println!("Could not extract space due to: {e}");
                    None
                }
            }
        }
    }
}

fn generate_recon_data(settings: &Settings, render_space: Option<&NmFrame>, results: &mut Results<String>) -> Result<Option<NmFrame>, Error>
{
    let localisation_data = settings.localisation_data();
    match localisation_data
    {
        None => 
        {
            println!("Localisation data not specified");
            Ok(None)
        }
        Some(l) => 
        {
            let output_directory = settings.reference_recon_directory();
            let config = settings.reference_recon_setting(render_space)?;
            tools::renderer::reconstrcut_data(l, output_directory, &config, Some(results)).map(|p| p.data_frame().clone()).map(Some)
        }
    }
}

fn generate_hawk_recon_data(settings: &Settings, render_space: Option<&NmFrame>) -> Result<Option<RenderProperties>, Error>
{
    let localisation_data = settings.hawk_localisation_data();
    match localisation_data
    {
        None => 
        {
            println!("Hawked localisation data not provided");
            Ok(None)
        }
        Some(l) => 
        {
            let output_directory = settings.hawk_recon_directory();
            let config = settings.hawk_recon_setting(render_space)?;
            tools::renderer::reconstrcut_data(l, output_directory, &config, None).map(Some)
        }
    }
}

fn _generate_hawkman_data(settings: &Settings, results: &mut Results<String>) -> Result<(), Error>
{
    let settings = settings.to_hawkman_settings();
    let hawkman_result = tools::hawkman::run(&settings).map_err(Error::from)?;
    results.add_hawkman_results(hawkman_result);
    Ok(())
}

fn _hawkman_error_message(reference_exists: bool, test_exists: bool) -> String
{
    let mut core = format!("Missing");
    if !reference_exists
    {
        core.push_str(" reference (HAWK)");
    }
    if !reference_exists && !test_exists
    {
        core.push_str(" and test (Non HAWK)");
    }
    else if !test_exists
    {
        core.push_str(" test (Non HAWK)");
    }
    core.push_str(" - cannot generate HAWKMAN data");
    core
}

fn generate_hawkman_data(settings: &Settings, results: &mut Results<String>) -> Result<(), Error>
{
    let reference_image = settings.hawkman_reference_image();
    let test_image = settings.hawkman_test_image();
    if reference_image.exists() && test_image.exists()
    {
        println!("Running HAWMKMAN");
        _generate_hawkman_data(settings, results)
    }
    else 
    {
        println!("{}", _hawkman_error_message(reference_image.exists(), test_image.exists()));
        Ok(())
    }
}

fn generate_sum_of_frames_widefield(settings: &Settings) -> Result<(), Error>
{
    match settings.image_stack()
    {
        None => 
        {
            println!("Image stack not supplied - sum of frames widefield cannot be generated");
            Ok(())
        },
        Some(image_stack) => 
        {
            let output_directory = settings.output_directory();
            println!("Generating sum of frames widefield from {image_stack}");
            tools::widefield_generator::from_image_stack(image_stack, output_directory)
        }
    }
}

fn _squirrel_error_message(super_res: bool, widefield: bool) -> String
{
    let mut core = format!("Missing");
    if !super_res
    {
        core.push_str(" super res");
    }
    if !super_res && !widefield
    {
        core.push_str(" and widefield");
    }
    else if !widefield
    {
        core.push_str(" widefield");
    }
    core.push_str(&format!(" - cannot generate SQUIRREL data"));
    core
}

fn generate_squirrel_data(settings: &SquirrelSettings) -> Result<Option<SquirrelResult>, Error>
{
    let super_res_image = settings.super_res_image().exists();
    let widefield_image = settings.widefield_image().exists();
    if super_res_image && widefield_image
    {
        tools::squirrel::run(&settings).map_err(Error::from).map(Some)
    }
    else 
    {
        println!("{}", _squirrel_error_message(super_res_image, widefield_image));
        Ok(None)
    }
}

fn _generate_hawkman_and_squirrel(settings: &Settings, results: &mut Results<String>) -> Result<(), Error>
{
    println!("Generating HAWMKMAN output");
    let _ = generate_hawkman_data(settings, results)?;

    println!("Generating sum of frames widefield");
    let _ = generate_sum_of_frames_widefield(settings)?;

    // hawk or normal reference - both should have been copied by this point
    match settings.squirrel_reference_image()
    {
        None => 
        {
            println!("SQUIRREL has no super res reference image");
            return Ok(())
        }
        Some(image_name) => 
        {
            //output which reference using - want squirrel output to write down what happend
            println!("Using {} for SQUIRREL reference", image_name.display())
        }
    }
    
    match settings.non_linearity_squirrel_settings()
    {
        Err(e) => 
        {
            println!("Cannot run non linearity SQUIRREL due to: {e}")
        },
        Ok(sq_settings) =>
        {
            println!("Generating SQUIRREL output for non linearity");
            match generate_squirrel_data(&sq_settings)?
            {
                None => {},
                Some(data) => 
                {
                    results.add_generated_squirrel_results(data);
                }
            }
        }
    }

    println!("Copying Widefield image");
    let specified_widefield = settings.squirrel_specified_widefield();
    if specified_widefield.is_err()
    {

    }
    else 
    {
        let specified_widefield = specified_widefield.unwrap();
        let _ = fs_extra::copy_file(&specified_widefield, settings.squirrel_true_widefield_location())?;
        
        match settings.everything_else_squirrel_settings()
        {
            Err(e) => 
            {
                println!("Cannot run SQUIRREL due to: {e}");
            },
            Ok(sq_settings) => 
            {
                println!("Generating SQUIRREL output for all other errors");
                match generate_squirrel_data(&sq_settings)?
                {
                    None => {},
                    Some(data) => 
                    {
                        results.add_true_squirrel_results(data);
                    }
                }
            }
        }
    }
    Ok(())
}

fn _generate_localisation_based_metrics(settings: &Settings, results: &mut Results<String>) -> Result<(), Error>
{
    let render_space = get_camera_space(settings);
    println!("render space: {:?}", render_space);
    let recon_render_space = generate_recon_data(settings, render_space.as_ref(), results)?;
    println!("recon render space: {:?}", recon_render_space);
    let render_space = if render_space.is_some(){render_space} else{recon_render_space};
    println!("render space: {:?}", render_space);
    let _ = tools::frc::generate_frc_data(settings, render_space.as_ref(), results)?;
    
    let _hawk_render_space = generate_hawk_recon_data(settings, render_space.as_ref())?;    

    let _ = _generate_hawkman_and_squirrel(settings, results)?;
    Ok(())
}

fn _generate_metrics_using_images(settings: &Settings, results: &mut Results<String>) -> Result<(), Error>
{
    println!("Generating frc scores");
    let _ = tools::frc::generate_non_linear_frc_data(settings, results)?;

    println!("Copying reference image");
    match settings.specified_reference_image()
    {
        None => 
        {
            println!("No reference image specified");
        }
        Some(specified_reference) => 
        {
            println!("{}", specified_reference.display());
            let _ = fs_extra::copy_file(specified_reference, settings.hawkman_test_image())?;
        }
    }

    println!("Copying HAWK image");
    match settings.hawkman_specified_hawked_image()
    {
        None =>
        {
            println!("No HAWK image specified");
        }
        Some(specified_hawk) => 
        {
            println!("{}", specified_hawk.display());
            let _ = fs_extra::copy_file(specified_hawk, settings.hawkman_reference_image())?;
        }
    }

    let _ = _generate_hawkman_and_squirrel(settings, results)?;
    Ok(())
}

fn _generate_metrics(settings: &Settings) -> Result<Results<String>, Error>
{
    let output_directory = settings.output_directory();
    if !Path::new(&output_directory).exists()
    {
        let _ = std::fs::create_dir_all(&output_directory)?;
    }

    let settings_filename = io::settings_filename(&output_directory);
    let _ = io::settings::write_settings_to(settings_filename, settings)?;
    
    let mut results = Results::default();
    if settings.use_image_based_workflow()
    {
        println!("Using image based workflow");
        let _ = _generate_metrics_using_images(settings, &mut results)?;
    }
    else 
    {
        println!("Using localisation based workflow");
        let _ = _generate_localisation_based_metrics(settings, &mut results)?;
    }
    // let _ = extract_data(settings)?;
    Ok(results)
}

// fn _generate_metrics_and_finalise(settings: &Settings) -> Result<Results<String>, Error>
// {
//     let results = _generate_metrics(settings)?;
//     let cleanup = !settings.extract();
//     let _ = io::finalise_data(settings.output_directory(), cleanup)?;
//     Ok(results)
// }

// fn extract_data(settings: &Settings) -> Result<(), Error>
// {
//     if settings.extract()
//     {
//         let from_path = settings.output_directory();
//         let mut to_path = from_path.clone(); 
//         let new_filename = from_path.file_name()
//                                     .map(|n| format!("{}_data", n.display()))
//                                     .ok_or_else(|| format!("{} has no parent", from_path.display()))
//                                     .map_err(Error::extraction)?;
//         to_path.set_file_name(new_filename);
//         println!("extracting data to {}", to_path.display());
//         fs_extra::copy_dir_all(from_path, to_path).map_err(|e| Error::extraction(e.to_string()))
//     }
//     else 
//     {
//         Ok(())
//     }
// }

// pub fn assess_data(data_file: &str, settings: Option<&Settings>) -> Result<Report, Error>
// {
//     let (data_dir, results, created_settings) = io::read_results(data_file)?;
//     // println!("{:?}", data_dir);
//     // println!("{:?}", results);
//     let settings = settings.unwrap_or(&created_settings);
//     let report = assessment::assess_results(&results, settings.report_settings(), settings.output_directory());
//     let _ = io::finalise_data(&data_dir)?;
//     let _ = fs::remove_dir_all(data_dir)?;
//     Ok(report)
// }

pub fn generate_metrics_and_assess(settings: &Settings) -> Result<Report, Error>
{
    _generate_metrics(settings).map(|r| assessment::assess_results(settings, &r))
                               .and_then(|r| io::finalise_data(settings.output_directory(), !settings.extract()).map(|_| r))
}

pub fn extract<P: AsRef<Path>>(data_file: P) -> Result<(), Error>
{
    io::extract_data(data_file)
}

pub fn get_output_directory<P: AsRef<Path>>(working_directory: P, data_name: Option<&str>) -> String
{
    let od = io::output_directory(working_directory, data_name);
    io::path_to_string(&od)
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn hawkman_error_messages()
    {
        assert_eq!(_hawkman_error_message(true, false), "Missing test (Non HAWK) - cannot generate HAWKMAN data");
        assert_eq!(_hawkman_error_message(false, true), "Missing reference (HAWK) - cannot generate HAWKMAN data");
        assert_eq!(_hawkman_error_message(false, false), "Missing reference (HAWK) and test (Non HAWK) - cannot generate HAWKMAN data");
    }

    #[test]
    fn squirrel_error_messages()
    {
        assert_eq!(_squirrel_error_message(true, false), "Missing widefield - cannot generate SQUIRREL data");
        assert_eq!(_squirrel_error_message(false, true), "Missing super res - cannot generate SQUIRREL data");
        assert_eq!(_squirrel_error_message(false, false), "Missing super res and widefield - cannot generate SQUIRREL data");
    }
}
