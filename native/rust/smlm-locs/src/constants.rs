pub const DEFAULT_FRAME_NUMBER : u32 = 0;
pub const DEFAULT_INTENSITY : f64 = 0.0;

/// Stands for a quantity the file did not carry.
///
/// Previously the fitted peak width and the uncertainty each defaulted to 20.0 when a file had
/// no such column. That made "absent" indistinguishable from "measured as 20 nm", which is how
/// a missing column came to be reported as a 20 nm mean precision, and - because the default
/// psf sigma filter is the exclusive range (20, 2000) - how a file with no sigma column came to
/// be filtered away in its entirety.
///
/// NaN is used rather than an `Option` on the localisation because it keeps `AllocatedLocalisation`
/// a plain `Copy` struct of scalars, which the readers and renderers pass around in bulk. Every
/// comparison against NaN is false, so nothing silently treats it as a small number; ask
/// [`crate::is_measured_psf_sigma`] or [`crate::is_measured_uncertainty`] instead of comparing.
pub const MISSING : f64 = f64::NAN;

pub const THUNDER_STORM: &'static str = "ts";

pub const PARSE_INSTRUCTIONS: &'static str = "ts=thunderstom, csv=n_headers;delim;x_pos;y_pos;psf_sigma_pos;uncertainty_pos;frame_number_pos. delim, x_pos and_y_pos are mandatory all others are optional -1 or empty can signify missing fields";
