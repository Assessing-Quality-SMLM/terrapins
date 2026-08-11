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
        for col in col_start..(col_end + 1)
        {
            let ((x_lower, x_upper), (y_lower, y_upper)) = utils::pixel_boundaries((row, col), properties.pixel_size());
            let x_lower = x - x_lower;
            let x_upper = x_upper - x;

            let y_lower = y - y_lower;
            let y_upper = y_upper - y;
            // println!("{x_lower}, {x_upper}, {y_lower}, {y_upper}");
            let value = gaussian::integral_over(x_lower, x_upper, y_lower, y_upper, sigma_nm);
            *image.get_at_mut(row, col) += value;
        }
    }
    Ok(())
}

#[derive(Debug, Clone)]
pub struct IntegralPatchRenderer<F>
{
	factory: F
}

impl IntegralPatchRenderer<SigmaFactory>
{
    pub fn with_sigma(sigma: f64) -> Self
    {
        Self::new(SigmaFactory::new(sigma))
    }
}

impl<F> IntegralPatchRenderer<F>
{
	pub fn new(factory: F) -> Self
	{
		Self{factory}
	}
}

impl<F: PatchFactory> IntegralPatchRenderer<F>
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

impl<F: PatchFactory, L: UncertainLocalisation> RenderPolicy<L> for IntegralPatchRenderer<F>
{
    type Output = ();
    type Error = String;
    fn render(&self, localisation: L, image: &mut OwnedImage<DataType>, properties: &Properties) -> Result<Self::Output, Self::Error>
    {
        // println!("Integral");
    	self.render_as_patch(localisation, image, properties)
    }
}

#[cfg(test)]
mod tests 
{
	// use super::*;
}