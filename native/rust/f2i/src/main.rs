extern crate clap;
extern crate smlm_renderer as renderer;

use renderer::{io::parse_global_frame_nm, ParseMethod, NmFrame, PARSE_INSTRUCTIONS};
use renderer::config::{Config};


use clap::{Parser};

const DEFAULT_MAGNIFICATION_FACTOR : f64 = 10.0;

#[derive(Parser)]
#[command(version, about, long_about = None)]
struct Arguments 
{
    #[arg(short, long, help = "Input filename")]
    input : String,

    #[arg(short, long, help = "camera pixel size (nm)")]
    camera_pixel_size : f64,

    #[arg(short, long, help = "global space (nm) to render into row, column, height, width")]
    global_frame_nm : Option<String>,

    #[arg(short, long, help = "magnification factor")]
    magnification_factor : Option<f64>,

    #[arg(short, long, help = "border (pixels)")]
    border : Option<u64>,

    #[arg(short, long, help = "filename to output image to")]
    output_image : Option<String>,

    #[arg(long, help = PARSE_INSTRUCTIONS)]
    parse_method: Option<String>,

    #[arg(long, help = "filename to output zoom image to")]
    output_zoom : Option<String>,

    #[arg(long, help = "filename to output data to")]
    output_data : Option<String>,    

    #[arg(short, long, help = "sigma to render to. Localisation sigma is mulitplied by this")]
    sigma_scale : Option<f64>,    

    #[arg(short, long, help = "zoom level to render subpixel grid at")]
    zoom_level : Option<usize>,    

    #[arg(short, long, help = "Number of threads to use for rendering")]
    n_threads: Option<usize>,

    #[arg(long, help = "Write 32 bit floating point tiff outputs", default_value_t = false)]
    f32: bool,
}

impl Arguments
{
    pub fn input_filename(&self) -> &str
    {
        &self.input
    }

    pub fn camera_pixel_size_nm(&self) -> f64
    {
        self.camera_pixel_size
    }

    pub fn magnification_factor(&self) -> f64
    {
        self.magnification_factor.unwrap_or(DEFAULT_MAGNIFICATION_FACTOR)
    }

    pub fn border(&self) -> Option<u64>
    {
        self.border.clone()
    }

    pub fn parse_global_space(&self) -> Option<NmFrame>
    {

        match &self.global_frame_nm
        {
            None => None,
            Some(value) => 
            {
                match parse_global_frame_nm(&value)
                {
                    Err(e) => 
                    {
                        eprintln!("{e}");
                        None
                    }
                    Ok(frame) => Some(frame)
                }
            }
        }
    }

    pub fn parse_method(&self) -> Result<ParseMethod, String>
    {
        println!("{:?}", self.parse_method);
        self.parse_method.as_ref()
                         .map(|s| s.as_str())
                         .map(ParseMethod::try_from)
                         .unwrap_or(Ok(ParseMethod::default()))
    }
   
}

fn build_config(arguments: &Arguments) -> Config
{
    let output_image = arguments.output_image.clone().unwrap_or("image.tiff".to_string());
    let mut config = Config::new().with_camera_pixel_size(arguments.camera_pixel_size_nm())
                                  .with_sigma_scale(3.0)
                                  .with_magnification_factor(arguments.magnification_factor())
                                  .with_zoom_level(1)
                                  .with_n_threads(10)
                                  .with_image_filename(Some(output_image))
                                  .with_zoom_filename(arguments.output_zoom.clone())
                                  .with_data_filename(arguments.output_data.clone())
                                  .with_write_as_f32(arguments.f32);
    let frame = arguments.parse_global_space();
    if frame.is_some()
    {
        config = config.with_global_frame_nm(frame.unwrap());
    }

    let border = arguments.border();
    if border.is_some()
    {
        config = config.with_border(border.unwrap());
    }

    if arguments.sigma_scale.is_some()
    {
        config = config.with_sigma_scale(arguments.sigma_scale.unwrap());
    }

    if arguments.zoom_level.is_some()
    {
        config = config.with_zoom_level(arguments.zoom_level.unwrap());
    }

    if arguments.n_threads.is_some()
    {
        config = config.with_n_threads(arguments.n_threads.unwrap());
    }
    config
}

fn run(arguments: &Arguments) -> Result<(), String>
{
    let parse_method = arguments.parse_method()?;
    let filename = arguments.input_filename();
    println!("Rendering {filename}");
    let config = build_config(arguments);
    // println!("{:?}", config);
    renderer::render_localisation_file(&filename, &config, parse_method).map(|_| ())
}

fn main() 
{
    let arguments = Arguments::parse();
    match run(&arguments)
    {
        Ok(_) => 
        {
            println!("Finished ok");
            std::process::exit(0);
        },
        Err(e) => 
        {
            eprintln!("Finished with errors: {e}");
            std::process::exit(1);
        }
    }
}

#[cfg(test)]
mod tests 
{
    // use super::*;
}