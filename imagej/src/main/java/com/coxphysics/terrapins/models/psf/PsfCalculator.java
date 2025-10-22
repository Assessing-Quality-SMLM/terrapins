package com.coxphysics.terrapins.models.psf;

public class PsfCalculator
{
    private double camera_pixel_size_nm_ = 160;

    private double instrument_psf_fwhm_nm_ = 270;

    private double reconstruction_magnification_factor_ = 10;

    private PsfCalculator()
    {
    }

    public static PsfCalculator default_()
    {
        return new PsfCalculator();
    }

    public double calculate_psf()
    {
        double i_psf = instrument_psf_fwhm_nm_;
        double mf = reconstruction_magnification_factor_;
        double pixel_size = camera_pixel_size_nm_;
        return i_psf * mf / pixel_size;
    }
    public double camera_pixel_size_nm()
    {
        return camera_pixel_size_nm_;
    }

    public void set_camera_pixel_size_nm_(double camera_pixel_size)
    {
        camera_pixel_size_nm_ = camera_pixel_size;
    }

    public double instrument_psf_fwhm_nm()
    {
        return instrument_psf_fwhm_nm_;
    }

    public void set_instrument_psf_fwhm_nm(double instrument_psf_fwhm_nm)
    {
        instrument_psf_fwhm_nm_ = instrument_psf_fwhm_nm;
    }

    public double reconstruction_magnification_factor()
    {
        return reconstruction_magnification_factor_;
    }

    public void set_reconstruction_magnification_factor(double reconstruction_magnification_factor)
    {
        reconstruction_magnification_factor_ = reconstruction_magnification_factor;
    }
}
