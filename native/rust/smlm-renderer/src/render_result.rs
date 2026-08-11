use crate::{DataType, Properties};

use imp::OwnedImage;

#[derive(Debug)]
pub struct RenderResult 
{
    image: OwnedImage<DataType>,
    properties: Properties
}

impl RenderResult
{
    pub fn new(image: OwnedImage<DataType>, properties: Properties) -> Self
    {
        Self
        {
            image,
            properties
        }
    }

    pub fn image(&self) -> &OwnedImage<DataType>
    {
        &self.image
    }
    
    pub fn take_image(self) -> OwnedImage<DataType>
    {
        self.image
    }

    pub fn properties(&self) -> &Properties
    {
        &self.properties
    }
}