extern crate clap;
extern crate smlm_locs;

use smlm_locs::io::{ParseMethod};
use smlm_locs::split;
use smlm_locs::split::{SplitMethod};

use clap::{Parser};

#[derive(Parser)]
#[command(version, about, long_about = None)]
struct Arguments 
{
    #[arg(long, help = "localisations filename")]
    locs : String,
    #[arg(short, long, help = "a output filename")]
    a: Option<String>,
    #[arg(short, long, help = "b output filename")]
    b: Option<String>,
    #[arg(short, long, help = "split method - half,zip,rand - defaults to zip")]
    method: Option<String>,
    #[arg(long, help = "how to interpret the localisation file | ts=thunderstom, csv=n_headers;delim;x_pos;y_pos;sigma_pos")]
    parse_method: Option<String>
}

impl Arguments
{
    pub fn localisations_filename(&self) -> &str
    {
        &self.locs
    }

    pub fn a_filename(&self) -> &str
    {
        self.a.as_ref().map(|s| s.as_str()).unwrap_or("./a.out")
    }

    pub fn b_filename(&self) -> &str
    {
        self.b.as_ref().map(|s| s.as_str()).unwrap_or("./b.out")
    }

    pub fn method(&self) -> SplitMethod
    {
        match &self.method
        {
            None => SplitMethod::Zip,
            Some(value) => 
            {
                match SplitMethod::try_from(value.as_str())
                {
                    Ok(m) => m,
                    Err(e) => 
                    {
                        eprintln!("Could not parse split method: {e}\nDefaulting to zip");
                        SplitMethod::Zip
                    }
                }
            }
        }
    }

    pub fn parse_method(&self) -> Result<ParseMethod, String>
    {
        self.parse_method.as_ref()
                         .map(|s| s.as_str())
                         .map(ParseMethod::try_from)
                         .unwrap_or(Ok(ParseMethod::default()))
    }
}


fn run(arguments: &Arguments) -> Result<(), String>
{
    let settings = split::Settings::default().with_split_method(arguments.method())
                                             .with_parse_method(arguments.parse_method()?);
    split::split(arguments.localisations_filename(), arguments.a_filename(), arguments.b_filename(), &settings)
}

fn main() 
{
    let arguments = Arguments::parse();
    match run(&arguments)
    {
        Ok(_) => println!("Finished ok"),
        Err(e) => eprintln!("Finished with errors: {}", e)
    }
}
