use crate::data_properties::{ImageDimensions, Properties};
use crate::utils;

pub trait PatchFactory
{
    fn get_patch(&self, sigma: f64, nm_pp: &Properties) -> Result<ImageDimensions, String>;
}

impl<T: PatchFactory> PatchFactory for &T
{
    fn get_patch(&self, sigma: f64, nm_pp: &Properties) -> Result<ImageDimensions, String>
    {
        (*self).get_patch(sigma, nm_pp)
    }
}

#[derive(Debug, Clone)]
pub struct SigmaFactory
{
    scale: f64  
}

impl SigmaFactory
{
    pub fn new(scale_factor: f64) -> Self
    {
        Self{scale: scale_factor}
    }

    pub fn patch_size_nm(&self, sigma: f64) -> f64
    {
        sigma * self.scale * 2.0
    }
}

impl PatchFactory for SigmaFactory
{
    fn get_patch(&self, sigma_nm: f64, nm_pp: &Properties) -> Result<ImageDimensions, String>
    {
        let size_nm = self.patch_size_nm(sigma_nm);
        let mut n_pixels = utils::n_pixels_required(size_nm, nm_pp.pixel_size());
        n_pixels = std::cmp::max(n_pixels, 2);
        if n_pixels % 2 == 0
        {
            n_pixels += 1;
        }
        Ok(ImageDimensions::new(n_pixels, n_pixels))
    }
}

impl Default for SigmaFactory
{
    fn default() -> Self
    {
        Self::new(3.0)
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;
    use crate::data_properties::{DataProperties};

    #[test]
    fn patches_are_odd() 
    {
        let sigma_scale = 1.0;
        let factory = SigmaFactory::new(sigma_scale);

        let pixel_size = 1.0;
        let data_properties = DataProperties::from(0.0, 10.0, 0.0, 10.0);
        let properties = Properties::new(data_properties, pixel_size);
        let patch = factory.get_patch(2.0, &properties).unwrap();
        assert_eq!(utils::n_pixels_required(2.0, pixel_size), 2);
        assert_eq!(patch.width(), 5);
        assert_eq!(patch.height(), 5);
    }

    #[test]
    fn patches_at_least_2_pixels() 
    {
        let sigma_scale = 1.0;
        let factory = SigmaFactory::new(sigma_scale);

        let pixel_size = 1.0;
        let data_properties = DataProperties::from(0.0, 10.0, 0.0, 10.0);
        let properties = Properties::new(data_properties, pixel_size);
        
        let test_sigma = 1.0;
        let patch = factory.get_patch(test_sigma, &properties).unwrap();
        assert_eq!(utils::n_pixels_required(test_sigma, pixel_size), 1);
        assert_eq!(patch.width(), 3);
        assert_eq!(patch.height(), 3);
    }
}