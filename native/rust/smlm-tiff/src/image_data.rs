use crate::{DataType};
use tiff::decoder::{DecodingResult};

use std::io::{Write, Error as IoError};
use std::iter::{Iterator};

trait ToBeBytes
{
    type ByteArray;
    fn to_be_bytes(&self) -> Self::ByteArray;
}

impl ToBeBytes for u8
{
    type ByteArray = [u8; 1];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        u8::to_be_bytes(*self)
    }
}

impl ToBeBytes for u16
{
    type ByteArray = [u8; 2];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        u16::to_be_bytes(*self)
    }
}

impl ToBeBytes for u32
{
    type ByteArray = [u8; 4];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        u32::to_be_bytes(*self)
    }
}

impl ToBeBytes for u64
{
    type ByteArray = [u8; 8];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        u64::to_be_bytes(*self)
    }
}

impl ToBeBytes for i8
{
    type ByteArray = [u8; 1];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        i8::to_be_bytes(*self)
    }
}

impl ToBeBytes for i16
{
    type ByteArray = [u8; 2];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        i16::to_be_bytes(*self)
    }
}

impl ToBeBytes for i32
{
    type ByteArray = [u8; 4];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        i32::to_be_bytes(*self)
    }
}

impl ToBeBytes for i64
{
    type ByteArray = [u8; 8];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        i64::to_be_bytes(*self)
    }
}

impl ToBeBytes for f32
{
    type ByteArray = [u8; 4];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        f32::to_be_bytes(*self)
    }
}

impl ToBeBytes for f64
{
    type ByteArray = [u8; 8];
    fn to_be_bytes(&self) -> Self::ByteArray
    {
        f64::to_be_bytes(*self)
    }
}

fn transform<T, U>(data: &Vec<T>) -> Vec<U>
where T: Copy,
      U: From<T>
{
    data.into_iter().map(|x| U::from(*x)).collect()
}

fn try_transform<T, U>(data: &Vec<T>) -> Result<Vec<U>, <U as TryFrom<T>>::Error>
where T: Copy,
      U: TryFrom<T>
{
    data.into_iter().map(|v| U::try_from(*v)).collect()
}

fn write<'a, W: Write, S : AsRef<[u8]>, T: ToBeBytes<ByteArray=S>>(mut writer: W, data: &'a Vec<T>) -> Result<(), IoError>
{
    for item in data
    {
        let _ = writer.write(item.to_be_bytes().as_ref())?;
    }
    Ok(())
}

impl TryFrom<&ImageData> for Vec<f64>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_f64()
    }
}

impl TryFrom<&ImageData> for Vec<f32>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_f32()
    }
}

impl TryFrom<&ImageData> for Vec<u64>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_u64()
    }
}

impl TryFrom<&ImageData> for Vec<u32>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_u32()
    }
}

impl TryFrom<&ImageData> for Vec<u16>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_u16()
    }
}

impl TryFrom<&ImageData> for Vec<u8>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_u8()
    }
}

impl TryFrom<&ImageData> for Vec<i64>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_i64()
    }
}

impl TryFrom<&ImageData> for Vec<i32>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_i32()
    }
}

impl TryFrom<&ImageData> for Vec<i16>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_i16()
    }
}

impl TryFrom<&ImageData> for Vec<i8>
{
    type Error = String;
    fn try_from(data: &ImageData) -> Result<Self, Self::Error>
    { 
        data.to_i8()
    }
}

#[derive(Debug)]
pub struct ImageData 
{
    decoding_result: DecodingResult
}

impl ImageData
{
    pub fn new(decoding_result: DecodingResult) ->  Self
    {
        Self{decoding_result}
    }

    pub fn data_type(&self) -> DataType
    {
        match &self.decoding_result
        {
            DecodingResult::U8(_data) => DataType::U8,
            DecodingResult::U16(_data) => DataType::U16,
            DecodingResult::U32(_data) => DataType::U32,
            DecodingResult::U64(_data) => DataType::U64,
            DecodingResult::F32(_data) => DataType::F32,
            DecodingResult::F64(_data) => DataType::F64,
            DecodingResult::I8(_data) => DataType::I8,
            DecodingResult::I16(_data) => DataType::I16,
            DecodingResult::I32(_data) => DataType::I32,
            DecodingResult::I64(_data) => DataType::I64,
        }
    }

    pub fn to_f64(&self) -> Result<Vec<f64>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, f64>(data)),
            DecodingResult::U16(data) => Ok(transform::<u16, f64>(data)),
            DecodingResult::U32(data) => Ok(transform::<u32, f64>(data)),
            DecodingResult::U64(_data) => Err(String::from("Cannot convert U64 to f64")),
            DecodingResult::F32(data) => Ok(transform::<f32, f64>(data)),
            DecodingResult::F64(data) => Ok(data.clone()),
            DecodingResult::I8(data) => Ok(transform::<i8, f64>(data)),
            DecodingResult::I16(data) => Ok(transform::<i16, f64>(data)),
            DecodingResult::I32(data) => Ok(transform::<i32, f64>(data)),
            DecodingResult::I64(_data) => Err(String::from("Cannot convert I64 to f64")),
        }
    }

    pub fn to_f32(&self) -> Result<Vec<f32>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, f32>(data)),
            DecodingResult::U16(data) => Ok(transform::<u16, f32>(data)),
            DecodingResult::U32(_data) => Err(String::from("Cannot convert U32 to f32")),
            DecodingResult::U64(_data) => Err(String::from("Cannot convert U64 to f32")),
            DecodingResult::F32(data) => Ok(data.clone()),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to f32")),
            DecodingResult::I8(data) => Ok(transform::<i8, f32>(data)),
            DecodingResult::I16(data) => Ok(transform::<i16, f32>(data)),
            DecodingResult::I32(_data) => Err(String::from("Cannot convert I32 to f32")),
            DecodingResult::I64(_data) => Err(String::from("Cannot convert I64 to f32"))
        }
    }

    pub fn to_u64(&self) -> Result<Vec<u64>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, u64>(data)),
            DecodingResult::U16(data) => Ok(transform::<u16, u64>(data)),
            DecodingResult::U32(data) => Ok(transform::<u32, u64>(data)),
            DecodingResult::U64(data) => Ok(data.clone()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to u64")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to u64")),
            DecodingResult::I8(data) => try_transform::<i8, u64>(data).map_err(|e| e.to_string()),
            DecodingResult::I16(data) => try_transform::<i16, u64>(data).map_err(|e| e.to_string()),
            DecodingResult::I32(data) => try_transform::<i32, u64>(data).map_err(|e| e.to_string()),
            DecodingResult::I64(data) => try_transform::<i64, u64>(data).map_err(|e| e.to_string()),
        }
    }

    pub fn to_u32(&self) -> Result<Vec<u32>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, u32>(data)),
            DecodingResult::U16(data) => Ok(transform::<u16, u32>(data)),
            DecodingResult::U32(data) => Ok(data.clone()),
            DecodingResult::U64(data) => try_transform::<u64, u32>(data).map_err(|e| e.to_string()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to u32")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to u32")),
            DecodingResult::I8(data) => try_transform::<i8, u32>(data).map_err(|e| e.to_string()),
            DecodingResult::I16(data) => try_transform::<i16, u32>(data).map_err(|e| e.to_string()),
            DecodingResult::I32(data) => try_transform::<i32, u32>(data).map_err(|e| e.to_string()),
            DecodingResult::I64(data) => try_transform::<i64, u32>(data).map_err(|e| e.to_string()),
        }
    }

    pub fn to_u16(&self) -> Result<Vec<u16>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, u16>(data)),
            DecodingResult::U16(data) => Ok(data.clone()),
            DecodingResult::U32(data) => try_transform::<u32, u16>(data).map_err(|e| e.to_string()),
            DecodingResult::U64(data) => try_transform::<u64, u16>(data).map_err(|e| e.to_string()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to u16")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to u16")),
            DecodingResult::I8(data) => try_transform::<i8, u16>(data).map_err(|e| e.to_string()),
            DecodingResult::I16(data) => try_transform::<i16, u16>(data).map_err(|e| e.to_string()),
            DecodingResult::I32(data) => try_transform::<i32, u16>(data).map_err(|e| e.to_string()),
            DecodingResult::I64(data) => try_transform::<i64, u16>(data).map_err(|e| e.to_string())
        }
    }

    pub fn to_u8(&self) -> Result<Vec<u8>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(data.clone()),
            DecodingResult::U16(data) => try_transform::<u16, u8>(data).map_err(|e| e.to_string()),
            DecodingResult::U32(data) => try_transform::<u32, u8>(data).map_err(|e| e.to_string()),
            DecodingResult::U64(data) => try_transform::<u64, u8>(data).map_err(|e| e.to_string()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to u8")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to u8")),
            DecodingResult::I8(data) => try_transform::<i8, u8>(data).map_err(|e| e.to_string()),
            DecodingResult::I16(data) => try_transform::<i16, u8>(data).map_err(|e| e.to_string()),
            DecodingResult::I32(data) => try_transform::<i32, u8>(data).map_err(|e| e.to_string()),
            DecodingResult::I64(data) => try_transform::<i64, u8>(data).map_err(|e| e.to_string()),
        }
    }

    pub fn to_i64(&self) -> Result<Vec<i64>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, i64>(data)),
            DecodingResult::U16(data) => Ok(transform::<u16, i64>(data)),
            DecodingResult::U32(data) => try_transform::<u32, i64>(data).map_err(|e| e.to_string()),
            DecodingResult::U64(data) => try_transform::<u64, i64>(data).map_err(|e| e.to_string()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to i64")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to i64")),
            DecodingResult::I8(data) => Ok(transform::<i8, i64>(data)),
            DecodingResult::I16(data) => Ok(transform::<i16, i64>(data)),
            DecodingResult::I32(data) => Ok(transform::<i32, i64>(data)),
            DecodingResult::I64(data) =>  Ok(data.clone()),
        }
    }

    pub fn to_i32(&self) -> Result<Vec<i32>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, i32>(data)),
            DecodingResult::U16(data) => Ok(transform::<u16, i32>(data)),
            DecodingResult::U32(data) => try_transform::<u32, i32>(data).map_err(|e| e.to_string()),
            DecodingResult::U64(data) => try_transform::<u64, i32>(data).map_err(|e| e.to_string()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to i32")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to i32")),
            DecodingResult::I8(data) => Ok(transform::<i8, i32>(data)),
            DecodingResult::I16(data) => Ok(transform::<i16, i32>(data)),
            DecodingResult::I32(data) => Ok(data.clone()),
            DecodingResult::I64(data) => try_transform::<i64, i32>(data).map_err(|e| e.to_string())
        }
    }

    pub fn to_i16(&self) -> Result<Vec<i16>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => Ok(transform::<u8, i16>(data)),
            DecodingResult::U16(data) => try_transform::<u16, i16>(data).map_err(|e| e.to_string()),
            DecodingResult::U32(data) => try_transform::<u32, i16>(data).map_err(|e| e.to_string()),
            DecodingResult::U64(data) => try_transform::<u64, i16>(data).map_err(|e| e.to_string()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to i16")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to i16")),
            DecodingResult::I8(data) => Ok(transform::<i8, i16>(data)),
            DecodingResult::I16(data) => Ok(data.clone()),
            DecodingResult::I32(data) => try_transform::<i32, i16>(data).map_err(|e| e.to_string()),
            DecodingResult::I64(data) =>  try_transform::<i64, i16>(data).map_err(|e| e.to_string()),
        }
    }

    pub fn to_i8(&self) -> Result<Vec<i8>, String>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => try_transform::<u8, i8>(data).map_err(|e| e.to_string()),
            DecodingResult::U16(data) => try_transform::<u16, i8>(data).map_err(|e| e.to_string()),
            DecodingResult::U32(data) => try_transform::<u32, i8>(data).map_err(|e| e.to_string()),
            DecodingResult::U64(data) => try_transform::<u64, i8>(data).map_err(|e| e.to_string()),
            DecodingResult::F32(_data) => Err(String::from("Cannot convert F32 to i8")),
            DecodingResult::F64(_data) => Err(String::from("Cannot convert F64 to i8")),
            DecodingResult::I8(data) => Ok(data.clone()),
            DecodingResult::I16(data) => try_transform::<i16, i8>(data).map_err(|e| e.to_string()),
            DecodingResult::I32(data) => try_transform::<i32, i8>(data).map_err(|e| e.to_string()),
            DecodingResult::I64(data) =>  try_transform::<i64, i8>(data).map_err(|e| e.to_string()),
        }
    }

    pub fn write_to<W: Write>(&self, writer: W) -> Result<(), IoError>
    {
        match &self.decoding_result
        {
            DecodingResult::U8(data) => write(writer, data),
            DecodingResult::U16(data) => write(writer, data),
            DecodingResult::U32(data) => write(writer, data),
            DecodingResult::U64(data) => write(writer, data),
            DecodingResult::F32(data) => write(writer, data),
            DecodingResult::F64(data) => write(writer, data),
            DecodingResult::I8(data) => write(writer, data),
            DecodingResult::I16(data) => write(writer, data),
            DecodingResult::I32(data) => write(writer, data),
            DecodingResult::I64(data) => write(writer, data)
        }
    }
}

impl From<DecodingResult> for ImageData
{
    fn from(decoding_result: DecodingResult) -> Self
    {
        Self::new(decoding_result)
    }
}

fn data_matches_u8(result: &DecodingResult, i_data: &Vec<u8>) -> bool
{
    match result
    {
        DecodingResult::U8(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_u16(result: &DecodingResult, i_data: &Vec<u16>) -> bool
{
    match result
    {
        DecodingResult::U16(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_u32(result: &DecodingResult, i_data: &Vec<u32>) -> bool
{
    match result
    {
        DecodingResult::U32(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_u64(result: &DecodingResult, i_data: &Vec<u64>) -> bool
{
    match result
    {
        DecodingResult::U64(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_i8(result: &DecodingResult, i_data: &Vec<i8>) -> bool
{
    match result
    {
        DecodingResult::I8(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_i16(result: &DecodingResult, i_data: &Vec<i16>) -> bool
{
    match result
    {
        DecodingResult::I16(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_i32(result: &DecodingResult, i_data: &Vec<i32>) -> bool
{
    match result
    {
        DecodingResult::I32(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_i64(result: &DecodingResult, i_data: &Vec<i64>) -> bool
{
    match result
    {
        DecodingResult::I64(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_f32(result: &DecodingResult, i_data: &Vec<f32>) -> bool
{
    match result
    {
        DecodingResult::F32(data) => data == i_data,
        _                        => false
    }
}

fn data_matches_f64(result: &DecodingResult, i_data: &Vec<f64>) -> bool
{
    match result
    {
        DecodingResult::F64(data) => data == i_data,
        _                        => false
    }
}

fn decoding_results_match(a: &DecodingResult, b: &DecodingResult) -> bool
{
    match a
    {
        DecodingResult::U8(data) => data_matches_u8(b, data),
        DecodingResult::U16(data) => data_matches_u16(b, data),
        DecodingResult::U32(data) => data_matches_u32(b, data),
        DecodingResult::U64(data) => data_matches_u64(b, data),
        DecodingResult::F32(data) => data_matches_f32(b, data),
        DecodingResult::F64(data) => data_matches_f64(b, data),
        DecodingResult::I8(data) => data_matches_i8(b, data),
        DecodingResult::I16(data) => data_matches_i16(b, data),
        DecodingResult::I32(data) => data_matches_i32(b, data),
        DecodingResult::I64(data) => data_matches_i64(b, data),
    }
}

impl PartialEq for ImageData
{
    fn eq(&self, image_data: &ImageData) -> bool 
    {
        decoding_results_match(&self.decoding_result, &image_data.decoding_result)
    }
}


#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn partial_eq_respects_data_type() 
    {
        let a = ImageData::from(DecodingResult::U8(Vec::new()));
        let b = ImageData::from(DecodingResult::U16(Vec::new()));
        assert_ne!(a, b);
    }
}