use super::patch_utils;
use crate::{gaussian, utils, DataType, Properties, RenderPolicy};
use crate::patch::{PatchFactory, SigmaFactory};

use imp::{ImageMut, OwnedImage};

use locs::{UncertainLocalisation};

fn render_patch<I: ImageMut<Data=DataType>>(localisation: (f64, f64), pixel: (usize, usize), patch: (usize, usize), sigma_nm: f64, mut image: I, properties: &Properties) -> Result<(), String>
{
    let (n_rows, n_cols) = image.shape();
    let (x, y) = localisation;
    let (row, col) = pixel;
    // println!("row: {row}, col: {col}");
    let (patch_height, patch_width) = patch;
    // println!("{patch_height} x {patch_width}");

    let (row_start, row_end) = patch_utils::get_start_end(row, n_rows, patch_height);
    let (col_start, col_end) = patch_utils::get_start_end(col, n_cols, patch_width);
    // println!("{row_start}->{row_end}");
    // println!("{col_start}->{col_end}");
    for row in row_start..(row_end + 1)
    {
        // println!("global row: {row}");
        let row_nm = properties.get_row_centre(row);
        // println!("row_nm: {row_nm}");
        let y_dist = utils::distance(y, row_nm);
        // println!("y_dist: {y_dist} between {y} and {row_nm} for row {row}");
        for col in col_start..(col_end + 1)
        {
            // println!("row: {row}, p_col: {col}");
            // println!("global_row: {row}, global_col: {col}");
            let col_nm = properties.get_col_centre(col);
            let x_dist = utils::distance(x, col_nm);
            // println!("x_dist: {x_dist} for col {col}");
            let value = gaussian::blur_2d(x_dist, y_dist, sigma_nm);
            // println!("{value}");
            *image.get_at_mut(row, col) += value;
        }
    }
    Ok(())
}

#[derive(Debug, Clone)]
pub struct SinglePointPatchRenderer<F>
{
	factory: F
}

impl SinglePointPatchRenderer<SigmaFactory>
{
    pub fn with_sigma(sigma: f64) -> Self
    {
        Self::new(SigmaFactory::new(sigma))
    }
}

impl<F> SinglePointPatchRenderer<F>
{
	pub fn new(factory: F) -> Self
	{
		Self{factory}
	}
}

impl<F: PatchFactory> SinglePointPatchRenderer<F>
{
	fn render_as_patch<L: UncertainLocalisation, I: ImageMut<Data=DataType>>(&self, localisation: L, image: I, properties: &Properties) -> Result<(), String>
	{
	    let col = properties.get_column(localisation.x());
	    // println!("col: {col}");
	    let row = properties.get_row(localisation.y());
	    // println!("row: {row}");
	    let sigma_nm = localisation.uncertainty();
	    // println!("sigma (nm): {sigma_nm}");
	    let patch_size = self.factory.get_patch(sigma_nm, &properties)?;
	    // println!("sigma_patch: {:?}", patch_size);
	    let l = (localisation.x(), localisation.y());
	    let pixel = (row, col);
	    let patch = (patch_size.height(), patch_size.width());
	    // let _ = render_symmetrical_patch(l, pixel, patch, sigma_nm, image, properties);
	    let _ = render_patch(l, pixel, patch, sigma_nm, image, properties);
	    Ok(())
	}
}

impl<F: PatchFactory, L: UncertainLocalisation> RenderPolicy<L> for SinglePointPatchRenderer<F>
{
    type Output = ();
    type Error = String;
    fn render(&self, localisation: L, image: &mut OwnedImage<DataType>, properties: &Properties) -> Result<Self::Output, Self::Error>
    {
    	self.render_as_patch(localisation, image, properties)
    }
}

#[cfg(test)]
mod tests 
{
	use super::*;

	use crate::{DataProperties};
	use imp::{Image, OwnedImage};

	#[test]
    fn render_patch_test() 
    {
        let sigma = 1.0;
        let pixel_size = 1.0;
        let mut image = OwnedImage::<DataType>::zeros((5, 5));
        let localisation = (3.8, 2.9);
        let pixel_location = (2, 3);
        assert_eq!(utils::pixel_location(3.8, pixel_size), 3);
        assert_eq!(utils::pixel_location(2.9, pixel_size), 2);
        let patch_size = (3, 3);
        let data_properties = DataProperties::from(0.0, 5.0, 0.0, 5.0);
        let properties = Properties::new(data_properties, pixel_size);
        let _ = render_patch(localisation, pixel_location, patch_size, sigma, &mut image, &properties);
        let expected = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.02565858497688406, 0.05710423103325742, 0.0467529900777979, 0.0, 0.0, 0.06310993543702902, 0.1404537443096252, 0.11499379985124172, 0.0, 0.0, 0.05710423103325742, 0.1270878033546041, 0.10405069294754148, 0.0, 0.0, 0.0, 0.0, 0.0];
        assert_eq!(image.data(), expected);
    }
}