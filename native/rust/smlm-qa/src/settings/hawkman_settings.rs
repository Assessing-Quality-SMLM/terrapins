pub const DEFAULT_START_LEVEL : u32 = 1;
pub const DEFAULT_N_LEVELS : u32 = 10;


#[derive(Debug, Clone, PartialEq)]
pub struct ThresholdSettings 
{
    threshold: f64,
    smooth: f64,
    offset: f64,
}

impl ThresholdSettings
{
    pub fn new(threshold: f64, smooth: f64, offset: f64) -> Self
    {
        Self
        {
            threshold,
            smooth,
            offset
        }
    }

    pub fn threshold(&self) -> f64
    {
    	self.threshold
    }

	pub fn smooth(&self) -> f64
	{
		self.smooth
	}

    pub fn offset(&self) -> f64
    {
    	self.offset
    }

    pub fn fwhm_default() -> Self
    {
        Self::new(0.7, 0.1, 0.04)
    }

    pub fn skeleton_default() -> Self
    {
        Self::new(0.85, 0.1, 0.02)
    }

    pub fn to_string(&self) -> String
    {
        format!("{},{},{}", self.threshold, self.smooth, self.offset)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Settings 
{
    start_level: Option<u32>,
    n_levels: Option<u32>,
    fwhm: Option<ThresholdSettings>,
    skel: Option<ThresholdSettings>,
}

impl Settings
{
    pub fn start_level(&self) -> u32
    {
        self.start_level.unwrap_or(DEFAULT_START_LEVEL)
    }

    pub fn with_start_level(mut self, value: u32) -> Self
    {
        self.start_level = Some(value);
        self
    }

    pub fn n_levels(&self) -> u32
    {
        self.n_levels.unwrap_or(DEFAULT_N_LEVELS)
    }

    pub fn set_n_levels(&mut self, value: u32)
    {
        self.n_levels = Some(value);
    }

    pub fn with_n_levels(mut self, value: u32) -> Self
    {
        self.n_levels = Some(value);
        self
    }

    pub fn fwhm(&self) -> ThresholdSettings
    {
        self.fwhm.as_ref().map(|t| t.clone()).unwrap_or(ThresholdSettings::fwhm_default())
    }

    pub fn with_fwhm(mut self, value: ThresholdSettings) -> Self
    {
        self.fwhm = Some(value);
        self
    }

    pub fn skel(&self) -> ThresholdSettings
    {
        self.skel.as_ref().map(|t| t.clone()).unwrap_or(ThresholdSettings::skeleton_default())
    }

    pub fn with_skel(mut self, value: ThresholdSettings) -> Self
    {
        self.skel = Some(value);
        self
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn correct_default_start_level() 
    {
        assert_eq!(Settings::default().start_level(), DEFAULT_START_LEVEL)
    }

    #[test]
    fn correct_default_n_levels() 
    {
        assert_eq!(Settings::default().n_levels(), DEFAULT_N_LEVELS)
    }

    #[test]
    fn can_set_n_levels() 
    {
        let mut settings = Settings::default();
        settings.set_n_levels(DEFAULT_N_LEVELS + 10);
        assert_eq!(settings.n_levels(), 20)
    }
}