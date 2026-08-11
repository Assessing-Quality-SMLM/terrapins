pub const DEFAULT_FRAME_NUMBER : u32 = 0;
pub const DEFAULT_PSF_SIGMA : f64 = 20.0;
pub const DEFAULT_INTENSITY : f64 = 0.0;
pub const DEFAULT_UNCERTAINTY : f64 = 20.0;

pub const THUNDER_STORM: &'static str = "ts";

pub const PARSE_INSTRUCTIONS: &'static str = "ts=thunderstom, csv=n_headers;delim;x_pos;y_pos;psf_sigma_pos;uncertainty_pos;frame_number_pos. delim, x_pos and_y_pos are mandatory all others are optional -1 or empty can signify missing fields";
