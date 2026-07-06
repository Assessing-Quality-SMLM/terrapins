use crate::utils;

use std::cmp::{PartialOrd};
use std::fmt::{Display, Formatter};
use std::ops::{Add, Sub, Mul};

#[derive(Debug, PartialEq)]
pub struct Dimensions<T>
{
    height: T,
    width: T
}

impl<T>  Dimensions<T>
{
    pub fn new(height: T, width: T) -> Self
    {
        Self{height, width}
    }
}

impl<T: Copy>  Dimensions<T>
{
    pub fn height(&self) -> T
    {
        self.height
    }

    pub fn width(&self) -> T
    {
        self.width
    }
}

impl<T: Display> Display for Dimensions<T>
{
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), std::fmt::Error>
    {
        write!(f, "height: {}, width: {}", self.height, self.width)
    }
}

pub type ImageDimensions = Dimensions<usize>;

#[derive(Debug, Clone, PartialEq)]
pub struct Range<T>
{
    min: T,
    max: T,
}

impl<T: Copy> Range<T>
{
    pub fn new(min: T, max: T) -> Self
    {
        Self{min, max}
    }    

    pub fn min(&self) -> T
    {
        self.min
    }

    pub fn set_min(&mut self, value: T) -> ()
    {
        // let adjusted = if value > 0.0 {value} else{0.0};
        // self.min = adjusted;
        self.min = value;
    }

    pub fn max(&self) -> T
    {
        self.max
    }

    pub fn length(&self) -> T
    where T: Sub<Output = T>
    {
        self.max() - self.min()
    }

    // pub fn centre(&self) -> f64
    // {
    //     self.min() + (self.length() / 2.0)
    // }

    pub fn within_bounds(&self, value: T) -> bool
    where T: PartialOrd
    {
        self.min() <= value && value <= self.max()
    }
}

// pub type PixelRange = Range<usize>;
pub type NmRange = Range<f64>;


#[derive(Debug, Clone, PartialEq)]
pub struct Frame<T>
{
    col_range: Range<T>, // width
    row_range: Range<T>, //height
}

impl<T: Copy + Sub<Output=T> + Add<Output=T> + Mul<Output=T> + PartialOrd> Frame<T>
{
    pub fn new(col_range: Range<T>, row_range: Range<T>) -> Self
    {
        Self{row_range, col_range}
    }

    pub fn from(x_start: T, y_start: T, width: T, height: T) -> Self
    {
        let col_range = Range::new(x_start, x_start + width);
        let row_range = Range::new(y_start, y_start + height);
        Self::new(col_range, row_range)
    }

    pub fn start_col(&self) -> T
    {
        self.col_range.min()
    }

    pub fn width(&self) -> T
    {
        self.col_range.length()
    }    

    pub fn start_row(&self) -> T
    {
        self.row_range.min()
    }

    pub fn height(&self) -> T
    {
        self.row_range.length()
    }

    pub fn scale_by(&self, sf: T) -> Self
    {
        let height = self.height() * sf;
        let width = self.width() * sf;
        Self::from(self.start_row(), self.start_col(), height, width)
    }

    pub fn within_bounds(&self, row: T, col: T) -> bool
    {
        self.row_range.within_bounds(row) && self.col_range.within_bounds(col)
    }

    pub fn within_xy_bounds(&self, x: T, y: T) -> bool
    {
        self.within_bounds(y, x)
    }
}

impl<T: Copy + Sub<Output=T> + Add<Output=T> + Mul<Output=T> + PartialOrd + Display> Display for Frame<T>
{
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), std::fmt::Error>
    {
        write!(f, "start row: {}, start col: {}, height: {}, width: {}", self.start_row(), self.start_col(), self.height(), self.width())
    }
}

pub type ImageFrame = Frame<usize>;
pub type NmFrame = Frame<f64>;

#[derive(Debug, Clone)]
pub struct DataProperties 
{
    nm_frame: NmFrame
}

impl DataProperties
{
    pub fn new(nm_frame: NmFrame) -> Self
    {
        Self{nm_frame}
    }

    fn with(x_range: NmRange, y_range: NmRange) -> Self
    {
        Self::new(NmFrame::new(x_range, y_range))
    }

    pub fn from(min_x: f64, max_x: f64, min_y: f64, max_y: f64) -> Self
    {
        Self::with(Range::new(min_x, max_x), Range::new(min_y, max_y))
    }

    pub fn data_frame(&self) -> &NmFrame
    {
        &self.nm_frame
    }

    pub fn width(&self) -> f64
    {
        self.nm_frame.width()
    }

    pub fn width_offset(&self) -> f64
    {
        self.nm_frame.start_col()
    }

    pub fn set_width_offset(&mut self, _value: f64) -> ()
    {
        
    }

    // fn width_centre(&self) -> f64
    // {
    //     self.x_range.centre()
    // }

    pub fn height(&self) -> f64
    {
        self.nm_frame.height()
    }

    pub fn height_offset(&self) -> f64
    {
        self.nm_frame.start_row()
    }

    pub fn set_height_offset(&mut self, _value: f64) -> ()
    {
        
    }

    // fn height_centre(&self) -> f64
    // {
    //     self.y_range.centre()
    // }

    // pub fn max_length(&self) -> f64
    // {
    //     if self.height() < self.width()
    //     {
    //         self.width()
    //     }
    //     else 
    //     {
    //         self.height()
    //     }
    // }

    // fn length_difference(&self) -> f64
    // {
    //     utils::distance(self.height(), self.width())
    // }

    // pub fn adjust_shortest_dimension_offset(&mut self) -> ()
    // {
    //     // we minus here as we are in effect treating the image as a frame over global space
    //     // and pulling it backwards so the image is centred
    //     let difference = self.length_difference();
    //     let amount = difference / 2.0;
    //     if self.height() < self.width()
    //     {
    //         self.set_height_offset(self.height_offset() - amount)
    //     }
    //     else 
    //     {
    //         self.set_width_offset(self.width_offset() - amount)
    //     }
    // }

    pub fn calculate_image_height(&self, pixel_size: f64) -> usize
    {
        utils::n_pixels_required(self.height(), pixel_size)
    }

    pub fn calculate_image_width(&self, pixel_size: f64) -> usize
    {
        utils::n_pixels_required(self.width(), pixel_size)
    }

    // fn image_bounds(&self, image_size: usize, pixel_size_nm: f64) -> DataProperties
    // {
    //     let image_size_nm = utils::image_size_from_pixels(image_size, pixel_size_nm);
    //     let half_image = image_size_nm / 2.0;
    //     let min_x = self.width_centre() - half_image;
    //     let max_x = self.width_centre() + half_image;
    //     let min_y = self.height_centre() - half_image;
    //     let max_y = self.height_centre() + half_image;
    //     DataProperties::from(min_x, max_x, min_y, max_y)
    // }

    // // this is for cropping / padding images to fit image sizes with specific pixel sizes
    // // if data is too big then reduce the dimensions to where the image would be 
    // // given centre point
    // pub fn adjust_bounds_to_image_size(&mut self, image_size: usize, pixel_size_nm: f64) -> ()
    // {
    //     println!("{:?}", self);
    //     let new_bounds = self.image_bounds(image_size, pixel_size_nm);
    //     self.x_range = new_bounds.x_range;
    //     self.y_range = new_bounds.y_range;
    //     println!("{:?}", self);
    // }

    pub fn within_bounds(&self, x: f64, y: f64) -> bool
    {
        self.nm_frame.within_xy_bounds(x, y)
    }
}

#[derive(Debug, Clone)]
pub struct Properties 
{
    data_properties: DataProperties,
    pixel_size : f64
}

impl Properties
{
    pub fn new(data_properties: DataProperties, pixel_size: f64) -> Self
    {
        Self{data_properties, pixel_size}
    }

    pub fn pixel_size(&self) -> f64
    {
        self.pixel_size
    }

    pub fn data_frame(&self) -> &NmFrame
    {
        self.data_properties.data_frame()
    }

    pub fn height_offset(&self) -> f64
    {
        self.data_properties.height_offset()
    }

    pub fn width_offset(&self) -> f64
    {
        self.data_properties.width_offset()
    }

    pub fn get_column(&self, x: f64) -> usize
    {
        utils::shifted_pixel_location(x, self.width_offset(), self.pixel_size())
    }

    pub fn get_row(&self, y: f64) -> usize
    {
        utils::shifted_pixel_location(y, self.height_offset(), self.pixel_size())
    }

    pub fn get_row_centre(&self, row: usize) -> f64
    {
        utils::get_global_pixel_centre_nm(row, self.height_offset(), self.pixel_size())
    }

    pub fn get_col_centre(&self, col: usize) -> f64
    {
        utils::get_global_pixel_centre_nm(col, self.width_offset(), self.pixel_size())
    }

    pub fn within_bounds(&self, x: f64, y: f64) -> bool
    {
        self.data_properties.within_bounds(x, y)
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    // #[test]
    // fn range_centre()
    // {
    //     assert_eq!(Range::new(10.0, 20.0).centre(), 15.0);
    //     assert_eq!(Range::new(-10.0, 20.0).centre(), 5.0);
    // }

    #[test]
    fn range_within() 
    {
        let range = Range::new(1.0, 2.0);
        assert_eq!(range.within_bounds(1.0), true);
        assert_eq!(range.within_bounds(1.0 - std::f64::EPSILON), false);
        assert_eq!(range.within_bounds(2.0), true);
        assert_eq!(range.within_bounds(2.0000001), false);
    }

    #[test]
    fn frame_height() 
    {
        assert_eq!(Frame::from(10, 20, 100, 50).height(), 50);
    }

    #[test]
    fn frame_width() 
    {
        assert_eq!(Frame::from(10, 20, 100, 50).width(), 100);
    }

    #[test]
    fn data_properties_width_offset()
    {
        assert_eq!(DataProperties::from(1.0, 2.0, 1.0, 2.0).width_offset(), 1.0);
    }

    #[test]
    fn data_properties_height_offset()
    {
        assert_eq!(DataProperties::from(1.0, 2.0, 1.0, 2.0).height_offset(), 1.0);
    }

    #[test]
    fn data_properties_within_bounds()
    {
        let data_properties = DataProperties::from(1.0, 2.0, 1.0, 2.0);
        assert_eq!(data_properties.within_bounds(1.0, 1.0), true);
        assert_eq!(data_properties.within_bounds(0.99999, 1.0), false);
        assert_eq!(data_properties.within_bounds(1.0, 0.99999), false);
    }

    #[test]
    fn correct_offsets()
    {
        let data_properties = DataProperties::from(1.0, 2.0, 3.0, 4.0);
        println!("{:?}",data_properties);
        assert_eq!(data_properties.width_offset(), 1.0);
        assert_eq!(data_properties.height_offset(), 3.0);
    }

    // #[test]
    // fn adjust_offsets_for_shortest_dimension()
    // {
    //     let mut data_properties = DataProperties::from(10.0, 20.0, 30.0, 50.0);
    //     assert_eq!(data_properties.width_offset(), 10.0);
    //     data_properties.adjust_shortest_dimension_offset();
    //     assert_eq!(data_properties.width_offset(), 5.0);
    // }

    // #[test]
    // fn adjust_bounds_to_crop_to_image_size()
    // {
    //     let mut data_properties = DataProperties::from(10.0, 20.0, 30.0, 50.0);
    //     data_properties.adjust_bounds_to_image_size(5, 1.0);
    //     assert_eq!(data_properties.height(), 5.0);
    //     assert_eq!(data_properties.height_offset(), 37.5);
    //     assert_eq!(data_properties.width(), 5.0);
    //     assert_eq!(data_properties.width_offset(), 12.5);
    // }

    // #[test]
    // fn adjust_bounds_to_pad_to_image_size()
    // {
    //     let mut data_properties = DataProperties::from(10.0, 15.0, 30.0, 35.0);
    //     data_properties.adjust_bounds_to_image_size(100, 1.0);
    //     assert_eq!(data_properties.height(), 100.0);
    //     assert_eq!(data_properties.height_offset(), -17.5);
    //     assert_eq!(data_properties.width(), 100.0);
    //     assert_eq!(data_properties.width_offset(), -37.5);
    // }

    // #[test]
    // fn height_can_be_adjusted_to()
    // {
    //     let mut data_properties = DataProperties::from(30.0, 50.0, 10.0, 20.0);
    //     assert_eq!(data_properties.height_offset(), 10.0);
    //     data_properties.adjust_shortest_dimension_offset();
    //     assert_eq!(data_properties.height_offset(), 5.0);
    // }
}