use hawk_core::ui::{UserInteraction};

use std::ffi::{CString, c_void, c_char};

pub type ParameterPtr = *mut c_void;
pub type Callback = extern "C" fn (parameter: ParameterPtr, message: *const c_char) -> ();

pub struct UI
{
	parameter_ptr : ParameterPtr,
	callback: Callback
}

impl UI
{
	pub fn new(parameter_ptr: ParameterPtr, callback: Callback) -> Self
	{
		Self
		{
			parameter_ptr,
			callback
		}
	}
}

impl UserInteraction for UI
{
	fn show_message(&mut self, message: &str) -> ()
	{
		match CString::new(message)
		{
			Ok(s) => (self.callback)(self.parameter_ptr, s.as_ptr()),
			Err(_) => {}
		}
	}
}
