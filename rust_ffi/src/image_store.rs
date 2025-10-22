use hawk_core::pstreams::{ImageStore};

use hawk_core::num_traits::{Zero};

use std::ffi::{c_void};

pub type ParameterPtr = *mut c_void;
pub type SizeCallback = extern "C" fn (parameter: ParameterPtr) -> usize;
pub type GetImageCallback = extern "C" fn (parameter: ParameterPtr, frame: usize, data: *mut c_void) -> i8;

pub struct FfiImageStore
{
	parameter_ptr : ParameterPtr,
	size_callback: SizeCallback,
	image_callback: GetImageCallback,
	image_size: usize
}

impl FfiImageStore
{
	pub fn new(parameter_ptr: ParameterPtr, size_callback: SizeCallback, image_callback: GetImageCallback, image_size: usize) -> Self
	{
		Self
		{
			parameter_ptr,
			size_callback,
			image_callback,
			image_size,
		}
	}
}

impl<T: Zero + Clone> ImageStore<T> for FfiImageStore
{	
	fn size(&self) -> usize 
	{
		(self.size_callback)(self.parameter_ptr)
	}

	fn get_image(&self, frame: usize) -> Vec<T>
	{
		// This is very risky - not specifying that we expect c layout or any striding pattern
		// will see how it goes for now but may need to change
		let mut data = vec![T::zero(); self.image_size];
		match (self.image_callback)(self.parameter_ptr, frame, data.as_mut_ptr() as *mut c_void)
		{
			0 => data,
			_ => Vec::new()
		}
	}
}
