fn sample_from_sorted<T: Clone, I: Iterator<Item=usize>, J: Iterator<Item=T>>(mut samples: I, data: &mut J, sampled_data: &mut Vec<T>) -> ()
{
	let mut current = 0;
	match samples.next()
	{
		None => return (),
		Some(idx) => 
		{
			sampled_data.push(data.skip(idx).next().unwrap());
			current = idx;
		}
	}
	for idx in samples
	{
		// println!("idx: {idx}");
		let difference = idx - current;
		if idx == current
		{
			let item = &sampled_data[sampled_data.len() - 1];
			sampled_data.push(item.clone());
		}
		else 
		{
			// println!("difference: {difference}");
			let skip = difference - 1;
			// println!("at: {current} -> skip: {skip}");
			sampled_data.push(data.skip(skip).next().unwrap());
			current = idx;
		}
	}
}

pub fn parameterised_sorting_sample<T: Clone, I : Iterator<Item=T>>(sample_size: usize, upper_bound: usize, mut data: I) -> Vec<T>
{
	let mut samples = sig_proc::stats::uniform_sample(sample_size, upper_bound);
	samples.sort();
	let mut sampled_data = Vec::with_capacity(sample_size);
	sample_from_sorted(samples.into_iter(), &mut data, &mut sampled_data);
	sampled_data
}

pub fn sorting_sample<T: Clone, I : ExactSizeIterator<Item=T>>(sample_size: usize, data: I) -> Vec<T>
{
	parameterised_sorting_sample(sample_size, data.len(), data)
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn sample_empty() 
	{
		let data = [3, 4, 5, 6, 7, 8, 9];
		let samples = [];
		let mut sampled_data = Vec::new();
		sample_from_sorted(samples.into_iter(), &mut data.into_iter(), &mut sampled_data);
		assert_eq!(sampled_data, Vec::new());
	}

	#[test]
	fn sample_data() 
	{
		let data = [3, 4, 5, 6, 7, 8, 9];
		let samples = [1, 2, 3];
		let mut sampled_data = Vec::new();
		sample_from_sorted(samples.into_iter(), &mut data.into_iter(), &mut sampled_data);
		assert_eq!(sampled_data, vec![4, 5, 6]);
	}

	#[test]
	fn duplicate_indexes() 
	{
		let data = [3, 4, 5, 6, 7, 8, 9];
		let samples = [1, 1, 1];
		let mut sampled_data = Vec::new();
		sample_from_sorted(samples.into_iter(), &mut data.into_iter(), &mut sampled_data);
		assert_eq!(sampled_data, vec![4, 4, 4]);
	}
}
