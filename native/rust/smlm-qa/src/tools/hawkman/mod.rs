mod results;

pub use self::results::{HawkmanResult};

use super::{ExternalError};
use super::external;

use crate::{io, settings::{HawkmanThresholdSettings}};

use std::env::current_exe;
use std::path::PathBuf;

#[cfg(target_os = "windows")]
const HAWKMAN: &str = "hawkman.exe";

#[cfg(any(target_os = "linux",
          target_os = "macos"
          ))]
const HAWKMAN: &str = "hawkman";
// const HAWKMAN: &str = "/home/nik/Documents/repositories/assessment/assessment/target/release/hawkman";


fn default_path() -> Result<PathBuf, ExternalError>
{
    let exe_path = current_exe()?;
    exe_path.parent().ok_or_else(|| ExternalError::from(format!("{} has no parent", exe_path.display()))).map(|pb| pb.join(HAWKMAN))
}

#[derive(Debug)]
pub struct Settings 
{
    custom_exe: Option<String>,
    ref_image: String,
    test_image: String,
    output_directory: Option<String>,
    psf: Option<f64>,
    start_level: Option<u32>,
    n_levels: Option<u32>,
    n_threads: Option<u32>,
    fwhm: Option<HawkmanThresholdSettings>,
    skel: Option<HawkmanThresholdSettings>
}

impl Settings
{
    pub fn new(ref_image: &str, test_image: &str) -> Self
    {
        Self
        {
            custom_exe: None,
            ref_image: ref_image.to_string(),
            test_image: test_image.to_string(),
            output_directory: None,
            start_level: None,
            n_levels: None,
            n_threads: None,
            psf: None,
            fwhm: None,
            skel: None,
        }
    }

    pub fn location(&self) -> Result<String, ExternalError>
    {
        match self.custom_exe.as_ref().map(|s| s.as_str())
        {
            Some(s) => Ok(s.to_string()),
            None => default_path().map(|p| io::path_to_string(&p))
        }
    }

    pub fn with_location(mut self, value: &str) -> Self
    {
        self.custom_exe = Some(value.to_string());
        self
    }

    fn ref_arg(&self) -> Option<(&str, String)>
    {
        Some(("ref", self.ref_image.clone()))
    }

    fn test(&self) -> Option<(&str, String)>
    {
        Some(("test", self.test_image.clone()))
    }

    fn start(&self) -> Option<(&str, String)>
    {
        self.start_level.map(|t| ("start", t.to_string()))
    }

    pub fn with_start_level(mut self, value: u32) -> Self
    {
        self.start_level = Some(value);
        self
    }

    fn n_levels(&self) -> Option<(&str, String)>
    {
        self.n_levels.map(|t| ("n", t.to_string()))
    }

    pub fn with_n_levels(mut self, value: u32) -> Self
    {
        self.n_levels = Some(value);
        self
    }

    fn psf(&self) -> Option<(&str, String)>
    {
        self.psf.map(|t| ("psf", t.to_string()))
    }

    pub fn with_psf(mut self, value: f64) -> Self
    {
        self.psf = Some(value);
        self
    }

    fn fwhm(&self) -> Option<(&str, String)>
    {
        self.fwhm.as_ref().map(|t| ("fwhm", t.to_string()))
    }

    pub fn with_fwhm(mut self, value: HawkmanThresholdSettings) -> Self
    {
        self.fwhm = Some(value);
        self
    }

    fn skel(&self) -> Option<(&str, String)>
    {
        self.skel.as_ref().map(|t| ("skel", t.to_string()))
    }

    pub fn with_skel(mut self, value: HawkmanThresholdSettings) -> Self
    {
        self.skel = Some(value);
        self
    }

    fn n_threads(&self) -> Option<(&str, String)>
    {
        self.n_threads.map(|t| ("threads", t.to_string()))
    }

    pub fn with_n_threads(mut self, value: u32) -> Self
    {
        self.n_threads = Some(value);
        self
    }

    fn output_directory(&self) -> String
    {
        self.output_directory.as_ref().map(|s| s.to_string()).unwrap_or(".".to_string())
    }

    fn output_directory_arg(&self) -> Option<(&str, String)>
    {
        self.output_directory.as_ref().map(|o| ("o", o.to_string()))
    }

    pub fn with_output_directory(mut self, value: &str) -> Self
    {
        self.output_directory = Some(value.to_string());
        self
    }

    pub fn args(&self) -> Vec<String>
    {
        vec![self.ref_arg(), 
             self.test(),
             self.output_directory_arg(),
             self.start(),
             self.n_levels(),
             self.psf(),
             self.fwhm(),
             self.skel(),
             self.n_threads()
             ].into_iter()
              .filter(|o| o.is_some())
              .map(|o| {let t = o.unwrap(); format!("{}={}", t.0, t.1)})
              .collect()
    }
}


fn get_args(settings: &Settings) -> (Result<String, ExternalError>, Vec<String>)
{
    let exe = settings.location();
    (exe, settings.args())
}

pub fn run(settings: &Settings) -> Result<HawkmanResult<String>, ExternalError>
{
    let (exe, args) = get_args(settings);
    // println!("{exe}\n{:?}", args);
    external::run_with_output(&exe?, &args).map(|_| HawkmanResult::new(settings.output_directory()))
}

// #[cfg(test)]
// mod tests 
// {
//     use super::*;

// }
