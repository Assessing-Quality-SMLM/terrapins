pub use self::results::{SquirrelResult};
mod results;


use super::{ExternalError};
use super::external;

use crate::{io, settings::SquirrelSettings};

use std::env::current_exe;
use std::path::{Path, PathBuf};

type Arg<'a> = (&'a str, String);

#[cfg(target_os = "windows")]
const SQUIRREL: &str = "squirrel.exe";

#[cfg(any(target_os = "linux",
          target_os = "macos"
          ))]
const SQUIRREL: &str = "squirrel";

const NULL_VALUE: &str = "";

fn create_option(name: &str) -> Arg
{
    (name, NULL_VALUE.to_string())
}

fn default_path() -> Result<PathBuf, ExternalError>
{
    let exe_path = current_exe()?;
    exe_path.parent().ok_or_else(|| ExternalError::from(format!("{} has no parent", exe_path.display()))).map(|pb| pb.join(SQUIRREL))
}

#[derive(Debug,  PartialEq)]
pub struct Settings 
{
    custom_exe: Option<String>,
    widefield_image: String,
    super_res_image: String,    
    pixel_size_nm: f64,
    sigma_nm: f64,
    settings: SquirrelSettings,
    output_directory: Option<String>,
    write_optimiser_data: Option<bool>,
    n_threads: Option<u32>
}

impl Settings
{
    pub fn new(widefield_image: &str, super_res_image: &str, pixel_size_nm: f64, sigma_nm: f64) -> Self
    {
        Self
        {
            custom_exe: None,
            widefield_image: widefield_image.to_string(),
            super_res_image: super_res_image.to_string(),
            pixel_size_nm,
            sigma_nm,
            settings: SquirrelSettings::default(),
            output_directory: None,
            write_optimiser_data: Some(true),
            n_threads: None,
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

    pub fn widefield_image(&self) -> &Path
    {
        Path::new(&self.widefield_image)
    }

    pub fn super_res_image(&self) -> &Path
    {
        Path::new(&self.super_res_image)
    }

    pub fn with_squirrel_settings(mut self, value: SquirrelSettings) -> Self
    {
        self.settings = value;
        self
    }

    fn widefield(&self) -> Option<(&str, String)>
    {
        Some(("wf", self.widefield_image.clone()))
    }

    fn super_res(&self) -> Option<(&str, String)>
    {
        Some(("sr", self.super_res_image.clone()))
    }

    fn sigma(&self) -> Option<(&str, String)>
    {
        Some(("sigma", self.sigma_nm.to_string()))
    }

    fn pixel_size_nm(&self) -> Option<(&str, String)>
    {
        Some(("px", self.pixel_size_nm.to_string()))
    }

    fn write_optimiser_data(&self) -> bool
    {
        self.write_optimiser_data.clone().unwrap_or(false)
    }

    fn optimiser_data(&self) -> Option<(&str, String)>
    {
        match self.write_optimiser_data()
        {
            true => Some(create_option("wo")),
            false => None
        }
    }

    pub fn with_write_optimiser_data(mut self, value: bool) -> Self
    {
        self.write_optimiser_data = Some(value);
        self
    }

    fn n_threads(&self) -> Option<(&str, String)>
    {
        if self.settings.patchwise()
        {
            self.n_threads.map(|t| ("n", t.to_string()))
        }
        else 
        {
            None
        }
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
        self.output_directory.as_ref().map(|o| ("od", o.to_string()))
    }

    pub fn with_output_directory(mut self, value: &str) -> Self
    {
        self.output_directory = Some(value.to_string());
        self
    }

    fn do_registration(&self) -> Option<Arg>
    {
        if self.settings.registration()
        {
            Some(create_option("reg"))
        }
        else 
        {
            None
        }
    }

    fn registration_method(&self) -> Option<Arg>
    {
        self.settings.registration_method().map(|s| ("reg_meth", s.to_string()))
    }

    fn border_size_wf_px(&self) -> Option<Arg>
    {
        Some(("wfb", self.settings.border_wf_px().to_string()))
    }

    fn optimiser_algorithm(&self) -> Option<Arg>
    {
        Some(("opt", self.settings.optimiser_algorithm().to_string()))
    }

    fn three_parameter_solve(&self) -> Option<Arg>
    {
        if self.settings.three_parameter_solve()
        {
            Some(create_option("3p"))
        }
        else 
        {
            None
        }
    }

    fn show_positive_negative(&self) -> Option<Arg>
    {
        if self.settings.show_positive_negative()
        {
            Some(create_option("pn"))
        }
        else 
        {
            None
        }
    }

    fn patchwise(&self) -> Option<Arg>
    {
        if self.settings.patchwise()
        {
            Some(create_option("pw"))
        }
        else 
        {
            None
        }
    }

    fn patch_size(&self) -> Option<(&str, String)>
    {
        if self.settings.patchwise()
        {
            Some(("ps", self.settings.patch_size().to_string()))
        }
        else 
        {
            None
        }
    }

    fn step_size(&self) -> Option<(&str, String)>
    {
        if self.settings.patchwise()
        {
            Some(("ss", self.settings.step_size().to_string()))
        }
        else 
        {
            None
        }
    }

    pub fn args(&self) -> Vec<String>
    {
        vec![self.widefield(), 
             self.super_res(),
             self.sigma(),
             self.pixel_size_nm(),
             self.do_registration(),
             self.registration_method(),
             self.border_size_wf_px(),
             self.optimiser_algorithm(),
             self.three_parameter_solve(),
             self.show_positive_negative(),
             self.optimiser_data(),
             self.output_directory_arg(),
             self.patchwise(),
             self.patch_size(),
             self.step_size(),
             self.n_threads()
             ].into_iter()
              .filter_map(Self::to_argument)
              .collect()
    }

    fn to_argument(arg: Option<(&str, String)>) -> Option<String>
    {
        match arg
        {
            None => None,
            Some((name, value)) => 
            {
                if value.is_empty()
                {
                    Some(name.to_string())
                }
                else 
                {
                    Some(format!("{}={}", name, value))
                }
            }
        }
    }
}

fn get_args(settings: &Settings) -> (Result<String, ExternalError>, Vec<String>)
{
    let exe = settings.location();

    (exe, settings.args())
}

pub fn run(settings: &Settings) -> Result<SquirrelResult, ExternalError>
{
    let (exe, args) = get_args(settings);
    external::run_with_output(&exe?, &args).map(|_| SquirrelResult::new(&settings.output_directory()))
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn default_flags()
    {
        let settings = Settings::new("some", "thing", 200.0, 200.0);
        let (_, args) = get_args(&&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD",  "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn sigma_can_be_set() 
    {
        let settings = Settings::new("some", "thing", 200.0, 10.1);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=10.1", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn output_directory_flag() 
    {
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_output_directory("else");
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo", "od=else"];
        assert_eq!(args, expected)
    }

    #[test]
    fn pixel_size_flag() 
    {
        let settings = Settings::new("some", "thing", 10.1, 200.0);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=10.1", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn registration_is_set_by_default() 
    {
        let settings = Settings::new("some", "thing", 200.0, 200.0);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_turn_registration_off() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_registration(false);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_set_registration_method() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_registration_method("something");
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "reg_meth=something", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_set_border_wf_px() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_border_in_wf_px(18);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=18", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn default_wf_px_border() 
    {
        let squirrel_settings = SquirrelSettings::default();
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD","wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_turn_three_parameter_solver_on() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_three_parameter_solve(true);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "3p", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_change_optimisation_algorithm() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_optimisation_algorithm("something");
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=something", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn positive_negative_off_by_default() 
    {
        let settings = Settings::new("some", "thing", 200.0, 200.0);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_turn_positive_negative_off() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_show_positive_negative(false);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }


    #[test]
    fn can_turn_positive_negative_on() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_show_positive_negative(true);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "pn", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn write_optimiser_data_flag_set() 
    {
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_write_optimiser_data(true);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo"];
        assert_eq!(args, expected)
    }

    #[test]
    fn write_optimiser_data_flag_not_set() 
    {
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_write_optimiser_data(false);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_set_patchwise_flag() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_patchwise(true);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo", "pw", "ps=32", "ss=16"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_set_patchsize() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_patchwise(true);
        squirrel_settings.set_patch_size(123);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo", "pw", "ps=123", "ss=16"];
        assert_eq!(args, expected)
    }

    #[test]
    fn can_set_step_size() 
    {
        let mut squirrel_settings = SquirrelSettings::default();
        squirrel_settings.set_patchwise(true);
        squirrel_settings.set_step_size(123);
        let settings = Settings::new("some", "thing", 200.0, 200.0).with_squirrel_settings(squirrel_settings);
        let (_, args) = get_args(&settings);
        let expected = vec!["wf=some", "sr=thing", "sigma=200", "px=200", "reg", "wfb=2", "opt=LN_NELDERMEAD", "wo", "pw", "ps=32", "ss=123"];
        assert_eq!(args, expected)
    }
}
