extern crate smlm_hawk_core as hawk_core;

mod error;
mod image_store;
mod pu;
mod ui;

use self::error::{Error};
use self::image_store::{FfiImageStore, ParameterPtr as ISParamPtr, SizeCallback, GetImageCallback};
use self::ui::{UI as FfiUI, ParameterPtr as UIDataPtr, Callback as ShowMessageCallback};
use self::pu::{PU as FfiPU, ParameterPtr as PUDataPtr, UpdateProgressCallback, FinishCallback};

use hawk_core::config::{Config, Threading, Memory, RunStyle, AlgorithmConfig, NegativeHandling, OutputStyle, Validation};
use hawk_core::num_traits::identities::{Zero};
use hawk_core::num_traits::sign::{Signed};

use std::ops::{Sub, AddAssign};
use std::ffi::{CString};

const UNKNOWN_ORDER: u8 = 0;
const C_ORDER: u8 = 1;
const F_ORDER: u8 = 2;

type FFIConfig = Config;

fn create_c_string(message: &str) -> *mut i8
{
    match CString::new(message)
    {
        Err(_e) => std::ptr::null_mut(),
        Ok(s) => s.into_raw()
    }
}

fn free_c_string(string_ptr: *mut i8) -> ()
{
    if string_ptr != std::ptr::null_mut()
    {
        unsafe{drop(CString::from_raw(string_ptr));}
    }
}

fn drop_ptr<T>(ptr: *mut T) -> ()
{
    unsafe
    {
        let box_ptr = Box::from_raw(ptr);
        drop(box_ptr)
    }
}

#[no_mangle]
pub extern "system" fn ui_new(data_ptr: UIDataPtr, callback: ShowMessageCallback) -> *mut FfiUI
{
    Box::into_raw(Box::new(FfiUI::new(data_ptr, callback)))
}

#[no_mangle]
pub extern "system" fn ui_free(ui_ptr: *mut FfiUI) -> ()
{
    drop_ptr(ui_ptr)
}

#[no_mangle]
pub extern "system" fn progress_updater_new(data_ptr: PUDataPtr, update_progress: UpdateProgressCallback, finish: FinishCallback) -> *mut FfiPU
{
    Box::into_raw(Box::new(FfiPU::new(data_ptr, update_progress, finish)))
}

#[no_mangle]
pub extern "system" fn progress_updater_free(pu_ptr: *mut FfiPU) -> ()
{
    drop_ptr(pu_ptr)
}

#[no_mangle]
pub extern "system" fn error_code_none() -> i32
{
    Error::none().code()
}

#[no_mangle]
pub extern "system" fn error_message_from_code(code: i32) -> *mut i8
{
    let code = Error::from(code);
    create_c_string(&code.to_message())
}

#[no_mangle]
pub extern "system" fn error_message_free(error_message_ptr: *mut i8) -> ()
{
    free_c_string(error_message_ptr)
}

#[no_mangle]
pub extern "system" fn memory_order_unknown() -> u8
{
    UNKNOWN_ORDER
}

#[no_mangle]
pub extern "system" fn memory_order_c() -> u8
{
    C_ORDER
}

#[no_mangle]
pub extern "system" fn memory_order_f() -> u8
{
    F_ORDER
}

#[no_mangle]
pub extern "system" fn output_style_sequential() -> u8
{
    u8::from(OutputStyle::Sequential)
}

#[no_mangle]
pub extern "system" fn output_style_interleaved() -> u8
{
    u8::from(OutputStyle::Interleaved)
}

#[no_mangle]
pub extern "system" fn negative_handling_absolute() -> u8
{
    u8::from(NegativeHandling::Absolute)
}

#[no_mangle]
pub extern "system" fn negative_handling_separate() -> u8
{
    u8::from(NegativeHandling::Separate)
}

#[no_mangle]
pub extern "system" fn config_new(n_levels: u8, negative_handling: u8, output_style: u8) -> *mut FFIConfig
{
    let output_style = OutputStyle::try_from(output_style).unwrap_or_default();
    let negative_handling = NegativeHandling::try_from(negative_handling).unwrap_or_default();
    let algorithm_config = AlgorithmConfig::default().with_n_levels(n_levels as u64)
                                           .with_negative_handling(negative_handling)
                                           .with_output_style(output_style);
    let validation = Validation::default();
    let config = FFIConfig::new(Threading::default(), 
                                Memory::Contiguous, 
                                RunStyle::default(),
                                algorithm_config,
                                Some(validation));
    Box::into_raw(Box::new(config))
}

#[no_mangle]
pub extern "system" fn config_valid(input_data_size: u64, config_ptr: *mut FFIConfig) -> *mut i8
{
    unsafe
    {
        let config: &mut Config = &mut *config_ptr;
        match hawk_core::validate(input_data_size, config)
        {
            Ok(_) => std::ptr::null_mut(),
            Err(e) => create_c_string(&e.to_string())
        }
    }
}

#[no_mangle]
pub extern "system" fn config_validation_free(validation_message: *mut i8) -> ()
{
    free_c_string(validation_message)
}


#[no_mangle]
pub extern "system" fn config_cancel(config_ptr: *mut FFIConfig) -> ()
{
    unsafe
    {
        let config: &mut Config = &mut *config_ptr;
        config.cancel()
    }
}

#[no_mangle]
pub extern "system" fn config_free(config_ptr: *mut FFIConfig) -> ()
{
    drop_ptr(config_ptr)
}

#[no_mangle]
pub extern "system" fn metadata_get(config_ptr: *mut FFIConfig) -> *mut i8
{
    unsafe
    {
        let config: &FFIConfig = &*config_ptr;
        let metadata = hawk_core::get_metadata(config);
        create_c_string(metadata.as_str())
    }
}

#[no_mangle]
pub extern "system" fn metadata_free(metadata_ptr: *mut i8) -> ()
{
    free_c_string(metadata_ptr)
}

#[no_mangle]
pub extern "system" fn output_size(config_ptr: *const FFIConfig, n_frames: u64) -> u64
{
    let config: &FFIConfig = unsafe{&*config_ptr};
    hawk_core::utils::output_size(n_frames, config.as_ref())
}

#[no_mangle]
pub extern "system" fn hawk_this(input: *const f32, input_size: usize, output: *mut f32, output_size: usize, config_ptr: *const FFIConfig)
{
    unsafe
    {        
        let config: &FFIConfig = &*config_ptr;
        let input = std::slice::from_raw_parts(input, input_size);
        let mut output = std::slice::from_raw_parts_mut(output, output_size);
        hawk_core::hawk_timeseries::<f32>(&input, &mut output, config.as_ref());
    }    
}

fn hawk_image_<T>(input: *const T, input_size: usize, input_shape: *const usize, output: *mut T, output_size: usize, config_ptr: *const FFIConfig, time_dimension: usize, memory_order: u8, ui_ptr: *mut FfiUI, pu_ptr: *mut FfiPU) -> i32
where T: Sub<Output=T> + Zero + AddAssign + Signed + Copy + PartialOrd + Send + Sync + std::fmt::Display + std::fmt::Debug,
{
    unsafe
    { 
        let _ui : &mut FfiUI = &mut *ui_ptr;
        let pu : &mut FfiPU = &mut *pu_ptr;        
        let config: &FFIConfig = &*config_ptr;
        let input = std::slice::from_raw_parts(input, input_size);
        let shape_array = std::slice::from_raw_parts(input_shape, 3);
        let output = std::slice::from_raw_parts_mut(output, output_size);
        match hawk_core::striding::hawk_image(input, shape_array, output, config, time_dimension, memory_order, pu)
        {
            Ok(()) => Error::none().code(),
            Err(e) => Error::from(e).code()
        }
    }
}

#[macro_export]
macro_rules! hawk_image
{
    ($i:ident, $t:ty) => {
        #[no_mangle]
        pub extern "system" fn $i(input: *const $t, input_size: usize, input_shape: *const usize, output: *mut $t, output_size: usize, config_ptr: *const FFIConfig, time_dimension: usize, memory_order: u8, ui_ptr: *mut FfiUI, pu_ptr: *mut FfiPU) -> i32
        where $t: Sub<Output=$t> + Zero + AddAssign + Signed + Copy + PartialOrd + Send + Sync + std::fmt::Display + std::fmt::Debug,
        {
            hawk_image_(input, input_size, input_shape, output, output_size, config_ptr, time_dimension, memory_order, ui_ptr, pu_ptr)
        }
    };
}

hawk_image!(hawk_image_f32, f32);
hawk_image!(hawk_image_f64, f64);
hawk_image!(hawk_image_i64, i64);
hawk_image!(hawk_image_i32, i32);
hawk_image!(hawk_image_i16, i16);
hawk_image!(hawk_image_i8, i8);

#[no_mangle]
pub extern "system" fn image_store_new(parameter_ptr: ISParamPtr, size_callback: SizeCallback, get_image_callback: GetImageCallback, image_size: usize) -> *mut FfiImageStore
{
    Box::into_raw(Box::new(FfiImageStore::new(parameter_ptr, size_callback, get_image_callback, image_size)))
}


#[no_mangle]
pub extern "system" fn image_store_free(is_ptr: *mut FfiImageStore) -> ()
{
    drop_ptr(is_ptr)
}

fn hawk_stream_get_image<T>(is_ptr: *const FfiImageStore , config_ptr: *const FFIConfig, output: *mut T, n_pixels: usize, frame_index: usize) -> i32
where  T: Zero + Sub + AddAssign<<T as Sub>::Output> + Signed + PartialOrd + Copy + std::fmt::Debug + std::fmt::Display,
{
    unsafe
    { 
        let image_store : &FfiImageStore = &*is_ptr;
        let config: &FFIConfig = &*config_ptr;
        let frame = hawk_core::pstreams::get_hawk_stream_value(&image_store, frame_index, n_pixels, config.algorithm_config());
        let output = std::slice::from_raw_parts_mut(output, n_pixels);
        output.copy_from_slice(&frame);
        0
    }
}

#[macro_export]
macro_rules! is_get_image 
{
    ($i:ident, $t:ty) => {
        #[no_mangle]
        pub extern "system" fn $i(is_ptr: *const FfiImageStore , config_ptr: *const FFIConfig, output: *mut $t, n_pixels: usize, frame_index: usize) -> i32
        {
            hawk_stream_get_image(is_ptr, config_ptr, output, n_pixels, frame_index)
        }
    };
}

is_get_image!(hawk_stream_get_image_f64, f64);
is_get_image!(hawk_stream_get_image_f32, f32);
is_get_image!(hawk_stream_get_image_i64, i64);
is_get_image!(hawk_stream_get_image_i32, i32);
is_get_image!(hawk_stream_get_image_i16, i16);
is_get_image!(hawk_stream_get_image_i8, i8);


#[cfg(test)]
mod tests 
{
    use super::*;

    use hawk_core::{Error as HawkError};


    #[test]
    fn error_code_none() 
    {
        assert_eq!(Error::none().code(), 0);
    }

    #[test]
    fn error_code_striding() 
    {
        assert_eq!(Error::from(HawkError::unknown_memory_order()).code(), 1);
    }

    #[test]
    fn error_from_code_none() 
    {
        assert_eq!(Error::from(0), Error::none());
    }

    #[test]
    fn error_from_code_striding()
    {
        assert_eq!(Error::from(1), Error::Striding(HawkError::unknown_memory_order()));
    }

    #[test]
    fn drop_null_string_pointer()
    {
        let ptr = std::ptr::null_mut();
        free_c_string(ptr);
        assert_eq!(true, true)
    }
}