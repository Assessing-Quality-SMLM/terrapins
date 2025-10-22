package com.coxphysics.terrapins.models.squirrel;

public class SquirrelSettings
{
    private String reference_image_ = "";

    private String super_res_image_ = "";

    // processing options
    private double sigma_nm_ = 200; // sigma for smart boundary

    private boolean purge_empty_frames_ = false; // check and purge empty frames

    private boolean crop_borders_ = true; // crop black borders from super-resolution image

    private boolean do_registration_ = true; // Enable registration

    private double max_expected_misalignment_ = 0; // Maximum expected misalignment

    // output image options

    private boolean show_intensity_normalised_ = true; // Show intensity-normalised and cropped super-resolution image(s)

    private boolean show_convolved_ = true; // Show_RSF-convolved super-resolution image(s)

    private boolean show_positive_negative_ = false; // Show_positive and negative contributions to error map

    private SquirrelSettings()
    {

    }

    public static SquirrelSettings default_()
    {
        return new SquirrelSettings();
    }

    public String reference_image()
    {
        return reference_image_;
    }

    public String super_res_image()
    {
        return super_res_image_;
    }

    public double sigma_nm()
    {
        return sigma_nm_;
    }

    public boolean purge_empty_frames()
    {
        return purge_empty_frames_;
    }

    public boolean crop_borders()
    {
        return crop_borders_;
    }

    public boolean register()
    {
        return do_registration_;
    }

    public double misalignment()
    {
        return max_expected_misalignment_;
    }

    public boolean crop_and_normalise()
    {
        return show_intensity_normalised_;
    }

    public boolean show_rsf_convolved()
    {
        return show_convolved_;
    }

    public boolean show_positive_and_negative()
    {
        return show_positive_negative_;
    }

    public void set_reference_image(String reference_image)
    {
        reference_image_ = reference_image;
    }

    public void set_super_res_image(String super_res_image)
    {
        super_res_image_ = super_res_image;
    }

    public void set_sigma_nm(int value)
    {
        sigma_nm_ = value;
    }

    public void set_purge_empty_frames(boolean value)
    {
        purge_empty_frames_ = value;
    }


    public void set_crop_borders(boolean value)
    {
        crop_borders_ = value;
    }

    public void set_registration(boolean value)
    {
        do_registration_ =  value;
    }

    public void set_misalignment(int value)
    {
        max_expected_misalignment_ = value;
    }

    public void set_crop_and_normalise(boolean value)
    {
        show_intensity_normalised_ = value;
    }

    public void set_show_rsf_convovled(boolean value)
    {
        show_convolved_ = value;
    }

    public void set_show_positive_and_negative(boolean value)
    {
        show_positive_negative_ = value;
    }
}
