use super::method::{SplitMethod};

use crate::io::{ParseMethod};

#[derive(Debug, Default)]
pub struct Settings
{
    method: SplitMethod,
    parse_method: ParseMethod,
}

impl Settings
{
    pub fn method(&self) -> &SplitMethod
    {
        &self.method
    }

    pub fn with_split_method(mut self, value: SplitMethod) -> Self
    {
        self.method = value;
        self
    }

    pub fn parse_method(&self) -> &ParseMethod
    {
        &self.parse_method
    }

    pub fn with_parse_method(mut self, value: ParseMethod) -> Self
    {
        self.parse_method = value;
        self
    }
}