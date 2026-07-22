#[derive(Debug)]
pub struct Config
{
    image_width: usize,
}

impl Config
{
    pub fn new(image_width: usize) -> Self
    {
        Self{image_width}
    }

    ///pixel size in image space
    #[allow(non_snake_case)]
    pub fn L(&self) -> f64
    {
        self.image_width as f64
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn test_l() 
    {
        assert_eq!(Config::new(10).L(), 10.0)
    }
}
