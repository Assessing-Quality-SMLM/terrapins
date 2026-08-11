use nalgebra::{DMatrix, Dim, Matrix, RawStorage};

use std::{fmt::Debug, iter::DoubleEndedIterator};


fn correlate_region_iterators<I: Iterator<Item = f64>, J: Iterator<Item = f64>>(image: I, kernel: J) -> f64
{
	image.zip(kernel).map(|(i, k)| i * k).sum()
}

fn convolve_region_iterators<I: Iterator<Item = f64>, J: Iterator<Item = f64> + DoubleEndedIterator>(image: I, kernel: J) -> f64
{
	correlate_region_iterators(image, kernel.rev())
}

//image is image region of same size as kernel
fn apply_convolution<R, C, S>(image: &Matrix<f64, R, C, S>, kernel: &DMatrix<f64>) -> f64 
where C: Dim, R: Dim, S: RawStorage<f64, R, C>
{
	convolve_region_iterators(image.iter().copied(), kernel.iter().copied())
	// let mut total = 0.0;
	// for row in 0..image.nrows()
	// {
	// 	let k_row = kernel.nrows() - row - 1;
	// 	for col in 0..image.ncols()
	// 	{
	// 		let k_col = kernel.ncols() - col - 1;
	// 		// println!("{row},{col} -> {k_row},{k_col}");
	// 		total += image[(row, col)] * kernel[(k_row, k_col)];
	// 	}
	// }
	// total
}

fn convolve_internal(image: &DMatrix<f64>, kernel: &DMatrix<f64>, row_offset: usize, col_offset: usize, output_image: &mut DMatrix<f64>)
{
	let internal_row_start = row_offset;
	let internal_row_end = image.nrows() - row_offset;
	let internal_col_start = col_offset;
	let internal_col_end = image.ncols() - col_offset;
	// println!("{internal_row_start},{internal_col_start} -> {internal_row_end},{internal_col_end}");
	for row_centre in internal_row_start..internal_row_end
	{
		let row = row_centre - internal_row_start;
		for col_centre in internal_col_start..internal_col_end
		{
			let col = col_centre - internal_col_start;
			// println!("{row},{col}");
			// println!("{row_centre},{col_centre}");
			let image_region = image.view((row, col), kernel.shape());
			// println!("region: {image_region}");
			let value = apply_convolution(&image_region, kernel);
			// println!("value: {value}");
			*output_image.index_mut((row_centre, col_centre)) = value;
		}
	}
}

// fn conv_ul_region_reflective<R, C, S>(region: &Matrix<f64, R, C, S>, kernel: &DMatrix<f64>, output_image: &mut DMatrix<f64>)
// where C: Dim, R: Dim, S: RawStorage<f64, R, C>

// {
// 	println!("upper left: {region}");
// 	let mut mask = DMatrix::zeros(kernel.nrows(), kernel.ncols());
// 	for row in 0..mask.nrows()
// 	{
// 		let k_row = mask.nrows() - row - 1;
// 		for col in 0..mask.ncols()
// 		{
// 			let k_col = mask.ncols() - col - 1;
// 			println!("{k_row},{k_col}");
// 			*mask.index_mut((k_row, k_col)) = *region.index((row, col));
// 			println!("mask: {mask}");
// 			let value = apply_convolution(&mask, kernel);
// 			*output_image.index_mut((row, col)) = value;
// 		}
// 	}
// }

// fn conv_ul_region<R, C, S>(region: &Matrix<f64, R, C, S>, kernel: &DMatrix<f64>, output_image: &mut DMatrix<f64>)
// where C: Dim, R: Dim, S: RawStorage<f64, R, C>
// {
// 	// println!("upper left: {region}");
// 	let mut mask = DMatrix::zeros(kernel.nrows(), kernel.ncols());
// 	for row in 0..mask.nrows()
// 	{
// 		for col in 0..mask.ncols()
// 		{
// 			mask.fill(0.0);
// 			let row_size = row + 1;
// 			let col_size = col + 1;
// 			let sub_region = region.view((0, 0), (row_size, col_size));
// 			// println!("sub: {sub_region}");
// 			for region_row in 0..row_size
// 			{
// 				let row_idx = row_size - region_row - 1;
// 				let k_row = kernel.nrows() - region_row - 1;
// 				for region_col in 0..col_size
// 				{
// 					let col_idx = col_size - region_col - 1;
// 					let k_col = kernel.ncols() - region_col - 1;
// 					*mask.index_mut((k_row, k_col)) = *sub_region.index((row_idx, col_idx));
// 				}
// 			} 
// 			// println!("mask: {mask}");
// 			let value = apply_convolution(&mask, kernel);
// 			*output_image.index_mut((row, col)) = value;
// 		}
// 	}
// }

// fn conv_ur_region<R, C, S>(region: &Matrix<f64, R, C, S>, kernel: &DMatrix<f64>, output_image: &mut DMatrix<f64>)
// where C: Dim, R: Dim, S: RawStorage<f64, R, C>
// {
// 	println!("upper right: {region}");
// 	let mut mask = DMatrix::zeros(kernel.nrows(), kernel.ncols());
// 	for row in 0..mask.nrows()
// 	{
// 		for col in 0..mask.ncols()
// 		{
// 			mask.fill(0.0);
// 			let row_size = row + 1;
// 			let col_size = col + 1;
// 			let sub_region_col_start = region.ncols() - col;
// 			let sub_region = region.view((row, sub_region_col_start), (row_size, col_size));
// 			println!("sub: {sub_region}");
// 			for region_row in 0..row_size
// 			{
// 				let row_idx = row_size - region_row - 1;
// 				let k_row = kernel.nrows() - region_row - 1;
// 				for region_col in 0..col_size
// 				{
// 					let col_idx = col_size - region_col - 1;
// 					let k_col = kernel.ncols() - region_col - 1;
// 					*mask.index_mut((k_row, k_col)) = *sub_region.index((row_idx, col_idx));
// 				}
// 			} 
// 			// println!("mask: {mask}");
// 			let value = apply_convolution(&mask, kernel);
// 			*output_image.index_mut((row, col)) = value;
// 		}
// 	}
// }

// fn fill_mask(image: &DMatrix<f64>, mask: &mut DMatrix<f64>, row_start: i64, col_start: i64)
// {
// 	for row in row_start..-row_start
// 	{
// 		for col in col_start..-col_start
// 		{

// 		}
// 	}
// }

// fn convolve_borders(image: &DMatrix<f64>, kernel: &DMatrix<f64>, row_offset: usize, col_offset: usize, output_image: &mut DMatrix<f64>)
// {
// 	// let ul_region = image.view((0, 0), kernel.shape());
// 	// conv_ul_region(&ul_region, kernel, output_image);
	
// 	// let ur_col_start = image.ncols() - kernel.ncols();
// 	// let ur_region = image.view((0, ur_col_start), kernel.shape());
// 	// conv_ur_region(&ur_region, kernel, output_image);

// 	let kernel_rows = kernel.nrows() as i64;
// 	let kernel_cols = kernel.ncols() as i64;

// 	let image_rows = image.nrows() as i64;
// 	let image_cols = image.ncols() as i64;

// 	let row_offset = row_offset as i64;
// 	let col_offset = col_offset as i64;

// 	let half_row_offset = row_offset / 2;
// 	let half_col_offset = col_offset / 2;

// 	let total_rows = image_rows + row_offset;
// 	let total_cols = image_cols + col_offset;


// 	// println!("{internal_row_start},{internal_col_start} -> {internal_row_end},{internal_col_end}");
// 	let mut mask = DMatrix::zeros(kernel.nrows(), kernel.ncols());
// 	for row_centre in -half_row_offset..(image_rows + half_row_offset)
// 	{
// 		let row_start = row_centre - row_offset;
// 		for col_centre in -half_col_offset..(image_cols + half_col_offset)
// 		{
// 			let col_start = col_centre - col_offset;
// 			if row_start >= 0 && col_start >= 0 // internal -> already covered
// 			{
// 				continue;
// 			}
// 			println!("{row_start},{col_start}");
// 			mask.fill(0.0);


// 			// println!("region: {image_region}");
// 			let value = apply_convolution(&mask, kernel);
// 			*output_image.index_mut((row_centre as usize, col_centre as usize)) = value;
// 		}
// 	}
// }

fn pad_image<T: Clone + PartialEq + Debug + 'static>(image: &DMatrix<T>, row_offset: usize, col_offset: usize,  value: T) -> DMatrix<T>
{
	let total_rows = image.nrows() + row_offset + row_offset;
	let total_cols = image.ncols() + col_offset + col_offset;
	let mut padded = DMatrix::from_element(total_rows, total_cols, value);
	for (p, i) in padded.view_mut((row_offset, col_offset), image.shape()).iter_mut().zip(image.iter())
	{
		*p = i.clone()
	}
	padded
}

fn offset(kernel_size: usize) -> usize
{
	kernel_size / 2
}

fn convolve(image: &DMatrix<f64>, kernel: &DMatrix<f64>) -> DMatrix<f64>
{
	let row_offset = offset(kernel.nrows());
	let col_offset = offset(kernel.nrows());
	let padded = pad_image(image, row_offset + 1, col_offset + 1, 0.0);
	// println!("{padded}");
	let mut output_image = DMatrix::zeros(padded.nrows(), padded.ncols());
	convolve_internal(&padded, kernel, row_offset, col_offset, &mut output_image);
	// convolve_borders(image, kernel, row_offset, col_offset, &mut output_image);
	let output_image = output_image.remove_row(0).remove_column(0);
	let output_image = output_image.clone().remove_row(output_image.nrows() - 1);
	output_image.clone().remove_column(output_image.ncols() - 1)
}

fn row_data<T: Copy + std::cmp::PartialEq + std::fmt::Debug + 'static>(image: &DMatrix<T>) -> Vec<T>
{
	image.row_iter().flat_map(|r| r.into_iter().collect::<Vec<_>>()).copied().collect()
}

fn matrix_from_image<T: Clone + PartialEq + Debug + 'static>(data: &[T], n_rows: usize, n_cols: usize) -> DMatrix<T>
{
	DMatrix::from_row_slice(n_rows, n_cols, data)
}

pub fn direct_convolve(image_data: &[f64], n_rows: usize, n_cols: usize, kernel: &[f64], kernel_rows: usize, kernel_cols: usize) -> Vec<f64>
{
	let image = matrix_from_image(image_data, n_rows, n_cols);
	println!("{image}");
	let kernel = matrix_from_image(kernel, kernel_rows, kernel_cols);
	println!("{kernel}");
	let output = convolve(&image, &kernel);
	row_data(&output)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn image_to_matrix()
	{
		let data = [1, 2, 3, 4, 5, 6];
		let m = matrix_from_image(&data, 2, 3);
		let expected = [1, 4, 2, 5, 3, 6];
		assert_eq!(*m.data.as_vec(), expected)
	}

	#[test]
	fn iterator_correlation() 
	{
		let image = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
		let kernel = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
		let value = correlate_region_iterators(image.iter().copied(), kernel.iter().copied());
		assert_eq!(value, 285.0)
	}

	#[test]
	fn iterator_convolution() 
	{
		let image = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
		let kernel = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
		let value = convolve_region_iterators(image.iter().copied(), kernel.iter().copied());
		assert_eq!(value, 165.0)
	}

	#[test]
	fn basic_application_of_convolution() 
	{
		let image = DMatrix::from_row_slice(3, 3, &[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]);
		let kernel = DMatrix::from_row_slice(3, 3, &[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]);
		let value = apply_convolution(&image, &kernel);
		assert_eq!(value, 165.0)
	}

	#[test]
	fn basic_image_convolution() 
	{
		let data = [ 25.0, 100.0, 75.0, 49.0, 130.0,
					 50.0,  80.0,  0.0, 70.0, 100.0,
					  5.0,  10.0, 20.0, 30.0,   0.0,
					 60.0,  50.0, 12.0, 24.0,  32.0,
					 37.0,  53.0, 55.0, 21.0,  90.0,
					140.0,  17.0,  0.0, 23.0, 222.0 ];
		let image = DMatrix::from_row_slice(6, 5, &data);
		// println!("{image}");
		let kernel = DMatrix::from_row_slice(3, 3, &[1.0, 0.0, 1.0, 
												  	 0.0, 1.0, 0.0, 
												  	 0.0, 0.0, 1.0]);
		let convolved_image = convolve(&image, &kernel);
		let expected_data = [ 25.0, 100.0, 100.0, 149.0, 205.0,  49.0, 130.0,
							  50.0, 105.0, 150.0, 225.0, 149.0, 200.0, 100.0,
							   5.0,  60.0, 130.0, 140.0, 165.0, 179.0, 130.0,
							  60.0,  55.0, 132.0, 174.0,  74.0,  94.0, 132.0, 
							  37.0, 113.0, 147.0,  96.0, 189.0,  83.0,  90.0,
							 140.0,  54.0, 253.0, 145.0, 255.0, 137.0, 254.0,
							   0.0, 140.0,  54.0,  53.0,  78.0, 243.0,  90.0, 
							   0.0,   0.0, 140.0,  17.0,   0.0,  23.0, 222.0];
		let expected_image = DMatrix::from_row_slice(8, 7, &expected_data);
		assert_eq!(convolved_image, expected_image)
	}

	#[test]
	fn matrix_to_vec() 
	{
		let image = DMatrix::from_row_slice(3, 3, &[0, 1, 2, 3, 4, 5, 6, 7, 8]);
		let data = row_data(&image);
		let expected_image = [0, 1, 2, 3, 4, 5, 6, 7, 8];
		assert_eq!(*data, expected_image)
	}

	#[test]
	fn col_major_storage()
	{
		let zeros = DMatrix::from_row_slice(3, 3, &[0, 1, 2, 3, 4, 5, 6, 7, 8]);
		assert_eq!(zeros[0], 0);
		assert_eq!(zeros[1], 3);
		assert_eq!(zeros[2], 6);
	}

	#[test]
	fn pad_image_test()
	{
		let image = DMatrix::from_row_slice(2, 3, &[1, 2, 3, 4, 5, 6]);
		let pad = pad_image(&image, 2, 2, 1);
		let expected_data = [1, 1, 1, 1, 1, 1, 1,
							 1, 1, 1, 1, 1, 1, 1,
							 1, 1, 1, 2, 3, 1, 1,
							 1, 1, 4, 5, 6, 1, 1,
							 1, 1, 1, 1, 1, 1, 1,
							 1, 1, 1, 1, 1, 1, 1,];
		let expected = DMatrix::from_row_slice(6, 7, &expected_data);
		assert_eq!(pad, expected)

	}

	#[test]
	fn offset_test()
	{
		assert_eq!(offset(3), 1);
		assert_eq!(offset(5), 2);
		assert_eq!(offset(11), 5);
	}
}