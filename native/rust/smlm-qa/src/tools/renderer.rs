use crate::{Error};
use crate::settings::{LocalisationData};
use crate::results::Results;

use locs::UncertainLocalisation;
use renderer::{RenderResult, Properties as RenderProperties};
use renderer::config::{Config as RenderConfig};

use std::io::{Error as IoError};
use std::path::Path;

fn determine_localisation_precision<T: UncertainLocalisation>(localisations: &[T]) -> f64
{
    let n = localisations.len() as f64;
    let total = localisations.iter().map(|l| l.uncertainty()).sum::<f64>();
    total / n
}

fn _reconstrcut_data(localisation_data: &LocalisationData, renderer_config: &RenderConfig, results: Option<&mut Results<String>>) -> Result<RenderResult, Error>
{
    let localisations = localisation_data.to_localisations().map_err(Error::parse)?;
    match results
    {
        None => {},
        Some(r) => 
        {
            let precision = determine_localisation_precision(&localisations);
            r.add_mean_precision_results(precision);
        }
    }
    renderer::render_localisations(renderer_config, &localisations).map_err(Error::Render)
}

fn copy_data<P: AsRef<Path>>(localisation_data: &LocalisationData, output_directory: P) -> Result<(), IoError>
{
    let source = localisation_data.filepath();
    let dest = output_directory.as_ref().join("localisation_data");
    println!("Copying {} to {}", source, dest.display());
    std::fs::copy(source, dest).map(|_| ())
}

pub fn reconstrcut_data<P: AsRef<Path>>(localisation_data: &LocalisationData, output_directory: P, config: &RenderConfig, results: Option<&mut Results<String>>) -> Result<RenderProperties, Error>
{
    let localisation_file = localisation_data.filepath();
    println!("Reconstructing {localisation_file}");
    let ref_recon_dir = output_directory.as_ref();
    if !ref_recon_dir.exists()
    {
        let _ = std::fs::create_dir_all(ref_recon_dir)?;
    }
    copy_data(localisation_data, output_directory)?;
    _reconstrcut_data(localisation_data, config, results).map(|r| r.properties().clone())
}

#[cfg(test)]
mod tests 
{
    use locs::AllocatedLocalisation;

    use super::*;
    #[test]
    fn localisation_precision_is_mean() 
    {
        let localisations = [AllocatedLocalisation::new(0, 0.0, 0.0, 0.0, 0.0, 1.0),
                             AllocatedLocalisation::new(0, 0.0, 0.0, 0.0, 0.0, 2.0)];
        assert_eq!(determine_localisation_precision(&localisations), 1.5)
    }
}