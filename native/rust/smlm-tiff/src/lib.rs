extern crate tiff;

mod data_type;
mod image_data;
pub mod reader;
pub mod writer;

pub use self::data_type::{DataType};
pub use self::image_data::{ImageData};

use crate::reader::{TiffReader};
use crate::writer::{StandardTiffWriter, TiffValue, ToColourType};

use tiff::{TiffError};

use std::fmt::{Display, Formatter, Error as FmtError};
use std::fs::{File};
use std::io::{Read, Seek, BufReader, Error as IoError, ErrorKind};
use std::path::{Path};

#[derive(Debug)]
pub enum TiffWrapError
{
    IO(IoError),
    Tiff(TiffError),
    Conversion(String)
}

impl TiffWrapError
{
    pub fn conversion(error: String) -> Self
    {
        Self::Conversion(error)
    }
}

impl From<IoError> for TiffWrapError
{
    fn from(error: IoError) -> Self
    {
        TiffWrapError::IO(error)
    }
}

impl From<TiffError> for TiffWrapError
{
    fn from(error: TiffError) -> Self
    {
        TiffWrapError::Tiff(error)
    }
}

impl From<String> for TiffWrapError
{
    fn from(error: String) -> Self
    {
        Self::conversion(error)
    }
}

impl From<TiffWrapError> for IoError
{
    fn from(error: TiffWrapError) -> Self
    {
        match error
        {
            TiffWrapError::IO(e) => e,
            TiffWrapError::Tiff(e) => IoError::new(ErrorKind::Other, e.to_string()),
            TiffWrapError::Conversion(e) => IoError::new(ErrorKind::Other, e.to_string())
        }
    }
}

impl Display for TiffWrapError
{
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError> 
    { 
        match self
        {
            Self::Tiff(e) => write!(f, "{}", e),
            Self::IO(e) => write!(f, "{}", e),
            Self::Conversion(e) => write!(f, "{}", e)
        }
    }
}


#[derive(Debug)]
pub struct Tiff;

impl Tiff
{
    pub fn read<P : AsRef<Path>>(filepath: P) -> Result<TiffReader<BufReader<File>>, TiffWrapError>
    {
        TiffReader::from_disk(filepath)
    }

    pub fn read_from<R: Read + Seek>(reader: R) -> Result<TiffReader<BufReader<R>>, TiffWrapError>
    {
        TiffReader::from_buffered(reader)
    }

    pub fn write_u16<'a, P : AsRef<Path>, I>(filepath: P, width: u32, height: u32, images: I) -> Result<(), TiffWrapError>
    where I: Iterator<Item = &'a [u16]>
    {
        StandardTiffWriter::from_disk(filepath, width, height).and_then(|mut w| w.write_images_for(images))
    }

    pub fn write_frame<'a, T, P : AsRef<Path>>(filepath: P, width: u32, height: u32, frame: &[T]) -> Result<(), TiffWrapError>
    where T: ToColourType, [T]: TiffValue
    {
        StandardTiffWriter::from_disk(filepath, width, height).and_then(|mut w| w.write_image_for(frame))
    }
}