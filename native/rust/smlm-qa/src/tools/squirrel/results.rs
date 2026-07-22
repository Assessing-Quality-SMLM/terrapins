use crate::{io, utils, Error};

use imp::{OwnedImage};

use std::fs::File;
use std::io::{BufReader, BufRead};
use std::path::{Path, PathBuf};

#[allow(dead_code)]
const ERROR_MAP: &str = io::SQUIRREL_ERROR_MAP;
const METRICS: &str = io::SQUIRREL_METRICS;

#[derive(Debug)]
pub struct Metrics
{
    #[allow(dead_code)]
    rmse: f64,
    pearsons: f64
}

impl Metrics
{
    pub fn new(rmse: f64, pearsons: f64) -> Self
    {
        Self{rmse, pearsons}
    }

    #[allow(dead_code)]
    pub fn rmse(&self) -> f64
    {
        self.rmse
    }

    pub fn pearsons(&self) -> f64
    {
        self.pearsons
    }
}

fn to_f64(value: &str) -> Result<f64, String>
{
    value.parse::<f64>().map_err(|e| format!("Cannot parse {value} to f64: {e}"))
}

fn parse_metric_line(line: &str) -> Result<Metrics, String>
{
    let splits = line.split(",").collect::<Vec<&str>>();
    if splits.len() < 2
    {
        return Err(format!("Cannot split {line} into 2 with ,"))
    }
    let rmse = to_f64(splits[0])?;
    let pearsons = to_f64(splits[1])?;
    Ok(Metrics::new(rmse, pearsons))
}

fn parse_metrics_from<R: BufRead>(reader: R) -> Result<Metrics, String>
{
    reader.lines()
          .skip(1)
          .next().ok_or_else(|| "No more lines in metric file".to_string())
          .and_then(|r| r.map_err(|e| e.to_string()).and_then(|s| parse_metric_line(&s)))
}

fn read_metrics_from<P: AsRef<Path>>(filename: P) -> Result<Metrics, String>
{
    File::open(filename).map(BufReader::new).map_err(|e| e.to_string()).and_then(parse_metrics_from)
}

#[derive(Debug)]
pub struct SquirrelResult 
{
    output_directory: String
}

impl SquirrelResult
{
    pub fn new(output_directory: &str) -> Self
    {
        Self
        {
            output_directory: output_directory.to_string()
        }
    }

    fn output_directory(&self) -> PathBuf
    {
        PathBuf::from(&self.output_directory)
    }

    #[allow(dead_code)]
    fn error_map(&self) -> Result<OwnedImage<f64>, Error>
    {
        let error_map = self.output_directory().join(ERROR_MAP);
        utils::read_image(error_map)
    }

    pub fn metrics(&self) -> Result<Metrics, String>
    {
        let metrics_file = self.output_directory().join(METRICS);
        read_metrics_from(metrics_file)
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;
    
    #[test]
    fn can_parse_metrics() 
    {
        let data = "rmse,pearsons\n123,456";
        let reader = BufReader::new(data.as_bytes());
        let metrics = parse_metrics_from(reader).unwrap();
        assert_eq!(metrics.rmse(), 123.0);
        assert_eq!(metrics.pearsons(), 456.0);
    }

    #[test]
    fn need_headers() 
    {
        let data = "123,456";
        let reader = BufReader::new(data.as_bytes());
        let error = parse_metrics_from(reader).unwrap_err();
        assert_eq!(error, "No more lines in metric file");
    }

    #[test]
    fn need_2_splits() 
    {
        let data = "something\n123";
        let reader = BufReader::new(data.as_bytes());
        let error = parse_metrics_from(reader).unwrap_err();
        assert_eq!(error, "Cannot split 123 into 2 with ,");
    }

    #[test]
    fn rmse_must_be_a_number() 
    {
        let data = "something\njunk,456";
        let reader = BufReader::new(data.as_bytes());
        let error = parse_metrics_from(reader).unwrap_err();
        assert_eq!(error, "Cannot parse junk to f64: invalid float literal");
    }

    #[test]
    fn pearsons_must_be_a_number() 
    {
        let data = "something\n123,junk";
        let reader = BufReader::new(data.as_bytes());
        let error = parse_metrics_from(reader).unwrap_err();
        assert_eq!(error, "Cannot parse junk to f64: invalid float literal");
    }
}