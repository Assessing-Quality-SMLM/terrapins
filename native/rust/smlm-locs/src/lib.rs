#[cfg(feature = "split")]
extern crate rand;

mod allocated_localisation;
mod builder;
pub mod constants;
pub mod filters;
pub mod io;

#[cfg(feature = "split")]
pub mod split;

pub use self::allocated_localisation::{AllocatedLocalisation};
pub use self::builder::{Builder as LocalisationBuilder};

pub trait Localisation
{
    fn x(&self) -> f64;
    fn y(&self) -> f64;
}

impl<T: Localisation> Localisation for &T
{
    fn x(&self) -> f64
    {
        (*self).x()
    }

    fn y(&self) -> f64
    {
        (*self).y()
    }
}

pub trait FitLocalisation : Localisation
{
    fn psf_sigma(&self) -> f64;

    /// Whether [`Self::psf_sigma`] is a measurement. See [`is_measured_psf_sigma`].
    fn has_measured_psf_sigma(&self) -> bool
    {
        is_measured_psf_sigma(self.psf_sigma())
    }
}

impl<T: FitLocalisation> FitLocalisation for &T
{
    fn psf_sigma(&self) -> f64
    {
        (*self).psf_sigma()
    }
}

/// Whether an uncertainty value is a measurement rather than a stand-in.
///
/// Not every fitter produces one. A moment method has no residual and so no goodness of fit, and
/// a file from one carries a placeholder in the column - a zero, because ThunderSTORM's importer
/// rejects the field when it is empty, and so nearly every writer fills it with something.
///
/// A zero read as a precision claims perfect confidence: it drives the mean localisation
/// precision to zero, scores the limiting-precision assessment at zero, and gives the renderer a
/// Gaussian of zero width whose normalisation is 0/0. None of that is a property of the data, so
/// zero, negative and non-finite values are all treated as "no measurement here" and callers are
/// expected to say so rather than average them in.
pub fn is_measured_uncertainty(value: f64) -> bool
{
    value.is_finite() && value > 0.0
}

/// Whether a fitted peak width is a measurement rather than a stand-in.
///
/// The same reasoning as [`is_measured_uncertainty`], for the other quantity that gets called
/// sigma. A file with no sigma column carries [`constants::MISSING`] here.
pub fn is_measured_psf_sigma(value: f64) -> bool
{
    value.is_finite() && value > 0.0
}

pub trait UncertainLocalisation : Localisation
{
    fn uncertainty(&self) -> f64;

    /// Whether [`Self::uncertainty`] is a measurement. See [`is_measured_uncertainty`].
    fn has_measured_uncertainty(&self) -> bool
    {
        is_measured_uncertainty(self.uncertainty())
    }
}

impl<T: UncertainLocalisation> UncertainLocalisation for &T
{
    fn uncertainty(&self) -> f64
    {
        (*self).uncertainty()
    }
}