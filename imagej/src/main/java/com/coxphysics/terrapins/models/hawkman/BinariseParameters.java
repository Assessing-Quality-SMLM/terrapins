package com.coxphysics.terrapins.models.hawkman;

public class BinariseParameters
{
    private double fwhm_threshold_; //FWHMthr = GryphonGD.FWHMthreshold;
    private double fwhm_smoothing_; //FWHMsmo = GryphonGD.FWHMsmooth;
    private double fwhm_offset_; //FWHMoff = GryphonGD.FWHMoffset;

    private double psf_size_;

    private BinariseParameters(double threshold, double smoothing, double offset)
    {
        fwhm_threshold_ = threshold;
        fwhm_smoothing_ = smoothing;
        fwhm_offset_ = offset;
    }

    public static BinariseParameters default_()
    {
        return new BinariseParameters(0.7, 0.1, 0.04);
    }

    public static BinariseParameters skeletonise()
    {
        return new BinariseParameters(0.85, 0.1, 0.02);
    }


    public int psf_size()
    {
        return (int)Math.ceil(psf_size_);
    }

    public double fwhm_threshold()
    {
        return fwhm_threshold_;
    }

    public double fwhm_smoothing()
    {
        return fwhm_smoothing_;
    }

    public double fwhm_offset()
    {
        return fwhm_offset_;
    }

    public void set_psf_size(double value)
    {
        psf_size_ = value;
    }

    public void set_fwhm_threshold(double value)
    {
        fwhm_threshold_ = value;
    }

    public void set_fwhm_smoothing(double value)
    {
        fwhm_smoothing_ = value;
    }

    public void set_fwhm_offset(double value)
    {
        fwhm_offset_ = value;
    }

    public double threshold_value(float threshold_image_value, float half_psf_value)
    {
        return util.calculate_binary_threshold(fwhm_threshold_, fwhm_smoothing_, fwhm_offset_, threshold_image_value, half_psf_value);
    }
}
