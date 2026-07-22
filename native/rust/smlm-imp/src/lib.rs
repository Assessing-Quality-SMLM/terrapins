extern crate smlm_sig_proc as sig_proc;
extern crate smlm_tiff as tiff_wrap;

#[cfg(feature = "plot")]
extern crate gnuplot;

pub use self::stats::Stats;
pub use self::images::{OwnedImage, BorrowedImage};

use tiff_wrap::{Tiff};

pub use sig_proc::fft::c64;
use sig_proc::fft::{fft_2d, fft_shift};

#[cfg(feature = "plot")]
use gnuplot::{Figure, AxesCommon, DataType};

use std::ops::{AddAssign, Mul, Div};
use std::path::Path;

// mod convolution;
pub mod circles;
pub mod filter;
pub mod images;
pub mod stats;
pub mod utils;

pub trait Image
{
    type Data;
    fn n_rows(&self) -> usize;
    fn n_cols(&self) -> usize;
    fn data(&self) -> &[Self::Data];

    fn width(&self) -> usize
    {
        self.n_cols()
    }

    fn height(&self) -> usize
    {
        self.n_rows()
    }

    /// shape of image in rows x cols format
    fn shape(&self) -> (usize, usize)
    {
        (self.n_rows(), self.n_cols())
    }

    /// n_rows * n_cols
    fn size(&self) -> usize
    {
        self.n_rows() * self.n_cols()
    }

    /// get data with position
    /// unsafe
    fn get_at(&self, row: usize, col: usize) -> &Self::Data
    {
        let index = self.get_index(row, col);
        self.get(index)
    }

    /// get data with index
    /// unsafe
    fn get(&self, idx: usize) -> &Self::Data
    {
        &self.data()[idx]
    }

    /// get data with indicies
    /// unsafe
    fn get_data(&self, indicies: &[usize]) -> Vec<&Self::Data>
    {
        indicies.iter().map(|x| self.get(*x)).collect()
    }

    /// get data index for row and column
    fn get_index(&self, row: usize, col: usize) -> usize
    {
        utils::get_index(row, col, self.n_cols())
    }

    /// get coordinates for index
    fn get_coords(&self, index: usize) -> (usize, usize)
    {
        utils::get_coords(index, self.n_cols())
    }


    //useful for things like frc
    fn is_square(&self) -> bool
    {
        self.n_rows() == self.n_cols()
    }

    /// get data index for centre of the image
    fn centre_index(&self) -> usize
    {
        let (row, col) = self.centre_position();
        utils::get_index(row, col, self.n_cols())
    }

    /// get central row
    fn centre_row(&self) -> usize
    {
        self.n_rows() / 2
    }

    /// get central column
    fn centre_col(&self) -> usize
    {
        self.n_cols() / 2
    }

    /// get row and column for central pixel
    fn centre_position(&self) -> (usize, usize)
    {
        (self.centre_row(), self.centre_col())
    }

    /// determine if image has a sqaure centre
    /// ie 4 pixels make up central part
    fn square_centre(&self) -> bool
    {
        self.n_rows() % 2 == 0 && self.n_rows() == self.n_cols()
    }

    ///indices for perimenter fo circle with specified radius using custom method
    fn perimimeter_indicies_using<P: circles::Perimeter>(&self, radius: usize) -> Vec<usize>
    where Self: Sized
    {
        P::get(self, radius as f64)
    }

    ///indices for perimenter fo circle with specified radius using known method
    fn perimimeter_indicies_with(&self, method: circles::Method, radius: usize) -> Vec<usize>
    where Self: Sized
    {
        method.get_perimeter(self, radius as f64)
    }

    ///indices for perimenter fo circle with specified radious
    /// defaults to angle carve
    fn perimimeter_indicies(&self, radius: usize) -> Vec<usize>
    where Self: Sized
    {
        circles::angle_carve(self, radius as f64)
    }

    ///pixels on the perimeter of a cirlce with specified radius
    fn perimimeter_pixels(&self, radius: usize) -> Vec<&Self::Data> 
    where Self: Sized
    {
        let data = self.data();
        self.perimimeter_indicies(radius)
            .into_iter()
            .map(|i| &data[i])
            .collect()        
    }

    fn apply_index_mask(&self, indicies: &[usize]) -> Vec<Self::Data>
    where Self::Data: Copy
    {
        indicies.iter().map(|i| self.data()[*i]).collect()
    }

    fn apply_mask<I: Image<Data=bool>>(&self, mask_image: I) -> Vec<Self::Data>
    where Self::Data: Copy
    {
        let mut iter = mask_image.data().iter();
        self.data().iter().filter(|_| *iter.next().unwrap()).map(|v| *v).collect()
    }

    fn mul<I: Image<Data=Self::Data>>(&self, rhs: I) -> OwnedImage<Self::Data>
    where Self::Data : Mul<Self::Data, Output = Self::Data> + Copy,
    {
        let data = self.data().iter().zip(rhs.data().iter()).map(|(a, b)| (*a) * (*b)).collect();
        OwnedImage::new(self.shape(), data)
    }

    fn div<I: Image<Data=Self::Data>>(&self, rhs: I) -> OwnedImage<Self::Data>
    where Self::Data : Div<Self::Data, Output = Self::Data> + Copy,
    {
        let data = self.data().iter().zip(rhs.data().iter()).map(|(a, b)| (*a) / (*b)).collect();
        OwnedImage::new(self.shape(), data)
    }

    fn map<T, F: FnMut(&Self::Data) -> T>(&self, f: F) -> OwnedImage<T>
    {
        let mut image : OwnedImage<_> = self.data().iter().map(f).collect();
        image.reshape(self.shape());
        image
    }

    #[cfg(feature = "plot")]
    fn plot(&self, title: Option<&str>) -> ()
    where Self::Data : DataType + Copy
    {
        let mut figure = Figure::new();
        figure.axes2d()
              .set_title(title.unwrap_or(""), &[])
              .image(self.data().iter().map(|x| *x), self.n_rows(), self.n_cols(), None, &[]);
        figure.show_and_keep_running().unwrap();
    }

    fn write_tiff<P: AsRef<Path>>(&self, path: P) -> Result<(), String>
    where Self::Data : tiff_wrap::writer::ToColourType, [Self::Data]: tiff_wrap::writer::TiffValue
    {        
        let (width, height) = self.get_tiff_writer_shape()?;
        let mut writer = tiff_wrap::writer::StandardTiffWriter::from_disk(path, width, height).map_err(|e| e.to_string())?;
        writer.write_image_for(self.data()).map_err(|e| e.to_string())
    }

    fn write_big_tiff<P: AsRef<Path>>(&self, path: P) -> Result<(), String>
    where Self::Data : tiff_wrap::writer::ToColourType, [Self::Data]: tiff_wrap::writer::TiffValue
    {
        let (width, height) = self.get_tiff_writer_shape()?;
        let mut writer = tiff_wrap::writer::BigTiffWriter::from_disk(path, width, height).map_err(|e| e.to_string())?;
        writer.write_image_for(self.data()).map_err(|e| e.to_string())
    }

    fn get_tiff_writer_shape(&self) -> Result<(u32, u32), String>
    {
        let (rows, cols) = self.shape();
        let width = cols.try_into().map_err(|e: std::num::TryFromIntError| e.to_string())?;
        let height = rows.try_into().map_err(|e: std::num::TryFromIntError| e.to_string())?;
        Ok((width, height))
    }
}

impl<I : Image> Image for &I
{
    type Data = I::Data;

    fn n_rows(&self) -> usize
    {
        (*self).n_rows()
    }

    fn n_cols(&self) -> usize
    {
        (*self).n_cols()
    }

    fn data(&self) -> &[I::Data]
    {
        (*self).data()
    }
}

impl<I : Image> Image for &mut I
{
    type Data = I::Data;

    fn n_rows(&self) -> usize
    {
        Image::n_rows(*self)
    }

    fn n_cols(&self) -> usize
    {
        Image::n_cols(*self)
    }

    fn data(&self) -> &[I::Data]
    {
        Image::data(*self)
    }
}

pub trait ImageMut: Image
{
    fn data_mut(&mut self) -> &mut [Self::Data];

    /// get data with position
    /// unsafe
    fn get_at_mut(&mut self, row: usize, col: usize) -> &mut Self::Data
    {
        // println!("{row},{col}");
        let index = self.get_index(row, col);
        self.get_mut(index)
    }

    /// get data with index
    /// unsafe
    fn get_mut(&mut self, idx: usize) -> &mut Self::Data
    {
        &mut self.data_mut()[idx]
    }

    fn add_to<'a, I: Image<Data=Self::Data> + 'a>(&'a mut self, other: I) -> () 
    where <Self as Image>::Data: Copy, 
          <Self as Image>::Data: AddAssign 
    {
        for (value, other_value) in self.data_mut().iter_mut().zip(other.data().iter().copied())
        {
            *value += other_value;
        }
    }
}

impl<I: ImageMut> ImageMut for &mut I
{
    fn data_mut(&mut self) -> &mut [<Self as Image>::Data]
    {
        <I as ImageMut>::data_mut(*self)
    }
}

pub trait Crop : Image
{
    fn take_rows(&mut self, n_rows: usize) -> Result<(), ()>;
    fn drop_rows(&mut self, n_rows: usize) -> Result<(), ()>;
    fn take_cols(&mut self, n: usize) -> Result<(), ()>;
    fn drop_cols(&mut self, n_cols: usize) -> Result<(), ()>;

    fn central_row_crop(&mut self, n_rows: usize) -> Result<(), ()>
    {
        let is_even = n_rows % 2 == 0;
        let half = n_rows / 2;
        let n_take = if is_even {half} else{half + 1};
        self.take_rows(n_take).and_then(|_| self.drop_rows(half))
    }

    fn central_col_crop(&mut self, n_cols: usize) -> Result<(), ()>
    {
        let is_even = n_cols % 2 == 0;
        let half = n_cols / 2;
        let n_take = if is_even {half} else{half + 1};
        self.take_cols(n_take).and_then(|_| self.drop_cols(half))
    }

    fn central_crop(&mut self, n_rows: usize, n_cols: usize) -> Result<(), ()>
    {
        self.central_row_crop(n_rows).and_then(|_| self.central_col_crop(n_cols))
    }
}

// undefined behaviour as to what happens if you take too many rows
// up to programmer to make sure this doesn't happen
impl<C : Crop> Crop for &mut C
{
    fn take_rows(&mut self, n_rows: usize) -> Result<(), ()>
    {
        Crop::take_rows(*self, n_rows)
    }

    fn drop_rows(&mut self, n: usize) -> Result<(), ()>
    {
        Crop::drop_rows(*self, n)
    }

    fn take_cols(&mut self, n: usize) -> Result<(), ()>
    {
        Crop::take_cols(*self, n)
    }

    fn drop_cols(&mut self, n: usize) -> Result<(), ()>
    {
        Crop::drop_cols(*self, n)
    }

    fn central_row_crop(&mut self, n_rows: usize) -> Result<(), ()>
    {
        Crop::central_row_crop(*self, n_rows)
    }

    fn central_col_crop(&mut self, n_cols: usize) -> Result<(), ()>
    {
        Crop::central_col_crop(*self, n_cols)
    }

    fn central_crop(&mut self, n_rows: usize, n_cols: usize) -> Result<(), ()>
    {
        Crop::central_crop(*self, n_rows, n_cols)
    }
}

pub trait Pad : Image
{
    fn pad_rows_start(&mut self, n_rows: usize) -> Result<(), ()>;
    fn pad_rows_end(&mut self, n_rows: usize) -> Result<(), ()>;
    fn pad_cols_start(&mut self, n_cols: usize) -> Result<(), ()>;
    fn pad_cols_end(&mut self, n_cols: usize) -> Result<(), ()>;

    fn central_row_pad(&mut self, n_rows: usize) -> Result<(), ()>
    {
        let is_even = n_rows % 2 == 0;
        let half = n_rows / 2;
        let start = if is_even {half} else{half + 1};
        self.pad_rows_start(start).and_then(|_| self.pad_rows_end(half))
    }

    fn central_col_pad(&mut self, n_cols: usize) -> Result<(), ()>
    {
        let is_even = n_cols % 2 == 0;
        let half = n_cols / 2;
        let start = if is_even {half} else{half + 1};
        self.pad_cols_start(start).and_then(|_| self.pad_cols_end(half))
    }

    fn central_pad(&mut self, n_rows: usize, n_cols: usize) -> Result<(), ()>
    {
        self.central_row_pad(n_rows).and_then(|_| self.central_col_pad(n_cols))
    }

    fn pad_to(&mut self, rows: usize, cols: usize) -> Result<(), ()>
    {
        let n_rows = rows - self.n_rows();
        let n_cols = cols - self.n_cols();
        self.central_pad(n_rows, n_cols)
    }
}

impl<P : Pad> Pad for &mut P
{
    fn pad_rows_start(&mut self, n_rows: usize) -> Result<(), ()>
    {
        Pad::pad_rows_start(*self, n_rows)
    }

    fn pad_rows_end(&mut self, n: usize) -> Result<(), ()>
    {
        Pad::pad_rows_end(*self, n)
    }

    fn pad_cols_start(&mut self, n: usize) -> Result<(), ()>
    {
        Pad::pad_cols_start(*self, n)
    }

    fn pad_cols_end(&mut self, n: usize) -> Result<(), ()>
    {
        Pad::pad_cols_end(*self, n)
    }

    fn central_row_pad(&mut self, n_rows: usize) -> Result<(), ()>
    {
        Pad::central_row_pad(*self, n_rows)
    }

    fn central_col_pad(&mut self, n_cols: usize) -> Result<(), ()>
    {
        Pad::central_col_pad(*self, n_cols)
    }

    fn central_pad(&mut self, n_rows: usize, n_cols: usize) -> Result<(), ()>
    {
        Pad::central_pad(*self, n_rows, n_cols)
    }

    fn pad_to(&mut self, rows: usize, cols: usize) -> Result<(), ()>
    {
        Pad::pad_to(*self, rows, cols)
    }
}

pub trait Fft
{
    type Output;
    fn fft(&self) -> Self::Output;    
    fn shifted_fft(&self) -> Self::Output;
}

impl<I> Fft for I
where I: Image<Data=f64>
{
    type Output = OwnedImage<c64>;
    fn fft(&self) -> Self::Output
    {
        let n_rows = self.n_rows();
        let n_cols = self.n_cols();
        let output_data = fft_2d(self.data(), n_rows, n_cols); //.all_data(n_rows, n_cols).into_iter().collect();
        Self::Output::new((n_rows, n_cols), output_data)
    }

    fn shifted_fft(&self) -> Self::Output
    {
        let mut new_data = vec![c64::ZERO; self.size()];
        fft_shift(self.fft().data(), &mut new_data, self.n_rows(), self.n_cols());
        OwnedImage::new((self.n_rows(), self.n_cols()), new_data)
    }
}

pub fn read_tiff_image_64<P: AsRef<Path>>(tiff_file: P) -> Result<OwnedImage<f64>, String>
{
    let mut reader = Tiff::read(tiff_file).map_err(|e| e.to_string())?;
    let (width, height) = reader.dimensions().map_err(|e| e.to_string())?;
    let image = reader.current_image().map_err(|e| e.to_string())?;
    let data = image.to_f64().map_err(|e| e.to_string())?;
    Ok(OwnedImage::new((height as usize, width as usize), data))
}

#[cfg(test)]
mod tests 
{
    use super::*;

    use crate::images::BorrowedImage;

    fn generate(n_rows: usize, n_cols: usize) -> ((usize, usize),Vec<usize>)
    {
        ((n_rows, n_cols), (0..(n_rows * n_cols)).collect())
    }

    #[test]
    fn centre_index_test() 
    {
        test_centre_index(4, 4, 10);
        test_centre_index(3, 4, 6);
        test_centre_index(3, 3, 4);
        test_centre_index(5, 5, 12);
    }

    fn test_centre_index(n_rows: usize, n_cols: usize, expected: usize) -> ()
    {
        let (shape, data) = generate(n_rows, n_cols);
        let image = BorrowedImage::new(shape, &data);
        assert_eq!(image.centre_index(), expected);
    }

    #[test]
    fn square_centre_test() 
    {        
        test_square_centre(4, 4, true);
        test_square_centre(5, 5, false);
    }

    fn test_square_centre(n_rows: usize, n_cols: usize, expected: bool) -> ()
    {
        let (shape, data) = generate(n_rows, n_cols);
        let image = BorrowedImage::new(shape, &data);
        assert_eq!(image.square_centre(), expected);
    }

    #[test]
    fn basic_fft()
    {
        let shape = (1, 4);
        let data = vec![0.0, 1.0, 2.0, 3.0];
        let image = BorrowedImage::new(shape, &data);
        let fft_data = image.fft();
        
        let expected = [ [6.0, 0.0], [-2.0, 2.0], [-2.0, 0.0], [-2.0, -2.0] ].map(|a| c64::new(a[0], a[1]));
        assert_eq!(fft_data.data(), expected);
    }

    #[test]
    fn shifted_fft()
    {
        let shape = (1, 4);
        let data = vec![0.0, 1.0, 2.0, 3.0];
        let image = BorrowedImage::new(shape, &data);
        let fft_data = image.shifted_fft();
        
        let expected = [ [-2.0, 0.0], [-2.0, -2.0], [6.0, 0.0], [-2.0, 2.0]].map(|a| c64::new(a[0], a[1]));
        assert_eq!(fft_data.data(), expected);
    }

    #[test]
    fn multiply()
    {
        let shape = (1, 4);
        let data = vec![0.0, 1.0, 2.0, 3.0];
        let image_1 = BorrowedImage::new(shape, &data);
        let image_2 = BorrowedImage::new(shape, &data);
        let m_image = image_1.mul(image_2);
        let expected = [ 0.0, 1.0, 4.0, 9.0];
        assert_eq!(m_image.data(), expected);
    }

    // #[test]
    // fn perimenter()
    // {
    //     let radius = 7.0;        
    //     let centre = (7.0, 7.0);
    //     assert_eq!(perimeter_location(radius, centre, (0.0, 2.0)), false);
    //     for col in 3..10
    //     {
    //         assert_eq!(perimeter_location(radius, centre, (0.0, col as f64)), true);
    //     }
    //     assert_eq!(perimeter_location(radius, centre, (0.0, 11.0)), false);
        
    //     assert_eq!(perimeter_location(radius, centre, (1.0, 1.0)), false);
    //     assert_eq!(perimeter_location(radius, centre, (1.0, 2.0)), true);
    //     assert_eq!(perimeter_location(radius, centre, (1.0, 3.0)), true);
    //     for col in 4..9
    //     {
    //         assert_eq!(perimeter_location(radius, centre, (1.0, col as f64)), false);
    //     }
    //     assert_eq!(perimeter_location(radius, centre, (1.0, 10.0)), true);
    //     assert_eq!(perimeter_location(radius, centre, (1.0, 11.0)), true);
    //     assert_eq!(perimeter_location(radius, centre, (1.0, 12.0)), false);
    // }

    #[test]
    fn perimenter_pixels()
    {
        let (shape, data) = generate(14, 14);
        let image = BorrowedImage::new(shape, &data);
        //local crossing expected
        //let expected = [60, 61, 62, 63, 64, 65, 74, 79, 88, 93, 102, 107, 116, 121, 130, 131, 132, 133, 134, 135];
        let expected = [61, 62, 63, 64, 65, 74, 75, 79, 80, 88, 94, 102, 108, 116, 122, 130, 131, 135, 136, 145, 146, 147, 148, 149];
        let mut result = image.perimimeter_pixels(3);
        result.sort();

        assert_eq!(result.into_iter().map(|x| *x).collect::<Vec<usize>>().as_slice(), expected);
    }

    #[test]
    fn drop_rows_test()
    {
        let (shape, data) = generate(5, 5);
        let mut image = OwnedImage::new(shape, data);
        assert_eq!(image.drop_rows(2).is_ok(), true);
        assert_eq!(image.shape(), (3, 5));
        assert_eq!(image.data(), [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14])
    }

    #[test]
    fn drop_cols_test()
    {
        let (shape, data) = generate(5, 5);
        let mut image = OwnedImage::new(shape, data);
        assert_eq!(image.drop_cols(2).is_ok(), true);
        assert_eq!(image.shape(), (5, 3));
        assert_eq!(image.data(), [0, 1, 2, 5, 6, 7, 10, 11, 12, 15, 16, 17, 20, 21, 22])
    }

    #[test]
    fn mut_image_is_image()
    {
        let (shape, data) = generate(5, 5);
        let mut image = OwnedImage::new(shape, data);
        let image_ref = &mut image;
        assert_eq!(image_ref.n_rows(), 5);
        assert_eq!(image_ref.n_cols(), 5);
        assert_eq!(image_ref.shape(), (5, 5));
    }

    #[test]
    fn mapped_image_is_correct_shape()
    {
        let (shape, data) = generate(5, 5);
        let image = OwnedImage::new(shape, data);
        let new_image = image.map(|_| 0);
        assert_eq!(new_image.data(), vec![0; 25]);
        assert_eq!(new_image.shape(), image.shape())
    }
}