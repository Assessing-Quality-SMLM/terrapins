use crate::Error;
use num_traits::ToPrimitive;

pub fn to_f64(n: usize) -> Result<f64, Error>
{
    n.to_f64()
     .ok_or_else(|| Error::F64Conversion(n))
}

#[cfg(test)]
mod tests 
{
    

    // This test should fail but need to work out a better failure mechanism
    // num traits permits loss of precision in conversion but this is not ideal

    // #[test]
    // fn invalid_f64()
    // {
    //     let n = to_f64(123_456_789_123_456_789 as usize); // this will lose precision in f64 land
    //     let e = n.unwrap_err();
    //     let expected = "Could not create f64 from: 123456789123456789";
    //     assert_eq!(e.to_string(), expected)
    // }
}