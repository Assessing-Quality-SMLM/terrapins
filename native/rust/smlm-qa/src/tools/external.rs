use std::ffi::{OsStr};
use std::io::{Error as IoError};
use std::process::{Command, Output, Stdio};


#[derive(Debug)]
pub enum Error 
{
    Io(IoError),
    Command(Output),
    Custom(String)
}

impl From<IoError> for Error
{
    fn from(error: IoError) -> Self
    {
        Self::Io(error)
    }
}

impl From<Output> for Error
{
    fn from(error: Output) -> Self
    {
        Self::Command(error)
    }
}

impl From<String> for Error
{
    fn from(error: String) -> Self
    {
        Self::Custom(error)
    }
}


fn to_result(output: Output) -> Result<(), Error>
{
    if output.status.success()
    {
        Ok(())
    }
    else 
    {
        Err(Error::from(output))
    }
}

pub fn run_with_output<I, S>(command: &str, arguments: I) -> Result<(), Error>
where I: IntoIterator<Item = S>,
	  S: AsRef<OsStr> + std::fmt::Debug,
{
    let args: Vec<_> = arguments.into_iter().collect();
    println!("Running {command} with {:?}", args);
    Command::new(command).args(&args)
                    	 .stdout(Stdio::inherit())
                    	 .stderr(Stdio::inherit())
                    	 .output()
                    	 .map_err(|e| Error::from(format!("Could not run {command} with {:?} due to {e}", args)))
                    	 .and_then(to_result)
}