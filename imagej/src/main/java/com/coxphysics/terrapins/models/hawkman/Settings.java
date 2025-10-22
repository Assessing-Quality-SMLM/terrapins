package com.coxphysics.terrapins.models.hawkman;

public class Settings
{
    private double psf_size_ = 13;

    private String test_name_;

    private String ref_name_;

    private final BinariseParameters binarise_parameters_ = BinariseParameters.default_();

    private final BinariseParameters skeletonise_parameters_ = BinariseParameters.skeletonise();

    boolean flatten_images_ = true;

    private int max_scale_ = 10;

    private boolean blur_skeletons_ = true;

    private DilateErode.Method dilate_erode_method_ = DilateErode.Method.A;



    // show the test results in output.display()
    private Boolean diagnose_;

    private Settings()
    {
    }

    public static Settings default_()
    {
        return new Settings();
    }

    public int psf_size()
    {
        return (int)Math.ceil(psf_size_);
    }

    public void set_psf_size(int psf_size)
    {
        psf_size_ = psf_size;
    }

    public int max_scale()
    {
        return max_scale_;
    }

    public void set_max_scale(int max_scale)
    {
        max_scale_ = max_scale;
    }

    public String test_name()
    {
        return test_name_;
    }

    public void set_test_name(String test_name)
    {
        test_name_ = test_name;
    }

    public String ref_name()
    {
        return ref_name_;
    }

    public void set_ref_name(String ref_name)
    {
        ref_name_ = ref_name;
    }

    public DilateErode.Method dilate_erode_method()
    {
        return dilate_erode_method_;
    }


    public boolean blur_skeletons()
    {
        return blur_skeletons_;
    }

    public BinariseParameters binarisation_parameters()
    {
        return binarise_parameters_;
    }

    public BinariseParameters skeletonise_parameters(){return skeletonise_parameters_;}

    public Boolean diagnonse()
    {
        return diagnose_;
    }

    public boolean flatten_images()
    {
        return false;
    }


    public double fwhm_threshold()
    {
        return binarise_parameters_.fwhm_threshold();
    }
    public void set_fwhm_threshold(double value)
    {
        binarise_parameters_.set_fwhm_threshold(value);
    }

    public double fwhm_smoothing()
    {
        return binarise_parameters_.fwhm_smoothing();
    }

    public void set_fwhm_smoothing(double value)
    {
        binarise_parameters_.set_fwhm_smoothing(value);
    }

    public double fwhm_offset()
    {
        return binarise_parameters_.fwhm_offset();
    }

    public void set_fwhm_offset(double value)
    {
        binarise_parameters_.set_fwhm_offset(value);
    }

    public double skeletonise_threshold()
    {
        return skeletonise_parameters_.fwhm_threshold();
    }

    public void set_skeletonisation_threshold(double value)
    {
        skeletonise_parameters_.set_fwhm_threshold(value);
    }

    public double skeletonise_smoothing()
    {
        return skeletonise_parameters_.fwhm_smoothing();
    }

    public void set_skeletonisation_smoothing(double value)
    {
        skeletonise_parameters_.set_fwhm_smoothing(value);
    }

    public double skeletonise_offset()
    {
        return skeletonise_parameters_.fwhm_offset();
    }

    public void set_skeletonisation_offset(double value)
    {
        skeletonise_parameters_.set_fwhm_offset(value);
    }

    public void set_flatten_images(boolean value)
    {
        flatten_images_ = value;
    }

    public void set_blur_skeleton(boolean value)
    {
        blur_skeletons_ = value;
    }

    public void set_dilate_erode_method(DilateErode.Method method)
    {
        dilate_erode_method_ = method;
    }
}

