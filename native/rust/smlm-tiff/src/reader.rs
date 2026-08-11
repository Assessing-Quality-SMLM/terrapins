use crate::{TiffWrapError, DataType};
use crate::image_data::{ImageData};

use tiff::decoder::{Decoder};

use std::fs::{File};
use std::iter::{Iterator};
use std::io::{Read, Write, Seek, BufReader};
use std::path::{Path};


#[derive(Debug)]
pub struct TiffReader<R: Read + Seek>
{
    decoder: Decoder<R>,
    current: usize,
}

impl TiffReader<BufReader<File>>
{
    pub fn from_disk<P: AsRef<Path>>(filename: P) -> Result<Self, TiffWrapError>
    {
        File::open(filename).map_err(TiffWrapError::from).and_then(Self::from_buffered)
    }
}

impl<R: Read + Seek> TiffReader<BufReader<R>>
{
    pub fn from_buffered(reader: R) -> Result<Self, TiffWrapError>
    {
        Self::try_from_reader(BufReader::new(reader))
    }
}

impl<R: Read + Seek> TiffReader<R>
{
    pub fn try_from_reader(reader : R) -> Result<Self, TiffWrapError>
    {
        Decoder::new(reader).map(Self::new).map_err(TiffWrapError::from)
    }

    pub fn new(decoder: Decoder<R>) -> Self
    {
        Self{decoder, current: 0}
    }

    pub fn decoder(self) -> Decoder<R>
    {
        self.decoder
    }

    pub fn data_type(&mut self) -> Option<DataType>
    {
        self.decoder.read_image()
                    .map(ImageData::new)
                    .map(|i| i.data_type())
                    .and_then(|s| self.decoder.seek_to_image(self.current).map(|_| s))
                    .ok()
    }

    pub fn current_image(&mut self) -> Result<ImageData, TiffWrapError>
    {
        self.decoder.read_image().map(ImageData::new).map_err(TiffWrapError::from)
    }

    pub fn read_image(&mut self, idx: usize) -> Result<ImageData, TiffWrapError>
    {
        let _ = self.decoder.seek_to_image(idx)?;
        self.decoder.read_image().map(ImageData::new).map_err(TiffWrapError::from)
    }

    pub fn n_images(&mut self) -> usize
    {
        let mut total = 1;
        let _ = self.decoder.seek_to_image(0);
        while self.decoder.more_images() 
        {
            total += 1;
            let _ = self.decoder.next_image();
        }
        let _ = self.decoder.seek_to_image(self.current);
        total
    }

    pub fn dimensions(&mut self) -> Result<(u32, u32), TiffWrapError>
    {
        self.decoder.dimensions().map_err(TiffWrapError::from)
    }

    //read tiff source - docs dont mention
    pub fn width(&mut self) -> Result<u32, TiffWrapError>
    {
        self.dimensions().map(|t| t.0)
    }

    //read tiff source - docs dont mention
    pub fn height(&mut self) -> Result<u32, TiffWrapError>
    {
        self.dimensions().map(|t| t.1)
    }

    pub fn colour_type(&mut self) -> String
    {
        match self.try_colour_type()
        {
            Ok(c) => c,
            Err(e) => e
        }
    }

    pub fn try_colour_type(&mut self) -> Result<String, String>
    {
        self.decoder.colortype().map(|c| format!("{:?}", c)).map_err(|e| e.to_string())
    }

    pub fn readers_match<S: Read + Seek>(&mut self, other_reader: &mut TiffReader<S>) -> bool
    {
        self.zip(other_reader).all(|(i, j)| i == j)
    }

    pub fn write_image_data<W: Write>(&mut self, mut writer : W) -> Result<(), TiffWrapError>
    {
        for frame in self
        {
            let _ = frame.write_to(&mut writer)?;
        }
        Ok(())
    }
}

impl<R: Read + Seek> Iterator for TiffReader<R>
{
    type Item = ImageData;
    fn next(&mut self) -> Option<Self::Item>
    {
        match self.decoder.seek_to_image(self.current)
        {
            Ok(_) => 
            {
                if self.decoder.more_images()
                {
                    self.current += 1;
                    self.decoder.read_image().map(ImageData::new).ok()
                }
                else 
                {
                    None
                }
            },
            Err(_) => None
        }
    }
}