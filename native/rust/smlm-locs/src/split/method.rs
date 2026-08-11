const HALF: &str = "half";
const ZIP: &str = "zip";
// const BLOCK: &str = "block";
const BLOCK_PREFIX: &str = "block=";
const RANDOM: &str = "random";

#[derive(Debug, Default, PartialEq)]
pub enum SplitMethod
{
    // HalfFrame,
    HalfIndex,
    #[default]
    Zip,
    Block(usize),
    Random
}

impl TryFrom<&str> for SplitMethod
{
    type Error = String;
    fn try_from(value: &str) -> Result<Self, Self::Error> 
    { 
        match value
        {
            HALF => Ok(Self::HalfIndex),
            ZIP => Ok(Self::Zip),
            RANDOM | "rand" => Ok(Self::Random),
            _ => 
            {
                if value.starts_with(BLOCK_PREFIX)
                {
                    let splits = value.split("=").collect::<Vec<&str>>();
                    if splits.len() < 2
                    {
                        return Err(format!("Cannot parse {value} to split method: could not split {value} on ="));
                    }
                    let block_size = splits[1];
                    block_size.parse::<usize>()
                                .map(|v| Self::Block(v))
                                .map_err(|e| format!("Cannot parse {value} to split method: {e}"))
                }
                else 
                {
                    Err(format!("Cannot parse {value} to split method"))
                }
            }
        }
    }
}

impl TryFrom<String> for SplitMethod
{
    type Error = String;
    fn try_from(value: String) -> Result<Self, Self::Error> 
    {
        Self::try_from(value.as_str())
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[allow(non_snake_case)]
    #[test]
    fn parse_ZIP() 
    {
        assert_eq!(SplitMethod::try_from(ZIP).unwrap(), SplitMethod::Zip)
    }

    #[allow(non_snake_case)]
    #[test]
    fn parse_zip() // cli names
    {
        assert_eq!(SplitMethod::try_from("zip").unwrap(), SplitMethod::Zip)
    }

    #[allow(non_snake_case)]
    #[test]
    fn parse_HALF() 
    {
        assert_eq!(SplitMethod::try_from(HALF).unwrap(), SplitMethod::HalfIndex)
    }

    #[test]
    fn parse_half() // cli names
    {
        assert_eq!(SplitMethod::try_from("half").unwrap(), SplitMethod::HalfIndex)
    }

    #[test]
    fn parse_random() 
    {
        assert_eq!(SplitMethod::try_from(RANDOM).unwrap(), SplitMethod::Random)
    }

    #[test]
    fn parse_rand() // cli names
    {
        assert_eq!(SplitMethod::try_from("rand").unwrap(), SplitMethod::Random)
    }

    #[test]
    fn error_on_junk_parse() // cli names
    {
        assert_eq!(SplitMethod::try_from("junk").unwrap_err(), "Cannot parse junk to split method")
    }

    #[test]
    fn parse_block_split()
    {
        assert_eq!(SplitMethod::try_from("block=1").unwrap(), SplitMethod::Block(1))
    }

    #[test]
    fn parse_block_split_fail()
    {
        assert_eq!(SplitMethod::try_from("block=junk").unwrap_err(), "Cannot parse block=junk to split method: invalid digit found in string")
    }
}