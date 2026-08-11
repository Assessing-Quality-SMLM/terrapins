use crate::Image;

use std::collections::HashSet;

fn get_next_location(d: &mut i32, x: &mut usize, y: &mut usize) -> ()
{
	let d_v = *d;
	let x_v = *x as i32;
	let y_v = *y as i32;
	if *d < 0
	{
		*d = d_v + 4 * x_v + 6;
		*x = x_v as usize + 1;
	}
	else 
	{
		*d = d_v + (4 * (x_v - y_v)) + 10;
		*x = x_v as usize + 1;
		*y = y_v as usize - 1;
	}
}

pub fn bresenham<I: Image>(image: I, radius: usize) -> Vec<usize>
{
	let (centre_row, centre_col) = image.centre_position();
	let p = centre_col;
	let q = centre_row;
	let mut dd = 3 - (2 * radius as i32);
	let mut x = 0;
	let mut y = radius;
	let mut values = HashSet::new();
	while x < y
	{
		let a = (x + p, y + q);
		let b = (y + p, x + q);
		let c = (p - y, x + q);
		let d = (p - x, y + q);
		let e = (p - x, q - y);
		let f = (p - y, q - x);
		let g = (y + p, q - x);
		let h = (x + p, q - y);
		values.insert(image.get_index(a.1, a.0));
		values.insert(image.get_index(b.1, b.0));
		values.insert(image.get_index(c.1, c.0));
		values.insert(image.get_index(d.1, d.0));
		values.insert(image.get_index(e.1, e.0));
		values.insert(image.get_index(f.1, f.0));
		values.insert(image.get_index(g.1, g.0));
		values.insert(image.get_index(h.1, h.0));
		get_next_location(&mut dd, &mut x, &mut y);
	}
	values.into_iter().collect()
}

#[cfg(test)]
mod tests 
{
	use super::*;
	
	use crate::BorrowedImage;

	fn generate(n_rows: usize, n_cols: usize) -> ((usize, usize),Vec<usize>)
    {
        ((n_rows, n_cols), (0..(n_rows * n_cols)).collect())
    }

	#[test]
    fn basic_test()
    {
        let (shape, data) = generate(14, 14);
        let image = BorrowedImage::new(shape, &data);
        //local crossing expected
        //let expected = [60, 61, 62, 63, 64, 65, 74, 79, 88, 93, 102, 107, 116, 121, 130, 131, 132, 133, 134, 135];
        let expected = [62, 63, 64, 88, 94, 102, 108, 116, 122, 146, 147, 148];
        let mut result = bresenham(image, 3);
        result.sort();
        assert_eq!(result.as_slice(), expected);
    }
}