use crate::{Error};
use crate::settings::{LocalisationData};
use crate::results::Results;

use locs::UncertainLocalisation;
use renderer::{RenderResult, Properties as RenderProperties};
use renderer::config::{Config as RenderConfig};

use std::io::{Error as IoError};
use std::path::Path;

/// Mean localisation precision, over the localisations that actually carry one.
///
/// Returns `None` when none of them do, which is the case for any fitter that reports no
/// uncertainty and fills the column with a placeholder. That `None` is the whole point: it is
/// what lets the limiting-precision assessment fall back to its stated assumption and say so in
/// the report, instead of averaging a column of zeros into a mean precision of zero and scoring
/// an automatic fail against the data rather than against the fitter.
///
/// Localisations are counted individually rather than the file being judged as a whole, so a
/// table where only some rows have a usable value still yields a mean over those rows.
fn determine_localisation_precision<T: UncertainLocalisation>(localisations: &[T]) -> Option<f64>
{
    let mut n = 0u64;
    let mut total = 0.0;
    for l in localisations
    {
        if l.has_measured_uncertainty()
        {
            n += 1;
            total += l.uncertainty();
        }
    }
    if n == 0 {None} else {Some(total / n as f64)}
}

fn _reconstrcut_data(localisation_data: &LocalisationData, renderer_config: &RenderConfig, results: Option<&mut Results<String>>) -> Result<RenderResult, Error>
{
    let localisations = localisation_data.to_localisations().map_err(Error::parse)?;
    match results
    {
        None => {},
        Some(r) => 
        {
            match determine_localisation_precision(&localisations)
            {
                // Leaving it unset is deliberate: Results::mean_precision() stays None and every
                // consumer takes its "precision not available" path.
                None => {},
                Some(precision) => r.add_mean_precision_results(precision)
            }
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
    use super::*;

    use locs::AllocatedLocalisation;

    fn with_uncertainty(uncertainty: f64) -> AllocatedLocalisation
    {
        AllocatedLocalisation::new(1, 0.0, 0.0, 100.0, 1000.0, uncertainty)
    }

    #[test]
    fn localisation_precision_is_mean()
    {
        let localisations = [AllocatedLocalisation::new(0, 0.0, 0.0, 0.0, 0.0, 1.0),
                             AllocatedLocalisation::new(0, 0.0, 0.0, 0.0, 0.0, 2.0)];
        assert_eq!(determine_localisation_precision(&localisations), Some(1.5))
    }

    #[test]
    fn mean_precision_of_measured_values()
    {
        let localisations = [with_uncertainty(10.0), with_uncertainty(20.0)];
        assert_eq!(determine_localisation_precision(&localisations), Some(15.0));
    }

    #[test]
    fn a_column_of_zeros_is_no_measurement()
    {
        // The placeholder a fitter with no uncertainty writes. Averaging it would report a mean
        // precision of 0 nm and fail the data for the fitter's limitation.
        let localisations = [with_uncertainty(0.0), with_uncertainty(0.0)];
        assert_eq!(determine_localisation_precision(&localisations), None);
    }

    #[test]
    fn nan_and_negative_are_no_measurement()
    {
        let localisations = [with_uncertainty(f64::NAN), with_uncertainty(-5.0)];
        assert_eq!(determine_localisation_precision(&localisations), None);
    }

    #[test]
    fn placeholders_do_not_drag_down_a_real_mean()
    {
        // A mean over all four would give 7.5 nm, which is better than any real localisation in
        // the file and is an artefact of counting the placeholders.
        let localisations = [with_uncertainty(10.0), with_uncertainty(20.0),
                             with_uncertainty(0.0), with_uncertainty(0.0)];
        assert_eq!(determine_localisation_precision(&localisations), Some(15.0));
    }

    #[test]
    fn no_localisations_is_no_measurement()
    {
        let localisations: [AllocatedLocalisation; 0] = [];
        assert_eq!(determine_localisation_precision(&localisations), None);
    }
}
