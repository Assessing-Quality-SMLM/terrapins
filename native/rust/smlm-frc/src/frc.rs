use crate::{Error, IntersectionError};
use crate::frc_result::FrcResult;
use crate::thresholds::Thresholder;


use imp::{c64, Fft, Image, OwnedImage};
use imp::circles::{Method};

use std::io::Write;

#[cfg(feature = "plot")]
use gnuplot::{Figure, AxesCommon};

pub fn get_threshold_curve<T: Thresholder>(n_pixels: &[usize], thresholder : T) -> Vec<f64>
{
    n_pixels.iter()
            .enumerate()
            //                                           + 1 as we start from ring of radius 1
            .flat_map(|(r, n)| thresholder.get_threshold(r + 1, *n))
            .collect()
}


pub fn find_intersection_with(frcs: &[f64], threshold_curve: &[f64]) -> Result<usize, IntersectionError>
{
    let data = frcs.iter().zip(threshold_curve.iter()).collect::<Vec<_>>();
    // check if starting above the threshold
    let (frc, t) = data.iter().next().ok_or_else(|| IntersectionError::NotEnoughData)?;
    let mut initialised = frc > t;

    let min_width = 5;  
    let mut count = 1;
    for window in data.windows(min_width)
    {
        if !initialised
        {
            initialised = window.iter().all(|(frc, t)| frc > t);
        }
        else
        {
            let crossed = window.iter().all(|(frc, t)| frc <= t);
            if crossed
            {
                return Ok(count)
            }
        }
        count += 1;
    }
    Err(IntersectionError::NoCrossing)
}

pub fn to_resolution(value: f64) -> f64
{
    1.0 / value
}

/// perimeter size is of constant spatial frequency -> unit is pixels
/// L pixel size in image space or size of the field of view in nm
/// this changes the unit from spatial frequency for width in pixels or nm^1 for width in pixels * pixel_size(nm)
#[allow(non_snake_case)]
pub fn calculate_q(perimeter_size: usize, L: f64) -> f64
{
    perimeter_size as f64 / L // 1 / L is pixel size in fourier space
}

pub fn get_intersection_resolution(qs: &[f64], frcs: &[f64], threshold_curve: &[f64]) -> Result<f64, IntersectionError>
{
    let intersection_idx = find_intersection_with(frcs, threshold_curve);
    intersection_idx.map(|i| qs[i]).map(to_resolution)
}

#[cfg(feature = "plot")]
fn plot_perimeter(indicies: &Vec<usize>, radius: f64, n_cols: usize) -> ()
{
    // let perimeter = imp::perimeter_image(&image, radius);
    let mut x = Vec::new();
    let mut y = Vec::new();
    // for (index, value) in perimeter.data().iter().enumerate()
    for idx in indicies
    {
        let (row, col) = imp::utils::get_coords(*idx, n_cols);
        // let (row, col) = perimeter.get_coords(index);
        // if *value
        {
            x.push(row);
            y.push(col);
        }
    }
    let mut figure = Figure::new();
    figure.axes2d()
          .set_title("perimeter", &[])
          .points(x, y, &[]);
    figure.show_and_keep_running().unwrap();
}

#[allow(unused)]
fn write_data<W: Write>(mut writer : W, data: &Vec<c64>)
{
    for value in data
    {
        let _ = writeln!(writer, "{},{}", value.re, value.im);
    }
}

fn compute_rings(width: usize, numerator: &OwnedImage<c64>, f_1: &OwnedImage<f64>, f_2: &OwnedImage<f64>) -> FrcResult
{
    // this works because the image is square
    let max_radius = width / 2;
    let mut frc_result = FrcResult::new(max_radius - 1);
    for radius in 1..max_radius
    {
        // println!("calculating for {radius} / {max_radius}");
        let mask = numerator.perimimeter_indicies_with(Method::Bresenham, radius); // this works because all images are the same size
        let mask_size = mask.len();
        //plot_perimeter(&mask, radius as f64, numerator.n_cols());
        let num : c64 = numerator.apply_index_mask(&mask).iter().sum();
        let s_1 : f64 = f_1.apply_index_mask(&mask).iter().sum();
        let s_2 : f64 = f_2.apply_index_mask(&mask).iter().sum();
        let denominator = (s_1 * s_2).sqrt();
        let _frc = frc_result.set_value(radius - 1, num.re, denominator, mask_size);
        // println!("frc: {frc}");
    }
    frc_result
}

pub fn get_frc_result<I: Image<Data=f64> + Fft<Output=OwnedImage<c64>>, J: Image<Data=f64> + Fft<Output=OwnedImage<c64>>>(image_1: I, image_2: J) -> FrcResult
{
    let fourier1 = image_1.shifted_fft();
    let fourier2 = image_2.shifted_fft();
    let fourier2_conjugate: OwnedImage<c64> = fourier2.map(|c| c.conj());

    let numerator = fourier1.mul(fourier2_conjugate);
    let f = |c:&c64| c.norm_sqr();
    let f_1 = fourier1.map(f);
    let f_2 = fourier2.map(f);
   
    compute_rings(image_1.n_cols(), &numerator, &f_1, &f_2)
}

pub fn frc<I: Image<Data=f64> + Fft<Output=OwnedImage<c64>>, J: Image<Data=f64> + Fft<Output=OwnedImage<c64>>>(image_1: I, image_2: J) -> Result<FrcResult, Error>
{
    if image_1.n_rows() != image_2.n_rows()
    {
        return Err(Error::DifferentRows(image_1.n_rows(), image_2.n_rows()))
    }

    if image_1.n_cols() != image_2.n_cols()
    {
        return Err(Error::DifferentCols(image_1.n_cols(), image_2.n_cols()))
    }

    if !image_1.is_square()
    {
        return Err(Error::NonSquare(image_1.n_rows(), image_1.n_cols()))
    }

    Ok(get_frc_result(image_1, image_2))
}

#[cfg(test)]
mod tests
{
    use super::*;

    use crate::thresholds::{Threshold, OneSeventh, ONESEVENTH};

    use imp::{ImageMut};

    fn default_frc<I: Image<Data=f64> + Fft<Output=OwnedImage<c64>>, J: Image<Data=f64> + Fft<Output=OwnedImage<c64>>>(image_1: I, image_2: J) -> Result<f64, Error>
    {
        frc(image_1, image_2).map(|result| result.get_resolution(1.0, Threshold::OneSeventh).as_value())
    }

    fn find_intersection<T: Thresholder>(frcs: &[f64], n_pixels: &[usize], thresholder : T) -> Result<usize, IntersectionError>
    {
        let threshold_curve = get_threshold_curve(n_pixels, thresholder);
        find_intersection_with(frcs, &threshold_curve)
    }


    #[test]
    fn different_sized_rows()
    {
        let image_1: OwnedImage<f64> = OwnedImage::new((1, 2), Vec::new());
        let image_2: OwnedImage<f64> = OwnedImage::new((2, 1), Vec::new());
        assert_eq!(default_frc(image_1, image_2).unwrap_err(), Error::DifferentRows(1, 2))
    }

    #[test]
    fn different_sized_cols()
    {
        let image_1: OwnedImage<f64> = OwnedImage::new((2, 2), Vec::new());
        let image_2: OwnedImage<f64> = OwnedImage::new((2, 1), Vec::new());
        assert_eq!(default_frc(image_1, image_2).unwrap_err(), Error::DifferentCols(2, 1))
    }

    #[test]
    fn intersection_all_below_threshold()
    {
        let value  = ONESEVENTH - 0.1;
        let frcs = (0..10).map(|_| value).collect::<Vec<f64>>();
        let n_pixels = (0..frcs.len()).collect::<Vec<usize>>();
        let error = find_intersection(&frcs, &n_pixels, OneSeventh).unwrap_err();
        assert_eq!(error, IntersectionError::NoCrossing)
    }

    #[test]
    fn intersection_all_above_threshold()
    {
        let value  = ONESEVENTH + 0.1;
        let frcs = (0..10).map(|_| value).collect::<Vec<f64>>();
        let n_pixels = (0..frcs.len()).collect::<Vec<usize>>();
        let error = find_intersection(&frcs, &n_pixels, OneSeventh).unwrap_err();
        assert_eq!(error, IntersectionError::NoCrossing)
    }

    #[test]
    fn intersection_crossing_not_long_enough()
    {
        let frcs = [ONESEVENTH + 0.1, ONESEVENTH - 0.1, ONESEVENTH + 0.1];
        let n_pixels = (0..frcs.len()).collect::<Vec<usize>>();
        let error = find_intersection(&frcs, &n_pixels, OneSeventh).unwrap_err();
        assert_eq!(error, IntersectionError::NoCrossing)
    }

    #[test]
    fn intersection_crossing_ok()
    {
        let v = ONESEVENTH - 0.1;
        let frcs = [ONESEVENTH + 0.1, v, v, v, v, v];
        let n_pixels = (0..frcs.len()).collect::<Vec<usize>>();
        let value = find_intersection(&frcs, &n_pixels, OneSeventh).unwrap();
        assert_eq!(value, 2)
    }

    #[test]
    fn unstable_crossing()
    {
        let u = ONESEVENTH + 0.1;
        let v = ONESEVENTH - 0.1;
        let frcs = [u, v, u, v, u, v];
        let n_pixels = (0..frcs.len()).collect::<Vec<usize>>();
        let value = find_intersection(&frcs, &n_pixels, OneSeventh).unwrap_err();
        assert_eq!(value, IntersectionError::NoCrossing);
    }

    #[test]
    fn must_be_downwards_crossing()
    {
        let u = ONESEVENTH + 0.1;
        let v = ONESEVENTH - 0.1;
        let frcs = [v, v, v, v, v, v, u, u, u, u, u];
        let n_pixels = (0..frcs.len()).collect::<Vec<usize>>();
        let value = find_intersection(&frcs, &n_pixels, OneSeventh).unwrap_err();
        assert_eq!(value, IntersectionError::NoCrossing);
    }

    #[test]
    fn can_start_below_then_come_down()
    {
        let u = ONESEVENTH + 0.1;
        let v = ONESEVENTH - 0.1;
        let frcs = [v, v, v, v, v, v, u, u, u, u, u, v, v, v, v, v];
        let n_pixels = (0..frcs.len()).collect::<Vec<usize>>();
        let value = find_intersection(&frcs, &n_pixels, OneSeventh).unwrap();
        assert_eq!(value, 12);
    }

    #[test]
    fn compute_rings_test()
    {
        let size = 10;
        let square = size * size;
        let mut numerator = OwnedImage::<c64>::zeros((size, size));
        let mut f_1 = OwnedImage::zeros((size, size));
        let mut f_2 = OwnedImage::zeros((size, size));
        for idx in 0..square
        {
            numerator.data_mut()[idx] = c64::new(idx as f64, 0.0);
            f_1.data_mut()[idx] = (square - idx) as f64;
            f_2.data_mut()[idx] = (square - idx) as f64;
        }
        let rings = compute_rings(size, &numerator, &f_1, &f_2).frcs().to_vec();
        let expected = [1.2222222222222223, 1.2222222222222223, 1.2222222222222223, 1.2222222222222223];
        assert_eq!(rings, expected)
    }
}