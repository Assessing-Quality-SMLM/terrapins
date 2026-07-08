use crate::{Pad, Crop, Image, ImageMut};

#[derive(Debug)]
pub struct OwnedImage<T>
{
    shape : (usize, usize),
    data : Vec<T>
}

impl<T> OwnedImage<T>
{
    pub fn new(shape: (usize, usize), data: Vec<T>) -> Self
    {
        Self{shape, data}
    }

    pub fn zeros(shape: (usize, usize)) -> Self
    where T: Default + Clone
    {
        Self::new(shape, vec![T::default(); shape.0 * shape.1])
    }

    pub fn reshape(&mut self, new_shape: (usize, usize)) -> ()
    {
        self.shape = new_shape
    }
}

impl<'a, T> Image for OwnedImage<T>
{
    type Data = T;

    fn n_rows(&self) -> usize { self.shape.0 }
    fn n_cols(&self) -> usize { self.shape.1 }
    fn data(&self) -> &[Self::Data]
    {
        &self.data
    }
}

impl<T> ImageMut for OwnedImage<T>
{
    fn data_mut(&mut self) -> &mut [<Self as Image>::Data]
    {
        &mut self.data
    }
}

impl<'a, T: Default + Clone> Pad for OwnedImage<T>
{
    fn pad_rows_start(&mut self, n: usize) -> Result<(), ()>
    {
        let n_rows = self.n_rows();
        let n_items = n * self.n_cols();
        let items = std::iter::once(T::default()).cycle().take(n_items);
        self.data.splice(0..0, items);
        self.shape = (n_rows + n, self.n_cols());
        Ok(())
    }

    fn pad_rows_end(&mut self, n: usize) -> Result<(), ()>
    {
        let n_rows = self.n_rows();
        let n_items = n * self.n_cols();
        let items = std::iter::once(T::default()).cycle().take(n_items);
        self.data.extend(items);
        self.shape = (n_rows + n, self.n_cols());
        Ok(())
    }

    fn pad_cols_start(&mut self, n: usize) -> Result<(), ()>
    {
        let n_cols = self.n_cols();
        let new_cols = self.n_cols() + n;
        for row in 0..self.n_rows()
        {
            let items = std::iter::once(T::default()).cycle().take(n);
            let idx = row * new_cols;
            self.data.splice(idx..idx, items);
        }
        self.shape = (self.n_rows(), n_cols + n);
        Ok(())
    }

    fn pad_cols_end(&mut self, n: usize) -> Result<(), ()>
    {
        let n_cols = self.n_cols();
        let new_cols = self.n_cols() + n;
        for row in 0..self.n_rows()
        {
            let items = std::iter::once(T::default()).cycle().take(n);
            let idx = (row * new_cols) + n_cols;
            self.data.splice(idx..idx, items);
        }
        self.shape = (self.n_rows(), n_cols + n);
        Ok(())
    }
}

impl<'a, T: Clone + std::fmt::Debug> Crop for OwnedImage<T>
{
    fn take_rows(&mut self, n: usize) -> Result<(), ()>
    {
        let end = n * self.n_cols();
        self.data.drain(0..end);
        self.shape = (self.n_rows() - n, self.n_cols());
        Ok(())
    }

    fn drop_rows(&mut self, n: usize) -> Result<(), ()>
    {
        let end_row = self.n_rows() - n;
        let index = self.get_index(end_row, 0);
        self.data.truncate(index);
        self.shape = (end_row, self.n_cols());
        Ok(())
    }

    fn take_cols(&mut self, n: usize) -> Result<(), ()>
    {
        let n_cols = self.n_cols();
        let remaining = n_cols - n;
        for row in 0..self.n_rows()
        {
            let start = row * remaining; 
            let end = start + n;
            self.data.splice(start..end, std::iter::empty());
        }
        self.shape = (self.n_rows(), remaining);
        Ok(())
    }

    fn drop_cols(&mut self, n: usize) -> Result<(), ()>
    {
        let head = self.n_cols() - n;
        for row in 0..self.n_rows()
        {
            let row_start = row * head;
            let start = row_start + head;
            let end = start + n;
            self.data.splice(start..end, std::iter::empty());
        }
        self.shape = (self.n_rows(), head);
        Ok(())
    }

    fn central_col_crop(&mut self, n: usize) -> Result<(), ()>
    {
        let n_cols = self.n_cols() - n;
        let mut new_data = Vec::with_capacity(self.n_rows() * n_cols);
        let is_even = n % 2 == 0;
        let half = n_cols / 2;
        let start = if is_even {half} else{half + 1};
        for row in 0..self.n_rows()
        {
            let index_start = self.get_index(row, start);
            let index_end = index_start + n_cols;
            new_data.extend_from_slice(&self.data[index_start..index_end]);
        }
        self.data = new_data;
        self.shape = (self.n_rows(), n_cols);
        Ok(())
    }
}

impl<T> FromIterator<T> for OwnedImage<T>
{
    fn from_iter<I: IntoIterator<Item=T>>(iter: I) -> Self
    {
        Self::new((1, 1), iter.into_iter().collect())
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn zeros_usize() 
    {
        let image = OwnedImage::<usize>::zeros((2, 2));
        assert_eq!(image.data(), [0, 0, 0, 0]);
    }

    #[test]
    fn zeros_f64() 
    {
        let image = OwnedImage::<f64>::zeros((2, 2));
        assert_eq!(image.data(), [0.0, 0.0, 0.0, 0.0]);
    }

    #[test]
    fn take_rows() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8];
        let mut image = OwnedImage::new((3, 3), data);
        let _ = image.take_rows(2);
        assert_eq!(image.shape(), (1, 3));
        assert_eq!(image.data(), [6, 7, 8]);
    }

    #[test]
    fn take_cols() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8];
        let mut image = OwnedImage::new((3, 3), data);
        let _ = image.take_cols(2);
        assert_eq!(image.shape(), (3, 1));
        assert_eq!(image.data(), [2, 5, 8]);
    }

    #[test]
    fn drop_cols() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8];
        let mut image = OwnedImage::new((3, 3), data);
        let _ = image.drop_cols(2);
        assert_eq!(image.shape(), (3, 1));
        assert_eq!(image.data(), [0, 3, 6]);
    }

    #[test]
    fn central_row_crop_odd() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19];
        let mut image = OwnedImage::new((5, 4), data);
        let _ = image.central_row_crop(3);
        assert_eq!(image.shape(), (2, 4));
        assert_eq!(image.data(), [8, 9, 10, 11, 12, 13, 14, 15]);
    }

    #[test]
    fn central_row_crop_even() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];
        let mut image = OwnedImage::new((4, 4), data);
        let _ = image.central_row_crop(2);
        assert_eq!(image.shape(), (2, 4));
        assert_eq!(image.data(), [4, 5, 6, 7, 8, 9, 10, 11]);
    }

    #[test]
    fn central_col_crop_odd() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19];
        let mut image = OwnedImage::new((4, 5), data);
        let _ = image.central_col_crop(3);
        assert_eq!(image.shape(), (4, 2));
        assert_eq!(image.data(), [2, 3, 7, 8, 12, 13, 17, 18]);
    }

    #[test]
    fn central_col_crop_even() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19];
        let mut image = OwnedImage::new((4, 5), data);
        let _ = image.central_col_crop(2);
        assert_eq!(image.shape(), (4, 3));
        assert_eq!(image.data(), [1, 2, 3, 6, 7, 8, 11, 12, 13, 16, 17, 18]);
    }

    #[test]
    fn central_crop() 
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];
        let mut image = OwnedImage::new((4, 4), data);
        let _ = image.central_crop(2, 2);
        assert_eq!(image.shape(), (2, 2));
        assert_eq!(image.data(), [5, 6, 9, 10]);
    }

    #[test]
    fn pad_rows_start_test()
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];
        let mut image = OwnedImage::new((4, 4), data);
        let _ = image.pad_rows_start(2);
        assert_eq!(image.shape(), (6, 4));
        assert_eq!(image.data(), [0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]);
    }

    #[test]
    fn pad_rows_end_test()
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];
        let mut image = OwnedImage::new((4, 4), data);
        let _ = image.pad_rows_end(2);
        assert_eq!(image.shape(), (6, 4));
        assert_eq!(image.data(), [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0]);
    }


    #[test]
    fn pad_cols_start_test()
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];
        let mut image = OwnedImage::new((4, 4), data);
        let _ = image.pad_cols_start(2);
        assert_eq!(image.shape(), (4, 6));
        assert_eq!(image.data(), [0, 0, 0, 1, 2, 3, 0, 0, 4, 5, 6, 7, 0, 0, 8, 9, 10, 11, 0, 0, 12, 13, 14, 15]);
    }

    #[test]
    fn pad_cols_end_test()
    {
        let data = vec![0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15];
        let mut image = OwnedImage::new((4, 4), data);
        let _ = image.pad_cols_end(2);
        assert_eq!(image.shape(), (4, 6));
        assert_eq!(image.data(), [0, 1, 2, 3, 0, 0, 4, 5, 6, 7, 0, 0, 8, 9, 10, 11, 0, 0, 12, 13, 14, 15, 0, 0])
    }
}