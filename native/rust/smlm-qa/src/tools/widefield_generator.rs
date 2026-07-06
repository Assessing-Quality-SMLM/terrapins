use crate::{io, Error};

use renderer::widefield;

use tiff_wrap::{Tiff};

use std::path::{Path};

pub fn from_image_stack<P: AsRef<Path>, Q: AsRef<Path>>(image_stack: P, output_directory: Q) -> Result<(), Error>
{
	let output = io::average_of_frames_image_name(output_directory);
	let mut reader = Tiff::read(image_stack).map_err(Error::from)?;
    let width = reader.width().map_err(Error::from)?;
    let height = reader.height().map_err(Error::from)?;
    let n = reader.n_images();
    let frames = (0..n).filter_map(|idx| reader.read_image(idx).ok().and_then(|id| id.to_f64().ok()));
    println!("Computing widefield");
    // have to downsample to f32 as imageJ throws a wobbly when you try to read it back in as f64
    let widefield_image : Vec<f32> = widefield::mean(frames).into_iter().map(|value: f64| value as f32).collect();
    // let widefield_image = widefield::sum(frames);
    println!("Writing widefield to {}", output.display());
    Tiff::write_frame(&output, width, height, &widefield_image).map_err(Error::from)
}