use crate::tools::{ExternalError};
use crate::io::{ZipError};

use frc::Error as FrcError;

use tiff_wrap::{TiffWrapError as TiffError};

use serde_json::{Error as JsonError};

use std::fmt::{Debug, Display, Formatter};
use std::io::{Error as IoError};

#[derive(Debug)]
pub enum Error 
{
    Io(IoError),
    Parse(String),
    Render(String),
    Data(String),
    Frc(FrcError),
    ImageParse(String),
    ZipReading(ZipError),
    External(ExternalError),
    Json(JsonError),
    Tiff(TiffError),
    Extraction(String)
}

impl Error
{
    pub fn data(error: &str) -> Self 
    {
        Self::Data(error.to_string())    
    }

    pub fn rendering(error: String) -> Self
    {
        Self::Render(error)
    }

    pub fn parse(error: String) -> Self
    {
        Self::Parse(error)
    }

    pub fn parse_localisation(error: String) -> Self
    {
        Self::parse(error)
    }

    pub fn io(error: IoError) -> Self
    {
        Self::Io(error)
    }

    pub fn image_parse(error: String) -> Self
    {
        Self::ImageParse(error)
    }

    pub fn extraction(error: String) -> Self
    {
        Self::Extraction(error)
    }
}

impl From<FrcError> for Error
{
    fn from(error: FrcError) -> Self
    {
        Self::Frc(error)
    }
}

impl From<IoError> for Error
{
    fn from(error: IoError) -> Self
    {
        Self::Io(error)
    }
}

impl From<ZipError> for Error
{
    fn from(error: ZipError) -> Self
    {
        Self::ZipReading(error)
    }
}

impl From<ExternalError> for Error
{
    fn from(error: ExternalError) -> Self
    {
        Self::External(error)
    }
}

impl From<JsonError> for Error
{
    fn from(error: JsonError) -> Self
    {
        Self::Json(error)
    }
}

impl From<TiffError> for Error
{
    fn from(error: TiffError) -> Self
    {
        Self::Tiff(error)
    }
}

impl Display for Error
{
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), std::fmt::Error>
    {
        write!(f, "{:?}", self)
    }
}

impl From<Error> for String
{
    fn from(e: Error) -> Self
    {
        e.to_string()
    }
}
