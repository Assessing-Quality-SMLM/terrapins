use crate::{utils};
use crate::data_properties::{NmFrame, ImageDimensions};

use std::io::{Write, Error as IoError};

#[derive(Debug, Clone)]
pub struct Config 
{
    global_frame_nm: Option<NmFrame>,
    camera_pixel_size_nm: f64,
    magnification_factor: f64,
    uncertainty_radius: f64,
    zoom_level: usize,
    
    n_threads: usize,

    image_filename: Option<String>,
    zoom_filename: Option<String>,
    data_filename: Option<String>,
    border: Option<u64>,

    write_as_f32 : bool
}

impl Config
{
    pub fn new() -> Self
    {
        Self
        {
            global_frame_nm: None,
            camera_pixel_size_nm: 100.0,
            magnification_factor: 10.0,
            uncertainty_radius: 3.0,
            zoom_level: 1,
            n_threads: 0 , // 0 or 1 means ST
            image_filename : Some(String::from("image.tiff")),
            zoom_filename : None,
            data_filename: None,
            border: None,
            write_as_f32: false
        }
    }

    pub fn global_frame_nm(&self) -> Option<NmFrame>
    {
        self.global_frame_nm.clone()
    }

    pub fn with_global_frame_nm(mut self, frame: NmFrame) -> Self
    {
        self.global_frame_nm = Some(frame);
        self
    }

    pub fn camera_pixel_size_nm(&self) -> f64
    {
        self.camera_pixel_size_nm
    }

    pub fn with_camera_pixel_size(mut self, value: f64) -> Self
    {
        self.camera_pixel_size_nm = value;
        self
    }

    pub fn magnification_factor(&self) -> f64
    {
        self.magnification_factor
    }

    pub fn with_magnification_factor(mut self, value: f64) -> Self
    {
        self.magnification_factor = value;
        self
    }

    pub fn super_res_pixel_size(&self) -> f64
    {
        utils::super_res_pixel_size(self.camera_pixel_size_nm(), self.magnification_factor())
    }

    pub fn data_frame(&self) -> Option<NmFrame>
    {
        self.global_frame_nm().clone()
    }

    pub fn final_image_size(&self) -> Option<ImageDimensions>
    {
        match &self.global_frame_nm
        {
            None => None,
            Some(frame) => 
            {
                let pixel_size = self.pixel_size_nm();
                let height = utils::n_pixels_required(frame.height(), pixel_size);
                let width = utils::n_pixels_required(frame.width(), pixel_size);
                Some(ImageDimensions::new(height, width))
            }
        }
    }

    pub fn pixel_size_nm(&self) -> f64
    {
        self.camera_pixel_size_nm / self.magnification_factor
    }

    pub fn sigma_scale(&self) -> f64
    {
        self.uncertainty_radius
    }

    pub fn with_sigma_scale(mut self, uncertainty_radius: f64) -> Self
    {
        self.uncertainty_radius = uncertainty_radius;
        self
    }

    pub fn zoom_level(&self) -> usize
    {
        self.zoom_level
    }

    pub fn with_zoom_level(mut self, zoom_level: usize) -> Self
    {
        self.zoom_level = zoom_level;
        self
    }

    pub fn n_threads(&self) -> usize
    {
        self.n_threads
    }

    pub fn with_n_threads(mut self, value: usize) -> Self
    {
        self.n_threads = value;
        self
    }

    pub fn image_filename(&self) -> Option<&str>
    {
        self.image_filename.as_ref().map(|x| x.as_str())
    }

    pub fn with_image_filename(mut self, filename: Option<String>) -> Self
    {
        self.image_filename = filename;
        self
    }

    pub fn write_image(&self) -> bool
    {
        self.image_filename.is_some()
    }

    pub fn zoom_filename(&self) -> Option<&str>
    {
        self.zoom_filename.as_ref().map(|x| x.as_str())
    }

    pub fn with_zoom_filename(mut self, filename: Option<String>) -> Self
    {
        self.zoom_filename = filename;
        self
    }

    pub fn write_as_f32(&self) -> bool
    {
        self.write_as_f32
    }

    pub fn with_write_as_f32(mut self, value: bool) -> Self
    {
        self.write_as_f32 = value;
        self
    }

    pub fn write_data_file(&self) -> bool
    {
        self.data_filename.is_some()
    }

    pub fn data_filename(&self) -> Option<&str>
    {
        self.data_filename.as_ref().map(|s| s.as_str())
    }

    pub fn with_data_filename(mut self, value: Option<String>) -> Self
    {
        self.data_filename = value;
        self
    }

    pub fn border(&self) -> Option<u64>
    {
        self.border.clone()
    }

    pub fn with_border(mut self, value: u64) -> Self
    {
        self.border = Some(value);
        self
    }

    pub fn write_to<W: Write>(&self, mut writer: W) -> Result<(), IoError>
    {
        write!(writer, "camera_pixel_size={}\nglobal_frame_nm={}\npixel_size_nm={}\nuncertainty_radius={}\nzoom_level={}\nimage_size={}\nn_threads={}\nimage_filename={}\nzoom_filename ={}\ndata_filename={}\nwrite_as_f32={}",
        self.camera_pixel_size_nm,
        self.global_frame_nm.as_ref().map(|f| f.to_string()).unwrap_or("-".to_string()),
        self.pixel_size_nm(),
        self.uncertainty_radius,
        self.zoom_level,
        self.final_image_size().map(|dim| dim.to_string()).unwrap_or("-".to_string()),
        self.n_threads,
        self.image_filename.as_ref().map(|s| s.as_str()).unwrap_or("-"),
        self.zoom_filename.as_ref().map(|s| s.as_str()).unwrap_or("-"),
        self.data_filename.as_ref().map(|s| s.as_str()).unwrap_or("-"),
        self.write_as_f32)
    }
}

impl Default for Config
{
    fn default() -> Self
    {
        Self::new()
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn defaults_test() 
    {
        let config = Config::default();
        assert_eq!(config.global_frame_nm(), None);
        assert_eq!(config.final_image_size(), None);
        assert_eq!(config.zoom_level, 1);
        assert_eq!(config.camera_pixel_size_nm(), 100.0);
        assert_eq!(config.magnification_factor(), 10.0);
        assert_eq!(config.pixel_size_nm(), 10.0);
    }

    #[test]
    fn super_res_pixel_size()
    {
        let config = Config::default();
        assert_eq!(config.camera_pixel_size_nm(), 100.0);
        assert_eq!(config.magnification_factor(), 10.0);
        assert_eq!(config.super_res_pixel_size(), 10.0);
    }

    #[test]
    fn data_frame()
    {
        let config = Config::default().with_global_frame_nm(NmFrame::from(0.0, 0.0, 1280.0, 1280.0));
        let nm_frame = config.data_frame().unwrap();
        let expected = NmFrame::from(0.0, 0.0, 1280.0, 1280.0);
        assert_eq!(nm_frame, expected)
    }

    #[test]
    fn basic_config_write_test()
    {
        let mut buffer = Vec::new();
        let config = Config::default();
        assert_eq!(config.write_to(&mut buffer).is_ok(), true);
        let data = std::str::from_utf8(&buffer).unwrap();
        let expected = "camera_pixel_size=100\nglobal_frame_nm=-\npixel_size_nm=10\nuncertainty_radius=3\nzoom_level=1\nimage_size=-\nn_threads=0\nimage_filename=image.tiff\nzoom_filename =-\ndata_filename=-\nwrite_as_f32=false";
        assert_eq!(data, expected);
    }

    #[test]
    fn should_write_image()
    {
        assert_eq!(Config::default().write_image(), true);
        assert_eq!(Config::default().with_image_filename(Some("thing.tiff".to_string())).write_image(), true);
    }

    #[test]
    fn should_not_write_image()
    {
        assert_eq!(Config::default().with_image_filename(None).write_image(), false);
    }

    #[test]
    fn basic_image_size() 
    {
        let config = Config::default().with_global_frame_nm(NmFrame::from(10.0, 20.0, 10000.0, 5000.0)).with_camera_pixel_size(100.0).with_magnification_factor(10.0);
        assert_eq!(config.final_image_size().unwrap(), ImageDimensions::new(500, 1000))
    }

    #[test]
    fn another_image_size() 
    {
        let config = Config::default().with_camera_pixel_size(100.0).with_global_frame_nm(NmFrame::from(0.0, 0.0, 12800.0, 12800.0)).with_magnification_factor(10.0);
        assert_eq!(config.pixel_size_nm(), 10.0);
        assert_eq!(config.final_image_size().unwrap(), ImageDimensions::new(1280, 1280))
    }
}