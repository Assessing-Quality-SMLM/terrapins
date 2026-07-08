use std::fmt::{Display, Formatter, Error as FmtError};

#[derive(Debug)]
pub enum DataType 
{
	F64,
	F32,
	I64,
	I32,
	I16,
	I8,
	U64,
	U32,
	U16,
	U8
}

impl Display for DataType
{
	fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), FmtError>
	{
	 	match self
	    {
	        Self::F64 => write!(f, "F64"),
	    	Self::F32 => write!(f, "F32"),
	        Self::U64 => write!(f, "U64"),
	        Self::U32 => write!(f, "U32"),
	        Self::U16 => write!(f, "U16"),
	        Self::U8 => write!(f, "U8"), 
	        Self::I64 => write!(f, "I64"),
	        Self::I32 => write!(f, "I32"),
	        Self::I16 => write!(f, "I16"),
	        Self::I8 => write!(f, "I8"),
	    }
	}
}