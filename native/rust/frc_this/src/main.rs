extern crate smlm_frc;
extern crate tiff_wrap;

// mod corrections;

use clap::{Parser};

use smlm_frc::{imp::{OwnedImage, Image}, {thresholds::Threshold}, Config as FrcConfig, FrcResult};
use smlm_frc::constants::DEFAULT_NM_PER_PIXEL;

use tiff_wrap::{Tiff};

use std::fs::{File};
use std::process;

#[derive(Parser)]
#[command(version, about, long_about = None)]
struct Arguments 
{
    #[arg(long, help = "image_1")]
    image_1 : String,
    #[arg(long, help = "image_2")]
    image_2 : String,
    #[arg(short, long, help = "run with Tukey filter")]
    filter: Option<String>,
    #[arg(short, long, help = "set alpha level of Tukey filter")]
    parameters: Option<Vec<f64>>,
    #[arg(short, long, help = "Threshold method to use: half=half bit; 17=1/7; sigma_x = sigma factor (x = factor)")]
    threshold: Option<String>,
    #[arg(long, help = "nanometers per pixel")]
    nm: Option<f64>,
    #[arg(long, help = "display plots", default_value_t = false)]
    plot: bool,
    #[arg(long, help = "data output file - fmt: resolution\\n[qs,frc,threshold]")]
    output_data: Option<String>
}

#[allow(non_snake_case)]
impl Arguments
{
    pub fn image_1(&self) -> &str
    {
        &self.image_1
    }

    pub fn image_2(&self) -> &str
    {
        &self.image_2
    }

    pub fn filter_name(&self) -> Option<&str>
    {
        self.filter.as_ref().map(|s| s.as_str())
    }

    pub fn parameters(&self) -> Option<&[f64]>
    {
        self.parameters.as_ref().map(|v| v.as_slice())
    }

    pub fn threshold_method(&self) -> Option<&str>
    {
        self.threshold.as_ref().map(|s| s.as_str())
    }

    pub fn filter(&self) -> Option<(&str, &[f64])>
    {
        self.filter_name().map(|name| (name, self.parameters().unwrap_or(&[])))
    }

    pub fn nm_per_pixel(&self) -> f64
    {
        match self.nm
        {
            Some(value) => value,
            None => 
            {
                println!("Nanometers per pixel not set defaulting to {DEFAULT_NM_PER_PIXEL}");
                DEFAULT_NM_PER_PIXEL
            }
        }
    }

    pub fn plot(&self) -> bool
    {
        self.plot
    }

    pub fn output_data_file(&self) -> Option<&str>
    {
        self.output_data.as_ref().map(|x| x.as_str())
    }
}


fn get_tiff(filename: &str) -> Result<OwnedImage<f64>, String>
{
    println!("Loading {filename}");
    Tiff::read(filename).map_err(|e| e.to_string())
                        .and_then(|mut tiff|  tiff.height()
                                                  .map_err(|e| e.to_string())
                                                  .and_then(|n_rows| tiff.width().map_err(|e| e.to_string()).map(|n_cols| (n_rows as usize, n_cols as usize)))
                                                  .map(|shape| (tiff, shape)))
                        .and_then(|(tiff, shape)| tiff.take(1)
                                                      .next()
                                                      .ok_or_else(|| "Tiff file empty".to_string())
                                                      .and_then(|i| i.to_f64().map_err(|e| e.to_string()).map(|data| (shape, data))))
                        .map(|(shape, data)| OwnedImage::new(shape, data))
}

fn get_frc_result(arguments: &Arguments) -> Result<(OwnedImage<f64>, OwnedImage<f64>, FrcResult), String>
{
    let mut image_1 = get_tiff(arguments.image_1())?;
    let mut image_2 = get_tiff(arguments.image_2())?;
    let result = smlm_frc::frc_result(&mut image_1, &mut image_2, arguments.filter()).map_err(|e| e.to_string())?;
    Ok((image_1, image_2, result))
}

fn run(arguments: &Arguments) -> Result<(), String>
{
    let threshold = match arguments.threshold_method()
        {
            None => Threshold::default(),
            Some(t) =>  Threshold::try_from(t)?
        };
    let (image_1, _image_2, result) = get_frc_result(arguments)?;
    let image_width = image_1.n_cols();
    let config = FrcConfig::new(image_width);
    let resolution = result.get_resolution(config.L(), threshold);
    let value = resolution.as_value();
    let value_nm = resolution.to_nm(arguments.nm_per_pixel());        
    println!("FRC: {value}");
    println!("FRC (nm): {value_nm}");
    if arguments.plot()
    {
        resolution.plot();
    }
    arguments.output_data_file()
             .map(|p| File::create(p).and_then(|f| resolution.write_to(arguments.nm_per_pixel(), f)).map_err(|e| e.to_string()))
             .unwrap_or(Ok(()))
}

fn main() 
{
    let arguments = Arguments::parse();
    match run(&arguments)
    {
        Ok(_) => 
        {
            println!("Finished ok");
            process::exit(0)
        },
        Err(e) => 
        {
            eprintln!("Finished with errors: {}", e);
            process::exit(1)
        }
    }
}
