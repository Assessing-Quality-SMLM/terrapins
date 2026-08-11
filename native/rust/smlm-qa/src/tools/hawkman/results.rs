use crate::{Error, io, utils};

use imp::{OwnedImage};

use std::fs::{File};
use std::io::{BufReader, BufRead, Error as IoError};
use std::path::{Path, PathBuf};

pub type Score = (u32, f64);

const SCORE_FILENAME : &str = "score";
const CONFIDENCE_MAP : &str = io::HAWKMAN_CONFIDENCE_MAP;
#[allow(dead_code)]
const SHARPENING_MAP : &str = io::HAWKMAN_SHARPENING_MAP;
#[allow(dead_code)]
const STRUCTURE_MAP : &str = io::HAWKMAN_STRUCTURE_MAP;

fn parse_line(line: &str) -> Result<Score, String>
{
    let splits = line.split(",").collect::<Vec<&str>>();
    if splits.len() < 2
    {
        return Err(format!("Could not split {line} in to 2 with delimiter of ,"));
    }
    let level = splits[0].parse::<u32>().map_err(|e| e.to_string())?;
    let value = splits[1].parse::<f64>().map_err(|e| e.to_string())?;
    Ok((level, value))
}

fn parse_score_file<R: BufRead>(reader: R) -> Result<Vec<Score>, Error>
{
    let mut data = reader.lines()
                         .map(|r| r.map_err(|e| Error::parse(e.to_string())).and_then(|s| parse_line(&s).map_err(Error::parse)))
                         .collect::<Result<Vec<_>, Error>>()?;
    data.sort_by_key(|(level, _)| *level);
    Ok(data)
}

#[derive(Debug)]
pub struct HawkmanResult<T>
{
    output_directory: T
}

impl<T: AsRef<Path>> HawkmanResult<T>
{
    pub fn new(output_directory: T) -> Self
    {
        Self
        {
            output_directory
        }
    }

    fn output_directory(&self) -> PathBuf
    {
        self.output_directory.as_ref().to_path_buf()
    }

    pub fn global_scores(&self) -> Result<Vec<Score>, Error>
    {
        self.get_score_from(CONFIDENCE_MAP)
    }

    #[allow(dead_code)]
    fn confidence_map_data(&self) -> Result<Vec<OwnedImage<f64>>, Error>
    {
        self.get_data_from(CONFIDENCE_MAP)
    }

    #[allow(dead_code)]
    fn sharpening_scores(&self) -> Result<Vec<Score>, Error>
    {
        self.get_score_from(SHARPENING_MAP)
    }

    #[allow(dead_code)]
    fn sharpening_map_data(&self) -> Result<Vec<OwnedImage<f64>>, Error>
    {
        self.get_data_from(SHARPENING_MAP)
    }

    #[allow(dead_code)]
    fn structure_map_data(&self) -> Result<Vec<OwnedImage<f64>>, Error>
    {
        self.get_data_from(STRUCTURE_MAP)
    }

    #[allow(dead_code)]
    fn structure_scores(&self) -> Result<Vec<Score>, Error>
    {
        self.get_score_from(STRUCTURE_MAP)
    }

    fn get_data_from(&self, directory: &str) -> Result<Vec<OwnedImage<f64>>, Error>
    {
        let files = self.get_data_files_from(directory).map_err(Error::from)?;
        files.iter().map(utils::read_image).collect()
    }

    fn get_data_files_from(&self, directory: &str) -> Result<Vec<PathBuf>, IoError>
    {
        let dir = self.output_directory().join(directory);
        utils::get_ordered_tiff_files_from(dir)
    }

    fn get_score_from(&self, directory: &str) -> Result<Vec<Score>, Error>
    {
        let score_file = self.output_directory().join(directory).join(SCORE_FILENAME);
        File::open(score_file).map_err(Error::from).map(BufReader::new).and_then(parse_score_file)
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn scores_are_ordered()
    {
        let data = "5,6.9\n1,2.7\n3,4.8";
        let reader = BufReader::new(data.as_bytes());
        let scores = parse_score_file(reader).unwrap();
        assert_eq!(scores[0], (1, 2.7));
        assert_eq!(scores[1], (3, 4.8));
        assert_eq!(scores[2], (5, 6.9))
    }

    #[test]
    fn errors_in_score()
    {
        let data = "5,junk\n1,2.7\n3,4.8";
        let reader = BufReader::new(data.as_bytes());
        let error = parse_score_file(reader).unwrap_err().to_string();
        assert_eq!(error, "Parse(\"invalid float literal\")");
    }

}