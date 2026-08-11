extern crate smlm_imp as imp;
extern crate smlm_locs as locs;
extern crate statrs;

pub use self::locs::constants::{PARSE_INSTRUCTIONS};
pub use self::locs::{AllocatedLocalisation};
pub use self::render_result::{RenderResult};

pub mod config;
mod data_properties;
mod gaussian;
pub mod io;
mod patch;
mod render_result;
mod render_styles;
mod utils;
pub mod widefield;

pub use locs::io::{ParseMethod};

use self::config::{Config};
pub use self::data_properties::{DataProperties, Properties, ImageDimensions, ImageFrame, NmFrame};
use self::patch::{SigmaFactory};
pub use self::render_styles::{RenderPolicy, SinglePointPatchRenderer, IntegralPatchRenderer};

pub use imp::{Image, ImageMut, Pad, Crop, OwnedImage, BorrowedImage};

use locs::{UncertainLocalisation};
use locs::io::{read_file, ParserFactory, Parser, ThunderStormParserFactory};

use std::fs::{File};
use std::io::{Write, Error as IoError};
// use std::ops::AddAssign;
use std::path::{Path};
use std::sync::{Arc, Mutex};
use std::thread;

type DataType = f64;

    
fn calculate_data_properties<L: UncertainLocalisation>(config: &Config, localisations: &[L]) -> DataProperties
{
    let mut min_x = std::f64::MAX;
    let mut max_x = 0.0;
    let mut min_y =std::f64::MAX;
    let mut max_y = 0.0;
    // idea is if we use the greatest sigma at the furthest point
    // so that all the blending can occur in the image
    // let factory = SigmaFactory::new(config.sigma_scale());
    // let mut max_sigma = 0.0;
    for l in localisations
    {
        let patch_size = 0.0;
        // this doesn't lock at zero
        let adjusted_x_min = l.x() - patch_size;
        let adjusted_x_max = l.x() + patch_size;
        // this doesn't lock at zero
        let adjusted_y_min = l.y() - patch_size;
        let adjusted_y_max = l.y() + patch_size;
        if adjusted_x_max > max_x
        {
            max_x = adjusted_x_max;
            // adjust_sigma(&mut max_sigma, l.uncertainty())
        }
        if adjusted_x_min < min_x
        {
            min_x = adjusted_x_min;
            // adjust_sigma(&mut max_sigma, l.uncertainty())
        }
        if adjusted_y_max > max_y
        {
            max_y = adjusted_y_max;
            // adjust_sigma(&mut max_sigma, l.uncertainty())
        }
        if adjusted_y_min < min_y
        {
            min_y = adjusted_y_min;
            // adjust_sigma(&mut max_sigma, l.uncertainty())
        }
    }

    match config.border()
    {
        None => {},
        Some(border_pixels) => 
        {
            let border = border_pixels as f64 * config.pixel_size_nm();
            min_x -= border;
            max_x += border;
            min_y -= border;
            max_y += border;
        }
    }
    DataProperties::from(min_x, max_x, min_y, max_y)
}


fn sum_values<I: Image<Data=DataType>>(image: I, row: usize, col: usize, step: usize) -> DataType
{
    let mut total = 0.0;
    for r in row..(row + step)
    {
        for c in col..(col + step)
        {
            total += image.get_at(r, c)
        }
    }
    total
}

fn down_sample_image<I: Image<Data=DataType>>(zoom_level: usize, image: I) -> OwnedImage<DataType>
{
    let (rendered_rows, rendered_cols) = image.shape();
    let rows = rendered_rows / zoom_level;
    let cols = rendered_cols / zoom_level;
    // println!("Image: {rows} x {cols}");
    let mut down_sampled_image = OwnedImage::zeros((rows, cols));
    let step_size = zoom_level;
    for (down_row, row) in (0..rendered_rows).step_by(step_size).enumerate()
    {
        for (down_col, col) in (0..rendered_cols).step_by(step_size).enumerate()
        {
            let value = sum_values(&image, row, col, step_size);
            *down_sampled_image.get_at_mut(down_row, down_col) = value;
        }
    }
    down_sampled_image

}

fn get_image_size_from_data_properties(config: &Config, data_properties: &DataProperties) -> ImageDimensions
{
    let pixel_size = config.pixel_size_nm();
    let image_width = data_properties.calculate_image_width(pixel_size);
    let image_height = data_properties.calculate_image_height(pixel_size);
    ImageDimensions::new(image_height, image_width)
}

fn get_data_properties<L: UncertainLocalisation>(config: &Config, localisations: &[L]) -> DataProperties
{
    match config.data_frame()
    {
        None => calculate_data_properties(config, localisations),
        Some(nm_frame) => 
        {
            // println!("nm frame: {nm_frame} (nm)");
            DataProperties::new(nm_frame)
        }
    }   
}

fn get_image_size(config: &Config, data_properties: &mut DataProperties) -> ImageDimensions
{
    config.final_image_size().unwrap_or_else(|| get_image_size_from_data_properties(config, data_properties))
}

fn render_with_properties<R: RenderPolicy<L>, L: UncertainLocalisation + std::fmt::Debug, I: Iterator<Item=L>>
    (
        renderer: R,
        properties: &Properties, 
        image_shape: (usize, usize), 
        localisations: I
    ) -> Result<OwnedImage<DataType>, <R as RenderPolicy<L>>::Error>
{
    let mut render_image = OwnedImage::zeros(image_shape);
    // println!("{:?}", properties);
    for localisation in localisations.filter(|l| properties.within_bounds(l.x(), l.y()))
    {
        // println!("{:?}", localisation);
        // let _ = render_as_patch(&factory, localisation, &mut render_image, &properties)?;
        let _ = renderer.render(localisation, &mut render_image, &properties)?;
    }
    // let t = std::thread::current();
    // println!("Patch Rendered - {:?}", t.name());
    Ok(render_image)
}

fn mt_rendering<R: for<'a> RenderPolicy<&'a L, Error=String> + Clone + std::marker::Send + 'static, L: UncertainLocalisation + Clone + std::marker::Send + 'static + std::fmt::Debug>(n_threads: usize,
                                                                             renderer: R,
                                                                             properties: &Properties, 
                                                                             image_shape: (usize, usize), 
                                                                             localisations: &[L]) -> Result<OwnedImage<DataType>, String>
{
    let render_image = OwnedImage::zeros(image_shape);
    let safe_image = Arc::new(Mutex::new(render_image));
    let batch_size = localisations.len() / n_threads;
    let mut batches = Vec::with_capacity(n_threads);
    let mut end = 0;
    for idx in 0..(n_threads - 1)
    {
        let start = idx * batch_size;
        end = start + batch_size;
        batches.push((start, end));
    }
    batches.push((end, localisations.len() - 1));
    // for (start, end) in &batches
    // {
    //     println!("{start} -> {end}");
    // }
    let mut threads = Vec::with_capacity(n_threads);

    for idx in 0..n_threads
    {
        let name = format!("renderer_{}", idx);
        let batch = batches[idx];
        let l = localisations[batch.0..batch.1].to_vec();
        // println!("{name} rendering: {} -> {}", batch.0, batch.1);
        let r = renderer.clone();
        let p = properties.clone();
        let i = safe_image.clone();
        let t = thread::Builder::new().name(name).spawn(move || 
        {
            let image = render_with_properties(r, &p, image_shape, l.iter());
            i.lock().unwrap().add_to(image.unwrap());
        }); 
        threads.push(t.unwrap());
    }

    for t in threads
    {
        // println!("Waiting for {:?}", t.thread().name());
        let _ = t.join();
        // println!("Thread joined");
    }
    Arc::into_inner(safe_image).ok_or(format!("could not unwrap image from arc"))
                               .and_then(|m| m.into_inner().map_err(|e| e.to_string()))
}

fn write_image_<F: FnOnce() -> Result<(), String>>(writer: F, filename: &str) -> () 
{
    match writer()
    {
        Ok(_) => {},
        Err(e) => 
        {
            eprintln!("error writing image to {filename} {e}");
        }
    }   
}

fn write_image<I: Image<Data=DataType>>(image: I, filename: Option<&str>, write_as_f32: bool) -> ()
{
    match filename
    {
        None => {},
        Some(f) => 
        {
            if write_as_f32
            {
                let shape = image.shape();
                // println!("creating new image");
                // println!("{:?}", image.data());
                let new_image_data = image.data().iter().map(|v| *v as f32).collect::<Vec<f32>>();
                // let new_image_data = utils::downstep(image.data_mut());
                let new_image = BorrowedImage::new(shape, &new_image_data);
                // println!("{:?}", new_image.data());
                // println!("sending to writer");
                // let writer = || new_image.write_big_tiff(f);
                let writer = || new_image.write_tiff(f);
                write_image_(writer, f)
            }
            else 
            {
                // let writer = || image.write_big_tiff(f);
                let writer = || image.write_tiff(f);
                write_image_(writer, f)
            }
        }
    };    
    
}

fn write_data_file<W: Write>(mut writer: W, config: &Config, n_rows: usize, n_cols: usize, pixel_size_nm: f64) -> Result<(), IoError>
{
    let _ = writeln!(writer, "configured with:")?;
    let _ = config.write_to(&mut writer)?;
    writeln!(writer, "\n\ncreated:\nrowsxcols={n_rows}x{n_cols}\npixel size (nm)={pixel_size_nm}")
}

pub fn render_localisations_with<R: for<'a> RenderPolicy<&'a L, Error=String> + std::fmt::Debug + Clone + Send + 'static, L: UncertainLocalisation + std::fmt::Debug + Clone + Send + 'static>(config: &Config, localisations: &[L], renderer: R) -> Result<RenderResult, String>
{
    if localisations.is_empty()
    {
        return Err("No localisations given".to_string())
    }
    println!("{:?}", config);
    let mut data_properties = get_data_properties(config, localisations);
    let image_dimensions = get_image_size(config, &mut data_properties);
    let image_height = image_dimensions.height();
    let image_width = image_dimensions.width();
    let pixel_size = config.pixel_size_nm();
    println!("height x width: {image_height} x {image_width}");

    let zoom_level = config.zoom_level();
    let render_width = image_width * zoom_level;
    let render_height = image_height * zoom_level;
    let rendering_pixel_size = utils::rendering_pixel_size(pixel_size, config.zoom_level());
    println!("pixel_size: {rendering_pixel_size}");
    
    let properties = Properties::new(data_properties, rendering_pixel_size);
    let n_threads = config.n_threads();
    
    let image_shape = (render_height, render_width);
    let zoom_image = if n_threads > 1
    {
        mt_rendering(n_threads, renderer, &properties, image_shape, localisations)?
    }
    else 
    {
        render_with_properties(renderer, &properties, image_shape, localisations.iter())?
    };    
    println!("writing zoom image");
    write_image(&zoom_image, config.zoom_filename(), config.write_as_f32());
    println!("writing finished");

    println!("downsampling image");
    let image = down_sample_image(config.zoom_level(), zoom_image);
    println!("downsampling complete");
    if config.write_image()
    {
        println!("writing real image");
        write_image(&image, config.image_filename(), config.write_as_f32());
    }
    if config.write_data_file()
    {
        // recalculate as we could have just padded / cropped
        let n_rows = image.n_rows();
        let n_cols = image.n_cols();
        let _ = config.data_filename()
                      .ok_or(format!("data filename not set"))
                      .and_then(|filename| File::create(filename).and_then(|f| write_data_file(f, config, n_rows, n_cols, pixel_size))
                                                                 .map_err(|e| e.to_string()))?;
    }

    Ok(RenderResult::new(image, properties))
}

pub fn render_localisations<L: UncertainLocalisation + std::fmt::Debug + Clone + Send + 'static>(config: &Config, localisations: &[L]) -> Result<RenderResult, String>
{    
    let renderer = SinglePointPatchRenderer::with_sigma(config.sigma_scale());
    render_localisations_with(config, localisations, renderer)
}

fn render_and_write_with<R: for<'a> RenderPolicy<&'a <<PA as ParserFactory>::Parser as Parser>::Localisation, Error=String> + std::fmt::Debug + Clone + Send + 'static, P: AsRef<Path>, PA: ParserFactory + 'static>(path: P, config: &Config, parser: PA, renderer: R) -> Result<RenderResult, String> 
where String: From<<PA as ParserFactory>::Error>, 
     <<PA as ParserFactory>::Parser as Parser>::Error: std::fmt::Display, 
     <<PA as ParserFactory>::Parser as Parser>::Localisation: Send + Clone + std::fmt::Debug + UncertainLocalisation 
{
    type L<T>= <<T as ParserFactory>::Parser as Parser>::Localisation;
    let localisations = read_file(path, parser).map_err(|e| e.to_string())?.collect::<Result<Vec<L<PA>>, String>>()?;
    render_localisations_with(&config, &localisations, renderer)
}

pub fn render_localisation_file_with<R: for<'a> RenderPolicy<&'a AllocatedLocalisation, Error=String> + std::fmt::Debug + Clone + Send + 'static,  P: AsRef<Path>>(path: P, config: &Config, parse_method: ParseMethod, renderer: R) -> Result<RenderResult, String>
{    
    match parse_method
    {
        ParseMethod::ThunderStorm => 
        {
            render_and_write_with(path, config, ThunderStormParserFactory, renderer)
        },
        ParseMethod::Csv(settings) => 
        {
            render_and_write_with(path, config, settings, renderer)
        }
    }
}

pub fn render_localisation_file<P: AsRef<Path>>(path: P, config: &Config, parse_method: ParseMethod) -> Result<RenderResult, String>
{
    let factory = SigmaFactory::new(config.sigma_scale());
    let renderer = SinglePointPatchRenderer::new(factory);
    render_localisation_file_with(path, config, parse_method, renderer)
}

#[cfg(test)]
mod tests 
{
    use super::*;
    
    use imp::{OwnedImage};

    use locs::{AllocatedLocalisation};

    #[test]
    fn can_handle_empty_localisations() 
    {
        let localisations : [AllocatedLocalisation; 0] = [];
        let config = Config::default();
        let result = render_localisations(&config, &localisations);
        assert_eq!(result.is_err(), true);
        assert_eq!(result.unwrap_err(), "No localisations given");
    }

    #[test]
    fn down_sample_test() 
    {
        let mut image = OwnedImage::<DataType>::zeros((4, 4));
        for idx in 0..16
        {
            image.data_mut()[idx] = idx as DataType;
        }
        let new_image = down_sample_image(2, image);
        let expected = [10.0, 18.0, 42.0, 50.0];
        assert_eq!(new_image.data(), expected);

    }

    #[test]
    fn do_not_render_localisation_beyond_image_bounds() 
    {
        let factory = SigmaFactory::new(1.0);
        let renderer = SinglePointPatchRenderer::new(factory);
        let pixel_size_nm = 1.0;
        let data_properties = DataProperties::from(10.0, 20.0, 10.0, 20.0);
        let localisations = [AllocatedLocalisation::new(0, 5.0, 15.0, 1.0, 0.0, 0.0), // past min x
                             AllocatedLocalisation::new(0, 25.0, 15.0, 1.0, 0.0, 0.0), // past max x
                             AllocatedLocalisation::new(0, 15.0, 5.0, 1.0, 0.0, 0.0), // past min y
                             AllocatedLocalisation::new(0, 15.0, 25.0, 1.0, 0.0, 0.0), // past min y
                             ];
        for l in &localisations
        {
            assert_eq!(data_properties.within_bounds(l.x(), l.y()), false);
        }
        let properties = Properties::new(data_properties, pixel_size_nm);
        let image = render_with_properties(renderer, &properties, (10, 10), localisations.iter()).unwrap();
        assert_eq!(image.data().iter().all(|x| *x == 0.0), true);
    }

    #[test]
    fn render_with_global_reference_frame() 
    {
        let pixel_size_nm = 1.0;
        let global_reference_frame = DataProperties::from(10.0, 20.0, 10.0, 20.0);
        let factory = SigmaFactory::new(1.0);
        let renderer = SinglePointPatchRenderer::new(factory);
        let localisations = [AllocatedLocalisation::new(0, 10.1, 10.1, 0.0, 0.0, 1.0)]; 
        for l in &localisations
        {
            assert_eq!(global_reference_frame.within_bounds(l.x(), l.y()), true);
        }
        let properties = Properties::new(global_reference_frame, pixel_size_nm);
        let image = render_with_properties(renderer, &properties, (10, 10), localisations.iter()).unwrap();
        assert_eq!(*image.get_at(0, 0), 0.1356228962390294);
        assert_eq!(*image.get_at(0, 1), 0.055140154776936734);
        assert_eq!(*image.get_at(1, 0), 0.055140154776936734);
        assert_eq!(*image.get_at(1, 1), 0.02241831396570313);
    }
}
