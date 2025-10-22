use hawk_core::{Error as HawkError};

#[derive(Debug, PartialEq)]
pub enum Error 
{
    None,
    Striding(HawkError)
}

impl Error
{
    pub fn none() -> Self
    {
        Self::None
    }

    pub fn code(&self) -> i32
    {
        match self
        {
            Self::None => 0,
            Self::Striding(error) => error.code()
        }
    }

    pub fn to_message(&self) -> String
    {
        match self
        {
            Self::None => String::from("None"),
            Self::Striding(error) => error.to_string()
        }
    }
}

impl From<HawkError> for Error
{
    fn from(error: HawkError) -> Self
    {
        Self::Striding(error)
    }
}

impl From<i32> for Error
{
    fn from(error_code: i32) -> Self
    {
        match HawkError::try_from(error_code)
        {
            Ok(e) => Self::from(e),
            Err(_) => Self::None
        }
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn unknown_order_error_message_reconstruction()
    {
        error_message_reconstruction(1, "Unknown Memory Order")
    }

    #[test]
    fn cancelled_error_message_reconstruction()
    {
        error_message_reconstruction(2, "Cancelled")
    }

    #[test]
    fn poisoned_mutex_error_message_reconstruction()
    {
        error_message_reconstruction(3, "Poisoned Mutex ")
    }

    fn error_message_reconstruction(code: i32, message: &str) 
    {
        let error = Error::try_from(code).unwrap().to_message();
        assert_eq!(error, message)
    }
}