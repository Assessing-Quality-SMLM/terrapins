use crate::{TiffWrapError};

use tiff::encoder::{TiffEncoder, ImageEncoder, TiffKindStandard, TiffKindBig, TiffKind};
use tiff::encoder::colortype;
use tiff::encoder::colortype::{ColorType};
use tiff::tags::{Tag};

use std::fmt::{Debug, Formatter, Error as FmtError};
use std::fs::{File};
use std::iter::{Iterator};
use std::io::{Seek, BufWriter, Write};
use std::path::{Path};

pub struct TiffImage<'a, W: 'a + Write + Seek, C: ColorType, K: TiffKind>
{
    encoder: ImageEncoder<'a,W, C, K>
}

impl<'a, W: 'a + Write + Seek, C: ColorType, K: TiffKind> TiffImage<'a, W, C, K> 
{
    pub fn new(encoder: ImageEncoder<'a, W, C, K>) -> Self
    {
        Self{encoder}
    }

    pub fn write_image_description(&mut self, description: &str) -> Result<(), TiffWrapError>
    {
        self.encoder.encoder()
                    .write_tag(Tag::ImageDescription, description)
                    .map_err(TiffWrapError::from)
    }

    pub fn write_data(self,  image_data: &[C::Inner]) -> Result<(), TiffWrapError>
    where [<C as ColorType>::Inner]: TiffValue
    {
        self.encoder.write_data(image_data).map_err(TiffWrapError::from)
    }
}

pub use tiff::encoder::TiffValue;

pub trait ToColourType
{
    type ColourType : ColorType<Inner=Self>;
}

impl ToColourType for f64
{
    type ColourType = colortype::Gray64Float;
}

impl ToColourType for f32
{
    type ColourType = colortype::Gray32Float;
}

impl ToColourType for i64
{
    type ColourType = colortype::GrayI64;
}

impl ToColourType for i32
{
    type ColourType = colortype::GrayI32;
}

impl ToColourType for i16
{
    type ColourType = colortype::GrayI16;
}

impl ToColourType for i8
{
    type ColourType = colortype::GrayI8;
}

impl ToColourType for u64
{
    type ColourType = colortype::Gray64;
}

impl ToColourType for u32
{
    type ColourType = colortype::Gray32;
}

impl ToColourType for u16
{
    type ColourType = colortype::Gray16;
}

impl ToColourType for u8
{
    type ColourType = colortype::Gray8;
}

pub struct TiffWriter<W: Write + Seek, K: TiffKind>
{
    encoder: TiffEncoder<W, K>,
    width: u32,
    height: u32,
}

impl<K: TiffKind> TiffWriter<BufWriter<File>, K>
{
    pub fn from_disk<P: AsRef<Path>>(filename: P, width: u32, height: u32) -> Result<Self, TiffWrapError>
    {
        File::create(filename).map_err(TiffWrapError::from)
                              .and_then(|f| Self::buffered(f, width, height))
    }
}

impl<W: Write + Seek, K: TiffKind> TiffWriter<BufWriter<W>, K>
{
    pub fn buffered(writer: W, width: u32, height: u32) -> Result<Self, TiffWrapError>
    {
        Self::new(BufWriter::new(writer), width, height)
    }
}

impl<W: Write + Seek, K: TiffKind> TiffWriter<W, K> 
{
    pub fn new(writer: W, width: u32, height: u32) -> Result<Self, TiffWrapError>
    {
        TiffEncoder::new_generic(writer).map_err(TiffWrapError::from).map(|e| Self{encoder: e, width, height})
    }

    pub fn write_images_for<'a, T, I: Iterator<Item = &'a [T]>>(&mut self, images: I) -> Result<(), TiffWrapError>
    where T: ToColourType + 'a, [T]: TiffValue
    {
        images.map(|i| self.write_image_for(i)).collect()
    }

    pub fn write_image_for<T>(&mut self, image: &[T]) -> Result<(), TiffWrapError>
    where T: ToColourType, [T]: TiffValue
    {
        self.write_image::<<T as ToColourType>::ColourType>(image)
    }

    pub fn write_image<C: ColorType>(&mut self, image_data: &[C::Inner]) -> Result<(), TiffWrapError>
    where [C::Inner]: TiffValue,
    {
        self.create_image::<C>().and_then(|i| i.write_data(image_data))
    }

    pub fn create_image_for<T>(&mut self) -> Result<TiffImage<W, T::ColourType, K>, TiffWrapError>
    where T: ToColourType,
    {
        self.create_image::<<T as ToColourType>::ColourType>()
    }

    pub fn create_image<C: ColorType>(&mut self) -> Result<TiffImage<W, C, K>, TiffWrapError>
    {
        self.encoder.new_image::<C>(self.width, self.height).map(TiffImage::new).map_err(TiffWrapError::from)
    }
}

impl<W: Write + Seek, K: TiffKind> Debug for TiffWriter<W, K>
{
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError>
    { 
        f.debug_struct("TiffWriter")
         .field("encoder", &"")
         .field("width", &self.width)
         .field("height", &self.height)
         .finish()
    }
}

pub type BigTiffWriter<W> = TiffWriter<W, TiffKindBig>;
pub type StandardTiffWriter<W> = TiffWriter<W, TiffKindStandard>;
