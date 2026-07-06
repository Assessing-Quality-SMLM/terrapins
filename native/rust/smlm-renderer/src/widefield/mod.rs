use std::ops::AddAssign;

fn merge_data<T: AddAssign + Clone>(total: &mut [T], frame: &[T]) -> ()
{
	for idx in 0..total.len()
	{
		total[idx] += frame[idx].clone();
	}
}


fn merge_frames<T: AddAssign + Clone, R: AsRef<[T]>, I: Iterator<Item=R>>(total: &mut[T], frames: I) -> ()
{
	for frame in frames
	{
		merge_data(total, frame.as_ref())
	}
}

fn mean_merge_frames<R: AsRef<[f64]>, I: Iterator<Item=R>>(total: &mut[f64], frames: I) -> ()
{
	let mut frame_count = 1; // start from 1 as we init with first frame
	for frame in frames
	{
		merge_data(total, frame.as_ref());
		frame_count += 1;
	}

	for idx in 0..total.len()
	{
		total[idx] /= frame_count as f64;
	}
}

pub fn sum<T: AddAssign + Clone, R: AsRef<[T]>, I: Iterator<Item=R>>(mut frames: I) -> Vec<T>
{
	match frames.next()
	{
		None => Vec::new(),
		Some(frame) => 
		{
			let mut total = frame.as_ref().to_vec();
			merge_frames(&mut total, frames);
			total
		}
	}
}

pub fn mean<R: AsRef<[f64]>, I: Iterator<Item=R>>(mut frames: I) -> Vec<f64>
{
	match frames.next()
	{
		None => Vec::new(),
		Some(frame) => 
		{
			let mut total = frame.as_ref().to_vec();
			mean_merge_frames(&mut total, frames);
			total
		}
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn empty_iterator() 
	{		
		assert_eq!(sum(std::iter::empty::<&[i32]>()), Vec::new());
	}

	#[test]
	fn single_frame() 
	{
		let time_series = vec![[1, 2]];
		let frames = time_series.iter();
		assert_eq!(sum(frames), vec![1, 2]);
	}

	#[test]
	fn basic_test() 
	{
		let time_series = vec![[1, 2], [3, 4], [5, 6]];
		let frames = time_series.iter().map(|a| a.as_slice());
		assert_eq!(sum(frames), vec![9, 12]);
	}

	#[test]
	fn mean_test() 
	{
		let time_series = vec![[1.0, 2.0], [3.0, 4.0], [5.0, 6.0]];
		let frames = time_series.iter().map(|a| a.as_slice());
		assert_eq!(mean(frames), vec![3.0, 4.0]);
	}
}