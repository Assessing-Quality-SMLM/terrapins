package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.reports.Helper;
import com.coxphysics.terrapins.models.localisations.ParseMethod;
import com.coxphysics.terrapins.models.localisations.SplitSettings;
import com.coxphysics.terrapins.models.renderer.RenderSettings;

import java.io.Writer;

import static com.coxphysics.terrapins.models.utils.StringUtils.EMPTY_STRING;


public class FRCDialogSettings
{
    private static final String DEFAULT_PATH = EMPTY_STRING;
    private static final boolean DEFAULT_USE_EXISTING_IMAGES = false;
    private static final boolean DEFAULT_SPLIT_SPECIFIED = false;
    private static ThresholdMethod DEFAULT_THRESHOLD_METHOD = ThresholdMethod.ONE_SEVENTH;

    private String localisation_path_ = EMPTY_STRING;

    private final ParseMethod parse_method_ = ParseMethod.default_();

    private SplitSettings split_settings_ = SplitSettings.with_parse_method(parse_method_);

    private RenderSettings render_settings_ = RenderSettings.with_parse_method(parse_method_);

    private boolean specify_split_ = DEFAULT_SPLIT_SPECIFIED;

    private String extra_localisation_path_ = EMPTY_STRING;

    private boolean use_existing_images_ = DEFAULT_USE_EXISTING_IMAGES;

    private String image_1_ = EMPTY_STRING;

    private String image_2_ = EMPTY_STRING;

    private ThresholdMethod threshold_method_ = DEFAULT_THRESHOLD_METHOD;

    private String output_data_path_ = EMPTY_STRING;

    private FRCDialogSettings()
    {
    }

    public static FRCDialogSettings default_()
    {
        return new FRCDialogSettings();
    }

    public String localisation_path()
    {
        return localisation_path_;
    }

    public void set_localisation_file(String filepath)
    {
        localisation_path_ = filepath;
    }

    public RenderSettings render_settings()
    {
        return render_settings_;
    }

    public String extra_localisation_path()
    {
        return extra_localisation_path_;
    }

    public void set_extra_localisation_file(String filepath)
    {
        extra_localisation_path_ = filepath;
    }

    public SplitSettings split_settings()
    {
        return split_settings_;
    }

    public boolean use_existing_images()
    {
        return use_existing_images_;
    }

    public void set_use_existing_images(boolean value)
    {
        use_existing_images_ = value;
    }

    public boolean split_specified()
    {
        return specify_split_;
    }

    public void set_split_specified(boolean value)
    {
        specify_split_ = value;
    }

    public String image_1()
    {
        return image_1_;
    }

    public void set_image_1(String image_name)
    {
        image_1_ = image_name;
    }

    public String image_2()
    {
        return image_2_;
    }

    public void set_image_2(String image_name)
    {
        image_2_ = image_name;
    }

    public ThresholdMethod threshold_method()
    {
        return threshold_method_;
    }

    public void set_render_settings(RenderSettings render_settings)
    {
        render_settings_ = render_settings;
    }

    public void set_split_settings(SplitSettings split_settings)
    {
        split_settings_ = split_settings;
    }

    public void set_threshold_method(ThresholdMethod threshold_method)
    {
        threshold_method_ = threshold_method;
    }

    public float pixel_size()
    {
        return render_settings().camera_pixel_size_nm();
    }

    public String output_data_path()
    {
        return output_data_path_;
    }

    public void set_output_data_directory(String value)
    {
        output_data_path_ = value;
    }

    // write settings that have been changed i.e not default
    public void write_to(Writer writer)
    {
        Helper.write_if_set_default(writer, "Use Existing Images", use_existing_images(), DEFAULT_USE_EXISTING_IMAGES);
        Helper.write_if_set_default(writer,"Localisation Path", localisation_path(), DEFAULT_PATH);
        Helper.write_if_set_default(writer,"Image 1", image_1(), DEFAULT_PATH);
        Helper.write_if_set_default(writer,"Image 2", image_2(), DEFAULT_PATH);
        Helper.write_if_set_default(writer, "Split Specified", split_specified(), DEFAULT_SPLIT_SPECIFIED);
        Helper.write_if_set_default(writer, "Extra Localisation File", extra_localisation_path(), DEFAULT_PATH);
        Helper.write_if_set(writer, "Split Settings", split_settings(), SplitSettings.default_());
        Helper.write_if_set(writer, "Render Settings", render_settings(), RenderSettings.default_());
        Helper.write_if_set_default(writer, "Threshold Method", threshold_method(), DEFAULT_THRESHOLD_METHOD);
    }
}

