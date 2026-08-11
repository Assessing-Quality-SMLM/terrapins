pub use self::integral_renderer::{IntegralPatchRenderer};
pub use self::single_point_patch_renderer::{SinglePointPatchRenderer};

mod integral_renderer;
mod patch_utils;
mod single_point_patch_renderer;

use crate::{DataType, Properties};
use smlm_imp::OwnedImage;

pub trait RenderPolicy<L>
{
    type Output;
    type Error;
    fn render(&self, localisation: L, image: &mut OwnedImage<DataType>, properties: &Properties) -> Result<Self::Output, Self::Error>;
}