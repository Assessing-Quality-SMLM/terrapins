use crate::stats;

use num_traits::FromPrimitive;

use std::ops::{Add, Mul};

fn sqrt_n_bins(n: usize) -> usize
{
	(n as f64).sqrt().ceil() as usize
}

fn sturges_n_bins(n: usize) -> usize
{
	let value = 1.0 + (n as f64).log2();
	value.ceil() as usize
}

fn scotts_bin_width(data: &[f64]) -> f64
{
	let n = data.len() as f64;
	let sigma = stats::standard_deviation(data.iter().copied());
	let n_cubed_root = n.powf(1.0 / 3.0);
	let value = (3.5 * sigma) / n_cubed_root;
	value
}

fn ts_bin_width(n: usize) -> f64
{
	(4.0 / (n as f64)).powf(1.0 / 3.0)
}

fn ts_n_bins(n: usize) -> usize
{
	(2.0 * (n as f64)).powf(1.0 / 3.0).ceil() as usize
}

fn rices_n_bins(n: usize) -> usize
{
	let n = n as f64;
	let n_cubed_root = n.powf(1.0 / 3.0);
	(2.0 * n_cubed_root).ceil() as usize
}

fn bin_width(n_bins: usize, ordered_data: &[f64]) -> f64
{
	let min = ordered_data[0];
	let max = ordered_data[ordered_data.len() - 1];	
	let range = max - min;
	let width = range / n_bins as f64;
	width
}

#[derive(Debug)]
pub struct Bin
{
	start_idx: usize,
	end_idx: usize,
}

impl Bin
{

	pub fn new(start_idx: usize, end_idx: usize) -> Self
	{
		Self{start_idx, end_idx}
	}

	pub fn start(&self) -> usize
	{
		self.start_idx
	}

	pub fn end(&self) -> usize
	{
		self.end_idx
	}

	fn valid_index_range(&self) -> usize
	{
		self.end() - self.start()
	}

	pub fn count(&self) -> usize
	{
		if self.valid(){self.valid_index_range() + 1} else{0}
	}

	pub fn valid(&self) -> bool
	{
		self.end() >= self.start()
	}

	pub fn to_tuple(&self) -> (usize, usize)
	{
		(self.start(), self.end())
	}
}

fn from_ends<T: std::cmp::PartialOrd, I: Iterator<Item=T>>(ordered_data: &[T], bin_ends: I) -> impl Iterator<Item=Bin> + use<'_, T, I>
{
	bin_ends.scan(0, |data_idx, end_value|
	{
		let bin_start_idx = *data_idx;
		while *data_idx < ordered_data.len() && ordered_data[*data_idx] < end_value
		{
			// println!("{} at {}", ordered_data[data_idx], data_idx);
			*data_idx += 1;
		}
		let bin_end_idx = *data_idx - 1;
		// println!("bin_end_idx: {bin_end_idx}");
		Some(Bin::new(bin_start_idx, bin_end_idx))
	})
}

fn generate_fixed_width_ends<T: FromPrimitive + Mul<Output = T> + Copy + Add<Output = T>>(n_bins: usize, width: T, offset: T) -> impl Iterator<Item=T> 
{
	(0..n_bins).map(move |bin|
	{
		let bin_t = T::from_usize(bin).unwrap();
		let bin_start : T = (bin_t * width) + offset;
		let bin_end : T = bin_start + width;
		bin_end
	})
}

fn generate_bins<T: std::cmp::PartialOrd + FromPrimitive + Mul<Output=T> + Add<Output=T> + Copy>(ordered_data: &[T], n_bins: usize, width: T) -> Vec<Bin> 
{
	let min = ordered_data[0];
	let bin_ends = generate_fixed_width_ends(n_bins, width, min);
	from_ends(ordered_data, bin_ends).collect()
}

fn n_bins_from_width(ordered_data: &[f64], bin_width: f64) -> usize
{
	let min = ordered_data[0];
	let max = ordered_data[ordered_data.len() - 1];
	let range = (max - min) as f64;
	(range / bin_width).ceil() as usize
}

pub trait BinCalculator
{
	fn n_bins(ordered_data: &[f64]) -> (usize, f64);
}

struct Sqrt;
impl BinCalculator for Sqrt
{
	fn n_bins(ordered_data: &[f64]) -> (usize, f64)
	{
		let n_bins = sqrt_n_bins(ordered_data.len());
		let width = bin_width(n_bins, ordered_data);
		(n_bins, width)
	}
}

struct Scott;
impl BinCalculator for Scott
{
	fn n_bins(ordered_data: &[f64]) -> (usize, f64)
	{
		let width = scotts_bin_width(ordered_data);
		let n_bins = n_bins_from_width(ordered_data, width);
		(n_bins, width)
	}
}

struct Sturges;
impl BinCalculator for Sturges
{
	fn n_bins(ordered_data: &[f64]) -> (usize, f64)
	{
		let n_bins = sturges_n_bins(ordered_data.len());
		let width = bin_width(n_bins, ordered_data);
		(n_bins, width)
	}
}

struct Rice;
impl BinCalculator for Rice
{
	fn n_bins(ordered_data: &[f64]) -> (usize, f64)
	{
		let n_bins = rices_n_bins(ordered_data.len());
		let width = bin_width(n_bins, ordered_data);
		(n_bins, width)
	}
}

struct TerrelScott;
impl BinCalculator for TerrelScott
{
	fn n_bins(ordered_data: &[f64]) -> (usize, f64)
	{
		let width = ts_bin_width(ordered_data.len());
		let n_bins = ts_n_bins(ordered_data.len());
		(n_bins, width)
	}
}

fn generate_bins_with<C: BinCalculator>(data: &[f64]) -> (Vec<Bin>, f64)
{
	let (n_bins, width) = C::n_bins(data);
	// println!("{n_bins}");
	// println!("{width}");
	(generate_bins(data, n_bins, width), width)
}


pub enum Method 
{
	Sqrt,
	Sturges,
	Rice,
	Scott,
	TerrelScott,    
}

impl Method
{
	pub fn calculate(&self, ordered_data: &[f64]) -> (Vec<Bin>, f64)
	{
		match self
		{
			Self::Sqrt => generate_bins_with::<Sqrt>(ordered_data),
			Self::Sturges => generate_bins_with::<Sturges>(ordered_data),
			Self::Rice => generate_bins_with::<Rice>(ordered_data),
			Self::Scott => generate_bins_with::<Scott>(ordered_data),
			Self::TerrelScott => generate_bins_with::<TerrelScott>(ordered_data),
		}
	}
}

#[derive(Debug)]
// pub struct Histogram<'a> 
pub struct Histogram
{
	//data: &'a [f64],
	bins: Vec<Bin>,
	width: f64,
}

// impl<'a> Histogram<'a>
impl Histogram
{	
	pub fn generate_from_ordered_with<C: BinCalculator>(data: &[f64]) -> Result<Self, String>
	{
		let (bins, width) = generate_bins_with::<C>(data);
		Ok(Self{bins, width})
	}

	pub fn generate_from_ordered(data: &[f64]) -> Result<Self, String>
	{
		Self::generate_from_ordered_with::<Scott>(data)
	}

	pub fn generate_from_ordered_using(data: &[f64], method: Method) -> Result<Self, String>
	{
		let (bins, width) = method.calculate(data);
		Ok(Self{bins, width})
	}

	pub fn bins(&self) -> &Vec<Bin>
	{
		&self.bins
	}

	// pub fn binned_data(&self) -> Vec<&[f64]>
	// {
	// 	self.bins.iter().map(|bin|
	// 	{
	// 		if bin.valid()
	// 		{
	// 			&self.data[bin.start..bin.end]
	// 		}
	// 		else
	// 		{
	// 			&[]
	// 		}
	// 	}).collect()
	// }

	pub fn counts(&self) -> Vec<usize>
	{
		self.bins().iter().map(Bin::count).collect()
	}

	pub fn counts_64(&self) -> Vec<f64>
	{
		self.bins().iter().map(Bin::count).map(|c| c as f64).collect()
	}

	///mean and std
	pub fn stats(&self) -> (f64, f64)
	{
		let data = self.counts_64();
		let iter = data.iter().copied();
		let mean = stats::mean(iter.clone());
		let var = stats::variance_unbiased(iter, mean);
		let std = stats::std(var);
		(mean, std)
	}

	///Theoretical centres based on width
	///rather than actual centre of data binned within
	pub fn centres(&self) -> Vec<f64>
	{
		let half_width = self.width / 2.0;
		(0..self.bins.len()).map(|bin| 
		{
			let start = (bin as f64) * self.width;
			start + half_width
		}).collect()
	}

	pub fn dataset(&self) -> (Vec<f64>, Vec<f64>)
	{
		let centres = self.centres();
		let mut x = Vec::with_capacity(centres.len());
		let mut y = Vec::with_capacity(centres.len());
		for (idx, bin) in self.bins().into_iter().enumerate()
		{
			let x_value = centres[idx];
			let y_value = bin.count() as f64;
			x.push(x_value);
			y.push(y_value);
		}
		(x, y)
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn sturges() 
	{
		assert_eq!(sturges_n_bins(10), 5)
	}

	#[test]
	fn basic_generate_bins()
	{
		let data = (0..100).map(|x| x as f64).collect::<Vec<f64>>();
		let bins = generate_bins(&data, 10, 10.0).iter().map(Bin::to_tuple).collect::<Vec<(usize, usize)>>();
		let expected = vec![(0, 9), (10, 19), (20, 29), (30, 39), (40, 49), (50, 59), (60, 69), (70, 79), (80, 89), (90, 99)];
		assert_eq!(bins, expected)
	}

	#[test]
	fn generate_bins_with_empty_start()
	{
		let data = [1.0, 1.1, 2.0, 3.0, 4.0, 5.0, 6.0, 9.0, 9.1, 9.2];
		let bins = generate_bins(&data, 10, 1.0);
		let hist = Histogram{bins, width:10.0};
		assert_eq!(hist.counts(), [2, 1, 1, 1, 1, 1, 0, 0, 3, 0])
	}

	#[test]
	fn generate_bins_floating_point()
	{
		let data = [10.0, 10.1, 10.2, 10.3, 10.5, 10.6, 10.7, 10.8, 10.9, 21.3];
		let bins = generate_bins(&data, 10, 1.13).iter().map(Bin::to_tuple).collect::<Vec<(usize, usize)>>();
		let expected = vec![(0, 8), (9, 8), (9, 8), (9, 8), (9, 8), (9, 8), (9, 8), (9, 8), (9, 8), (9, 8)];
		assert_eq!(bins, expected)
	}

	#[test]
	fn floating_point_centres()
	{
		let data = [10.0, 10.1, 10.2, 10.3, 10.5, 10.6, 10.7, 10.8, 10.9, 21.3];
		let hist = Histogram::generate_from_ordered(&data).unwrap();
		let expected = vec![2.652855827582381, 7.958567482747143, 13.264279137911906];
		assert_eq!(hist.centres(), expected)
	}

	#[test]
	fn basic() 
	{
		let rng = crate::stats::seeded_rng(1234);
		let mut data = crate::stats::Gaussian::new(0.0, 1.0).sample_box_muller_with(100, rng);
		data.sort_by(|a, b| a.partial_cmp(b).unwrap());
		let hist = Histogram::generate_from_ordered(&data).unwrap();
		let expected = vec![(0, 0), (1, 1), (2, 5), (6, 13), (14, 47), (48, 81), (82, 92), (93, 96), (97, 99)];
		assert_eq!(hist.bins().iter().map(Bin::to_tuple).collect::<Vec<(usize, usize)>>(), expected)
	}

	#[test]
	fn centres() 
	{
		let bins = (0..10).map(|bin| Bin::new(bin * 10, bin * 10 + 10)).collect();
		let hist = Histogram{bins: bins, width: 10.0 };
		assert_eq!(hist.centres(), [5.0, 15.0, 25.0, 35.0, 45.0, 55.0, 65.0, 75.0, 85.0, 95.0]);
	}

	#[test]
	fn dataset() 
	{
		let bins = (0..10).map(|bin| Bin::new(bin * 10, bin * 10 + 9)).collect();
		let hist = Histogram{bins: bins, width: 10.0 };
		let (x, y) = hist.dataset();
		assert_eq!(x, [5.0, 15.0, 25.0, 35.0, 45.0, 55.0, 65.0, 75.0, 85.0, 95.0]);
		assert_eq!(y, [10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0]);
	}

	#[test]
	fn counts() 
	{
		let data = [0.0, 0.1, 0.2, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
		let bins = generate_bins(&data, 10, 1.0);
		let hist = Histogram{bins, width:10.0};
		assert_eq!(hist.counts(), [3, 0, 0, 1, 1, 1, 1, 1, 1, 1])
	}

	#[test]
	fn basic_counts() 
	{
		let bins = (0..10).map(|bin| Bin::new(bin * 10, bin * 10 + 9)).collect();
		let hist = Histogram{bins: bins, width: 10.0 };
		assert_eq!(hist.counts(), [10, 10, 10, 10, 10, 10, 10, 10, 10, 10])
	}

	#[test]
	fn zero_counts() 
	{
		let hist = Histogram{bins: vec![Bin::new(0, 2), Bin::new(2, 1), Bin::new(2, 3)], width: 10.0 };
		assert_eq!(hist.counts(), [3, 0, 2])
	}

	#[test]
	fn stats() 
	{
		let hist = Histogram{bins: vec![Bin::new(0, 2), Bin::new(2, 1), Bin::new(2, 3)], width: 10.0 };
		let(mean, sigma) = hist.stats();
		assert_eq!(mean, 1.6666666666666667);
		assert_eq!(sigma, 1.5275252316519465);
	}
}