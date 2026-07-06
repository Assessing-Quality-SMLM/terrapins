use crate::Error;
use crate::frc;
use crate::resolution::{Resolution};
use crate::loess;

use loess::Options as LoessOptions;

use crate::thresholds::{Threshold, Thresholder, OneSeventh, HalfBit, Sigma};

use na::DVector;

pub struct FrcResult
{
    numerator: Vec<f64>,
    denom: Vec<f64>,
    frcs: Vec<f64>,
    n_pixels: Vec<usize> // number of pixels in each ring
}

impl FrcResult
{
    /// size runs from 1 to max radius, so = max radius - 1
    pub fn new(size: usize) -> Self
    {
        Self
        {
            numerator : vec![0.0; size],
            denom : vec![0.0; size],
            frcs : vec![0.0; size],
            n_pixels: vec![0; size]
        }
    }

    fn size(&self) -> usize
    {
        self.numerator.len()
    }

    pub fn set_value(&mut self, idx: usize, numerator: f64, denom: f64, n_pixels: usize) -> f64
    {
        let frc = numerator / denom;
        self.numerator[idx] = numerator;
        // self.numerator[idx] = numerator / n_pixels as f64; GDSC scales by number of pixels in ring
        self.denom[idx] = denom;
        // self.denom[idx] = denom / n_pixels as f64; GDSC scales by number of pixels in ring
        self.frcs[idx] = frc;
        self.n_pixels[idx] = n_pixels;
        frc
    }

    pub fn n_pixels(&self) -> &[usize]
    {
        &self.n_pixels
    }

    /// spatial frequencies
    #[allow(non_snake_case)]
    pub fn q_s(&self, L: f64) -> Vec<f64>
    {
        (1..(self.size() + 1)).map(|idx| frc::calculate_q(idx, L))
                              .collect()
    }

    pub fn numerator_values(&self) -> &[f64]
    {
        &self.numerator
    }

    pub fn frcs(&self) -> &[f64]
    {
        &self.frcs
    }

    fn default_loess_options(&self) -> LoessOptions
    {
        LoessOptions::new(7, 1)
    }

    pub fn smoothed_frcs(&self) -> Vec<f64>
    {
        self.smoothed_frcs_with(&self.default_loess_options())
    }

    pub fn smoothed_frcs_with(&self, options: &LoessOptions) -> Vec<f64>
    {
        let x = DVector::<f64>::from_vec((1..self.frcs().len() + 1).map(|x| x as f64).collect());
        let frc_values = DVector::<f64>::from_vec(self.frcs().to_vec());
        let l = loess::Loess::new(&x, &frc_values);
        let mut smoothed = Vec::with_capacity(self.frcs().len());
        for &x in x.iter() 
        {
            let y = l.estimate(x, options.window(), true, options.degree());
            smoothed.push(y);
        }
        smoothed
    }

    pub fn corrected_frcs(&self, correction_factor: &[f64]) -> Self
    {
        let mut new_result = Self::new(self.numerator.len());
        for idx in 0..self.numerator.len()
        {
            let factor = correction_factor[idx];
            // println!("correction[{idx}]: {factor}");
            new_result.set_value(idx, self.numerator[idx] - factor, self.denom[idx] + factor, self.n_pixels[idx]);
        }
        new_result
    }

    #[allow(non_snake_case)]
    pub fn get_resolution(&self, L: f64, thresholder: Threshold) -> Resolution
    {
        let loess_options = self.default_loess_options();
        match thresholder
        {
            Threshold::OneSeventh => self.get_resolution_with(L, &loess_options, OneSeventh),
            Threshold::HalfBit    => self.get_resolution_with(L, &loess_options, HalfBit::new()),
            Threshold::Sigma(sigma) => self.get_resolution_with(L, &loess_options, Sigma::new(sigma))
        }    
    }

    #[allow(non_snake_case)]
    fn get_resolution_with<T: Thresholder<Error=Error>>(&self, L: f64, options: &LoessOptions, thresholder: T) -> Resolution
    {
        let frcs = self.smoothed_frcs_with(options);
        let threshold_curve = frc::get_threshold_curve(self.n_pixels(), thresholder);
        let qs = self.q_s(L);
        Resolution::from(qs, frcs, threshold_curve)
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    fn get_test_result() -> FrcResult
    {
        let mut result = FrcResult::new(10);
        result.set_value(0, 1.0, 2.0, 1);
        result.set_value(1, 1.0, 2.0, 2);
        result.set_value(2, 1.0, 2.0, 3);
        result.set_value(3, 1.0, 2.0, 4);
        result.set_value(4, 1.0, 2.0, 5);
        result.set_value(5, 1.0, 2.0, 6);
        result.set_value(6, 1.0, 2.0, 7);
        result.set_value(7, 1.0, 2.0, 8);
        result.set_value(8, 1.0, 2.0, 9);
        result.set_value(9, 1.0, 2.0, 10);
        result
    }

    #[test]
    fn qs_1_based() 
    {
        let result = FrcResult::new(10);
        #[allow(non_snake_case)]
        let L = 80.0;
        let qs = result.q_s(L);
        let expected = [1.0 / L, 2.0 / L, 3.0 / L, 4.0 / L , 5.0 / L, 6.0 / L, 7.0 / L, 8.0 / L, 9.0 / L, 10.0 / L];
        assert_eq!(qs.len(), 10);
        assert_eq!(qs, expected)
    }

    #[test]
    fn numerators() 
    {
        let result = get_test_result();
        assert_eq!(result.numerator_values(), [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0])
    }

    #[test]
    fn ring_sizes() 
    {
        let result = get_test_result();
        assert_eq!(result.n_pixels(), [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);
    }

    #[test]
    fn frcs_applies_simple_division() 
    {
        let result = get_test_result();
        assert_eq!(result.frcs(), [0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,]);
    }

    #[test]
    fn frcs_corrections() 
    {
        let result = get_test_result();
        let corrections = [0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5];
        assert_eq!(result.corrected_frcs(&corrections).frcs(), [0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2]);
    }

    #[test]
    fn resolution() 
    {
        let mut result = FrcResult::new(10);
        result.set_value(0, 1.0, 2.0, 1);
        result.set_value(1, 1.0, 7.0, 2);
        result.set_value(2, 1.0, 7.0, 3);
        result.set_value(3, 1.0, 7.0, 4);
        result.set_value(4, 1.0, 7.0, 5); // crossing here
        result.set_value(5, 1.0, 7.0, 6);
        result.set_value(6, 1.0, 7.0, 7);
        result.set_value(7, 1.0, 7.0, 8);
        result.set_value(8, 1.0, 7.0, 9);
        result.set_value(9, 1.0, 7.0, 10);
        #[allow(non_snake_case)]
        let L = 80.0;
        assert_eq!(result.get_resolution(L, Threshold::OneSeventh).as_value(), 1.0 / (5.0 / L));
    }
}