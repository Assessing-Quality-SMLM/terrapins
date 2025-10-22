package com.coxphysics.terrapins.views.renderer;

import com.coxphysics.terrapins.models.localisations.ParseMethod;
import com.coxphysics.terrapins.models.renderer.RenderSettings;

import static com.coxphysics.terrapins.models.utils.StringUtils.EMPTY_STRING;
import static com.coxphysics.terrapins.models.utils.StringUtils.path_set;

public class RendererDialogSettings
{
    private String localisation_path_;

    private final RenderSettings settings_;


    private RendererDialogSettings(String localisation_path, RenderSettings settings)
    {
        localisation_path_ = localisation_path;
        settings_ = settings;
    }

    public static RendererDialogSettings from(String localisation_path, RenderSettings settings)
    {
        return new RendererDialogSettings(localisation_path, settings);
    }
    public static RendererDialogSettings default_()
    {
        return from(EMPTY_STRING, RenderSettings.default_());
    }

    public boolean has_localisation_path()
    {
        return path_set(localisation_path_);
    }

    public String localisation_path()
    {
        return localisation_path_;
    }

    public RenderSettings render_settings()
    {
        return settings_;
    }

    public float camera_pixel_size_nm()
    {
        return settings_.camera_pixel_size_nm();
    }

    public int magnification_factor()
    {
        return settings_.magnification_factor();
    }

    public String global_frame_of_reference()
    {
        return settings_.global_reference_frame();
    }

    public int sigma_scale()
    {
        return settings_.sigma_scale();
    }

    public int n_threads()
    {
        return settings_.n_threads();
    }

    public String image_path()
    {
        return settings_.image_path();
    }

    public String zoom_path()
    {
        return settings_.zoom_path();
    }

    public void set_localisation_path(String value)
    {
        localisation_path_ = value;
    }

    public void set_image_path(String filepath)
    {
        settings_.set_image_path(filepath);
    }

    public void set_zoom_path(String filepath)
    {
        settings_.set_zoom_path(filepath);
    }

    public void set_camera_pixel_size(float value)
    {
        settings_.set_camera_pixel_size(value);
    }

    public void set_magnification_factor(int value)
    {
        settings_.set_magnification_factor(value);
    }

    public void set_global_reference_frame(String value)
    {
        settings_.set_global_reference_frame(value);
    }

    public void set_sigma_scale(int value)
    {
        settings_.set_sigma_scale(value);
    }

    public void set_n_threads(int n_threads)
    {
        settings_.set_n_threads(n_threads);
    }

    public ParseMethod parse_method_settings()
    {
        return settings_.parse_method_settings();
    }

    public void set_parse_method(ParseMethod parse_method)
    {
        settings_.set_parse_method(parse_method);
    }
}
