use hawk_core::ui::{ProgressUpdater};

use std::ffi::{c_void};

pub type ParameterPtr = *const c_void;
pub type UpdateProgressCallback = extern "C" fn (parameter: ParameterPtr, current: u64, total: u64) -> ();
pub type FinishCallback = extern "C" fn (parameter: ParameterPtr) -> ();

pub struct PU
{
	parameter_ptr : ParameterPtr,
	update_progress: UpdateProgressCallback,
	finish: FinishCallback
}

impl PU
{
	pub fn new(parameter_ptr: ParameterPtr, update_progress: UpdateProgressCallback, finish: FinishCallback) -> Self
	{
		Self
		{
			parameter_ptr,
			update_progress,
			finish: finish
		}
	}
}

impl ProgressUpdater for PU
{
	fn update_progress(&mut self, current: u64, total: u64) -> ()
	{
		(self.update_progress)(self.parameter_ptr, current, total)
	}

	fn finish(&mut self) -> ()
	{
		(self.finish)(self.parameter_ptr)
	}
}

unsafe impl Send for PU {}
unsafe impl Sync for PU {}