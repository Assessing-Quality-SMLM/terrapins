use crate::{Image, OwnedImage, Pad};

use sig_proc::convolution::{checked_fft_convolve, direct_convolve as direct_convolve_imp};

fn frequency_convolve<I: Image<Data=f64>, J: Image<Data=f64> + Pad>(image: I, mut kernel: J) -> Result<OwnedImage<f64>, String>
{
    let n_rows = image.n_rows();
    let n_cols = image.n_cols();
    let _ = kernel.pad_to(n_rows, n_cols).map_err(|_| format!("Error padding window"))?;
    let new_image_data = checked_fft_convolve(image.data(), kernel.data(), n_rows, n_cols).map_err(|e| e.to_string())?;
    let mut shifted_data = vec![0.0; n_rows * n_cols];
    sig_proc::fft::fft_shift(&new_image_data, &mut shifted_data, n_rows, n_cols);
    Ok(OwnedImage::new((n_rows, n_cols), shifted_data))
}

fn direct_convolve<I: Image<Data=f64>, J: Image<Data=f64> + Pad>(image: I, kernel: J) -> Result<OwnedImage<f64>, String>
{
    let data = direct_convolve_imp(image.data(), image.n_rows(), image.n_cols(), kernel.data(), kernel.n_rows(), kernel.n_cols());
    Ok(OwnedImage::new((image.n_rows(), image.n_cols()), data))
}

#[cfg(test)]
mod tests 
{
    use crate::Crop;

    use super::*;

    // #[test]
    // fn convolve_test() 
    // {
    //     use tiff_wrap::{Tiff};
    //     let mut reader = Tiff::read("/home/nik/Documents/repositories/renderer/rust/f2i/image.tiff").unwrap();
    //     let image_data = reader.next().unwrap().to_f64().unwrap();
    //     let mut image = OwnedImage::new((512, 512), image_data);
    //     image.central_crop(300, 300);
    //     image.take_rows(67);
    //     image.drop_rows(92);
    //     image.take_cols(139);
    //     image.drop_cols(55);
    //     let kernel = OwnedImage::new((3, 3), vec![1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0]);
    //     let new_image = frequency_convolve(&image, kernel).unwrap();
    //     let kernel = OwnedImage::new((3, 3), vec![1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0]);
    //     let other_new_image = direct_convolve(&image, kernel).unwrap();
    //     image.plot(Some("original"));
    //     new_image.plot(Some("frequency convolved"));
    //     other_new_image.plot(Some("direct convolved"));
    //     assert_eq!(true, false)
    // }
}