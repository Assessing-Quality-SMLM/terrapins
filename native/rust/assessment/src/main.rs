extern crate clap;
extern crate smlm_qa as reporting;

use std::path::PathBuf;

use reporting::{LOCALISATION_PARSE_INSTRUCTIONS, settings::
{
    DEFAULT_HAWK_PSF_SIGMA_FILTER,
    DataSettings,
    Images,
    LocalisationData,
    Localisations,
    Settings,
    Workflow
}
};

use clap::{Parser, Args, Subcommand};

#[derive(Debug, Args)]
struct LocalisationArgs 
{
    #[arg(long, help = "Localisations file")]
    locs: Option<String>,
    #[arg(long, help = LOCALISATION_PARSE_INSTRUCTIONS)]
    locs_format: Option<String>,
    #[arg(long, help = "HAWKed localisations file")]
    locs_hawk: Option<String>,
    #[arg(long, help = LOCALISATION_PARSE_INSTRUCTIONS)]
    locs_hawk_format: Option<String>
}

impl LocalisationArgs
{
    fn localisation_data(&self) -> Option<LocalisationData>
    {
        self.locs.as_ref().map(|l| LocalisationData::with(&l, self.locs_format.as_ref().map(|s| s.as_str())))
    }

    fn hawked_localisation_data(&self) -> Option<LocalisationData>
    {
        self.locs_hawk.as_ref().map(|l| 
            {
                let mut data = LocalisationData::with(&l, self.locs_hawk_format.as_ref().map(|s| s.as_str()));
                data.set_psf_sigma_filter(DEFAULT_HAWK_PSF_SIGMA_FILTER);
                data
            })
    }

    fn override_data_settings(&self, settings: &mut DataSettings, widefield: Option<String>, image_stack: Option<String>) -> ()
    {
        if widefield.is_some()
        {
            settings.set_widefield(widefield.as_ref().unwrap());
        }
        
        if image_stack.is_some()
        {
            settings.set_image_stack(image_stack.as_ref().unwrap());
        }
        
        match self.localisation_data()
        {
            None => {},
            Some(l) =>
            {
                settings.set_localisation_data(l);
            }
        }

        match self.hawked_localisation_data()
        {
            None => {},
            Some(l) => 
            {
                settings.set_hawk_localisation_data(l);
            }
        }
    }

    fn override_settings(&self, localisations: &mut Localisations, widefield: Option<String>, image_stack: Option<String>) -> Result<(), String>
    {
        self.override_data_settings(localisations.data_settings_mut(), widefield, image_stack);
        Ok(())
    }
}

#[derive(Debug, Args)]
struct ImageArgs 
{
    #[arg(long, help = "Image to use as reference")]
    reference_image: Option<String>,
    #[arg(long, help = "Image that has been generated from HAWK processing")]
    hawk_image: Option<String>,

    #[arg(long, help = "Rendering of first part of half split data")]
    half_split_a: Option<String>,
    #[arg(long, help = "Rendering of second part of half split data")]
    half_split_b: Option<String>,

    #[arg(long, help = "Rendering of first part of zip split data")]
    zip_split_a: Option<String>,
    #[arg(long, help = "Rendering of second part of zip split data")]
    zip_split_b: Option<String>,

    #[arg(long, help = "Rendering of first part of drift split data")]
    drift_split_a: Option<String>,
    #[arg(long, help = "Rendering of second part of drift split data")]
    drift_split_b: Option<String>
}

impl ImageArgs
{
    fn override_settings(&self, settings: &mut Images, widefield: Option<String>, image_stack: Option<String>) -> Result<(), String>
    {
        if widefield.is_some()
        {
            settings.set_widefield(widefield.as_ref().unwrap());
        }
        
        if image_stack.is_some()
        {
            settings.set_image_stack(image_stack.as_ref().unwrap());
        }

        match &self.reference_image
        {
            None => {},
            Some(s) => settings.set_reference_image(&s)
        }

        match &self.hawk_image
        {
            None => {},
            Some(s) => settings.set_hawk_image(&s)
        }

        match &self.half_split_a
        {
            None => {},
            Some(s) => settings.set_half_split_a_image(&s)
        }

        match &self.half_split_b
        {
            None => {},
            Some(s) => settings.set_half_split_b_image(&s)
        }

        match &self.zip_split_a
        {
            None => {},
            Some(s) => settings.set_zip_split_a_image(&s)
        }

        match &self.zip_split_b
        {
            None => {},
            Some(s) => settings.set_zip_split_b_image(&s)
        }

        match &self.drift_split_a
        {
            None => {},
            Some(s) => settings.set_drift_split_a_image(&s)
        }

        match &self.drift_split_b
        {
            None => {},
            Some(s) => settings.set_drift_split_b_image(&s)
        }
        
        Ok(())
    }
}

#[derive(Debug, Args)]
struct ExtractArgs 
{
    // #[arg(short, long, help = "Data to extract")]
    // input: String,
}

#[derive(Debug, Subcommand)]
enum Command
{
    /// Generate report based on pre-rendered images
    Image(ImageArgs),
    /// Generate report based on localisation data
    Localisation(LocalisationArgs),
    /// Extract results
    Extract(ExtractArgs)
}

impl Command
{
    fn override_settings(&self, settings: &mut Settings, widefield: Option<String>, image_stack: Option<String>) -> Result<(), String>
    {
        match self
        {
            Self::Image(i) => 
            {
                if settings.use_image_based_workflow()
                {
                    let ims = settings.images_mut().unwrap();
                    i.override_settings(ims, widefield, image_stack)
                }
                else
                {
                    let mut ims = Images::default();
                    ims.set_camera_pixel_size_nm(settings.camera_pixel_size_nm());
                    ims.set_instrument_psf_fwhm_nm(settings.instrument_psf_fwhm_nm());
                    ims.set_magnification(settings.magnification());
                    let _ = i.override_settings(&mut ims, widefield, image_stack)?;
                    settings.set_workflow(Workflow::images(ims));
                    Ok(())
                }
            }
            Self::Localisation(l) => 
            {
                if settings.use_image_based_workflow()
                {
                    let mut we = Localisations::default();
                    we.set_camera_pixel_size_nm(settings.camera_pixel_size_nm());
                    we.set_instrument_psf_fwhm_nm(settings.instrument_psf_fwhm_nm());
                    we.set_magnification(settings.magnification());
                    let _ = l.override_settings(&mut we, widefield, image_stack)?;
                    settings.set_workflow(Workflow::localisations(we));
                    Ok(())
                }
                else 
                {
                    let ab = settings.localisation_workflow_mut().unwrap();
                    let _ = l.override_settings(ab, widefield, image_stack)?;
                    Ok(())
                }
            },
            Self::Extract(_a) => 
            {
                Ok(()) 
            }
        }   
    }
}

#[derive(Parser)]
#[command(version, about, long_about = None)]
struct Arguments 
{
    #[arg(short, long, help = "input file")]
    input_file : Option<String>,

    #[arg(long, help = "write report to this file")]
    report_output : Option<String>,    

    #[arg(short, long, help = "data name")]
    data_name : Option<String>,
    #[arg(long, help = "working directory")]
    working_directory : Option<String>,

    #[arg(short, long, help = "Settings file")]
    settings: Option<String>,

    // equipment settings
    #[arg(long, help = "camera pixel size (nm)")]
    camera_pixel_size_nm: Option<String>,
    #[arg(long, help = "Instrument PSF FWHM (nm)")]
    instrument_psf_fwhm_nm: Option<String>,    

    // render settings
    #[arg(long, help = "magnification")]
    magnification: Option<f64>,
        
    #[arg(long, help = "Widefield file")]
    widefield: Option<String>,
    #[arg(long, help = "Image stack file")]
    image_stack: Option<String>,

    #[arg(long, help = "HAWKMAN: number of levels")]
    hawkman_n_levels : Option<u32>,

    #[arg(long, help = "SQUIRREL: Register images")]
    register : Option<bool>,
    #[arg(long, help = "SQUIRREL: border size to crop in wf pixels")]
    crop : Option<u32>,

    #[arg(long, help = "Number of threads to use")]
    n_threads: Option<u32>,

    #[clap(subcommand)]
    command: Command,

    #[arg(long, help = "Only generate metric file", default_value_t = false)]
    metrics_only: bool,
    #[arg(long, help = "Extract Data to directory", default_value_t = false)]
    extract: bool,
}

impl Arguments
{
    fn input_file(&self) -> Option<&str>
    {
        self.input_file.as_ref().map(|s| s.as_str())
    }

    fn data_name(&self) -> Option<&str>
    {
        self.data_name.as_ref().map(|s| s.as_str())
    }

    fn working_directory(&self) -> Option<&str>
    {
        self.working_directory.as_ref().map(|s| s.as_str())
    }

    fn settings_file(&self) -> Option<&str>
    {
        self.settings.as_ref().map(|s| s.as_str())
    }

    fn settings_provided(&self) -> bool
    {
        self.settings_file().is_some()
    }

    fn instrument_psf_fwhm_nm(&self) -> Option<Result<f64, String>>
    {
        self.instrument_psf_fwhm_nm.as_ref().map(|s| s.parse::<f64>().map_err(|e| e.to_string()))
    }

    fn camera_pixel_size_nm(&self) -> Option<Result<f64, String>>
    {
        self.camera_pixel_size_nm.as_ref().map(|s| s.parse::<f64>().map_err(|e| e.to_string()))
    }

    fn image_stack(&self) -> Option<String>
    {
        self.image_stack.clone()
    }

    fn widefield(&self) -> Option<String>
    {
        self.widefield.clone()
    }

    fn n_threads(&self) -> Option<u32>
    {
        self.n_threads.clone()
    }

    fn override_settings(&self, settings: &mut Settings) -> Result<(), String>
    {
        match self.camera_pixel_size_nm()
        {
            None => {},
            Some(v) => settings.set_camera_pixel_size_nm(v?)
        }

        match self.instrument_psf_fwhm_nm()
        {
            None => {},
            Some(v) => settings.set_instrument_psf_fwhm_nm(v?)
        }

        match &self.magnification
        {
            None => {},
            Some(v) => 
            {
                settings.set_magnification(*v)
            }
        }

        match &self.hawkman_n_levels
        {
            None => {},
            Some(v) => 
            {
                settings.set_hawkman_n_levels(*v)
            }
        }

        match &self.register
        {
            None => {},
            Some(v) => 
            {
                settings.set_squirrel_registration(*v)
            }
        }

        match &self.crop
        {
            None => {},
            Some(v) =>
            {
                settings.set_squirrel_wf_border_size(*v)
            }
        }

        match self.n_threads()
        {
            None => {},
            Some(t) => 
            {
                settings.set_n_threads(t);
            }
        }
        // println!("{:?}", settings);
        self.command.override_settings(settings, self.widefield(), self.image_stack())
    }

    fn metrics_only(&self) -> bool
    {
        self.metrics_only
    }

    fn extract_data(&self) -> bool
    {
        self.extract
    }
}


fn get_base_settings(arguments: &Arguments) -> Result<Settings, String>
{
    match arguments.settings_file()
    {
        None => Ok(Settings::default()),
        Some(s) => Settings::from_disk(s).map_err(|e| e.to_string())
    }
}

fn get_settings(arguments: &Arguments) -> Result<Settings, String>
{
    let mut settings = get_base_settings(arguments)?;
    let _ = arguments.override_settings(&mut settings)?;
    // println!("{:?}", settings);

    let wd = arguments.working_directory().map(PathBuf::from).unwrap_or(settings.output_directory());
    let data_name = arguments.data_name();
    let od = reporting::get_output_directory(wd, data_name);
    settings.set_output_directory(&od);
    // let report_output = arguments.report_output();
    // if report_output.is_some()
    // {
    //     settings.set_report_location(report_output.unwrap());
    // }
    settings.set_extract(arguments.extract_data());
    Ok(settings)
}

// fn assess_data(input_file: &str, settings: Option<&Settings>) -> Result<(), String>
// {
//     println!("Assessing data");
//     let report = reporting::assess_data(input_file, settings).map_err(|e| e.to_string())?;
//     println!("{report}");
//     Ok(())
// }

fn run_with(arguments: &Arguments) -> Result<(), String>
{
    let settings = get_settings(arguments)?;    
    match arguments.input_file()
    {
        None => 
        {
            let report = reporting::generate_metrics_and_assess(&settings).map_err(|e| e.to_string())?;
            println!("\nREPORT\n{report}");
            Ok(())
        }
        Some(input_file) => 
        {
            if arguments.extract_data()
            {
                println!("Extracting data");
                reporting::extract(input_file).map_err(|e| e.to_string())
            }
            else 
            {
                println!("NO-OP");
                Ok(())
                // if arguments.settings_provided()
                // {
                //     assess_data(input_file, Some(&settings))
                // }
                // else 
                // {
                //     assess_data(input_file, None)
                // }
            }
        }
    }
}

fn run() -> Result<(), String>
{
    let arguments = Arguments::parse();
    run_with(&arguments)
}

fn main() 
{
    match run()
    {
        Ok(_) => 
        {
            println!("Finished Ok");
            std::process::exit(0)
        }
        Err(e) => 
        {
            println!("Finished with errors: {e}");
            std::process::exit(1)
        }
    }
}


#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn can_set_n_threads() 
    {
        let arguments = vec!["assessment", "--n-threads", "10", "image"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.n_threads(), 4);
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.n_threads(), 10);
    }

    #[test]
    fn can_parse_equipment_settings_images() 
    {
        let arguments = vec!["assessment", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "image"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.camera_pixel_size_nm(), 160.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 270.0);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.camera_pixel_size_nm(), 1000.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 2000.0);
    }

    #[test]
    fn can_parse_equipment_settings_localisations() 
    {
        let arguments = vec!["assessment", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.camera_pixel_size_nm(), 160.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 270.0);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.camera_pixel_size_nm(), 1000.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 2000.0);
    }

    #[test]
    fn can_transfer_settings_from_localisations_to_images() 
    {
        let arguments = vec!["assessment", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "image"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default().with_workflow(Workflow::localisations(Localisations::default()));
        assert_eq!(settings.camera_pixel_size_nm(), 160.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 270.0);
        assert_eq!(settings.use_image_based_workflow(), false);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.use_image_based_workflow(), true);
        assert_eq!(settings.camera_pixel_size_nm(), 1000.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 2000.0);
    }

    #[test]
    fn can_transfer_settings_from_images_to_localisations() 
    {
        let arguments = vec!["assessment", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default().with_workflow(Workflow::images(Images::default()));
        assert_eq!(settings.camera_pixel_size_nm(), 160.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 270.0);
        assert_eq!(settings.use_image_based_workflow(), true);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.use_image_based_workflow(), false);
        assert_eq!(settings.camera_pixel_size_nm(), 1000.0);
        assert_eq!(settings.instrument_psf_fwhm_nm(), 2000.0);
    }

    #[test]
    fn can_set_squirrel_registration_flag_false()
    {
        let arguments = vec!["assessment", "--register=false", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.setup().squirrel_settings().registration(), true);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.setup().squirrel_settings().registration(), false);
    }

    #[test]
    fn can_set_squirrel_registration_flag_true()
    {
        let arguments = vec!["assessment", "--register=true", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        settings.set_squirrel_registration(false);
        assert_eq!(settings.setup().squirrel_settings().registration(), false);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.setup().squirrel_settings().registration(), true);
    }

    #[test]
    fn can_leave_squirrel_registration_flag_unset()
    {
        let arguments = vec!["assessment", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.setup().squirrel_settings().registration(), true);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.setup().squirrel_settings().registration(), true);
    }

    #[test]
    fn can_set_hawkman_n_levels()
    {
        let arguments = vec!["assessment", "--hawkman-n-levels=100", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.setup().hawkman_settings().n_levels(), 10);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.setup().hawkman_settings().n_levels(), 100);
    }

    #[test]
    fn can_leave_hawkman_n_levels_unset()
    {
        let arguments = vec!["assessment", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.setup().hawkman_settings().n_levels(), 10);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.setup().hawkman_settings().n_levels(), 10);
    }

    #[test]
    fn can_set_squirrel_crop_border_size()
    {
        let arguments = vec!["assessment", "--crop", "20", "--camera-pixel-size-nm", "1000", "--instrument-psf-fwhm-nm", "2000", "localisation"];
        let args = Arguments::try_parse_from(arguments).unwrap();
        let mut settings = Settings::default();
        assert_eq!(settings.setup().squirrel_settings().border_wf_px(), 2);
        
        let _ = args.override_settings(&mut settings).unwrap();
        assert_eq!(settings.setup().squirrel_settings().border_wf_px(), 20);
    }
}