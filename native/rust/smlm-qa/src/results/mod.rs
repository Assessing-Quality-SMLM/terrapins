pub use crate::tools::{hawkman::HawkmanResult, squirrel::SquirrelResult};

use frc::{Resolution as FrcResolution};

#[derive(Debug, Default)]
pub struct Results<T>
{
    mean_precision: Option<f64>,    
	frc_zip_split: Option<FrcResolution>,
    frc_half_split: Option<FrcResolution>,
    frc_drift_split: Option<FrcResolution>,
    frc_random: Vec<FrcResolution>,
    hawkman: Option<HawkmanResult<T>>,
    generated_wf_squirrel: Option<SquirrelResult>,
    true_wf_squirrel: Option<SquirrelResult>
}

impl<T> Results<T>
{
    pub fn mean_precision(&self) -> Option<f64>
    {
        self.mean_precision
    }

    pub fn add_mean_precision_results(&mut self, value: f64) -> ()
    {
        self.mean_precision = Some(value)
    }

    pub fn half_split_frc(&self) -> Option<&FrcResolution>
    {
        self.frc_half_split.as_ref()
    }

    pub fn add_frc_half_split_results(&mut self, value: FrcResolution) -> ()
    {
        self.frc_half_split = Some(value);
    }

    pub fn zip_split_frc(&self) -> Option<&FrcResolution>
    {
        self.frc_zip_split.as_ref()
    }
    
    pub fn drift_split_frc(&self) -> Option<&FrcResolution>
    {
        self.frc_drift_split.as_ref()
    }

    pub fn add_frc_zip_split_results(&mut self, value: FrcResolution) -> ()
    {
        self.frc_zip_split = Some(value);
    }

    pub fn add_frc_drift_split_results(&mut self, value: FrcResolution) -> ()
    {
        self.frc_drift_split = Some(value);
    }

    #[allow(dead_code)]
    pub fn random_frcs(&self) -> Option<&[FrcResolution]>
    {
        if self.frc_random.is_empty()
        {
            None
        }
        else 
        {
            Some(&self.frc_random)
        }
    }

    #[allow(dead_code)]
    pub fn add_frc_random_split_results(&mut self, value: Vec<FrcResolution>) -> ()
    {
        self.frc_random = value
    }

    pub fn add_frc_random_result(&mut self, value: FrcResolution) -> ()
    {
        self.frc_random.push(value);
    }

    pub fn hawkman(&self) -> Option<&HawkmanResult<T>>
    {
        self.hawkman.as_ref()
    }

    pub fn add_hawkman_results(&mut self, value: HawkmanResult<T>) -> ()
    {
        self.hawkman = Some(value)
    }

    pub fn generated_wf_squirrel(&self) -> Option<&SquirrelResult>
    {
        self.generated_wf_squirrel.as_ref()
    }

    pub fn add_generated_squirrel_results(&mut self, value: SquirrelResult) -> ()
    {
        self.generated_wf_squirrel = Some(value)
    }

    pub fn true_wf_squirrel(&self) -> Option<&SquirrelResult>
    {
        self.true_wf_squirrel.as_ref()
    }

    pub fn add_true_squirrel_results(&mut self, value: SquirrelResult) -> ()
    {
        self.true_wf_squirrel = Some(value)
    }
}