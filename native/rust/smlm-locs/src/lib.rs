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
}

impl<T: FitLocalisation> FitLocalisation for &T
{
    fn psf_sigma(&self) -> f64
    {
        (*self).psf_sigma()
    }
}

pub trait UncertainLocalisation : Localisation
{
    fn uncertainty(&self) -> f64;
}

impl<T: UncertainLocalisation> UncertainLocalisation for &T
{
    fn uncertainty(&self) -> f64
    {
        (*self).uncertainty()
    }
}