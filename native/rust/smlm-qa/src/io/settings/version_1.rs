use crate::settings::
{
	Settings, 
	Workflow as RealWorkflow, 
	Images as RealImages, 
	Localisations as RealLocalisations, 
	FRCData as RealFRCData, 
	LocalisationData as RealLocalisationData, 
	DataSettings as RealDataSettings, 
	Setup as RealSetup, 
	EquipmentSettings as RealEquipmentSettings, 
	ReconSettings as RealReconSettings, 
	FrcSettings as RealFrcSettings, 
	HawkmanSettings as RealHawkmanSettings, 
	HawkmanThresholdSettings as RealThresholdSettings, 
	SquirrelSettings as RealSquirrelSettings,
	DEFAULT_N_THREADS, 
	ReportSettings as RealReportSettings,
	report::{
		AssessmentSettings as RealAssessmentSettings,
		RatioSettings as RealRatioReportSettings ,
		BlinkingSettings as RealBlinkingReportSettings ,
		SamplingSettings as RealSamplingReportSettings,
		BiasSettings as RealBiasReportSettings,
		SquirrelSettings as RealSquirrelReportSettings}
};

use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
struct LocalisationData
{
	filename: String,
	format: Option<String>,
	psf_sigma_filter : Option<(f64, f64)>,
	uncertainty_filter: Option<(f64, f64)>
}

impl LocalisationData
{
	pub fn to_localisation_data(&self) -> RealLocalisationData
	{
		RealLocalisationData::new(&self.filename, self.format.clone(), self.psf_sigma_filter, self.uncertainty_filter)
	}
}

impl From<&RealLocalisationData> for LocalisationData
{
	fn from(localisation_data: &RealLocalisationData) -> Self
	{
		Self
		{
			filename : localisation_data.filepath().to_string(),
			format: localisation_data.format().map(|s| s.to_string()),
			psf_sigma_filter: localisation_data.psf_sigma_filter(),
			uncertainty_filter: localisation_data.uncertainty_filter()
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct FRCData
{
	a: String,
	b: String
}

impl FRCData
{
	pub fn to_frc_data(&self) -> RealFRCData
	{
		RealFRCData::new(&self.a, &self.b)
	}
}

impl From<&RealFRCData> for FRCData
{
	fn from(frc_data: &RealFRCData) -> Self
	{
		Self
		{
			a : frc_data.image_a().to_string(),
			b: frc_data.image_b().to_string()
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct DataSettings
{
	// organised in acquisition order
	widefield: Option<String>,
	image_stack : Option<String>,
	
	localisations: Option<LocalisationData>,
	hawked_localisations: Option<LocalisationData>,
}

impl DataSettings
{
	fn to_data_settings(&self) -> RealDataSettings
	{		
		let mut core = RealDataSettings::default();

		match &self.widefield
        {
        	None => {},
        	Some(s) => 
        	{
        		core = core.with_widefield(&s);
        	}
        }

		match &self.image_stack
		{
			None => {},
			Some(image_stack) => 
			{
				core = core.with_image_stack(&image_stack)
			}
		}

		match self.localisations.as_ref().map(|l| l.to_localisation_data())
		{
			None => {},
			Some(l) => 
			{
				core = core.with_localisation_data(l);
			}
		}

		match self.hawked_localisations.as_ref().map(|l| l.to_localisation_data())
		{
			None => {},
			Some(l) =>
			{
				core = core.with_hawk_localisation_data(l);
			}
		}
        core
	}
}

impl From<&RealDataSettings> for DataSettings
{
	fn from(settings: &RealDataSettings) -> Self
	{
		Self
		{
			widefield: settings.widefield().map(|s| s.to_string()),
			image_stack : settings.image_stack().map(|s| s.to_string()),

			localisations: settings.localisation_data().map(LocalisationData::from),
			hawked_localisations: settings.hawk_localisation_data().map(LocalisationData::from),
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct EquipmentSettings 
{
	instrument_psf_fwhm_nm: f64,
	camera_pixel_size_nm: f64
}

impl EquipmentSettings
{
	fn to_equipment_settings(&self) -> RealEquipmentSettings
	{
		RealEquipmentSettings::new(self.instrument_psf_fwhm_nm, self.camera_pixel_size_nm)
	}
}

impl From<&RealEquipmentSettings> for EquipmentSettings
{
	fn from(settings: &RealEquipmentSettings) -> Self
	{
		Self
		{
			instrument_psf_fwhm_nm: settings.instrument_psf_fwhm_nm(),
			camera_pixel_size_nm: settings.camera_pixel_size_nm()
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct ReconSettings 
{
	sigma_scale: f64,
    magnification: f64,
    global_position_nm: Option<String>,
}

impl ReconSettings
{
	fn to_recon_settings(&self) -> RealReconSettings
	{
		let settings = RealReconSettings::new(self.sigma_scale, self.magnification);
		match &self.global_position_nm
		{
			None => settings,
			Some(f) => settings.with_global_frame(&f)
		}
	}
}

impl From<&RealReconSettings> for ReconSettings
{
	fn from(settings: &RealReconSettings) -> Self
	{
		Self
		{
			sigma_scale: settings.sigma_scale(),
			magnification: settings.magnification(),
			global_position_nm: settings.global_frame().map(|s| s.to_string())
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
pub struct FrcSettings
{
	threshold: Option<String>,
	filter: Option<(String, Vec<f64>)>,
	n_random: Option<usize>,
	drift_block_size: Option<usize>,
}

impl FrcSettings
{
	pub fn to_frc_settings(&self) -> RealFrcSettings
	{
		let threshold = self.threshold.as_ref().map(|s| s.as_str());
		let filter = self.filter.as_ref().map(|(s, p)| (s.as_str(), p.clone()));		
		let n_random = self.n_random.unwrap_or(RealFrcSettings::DEFAULT_N_RANDOM_SPLITS);
		let drift_block_size = self.drift_block_size.unwrap_or(RealFrcSettings::DEFAULT_DRIFT_BLOCK_SIZE);
		RealFrcSettings::new(threshold, filter, n_random, drift_block_size)
	}
}

impl From<&RealFrcSettings> for FrcSettings
{
	fn from(settings: &RealFrcSettings) -> Self
	{
		let threshold = settings.threshold_str().map(|s| s.to_string());
		let filter = settings.filter().map(|(s, p)| (s.to_string(), p.to_vec()));
		let n_random = Some(settings.n_random_splits());
		let drift_block_size = Some(settings.drift_block_size());
		Self
		{
			threshold,
			filter,
			n_random: n_random,
			drift_block_size: drift_block_size
		}
	}
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ThresholdSettings
{
	threshold: f64,
    smooth: f64,
    offset: f64,
}

impl ThresholdSettings
{
	pub fn to_threshold_settings(&self) -> RealThresholdSettings
	{
		RealThresholdSettings::new(self.threshold, self.smooth, self.offset)
	}
}

impl From<&RealThresholdSettings> for ThresholdSettings
{
	fn from(settings: &RealThresholdSettings) -> Self
	{
		Self
		{
			threshold: settings.threshold(),
			smooth: settings.smooth(),
			offset: settings.offset(),
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
pub struct HawkmanSettings
{
	start_level : Option<u32>,
	n_levels : Option<u32>,
	fwhm: Option<ThresholdSettings>,
	skel: Option<ThresholdSettings>
}

impl HawkmanSettings
{
	pub fn to_hawkman_settings(&self) -> RealHawkmanSettings
	{
		let mut settings = RealHawkmanSettings::default();
		if self.start_level.is_some()
		{
			settings = settings.with_start_level(self.start_level.unwrap());
		}
		if self.n_levels.is_some()
		{
			settings = settings.with_n_levels(self.n_levels.unwrap());
		}
		if self.fwhm.is_some()
		{
			settings = settings.with_fwhm(self.fwhm.as_ref().unwrap().to_threshold_settings());
		}
		if self.skel.is_some()
		{
			settings = settings.with_skel(self.skel.as_ref().unwrap().to_threshold_settings());
		}
		settings
	}
}

impl From<&RealHawkmanSettings> for HawkmanSettings
{
	fn from(settings: &RealHawkmanSettings) -> Self
	{
		Self
		{
			start_level: Some(settings.start_level()),
			n_levels: Some(settings.n_levels()),
			fwhm: Some(ThresholdSettings::from(&settings.fwhm())),
			skel: Some(ThresholdSettings::from(&settings.skel()))
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SquirrelSettings
{
	registration : Option<bool>,
	registration_method: Option<String>,
	border_size_wf_px: Option<u32>,
	optimiser_algorithm: Option<String>,
	three_parameter_solver: Option<bool>,
	show_positive_negative: Option<bool>,
	patchwise : Option<bool>,
	patch_size : Option<u8>,
	step_size : Option<u8>,
}

impl SquirrelSettings
{
	pub fn to_squirrel_settings(&self) -> RealSquirrelSettings
	{
		let mut settings = RealSquirrelSettings::default();
		
		if self.border_size_wf_px.is_some()
		{
			settings.set_border_in_wf_px(self.border_size_wf_px.unwrap());
		}
		
		if self.registration.is_some()
		{
			settings.set_registration(self.registration.unwrap())
		}

		if self.registration_method.is_some()
		{
			settings.set_registration_method(self.registration_method.as_ref().unwrap())
		}

		if self.three_parameter_solver.is_some()
		{
			settings.set_three_parameter_solve(self.three_parameter_solver.unwrap());
		}
		
		if self.optimiser_algorithm.is_some()
		{
			settings.set_optimisation_algorithm(&self.optimiser_algorithm.as_ref().unwrap());
		}

		if self.show_positive_negative.is_some()
		{
			settings.set_show_positive_negative(self.show_positive_negative.unwrap());
		}

		if self.patchwise.is_some()
		{
			settings.set_patchwise(self.patchwise.unwrap());
		}

		if self.patch_size.is_some()
		{
			settings.set_patch_size(self.patch_size.unwrap());
		}

		if self.step_size.is_some()
		{
			settings.set_step_size(self.step_size.unwrap());
		}

		settings
	}
}

impl From<&RealSquirrelSettings> for SquirrelSettings
{
	fn from(settings: &RealSquirrelSettings) -> Self
	{
		Self
		{
			registration: Some(settings.registration()),
			registration_method: settings.registration_method().map(|s| s.to_string()),
			border_size_wf_px: Some(settings.border_wf_px()),
			optimiser_algorithm: Some(settings.optimiser_algorithm().to_string()),
			three_parameter_solver: Some(settings.three_parameter_solve()),
			show_positive_negative: Some(settings.show_positive_negative()),
			patchwise : Some(settings.patchwise()),
			patch_size : Some(settings.patch_size()),
			step_size : Some(settings.step_size()),
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct Setup
{
	// equipment: Option<EquipmentSettings>,
	// recon: Option<ReconSettings>,
	frc: Option<FrcSettings>,
	hawkman: Option<HawkmanSettings>,
	squirrel: Option<SquirrelSettings>,
	n_threads: Option<u32>,
}

impl Setup
{
	fn from(setup: &RealSetup) -> Self
	{
		Self
		{
			// equipment: Some(EquipmentSettings::from(setup.equipment_settings())),
			// recon: Some(ReconSettings::from(setup.recon_settings())),
			frc: Some(FrcSettings::from(setup.frc_settings())),
			hawkman : Some(HawkmanSettings::from(setup.hawkman_settings())),
			squirrel: Some(SquirrelSettings::from(setup.squirrel_settings())),
			n_threads: Some(setup.n_threads())
		}
	}

	// fn equipment_settings(&self) -> RealEquipmentSettings
	// {
	// 	self.equipment.as_ref().map(|e| e.to_equipment_settings()).unwrap_or_default()
	// }

	// fn recon_settings(&self) -> RealReconSettings
	// {
	// 	self.recon.as_ref().map(|r| r.to_recon_settings()).unwrap_or_default()
	// }

	fn frc_settings(&self) -> RealFrcSettings
	{
		self.frc.as_ref().map(|f| f.to_frc_settings()).unwrap_or_default()
	}

	fn hawkman_settings(&self) -> RealHawkmanSettings
	{
		self.hawkman.as_ref().map(|h| h.to_hawkman_settings()).unwrap_or_default()
	}

	fn squirrel_settings(&self) -> RealSquirrelSettings
	{
		self.squirrel.as_ref().map(|s| s.to_squirrel_settings()).unwrap_or_default()
	}

	fn n_threads(&self) -> u32
	{
		self.n_threads.unwrap_or(DEFAULT_N_THREADS)
	}

	pub fn to_setup(&self) -> RealSetup
	{
		// let equipment_settings = self.equipment_settings();
		// let recon_settings = self.recon_settings();
		let frc_settings = self.frc_settings();
		let hawkman_settings = self.hawkman_settings();
		let squirrel_settings = self.squirrel_settings();
		RealSetup::default().with_frc_settings(frc_settings)
							// .with_recon_settings(recon_settings)
							// .with_equipment_settings(equipment_settings)
							.with_hawkman_settings(hawkman_settings)
							.with_squirrel_settings(squirrel_settings)
							.with_n_threads(self.n_threads())
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct AssessmentSettings
{
	thresholds: [f64; 2],
	labels: [String; 3],
}

impl AssessmentSettings
{
	fn to_real(&self) -> RealAssessmentSettings
	{
		RealAssessmentSettings::new(self.thresholds.clone(), self.labels.clone())
	}
}

impl From<&RealAssessmentSettings> for AssessmentSettings
{
	fn from(settings: &RealAssessmentSettings) -> Self
	{
		Self
		{
			thresholds: settings.thresholds().clone(), 
			labels: settings.labels().clone()
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct RatioSettings
{
	lower: f64,
	upper: f64
}

impl RatioSettings
{
	fn to_real(&self) -> RealRatioReportSettings
	{
		RealRatioReportSettings::new(self.lower, self.upper)
	}
}

impl From<&RealRatioReportSettings> for RatioSettings
{
	fn from(settings: &RealRatioReportSettings) -> Self
	{
		Self{upper: settings.upper(), lower: settings.lower()}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct BlinkingReportSettings
{
	value: f64,
	assessment: AssessmentSettings
}

impl BlinkingReportSettings
{
	fn to_real(&self) -> RealBlinkingReportSettings
	{
		RealBlinkingReportSettings::new(self.value, self.assessment.to_real())
	}
}

impl From<&RealBlinkingReportSettings> for BlinkingReportSettings
{
	fn from(settings: &RealBlinkingReportSettings) -> Self
	{
		Self
		{
			value: settings.target(), 
			assessment: AssessmentSettings::from(settings.assessment_settings())
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct SamplingReportSettings
{
	ratio: RatioSettings,
	assessment: AssessmentSettings
}

impl SamplingReportSettings
{
	fn to_real(&self) -> RealSamplingReportSettings
	{
		RealSamplingReportSettings::new(self.ratio.to_real(), self.assessment.to_real())
	}
}

impl From<&RealSamplingReportSettings> for SamplingReportSettings
{
	fn from(settings: &RealSamplingReportSettings) -> Self
	{
		Self
		{
			ratio: RatioSettings::from(settings.ratio()),
			assessment: AssessmentSettings::from(settings.assessment())
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct BiasReportSettings
{
	global_score_threshold: f64,
	assessment_settings: AssessmentSettings,
}

impl BiasReportSettings
{
	fn to_real(&self) -> RealBiasReportSettings
	{
		RealBiasReportSettings::new(self.global_score_threshold, self.assessment_settings.to_real())
	}
}

impl From<&RealBiasReportSettings> for BiasReportSettings
{
	fn from(settings: &RealBiasReportSettings) -> Self
	{
		Self
		{
			global_score_threshold: settings.threshold(), 
			assessment_settings: AssessmentSettings::from(settings.assessment_settings())
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct SquirrelReportSettings
{
	assessment_settings: AssessmentSettings
}

impl SquirrelReportSettings
{
	fn to_real(&self) -> RealSquirrelReportSettings
	{
		RealSquirrelReportSettings::new(self.assessment_settings.to_real())
	}
}

impl From<&RealSquirrelReportSettings> for SquirrelReportSettings
{
	fn from(settings: &RealSquirrelReportSettings) -> Self
	{
		Self
		{
			assessment_settings: AssessmentSettings::from(settings.assessment_settings())
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct ReportSettings
{
	drift_settings: AssessmentSettings,
	sampling_settings: SamplingReportSettings,
	magnification: AssessmentSettings,
	blinking: BlinkingReportSettings,
	bias: BiasReportSettings,
	squirrel: SquirrelReportSettings,
	localisation_precision: AssessmentSettings,
	limiting_resolution: AssessmentSettings,
}

impl ReportSettings
{
	pub fn from(report_settings : &RealReportSettings) -> Self
	{
		Self
		{
			drift_settings: AssessmentSettings::from(report_settings.drift_settings()),
			sampling_settings: SamplingReportSettings::from(report_settings.sampling_settings()),
			magnification: AssessmentSettings::from(report_settings.magnification()),
			blinking: BlinkingReportSettings::from(report_settings.blinking()),
			bias: BiasReportSettings::from(report_settings.bias()),
			squirrel: SquirrelReportSettings::from(report_settings.squirrel()),
			localisation_precision: AssessmentSettings::from(report_settings.localisation_precision_settings()),
			limiting_resolution: AssessmentSettings::from(report_settings.limiting_resolution_settings())
		}
	}

	pub fn to_report_settings(&self) -> RealReportSettings
	{
		RealReportSettings::default().with_drift_settings(self.drift_settings.to_real())
									 .with_sampling_settings(self.sampling_settings.to_real())
									 .with_magnification(self.magnification.to_real())
									 .with_blinking_settings(self.blinking.to_real())
									 .with_bias(self.bias.to_real())
									 .with_squirrel(self.squirrel.to_real())
									 .with_localisation_precision(self.localisation_precision.to_real())
									 .with_limiting_resolution(self.limiting_resolution.to_real())

	}
}

#[derive(Debug, Serialize, Deserialize)]
struct Localisations
{
	data: Option<DataSettings>,
	equipment: Option<EquipmentSettings>,
	recon_settings: Option<ReconSettings>,
}

impl Localisations
{
	fn data_settings(&self) -> RealDataSettings
	{
		self.data.as_ref().map(|d| d.to_data_settings()).unwrap_or_default()
	}

	fn equipment_settings(&self) -> RealEquipmentSettings
	{
		self.equipment.as_ref().map(|e| e.to_equipment_settings()).unwrap_or_default()
	}

	fn recon_settings(&self) -> RealReconSettings
	{
		self.recon_settings.as_ref().map(|r| r.to_recon_settings()).unwrap_or_default()
	}

	pub fn to_localisations(&self) -> RealLocalisations
	{
		RealLocalisations::from_data(self.data_settings()).with_equipment_settings(self.equipment_settings())
												   		  .with_recon_settings(self.recon_settings())
	}
}

impl From<&RealLocalisations> for Localisations
{
	fn from(localisations: &RealLocalisations) -> Self
	{
		Self
		{
			data: Some(DataSettings::from(localisations.data_settings())),
			equipment: Some(EquipmentSettings::from(localisations.equipment_settings())),
			recon_settings: Some(ReconSettings::from(localisations.recon_settings()))
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
struct Images
{
	widefield: Option<String>,
	image_stack: Option<String>,

	reference_image: Option<String>,
	hawk_image: Option<String>,	

	half_split: Option<FRCData>,
	zip_split: Option<FRCData>,
	drift_split: Option<FRCData>,

	equipment: Option<EquipmentSettings>,
	magnification: Option<f64>,
}

impl Images
{
	fn to_images(&self) -> RealImages
	{
		let mut images = RealImages::default();
		
		if let Some(w) = self.widefield.as_ref().map(|s| s.as_str())
		{
        	images = images.with_widefield(w);
    	}
    	
    	if let Some(i) = self.image_stack.as_ref().map(|s| s.as_str())
		{
        	images = images.with_image_stack(i);
    	}
    	
    	if let Some(r) = self.reference_image.as_ref().map(|s| s.as_str())
		{
        	images = images.with_reference_image(r);
    	}
    	
    	if let Some(h) = self.hawk_image.as_ref().map(|s| s.as_str())
		{
        	images = images.with_hawk_image(h);
    	}
    	
    	if let Some(h) = self.half_split.as_ref()
		{
        	images = images.with_half_split(h.to_frc_data());
    	}

    	if let Some(z) = self.zip_split.as_ref()
		{
        	images = images.with_zip_split(z.to_frc_data());
    	}

    	if let Some(d) = self.drift_split.as_ref()
		{
        	images = images.with_drift_split(d.to_frc_data());
    	}


    	if let Some(e) = self.equipment.as_ref().map(|e| e.to_equipment_settings())
		{
        	images = images.with_equipment_settings(e);
    	}

    	if let Some(p) = self.magnification.as_ref()
		{
        	images = images.with_magnification(*p);
    	}

		images
	}
}

impl From<&RealImages> for Images
{
	fn from(images: &RealImages) -> Self
	{
		Self
		{
			widefield: images.widefield().map(|s| s.to_string()),
			image_stack: images.image_stack().map(|s| s.to_string()),

			reference_image: images.reference_image().map(|s| s.to_string()),
			hawk_image: images.hawk_image().map(|s| s.to_string()),

			half_split: images.half_split_data().map(FRCData::from),
			zip_split: images.zip_split_data().map(FRCData::from),
			drift_split: images.drift_split_data().map(FRCData::from),

			equipment: Some(EquipmentSettings::from(images.equipment_settings())),
			magnification: Some(images.magnification()),
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
enum Workflow 
{
	Image(Images),
	Localisation(Localisations)
}

impl Workflow
{
	fn to_workflow(&self) -> RealWorkflow
	{
		match self
		{
			Self::Image(images) => RealWorkflow::images(images.to_images()),
			Self::Localisation(localisations) => RealWorkflow::localisations(localisations.to_localisations())
		}
	}
}

impl From<&RealWorkflow> for Workflow
{
	fn from(workflow: &RealWorkflow) -> Self
	{
		match workflow
		{
			RealWorkflow::Images(i) => Self::Image(Images::from(i)),
			RealWorkflow::Localisations(l) => Self::Localisation(Localisations::from(l))
		}
	}
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Version1 
{
	// output_directory: Option<String>,
	workflow: Option<Workflow>,
	setup: Option<Setup>,
	report: Option<ReportSettings>	
}

impl Version1
{
	fn workflow(&self) -> RealWorkflow
	{
		self.workflow.as_ref().map(|w| w.to_workflow()).unwrap_or_default()
	}

	fn setup(&self) -> RealSetup
	{
		self.setup.as_ref().map(|s| s.to_setup()).unwrap_or_default()
	}

	fn report_settings(&self) -> RealReportSettings
	{
		self.report.as_ref().map(|r| r.to_report_settings()).unwrap_or_default()
	}

	pub fn to_settings(&self) -> Settings
	{
		let workflow = self.workflow();
		let setup = self.setup();
		let report_settings = self.report_settings();
		let settings = Settings::from_workflow(workflow).with_setup(setup).with_report_settings(report_settings);
		// let settings = Settings::from_workflow(RealWorkflow::default());
		settings
		// match self.output_directory.as_ref()
		// {
		// 	None => settings,
		// 	Some(od) => settings.with_output_directory(od)
		// }
	}
}

impl From<&Settings> for Version1
{
	fn from(settings: &Settings) -> Self
	{
		Self
		{
			// output_directory: Some(settings.output_directory()),
			workflow: Some(Workflow::from(settings.workflow())),
			setup: Some(Setup::from(settings.setup())),
			report: Some(ReportSettings::from(settings.report_settings()))
		}
	}
}

#[cfg(test)]
mod tests 
{
	use super::*;

	#[test]
	fn basic_parse_test() 
	{
		let data = r#"{"output_directory":".","equipment":{"instrument_psf_fwhm_nm":270.0,"camera_pixel_size_nm":160.0},"recon":{"sigma_scale":3.0,"magnification":10},"frc":{"threshold":"17","filter":["tukey", [0.25]]},"n_threads":4}"#;
		let v1 : Version1 = serde_json::from_str(data).unwrap();
		let expected = Settings::from_data(RealDataSettings::default());
		assert_eq!(v1.to_settings(), expected)
	}

	#[test]
	fn can_parse_localisation_widefield() 
	{
		let data = r#"
{
    "output_directory": ".",
    "workflow":
    {
        "Localisation":
        {
            "data":{"widefield": "something"}
        }
    }
}
"#;
		let v1 : Version1 = serde_json::from_str(data).unwrap();		
		let expected = Settings::from_data(RealDataSettings::default().with_widefield("something"));
		assert_eq!(v1.to_settings(), expected)
	}

	#[test]
	fn can_parse_image_widefield() 
	{
		let data = r#"
{
    "output_directory": ".",
    "workflow":
    {
        "Image":
        {
            "widefield": "something"
        }
    }
}
"#;
		let v1 : Version1 = serde_json::from_str(data).unwrap();		
		let expected = Settings::from_workflow(RealWorkflow::Images(RealImages::default().with_widefield("something")));
		assert_eq!(v1.to_settings(), expected)
	}

	#[test]
	fn empty_file_leads_to_default_settings() 
	{		
		let data = r#"{}"#;
		let v1 : Version1 = serde_json::from_str(data).unwrap();
		let expected = Settings::from_data(RealDataSettings::default());
		assert_eq!(v1.to_settings(), expected)
	}
}