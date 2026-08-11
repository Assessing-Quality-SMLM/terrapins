use crate::{Image};

use sig_proc::filters;
use filters::{Window, create_2d_window_from, apply_window as apply_window_imp};

use crate::OwnedImage;

pub fn apply_window<I: Image<Data = f64>, W: Window>(image: I, window: W) -> Result<OwnedImage<I::Data>, W::Error>
{
	create_2d_window_from(window, image.n_rows(), image.n_cols()).map(|w| apply_window_imp(image.data(), &w))
																 .map(|d| OwnedImage::new(image.shape(), d))
}

#[cfg(test)]
mod tests 
{
	use filters::Tukey;

	use super::*;

	#[test]
	fn apply_window_test() 
	{
		let data = (0..100).map(|x| x as f64).collect::<Vec<f64>>();
		let image = OwnedImage::new((10, 10), data);
		let filt_image = apply_window(image, Tukey::new(0.25)).unwrap();
		let expected = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.34662052361109, 11.63815572471545, 12.608002035108404, 13.577848345501359, 14.547694655894313, 15.517540966287267, 16.48738727668022, 16.930833584090877, 0.0, 0.0, 20.366772518252038, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 27.155696691002717, 0.0, 0.0, 30.065235622181582, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 36.85415979493226, 0.0, 0.0, 39.76369872611112, 42.0, 43.0, 44.0, 45.0, 46.0, 47.0, 46.5526228988618, 0.0, 0.0, 49.462161830040664, 52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 56.25108600279135, 0.0, 0.0, 59.160624933970205, 62.0, 63.0, 64.0, 65.0, 66.0, 67.0, 65.94954910672088, 0.0, 0.0, 68.85908803789975, 72.0, 73.0, 74.0, 75.0, 76.0, 77.0, 75.64801221065044, 0.0, 0.0, 76.18875112840894, 79.52739745222225, 80.4972437626152, 81.46709007300815, 82.4369363834011, 83.40678269379406, 84.37662900418702, 82.77296418888872, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
		assert_eq!(filt_image.data(), expected);
	}
}