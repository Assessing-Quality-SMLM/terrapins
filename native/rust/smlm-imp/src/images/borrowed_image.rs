use crate::Image;

#[derive(Debug)]
pub struct BorrowedImage<'a, T>
{
    shape: (usize, usize),
    data: &'a [T]
}

impl<'a, T> BorrowedImage<'a, T>
{
    pub fn new(shape: (usize, usize), data: &'a[T]) -> Self
    {
        Self{shape, data}
    }
}

impl<'a, T> Image for BorrowedImage<'a, T>
{
    type Data = T;

    fn n_rows(&self) -> usize { self.shape.0 }
    fn n_cols(&self) -> usize { self.shape.1 }
    fn data(&self) -> &[Self::Data]
    {
        self.data
    }
}