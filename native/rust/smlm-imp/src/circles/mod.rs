mod bresenham;

use crate::{Image};
use crate::utils;

use std::collections::{HashSet};

fn within_radius(radius: f64, centre_position: (f64, f64), pixel: (f64, f64)) -> bool
{
    let (centre_row, centre_col) = centre_position;
    let (row, col) = pixel;

    let x = col - centre_col;
    let y = row - centre_row;
    let distance = (x.powi(2) + y.powi(2)).sqrt();
    distance < radius
}

fn locale(pixel: (usize, usize)) -> [(usize, usize); 4]
{
    let (row, col) = pixel;
    let top_right = (row, col + 1);
    let lower_left = (row + 1, col);
    let lower_right = (row + 1, col + 1);
    [pixel, top_right, lower_left, lower_right]
}

fn locale_indicies<I: Image>(image: I, pixel: (usize, usize)) -> [usize; 4]
{
    locale(pixel).map(|(r, c)| image.get_index(r, c))
}

fn within_image<I: Image>(image: I, radius: f64) -> Vec<bool>
{
    let centre = image.centre_position();
    let centre = (centre.0 as f64, centre.1 as f64);
    // println!("centre: {:?}", centre);
    let mut within = vec![false; image.size()];
    for row in 0..image.n_rows()
    {
        for col in 0..image.n_cols()
        {
            let pixel = (row as f64, col as f64);
            let index = image.get_index(row, col);
            within[index] = within_radius(radius, centre, pixel);
            // println!("{index} {row},{col}={}", within[index])
        }
    }
    within
}

fn local_crossing<I: Image>(image: I, radius: f64) -> Vec<usize>
{
    let within = within_image(&image, radius);
    // println!("{:?}", w_data);
    let mut perimeter = HashSet::new();
    for row in 0..(image.n_rows() - 1)
    {
        for col in 0..(image.n_cols() - 1)
        {
            let pixel = (row, col);
            let locale = locale_indicies(&image, pixel);
            let locale_data = locale.map(|i| within[i]);
            // println!("{:?} = {:?}", pixel, locale_data);
            let seed = locale_data[0];
            let index = image.get_index(row, col);
            for value in &locale_data[1..]
            {
                // check if perimeter goes through pixel
                if *value != seed 
                {
                    perimeter.insert(index);
                }
            }
        }
    }
    perimeter.into_iter().collect()
}

pub fn angle_carve<I: Image>(image: I, radius: f64) -> Vec<usize>
{
    let mut indicies = HashSet::new();
    let (centre_row, centre_col) = image.centre_position();
    for radian in 0..360
    {
        let row = ((radian as f64).sin() * radius + centre_row as f64).round() as usize;
        let col = ((radian as f64).cos() * radius + centre_col as f64).round() as usize;
        let index = utils::get_index(row, col, image.n_cols());
        indicies.insert(index);
    }
    indicies.into_iter().collect()
}

pub fn bresenham<I: Image>(image: I, radius: f64) -> Vec<usize>
{
    bresenham::bresenham(image, radius.round() as usize)
}

pub trait Perimeter
{
    fn get<I: Image>(image: I, radius: f64) -> Vec<usize>;
}

pub struct AngleCarve;
impl Perimeter for AngleCarve
{
    fn get<I>(image: I, radius: f64) -> Vec<usize> where I: Image
    {
        angle_carve(image, radius)
    }
}

pub struct LocalCrossing;
impl Perimeter for LocalCrossing
{
    fn get<I>(image: I, radius: f64) -> Vec<usize> where I: Image
    {
        local_crossing(image, radius)
    }
}

pub struct Bresenham;
impl Perimeter for Bresenham
{
    fn get<I>(image: I, radius: f64) -> Vec<usize> where I: Image
    {
        bresenham(image, radius)
    }
}

#[derive(Debug)]
pub enum Method 
{
    LocalCrossing,
    AngleCarve,
    Bresenham
}

impl Method
{
    pub fn get_perimeter<I: Image>(&self, image: I, radius: f64) -> Vec<usize>
    {
        match self
        {
            Self::LocalCrossing => local_crossing(image, radius),
            Self::AngleCarve => angle_carve(image, radius),
            Self::Bresenham => bresenham(image, radius)
        }
    }
}

#[cfg(test)]
mod tests 
{
    use crate::images::BorrowedImage;
    
    fn generate(n_rows: usize, n_cols: usize) -> ((usize, usize),Vec<usize>)
    {
        ((n_rows, n_cols), (0..(n_rows * n_cols)).collect())
    }


    // These tests have slightly different results they are both correct in what they do but result in different circles

    #[test]
    fn local_crossing()
    {
        let (shape, data) = generate(14, 14);
        let image = BorrowedImage::new(shape, &data);
        let expected = [60, 61, 62, 63, 64, 65, 74, 79, 88, 93, 102, 107, 116, 121, 130, 131, 132, 133, 134, 135];
        let mut result = super::local_crossing(image, 3.0);
        result.sort();
        assert_eq!(result.as_slice(), expected);
    }

    #[test]
    fn angle_carve()
    {
        let (shape, data) = generate(14, 14);
        let image = BorrowedImage::new(shape, &data);
        let expected = [61, 62, 63, 64, 65, 74, 75, 79, 80, 88, 94, 102, 108, 116, 122, 130, 131, 135, 136, 145, 146, 147, 148, 149];
        let mut result = super::angle_carve(image, 3.0);
        result.sort();
        assert_eq!(result.as_slice(), expected);
    }
}