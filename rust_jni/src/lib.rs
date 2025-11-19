extern crate smlm_hawk_core as hawk_core;
extern crate smlm_tiff as tiff_wrap;
extern crate jni;

use hawk_core::config::{Config, AlgorithmConfig, Threading, Memory, RunStyle, NegativeHandling, OutputStyle, Validation};
use hawk_core::pstreams::{ImageStore};

use tiff_wrap::writer::{StandardTiffWriter, BigTiffWriter};

use jni::{JNIEnv};
use jni::objects::{JClass, JObject, JFloatArray, ReleaseMode, JString};
use jni::sys::{jboolean, jfloatArray, jint, jlong, jshort, jstring, JNI_FALSE, JNI_TRUE};

use std::cell::RefCell;
use std::fs::File;

type JavaConfig = Config;

fn empty_jstring() -> JString<'static>
{
    unsafe{JString::from_raw(std::ptr::null_mut())}
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_output_1style_1sequential<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) -> jshort
{
    u8::from(OutputStyle::Sequential) as i16
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_output_1style_1interleaved<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) -> jshort
{
    u8::from(OutputStyle::Interleaved) as i16
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_negative_1handling_1absolute<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) -> jshort
{
    u8::from(NegativeHandling::Absolute) as i16
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_negative_1handling_1separate<'local>(_env: JNIEnv<'local>, _class: JClass<'local>) -> jshort
{
    u8::from(NegativeHandling::Separate) as i16
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_config_1new<'local>(mut _env: JNIEnv<'local>, _class: JClass<'local>, n_levels: jlong, negative_handling: jshort, output_style: jshort) -> jlong
{
    let output_style = u8::try_from(output_style).map_err(|_| 0).and_then(OutputStyle::try_from).unwrap_or_default();
    let negative_handling = u8::try_from(negative_handling).map_err(|_| 0).and_then(NegativeHandling::try_from).unwrap_or_default();
    let algorithm_config = AlgorithmConfig::default().with_n_levels(n_levels as u64)
                                           .with_negative_handling(negative_handling)
                                           .with_output_style(output_style);
    let validation = Validation::LimitOutputsToUnder32Bits(true);
    let config = JavaConfig::new(Threading::default(), Memory::Contiguous, RunStyle::default(), algorithm_config, Some(validation));
    let ptr = Box::into_raw(Box::new(config));
    ptr as i64
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_config_1validate<'local>(env: JNIEnv<'local>, _class: JClass<'local>, config_ptr_address: jlong, n_data: jlong) -> jstring
{
    let config_ptr = config_ptr_address as *mut JavaConfig;
    unsafe
    {
        let config : &JavaConfig = &*config_ptr;
        //upcasting is fine
        match hawk_core::validate(n_data as u64, config)
        {
            Ok(_) => empty_jstring().into_raw(),
            Err(e) => 
            {
                let output = env.new_string(e.to_string()).unwrap_or_else(|_e| empty_jstring());
                output.into_raw()
            }
        }
    }

}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_config_1free<'local>(mut _env: JNIEnv<'local>, _class: JClass<'local>, config_ptr_address: jlong) -> ()
{
    let config_ptr = config_ptr_address as *mut JavaConfig;
    unsafe
    {
        let box_ptr = Box::from_raw(config_ptr);
        drop(box_ptr)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_get_1metadata<'local>(env: JNIEnv<'local>, _class: JClass<'local>, config_ptr_address: jlong) -> jstring
{
    unsafe
    {
        let config_ptr = config_ptr_address as *const JavaConfig;
        let config : &JavaConfig = &*config_ptr;
        let metadata = hawk_core::get_metadata(config);
        let output = env.new_string(metadata).unwrap_or_else(|_e| empty_jstring());
        output.into_raw()
    }
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_output_1size<'local>(mut _env: JNIEnv<'local>, _class: JClass<'local>, config_ptr_address: jlong, n_frames: jint) -> jlong
{
    let config_ptr = config_ptr_address as *mut JavaConfig;
    let config: &JavaConfig = unsafe{&*config_ptr};
    // java has no type for unsigned long, long just supports unsigned-ness hence the cast.
    hawk_core::utils::output_size(n_frames as u64, config.as_ref()) as i64
}

struct ImageJImageStore<'local>
{
    env: RefCell<JNIEnv<'local>>,
    stack_wrapper: JObject<'local>
}

impl<'local> ImageJImageStore<'local>
{
    pub fn new(env: JNIEnv<'local>, stack_wrapper: JObject<'local>) -> Self
    {
        Self{env : RefCell::new(env), stack_wrapper}
    }

    pub fn take(self) -> JNIEnv<'local>
    {
        self.env.into_inner()
    }

    fn get_stack_size(&self) -> Result<usize, ()>
    {
        let signature = "()I";
        let value = self.env.borrow_mut()
                             .call_method(&self.stack_wrapper, "get_stack_size", signature, &[])
                             .and_then(|v| v.i())
                             .map_err(|_| ())?;
        match value
        {
            v if v < 0 => Err(()),
            v @ _ => Ok(v as usize)
        }
    }

    fn get_float_image(&self, frame: usize) -> Result<Vec<f32>, ()>
    {
        let signature = "(I)[F";
        let object = self.env.borrow_mut()
                             .call_method(&self.stack_wrapper, "get_float_frame", signature, &[(frame as jint).into()])
                             .and_then(|v| v.l())
                             .map_err(|_| ())?;
        let f_array = JFloatArray::from(object);
        unsafe
        {
            let elements = self.env.borrow_mut().get_array_elements(&f_array, ReleaseMode::NoCopyBack).map_err(|_| ())?;
            Ok(elements.iter().map(|v| *v).collect())
        }
    }
}

impl<'local> ImageStore<f32> for ImageJImageStore<'local>
{
    fn size(&self) -> usize 
    { 
        self.get_stack_size().unwrap_or_default()
    }
    
    fn get_image(&self, start: usize) -> Vec<f32> 
    {
        self.get_float_image(start).unwrap_or_default()
    }
}

fn create_image_array<'local>(env: &mut JNIEnv<'local>, frame_data: &Vec<f32>) -> Result<JFloatArray<'local>, ()>
{
    let output_size = frame_data.len().try_into().map_err(|_| ())?;
    let output_array = env.new_float_array(output_size).map_err(|_| ())?;
    unsafe
    {
        let mut output_elements = env.get_array_elements(&output_array, ReleaseMode::CopyBack).map_err(|_|())?;
        for (java_data, data) in output_elements.iter_mut().zip(frame_data)
        {
            *java_data = *data;
        }
    }
    Ok(output_array)
}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_hawk_1stream_1get_1image_1float<'local>(env: JNIEnv<'local>, _class: JClass<'local>, stack_wrapper: JObject, config_ptr_address: jlong, stream_index: jint, n_pixels: jint) -> jfloatArray
{
    let config_ptr = config_ptr_address as *const JavaConfig;
    let config: &JavaConfig = unsafe{&*config_ptr};
    let image_store = ImageJImageStore::new(env, stack_wrapper);
    let frame = hawk_core::pstreams::get_hawk_stream_value(&image_store, stream_index as usize, n_pixels as usize, config.algorithm_config());
    let mut env = image_store.take();
    match create_image_array(&mut env, &frame)
    {
        Ok(a) => **a,
        Err(_e) => std::ptr::null_mut()
    }
}

fn write_to_tiff_stack(filename: &str, image_store: &ImageJImageStore, config: &Config, width: u32, height: u32) -> Result<bool, String>
{
    let n_frames = image_store.get_stack_size().map_err(|_e| format!("Could not get store size"))?;
    let algorithm_config = config.algorithm_config();
    let output_size = hawk_core::utils::output_size(n_frames as u64, algorithm_config);

    let height = height as u32;
    let width = width as u32;
    let writer = File::create(filename).map_err(|e| format!("Could not create file {filename} due to {e}"))?;
    let mut output_tiff = BigTiffWriter::buffered(writer, width, height).map_err(|e| format!("Could not create tiff writer due to {e}"))?;
    
    let mut tiff_image = output_tiff.create_image_for::<f32>().map_err(|e| e.to_string()).unwrap();
    let metadata  = hawk_core::get_metadata(config);
    let _ = tiff_image.write_image_description(&metadata);

    let n_pixels = (height * width).try_into().map_err(|e| format!("Could not cast {height} * {width} into usize due to {e}"))?;
    let image = hawk_core::pstreams::get_hawk_stream_value(image_store, 0, n_pixels, algorithm_config);
    tiff_image.write_data(&image).map_err(|e| e.to_string()).unwrap();

    for timepoint in 1..output_size
    {
        let frame_number = timepoint.try_into().map_err(|e| format!("Could not cast {timepoint} into usize due to {e}"))?;
        let image = hawk_core::pstreams::get_hawk_stream_value(image_store, frame_number, n_pixels, algorithm_config);
        let _ = output_tiff.write_image_for(&image).map_err(|e| format!("Could not write frame {timepoint} due to {e}"))?;
    }
    Ok(true)

}

#[no_mangle]
pub extern "system" fn Java_com_coxphysics_terrapins_models_hawk_NativeHAWK_hawk_1to_1file<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, stack_wrapper: JObject, config_ptr_address: jlong, height: jint, width: jint, filename: JString<'local>) -> jboolean
{
    let config_ptr = config_ptr_address as *const JavaConfig;
    let config: &JavaConfig = unsafe{&*config_ptr};
    let filename : String = env.get_string(&filename).expect("Couldn't get java string!").into();
    let image_store = ImageJImageStore::new(env, stack_wrapper);
    match write_to_tiff_stack(&filename, &image_store, config, width as u32, height as u32)
    {
        Ok(result) => if result{JNI_TRUE} else{JNI_FALSE},
        Err(_e) => 
        {
            println!("{_e}");
            JNI_FALSE
        }
    }
}
