pub extern crate smlm_imp as imp;
pub extern crate smlm_locs as locs;
extern crate nalgebra as na;
extern crate num_traits;
// extern crate nv_nlopt;
extern crate rand;
pub extern crate smlm_sig_proc as sig_proc;

#[cfg(feature = "plot")]
extern crate gnuplot;

pub use self::config::{Config};
// pub use self::corrections::{estimate_Q, QEstimationResult, Q_norm, refine_Q_and_sigmas, RefineQData, UncertaintyMethod, HqMethod, Filter, Estimation, Config as CorrectionsConfig};
pub use self::frc::{get_frc_result};
pub use self::frc_result::FrcResult;
pub use self::loess::{Options as LoessOptions};
// pub use self::solver_options::{Options as OptimiserOptions};

// pub mod analysis;
mod config;
pub mod filters;
pub mod constants;
// pub mod uncertainty_sampling;
// pub mod corrections;
mod frc;
mod frc_result;
mod loess;
// mod maths;
// mod nans;
pub mod plotting;
mod resolution;
// mod solver_options;
pub mod thresholds;
mod utils;

pub use crate::resolution::{Resolution};
pub use crate::thresholds::{Threshold};

use imp::{Image, Crop, Fft, OwnedImage};

use sig_proc::fft::{c64};
use sig_proc::filters::{Window, Tukey, Error as SigError};

use std::{fmt::{Display, Error as FmtError, Formatter}, usize};

const NO_CROSSING : &str = "No Crossing";
const THRESHOLD_ERROR : &str = "Threshold Error";
const NOT_ENOUGH_DATA : &str = "Not Enough Data";
const PARSE_ERROR : &str = "Parsing Error";


#[derive(Debug, PartialEq)]
pub enum IntersectionError 
{
    NoCrossing,
    ThresholdError,
    NotEnoughData,
    ParseError,
}

impl IntersectionError
{
    pub fn as_str(&self) -> &str
    {
        match self
        {
            Self::NoCrossing => NO_CROSSING,
            Self::ThresholdError => THRESHOLD_ERROR,
            Self::NotEnoughData => NOT_ENOUGH_DATA,
            Self::ParseError => PARSE_ERROR
        }
    }

    pub fn from(error: &str) -> Self
    {
        match error
        {
            NO_CROSSING => Self::NoCrossing,
            THRESHOLD_ERROR => Self::ThresholdError,
            NOT_ENOUGH_DATA => Self::NotEnoughData,
            _ => Self::ParseError
        }
    }
}

impl Display for IntersectionError
{
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError> 
    { 
        write!(f, "{}", self.as_str())
    }
}


#[derive(Debug, PartialEq)]
pub enum Error 
{
    Crop,
    SignalProcessing(SigError),
    F64Conversion(usize),
    DifferentRows(usize, usize),
    DifferentCols(usize, usize),
    NonSquare(usize, usize),
    Intersection(IntersectionError),
    Threshold(u8),
    Custom(String)
}

impl Error
{
    fn message(&self) -> String
    {
         match self
        {
            Self::Crop => format!("Cropping Error"),
            Self::SignalProcessing(e) => e.to_string(),
            Self::F64Conversion(n) => format!("Could not create f64 from: {n}"),
            Self::DifferentRows(rows_1, rows_2) => format!("Different numbers of rows: {rows_1} vs {rows_2}"),
            Self::DifferentCols(cols_1, cols_2) => format!("Different numbers of columns: {cols_1} vs {cols_2}"),
            Self::NonSquare(n_rows, n_cols) => format!("Image is not square rows: {n_rows} columns: {n_cols}"),
            Self::Intersection(e) => format!("Could not determine Intersection: {e}"),
            Self::Threshold(value) => format!("{value} not recognised as threshold method"),
            Self::Custom(e) => e.to_string()
        }
    }
}

impl Display for Error
{
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError> 
    { 
        write!(f, "{}", self.message())
    }
}

fn shrink_cols<I: Image + Crop>(mut image: I, n_cols: usize) -> Result<(), Error>
{
    let difference = image.n_cols() - n_cols;
    image.drop_cols(difference).map_err(|_| Error::Crop)
}

fn shrink_rows<I: Image + Crop>(mut image: I, n_rows: usize) -> Result<(), Error>
{
    let difference = image.n_rows() - n_rows;
    image.drop_rows(difference).map_err(|_| Error::Crop)
}

fn shrink_to_square<I: Image + Crop, J: Image + Crop>(mut image_1: I, mut image_2: J) -> Result<(), Error>
{
    let values = [image_1.n_rows(), image_1.n_cols(), image_2.n_rows(), image_2.n_cols()];
    let min = *(values.iter().min().unwrap()); // iterator is never empty
    shrink_rows(&mut image_1, min).and_then(|_| shrink_rows(&mut image_2, min))
                                  .and_then(|_| shrink_cols(&mut image_1, min))
                                  .and_then(|_| shrink_cols(&mut image_2, min))
}

// assuming that shrink to square has happened by this point
fn apply_filter<I: Image<Data=f64>>(image: I, filter_name: &str, parameters: &[f64]) -> Result<OwnedImage<I::Data>, String>
{
    match filter_name
    {
        filters::TUKEY => Tukey::create(parameters).and_then(|w| imp::filter::apply_window(image, w))
                                            .map_err(|e| e.to_string()),
        _ => Err(format!("filter {filter_name} not found"))
    }
}

fn _frc<I: Image<Data=f64> + Fft<Output=OwnedImage<c64>>, 
        J : Image<Data=f64> + Fft<Output=OwnedImage<c64>>>(image_1: I, 
                                                           image_2: J, 
                                                           filter: Option<(&str, &[f64])>) -> Result<FrcResult, Error>
{
    if filter.is_some()
    {
        let (filter_name, parameters) = filter.unwrap();
        println!("Applying {filter_name} with parameters: {:?}", parameters);
        let filt_1 = apply_filter(image_1, filter_name, &parameters).map_err(Error::Custom)?;
        let filt_2 = apply_filter(image_2, filter_name, &parameters).map_err(Error::Custom)?;
        frc::frc(filt_1, filt_2)
    }
    else
    {
        frc::frc(image_1, image_2)
    }
}

pub fn frc_result<I: Image<Data=f64> + Crop + Fft<Output=OwnedImage<c64>>, 
           J : Image<Data=f64> + Crop + Fft<Output=OwnedImage<c64>>>(mut image_1: I, 
                                                                     mut image_2: J, 
                                                                     filter: Option<(&str, &[f64])>) -> Result<FrcResult, Error>
{
    let _ = shrink_to_square(&mut image_1, &mut image_2)?;

    // image_1.plot(Some("Image 1"));
    //let plot_data = image_1.shifted_fft().data().iter().map(|c| c.abs().ln()).collect();
    // OwnedImage::new(image_1.shape(), plot_data).plot(Some("Image 1 fft"));

    // filt_1.plot(Some("Image 1 filtered"));
    //let plot_data = filt_1.shifted_fft().data().iter().map(|c| c.abs().ln()).collect();
    // OwnedImage::new(filt_1.shape(), plot_data).plot(Some("Image 1 filtered fft"));
    
    let frc_result = _frc(&image_1, &image_2, filter)?;
    // let yy = frc_result.smoothed_frcs();
    // let qs = frc_result.q_s(config.L());
    // println!("qs: {:?}", qs);
    // plotting::plot_x_and_y(&qs, frc_result.frcs(), "FRC");
    // plotting::plot_x_and_y(&qs, &yy, "Smoothed FRC");
    Ok(frc_result)
}

pub fn frc<I: Image<Data=f64> + Crop + Fft<Output=OwnedImage<c64>>, 
           J : Image<Data=f64> + Crop + Fft<Output=OwnedImage<c64>>>(image_1: I, 
                                                                     image_2: J, 
                                                                     threshold: Threshold,
                                                                     filter: Option<(&str, &[f64])>,
                                                                     config: &Config) -> Result<Resolution, Error>
{
    frc_result(image_1, image_2, filter).map(|result| result.get_resolution(config.L(), threshold))
}

#[cfg(test)]
mod tests 
{
    use imp::OwnedImage;

    use super::*;

    #[test]
    fn conversion_error_string()
    {
        let expected = "Could not create f64 from: 2";
        assert_eq!(Error::F64Conversion(2).to_string(), expected)
    }

    #[test]
    fn different_rows_error_message()
    {
        let expected = "Different numbers of rows: 2 vs 1";
        assert_eq!(Error::DifferentRows(2, 1).to_string(), expected)
    }

    #[test]
    fn different_cols_error_message()
    {
        let expected = "Different numbers of columns: 2 vs 1";
        assert_eq!(Error::DifferentCols(2, 1).to_string(), expected)
    }

    #[test]
    fn already_square() 
    {
        let mut image_1 = generate_image(5, 5);
        let mut image_2 = generate_image(5, 5);
        assert_eq!(shrink_to_square(&mut image_1, &mut image_2).is_ok(), true);
        assert_eq!(image_1.shape(), (5, 5));
        assert_eq!(image_2.shape(), (5, 5));
        assert_eq!(image_1.data(), (0..25).collect::<Vec<_>>());
        assert_eq!(image_2.data(), (0..25).collect::<Vec<_>>());
    }

    #[test]
    fn rows_out() 
    {
        let mut image_1 = generate_image(5, 5);
        let mut image_2 = generate_image(4, 5);
        assert_eq!(shrink_to_square(&mut image_1, &mut image_2).is_ok(), true);
        assert_eq!(image_1.shape(), (4, 4));
        assert_eq!(image_2.shape(), (4, 4));
        assert_eq!(image_1.data(), [0, 1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18]);
        assert_eq!(image_2.data(), [0, 1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18])
    }

    #[test]
    fn cols_out() 
    {
        let mut image_1 = generate_image(5, 5);
        let mut image_2 = generate_image(5, 4);
        assert_eq!(shrink_to_square(&mut image_1, &mut image_2).is_ok(), true);
        assert_eq!(image_1.shape(), (4, 4));
        assert_eq!(image_2.shape(), (4, 4));
        assert_eq!(image_1.data(), [0, 1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18]);
        assert_eq!(image_2.data(), [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15])
    }

    #[test]
    fn disjoint_images() 
    {
        let mut image_1 = generate_image(5, 4);
        let mut image_2 = generate_image(4, 5);
        assert_eq!(shrink_to_square(&mut image_1, &mut image_2).is_ok(), true);
        assert_eq!(image_1.shape(), (4, 4));
        assert_eq!(image_2.shape(), (4, 4));
        assert_eq!(image_1.data(), [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]);
        assert_eq!(image_2.data(), [0, 1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18])
    }

    fn generate_image(n_rows: usize, n_cols: usize) -> OwnedImage<usize>
    {
        let (shape, data) = generate(n_rows, n_cols);
        OwnedImage::new(shape, data)
    }

    fn generate(n_rows: usize, n_cols: usize) -> ((usize, usize),Vec<usize>)
    {
        ((n_rows, n_cols), (0..(n_rows * n_cols)).collect())
    }

    #[test]
    fn parse_intersection_error()
    {
        assert_eq!(IntersectionError::from(NO_CROSSING), IntersectionError::NoCrossing);
        assert_eq!(IntersectionError::from(NOT_ENOUGH_DATA), IntersectionError::NotEnoughData);
        assert_eq!(IntersectionError::from(THRESHOLD_ERROR), IntersectionError::ThresholdError);
        assert_eq!(IntersectionError::from(PARSE_ERROR), IntersectionError::ParseError);
        assert_eq!(IntersectionError::from(""), IntersectionError::ParseError)
    }
}