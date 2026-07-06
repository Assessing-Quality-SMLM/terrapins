use crate::{Error};
use crate::settings::{EquipmentSettings, ReconSettings};

use imp::{OwnedImage};

use std::fs;
use std::io::{Error as IoError};
use std::path::{Path, PathBuf};

fn to_number<P: AsRef<Path>>(path: P) -> Option<u32>
{
    path.as_ref()
        .file_stem()
        .and_then(|s| s.to_str())
        .and_then(|s| s.parse().ok())
}

fn is_tiff<P: AsRef<Path>>(path: P) -> bool
{
    match path.as_ref().extension()
    {
        None => false,
        Some(ext) => 
        {
            ext == "tif" || ext == "tiff"
        }
    }
}

fn tiff_filter<P: AsRef<Path>>(path: P) -> bool
{
    path.as_ref().file_name().is_some() && is_tiff(path)
}

fn level_transform<P: AsRef<Path>>(path: P) -> Option<(P, u32)>
{
    match tiff_filter(&path)
    {
        false => None,
        true => to_number(&path).map(|l| (path, l))
    }
}

fn get_tiff_files<P: AsRef<Path>, I: Iterator<Item=P>>(iter: I) -> impl Iterator<Item=(P, u32)>
{
    iter.filter_map(|p| level_transform(p))
}

fn order_tiff_files<P: AsRef<Path>, I: Iterator<Item=P>>(entries: I) -> Vec<P>
{
    let mut e : Vec<(P, u32)> = get_tiff_files(entries).collect();
    e.sort_by_key(|(_, level)| *level);
    e.into_iter().map(|(p, _)| p).collect()
}

pub fn get_ordered_tiff_files_from<P: AsRef<Path>>(directory: P) -> Result<Vec<PathBuf>, IoError>
{
    let files = fs::read_dir(directory)?.filter_map(|r| r.ok()).map(|e| e.path());
    Ok(order_tiff_files(files))
}

pub fn read_image<P: AsRef<Path>>(file_name: P) -> Result<OwnedImage<f64>, Error>
{
    imp::read_tiff_image_64(file_name).map_err(Error::image_parse)
}

pub fn sr_pixel_size(camera_pixel_size: f64, magnification: f64) -> f64
{
    camera_pixel_size / magnification
}

pub fn magnification(widefield_pixel_size: f64, sr_pixel_size: f64) -> f64
{
    widefield_pixel_size / sr_pixel_size
}

#[allow(dead_code)]
pub fn recon_pixel_size(equipment: &EquipmentSettings, recon: &ReconSettings) -> f64
{
    sr_pixel_size(equipment.camera_pixel_size_nm(), recon.magnification())
}


#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn is_tiff_test() 
    {
        assert_eq!(is_tiff("some"), false);
        assert_eq!(is_tiff("some.thing"), false);
        assert_eq!(is_tiff("some.tif"), true);
        assert_eq!(is_tiff("some.tiff"), true);
    }

    #[test]
    fn order_tiffs_test()
    {
        let data = ["a.tif", "1.tiff", "4.tif", "something", "some.thing", "3.tiff", "0.tif"];
        let ordered_tiffs = order_tiff_files(data.into_iter());
        let expected = ["0.tif", "1.tiff", "3.tiff", "4.tif"];
        assert_eq!(ordered_tiffs, expected);
    }

    #[test]
    fn recon_pixel_size_test() 
    {
        let equipment = EquipmentSettings::new(0.0, 160.0);

        let recon = ReconSettings::new(0.0, 20.0);
        assert_eq!(recon_pixel_size(&equipment, &recon), 8.0)
    }

    #[test]
    fn magnification_test()
    {
        assert_eq!(magnification(4.0, 2.0), 2.0)
    }
}