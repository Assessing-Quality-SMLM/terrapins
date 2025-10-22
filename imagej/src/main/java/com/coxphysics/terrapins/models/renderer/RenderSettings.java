package com.coxphysics.terrapins.models.renderer;

import com.coxphysics.terrapins.models.reports.EqualSettings;
import com.coxphysics.terrapins.models.reports.Helper;
import com.coxphysics.terrapins.models.reports.Writable;
import com.coxphysics.terrapins.models.localisations.ParseMethod;
import ij.Prefs;

import java.io.Writer;
import java.util.Objects;

public class RenderSettings implements Writable, EqualSettings
{
    private  final static String EMPTY_STRING = "";

    private final static float DEFAULT_CAMERA_PIXEL_SIZE = 100;

    private final static String DEFAULT_GLOBAL_REFERENCE_FRAME = "0,0,256,256";

    private final static int DEFAULT_MAGNIFICATION_LEVEL = 10;

    private final static int DEFAULT_SIGMA_SCALE = 3;

    private String image_path_ = EMPTY_STRING;

    private String zoom_path_ = EMPTY_STRING;

    private String data_output_path_ = EMPTY_STRING;

    private float camera_pixel_size_nm_ = DEFAULT_CAMERA_PIXEL_SIZE;

    private String global_frame_of_reference_ = DEFAULT_GLOBAL_REFERENCE_FRAME;

    private int magnification_factor = DEFAULT_MAGNIFICATION_LEVEL;

    private int sigma_scale_ = DEFAULT_SIGMA_SCALE;

    private int n_threads_ = Prefs.getThreads();

    private ParseMethod parse_method_;

    private RenderSettings(ParseMethod parse_method)
    {
        parse_method_ = parse_method;
    }

    public static RenderSettings with_parse_method(ParseMethod parse_method)
    {
        return new RenderSettings(parse_method);
    }
    public static RenderSettings default_()
    {
        return with_parse_method(ParseMethod.default_());
    }

    public float camera_pixel_size_nm()
    {
        return camera_pixel_size_nm_;
    }

    public void set_camera_pixel_size(float value)
    {
        camera_pixel_size_nm_ = value;
    }

    public boolean global_reference_frame_set()
    {
        return !Objects.equals(global_reference_frame(), EMPTY_STRING);
    }
    public String global_reference_frame()
    {
        return global_frame_of_reference_;
    }

    public void set_image_width_height(int width, int height)
    {
        String global_frame_of_reference = String.format("0,0,%s,%s", width, height);
        set_global_reference_frame(global_frame_of_reference);
    }

    public void set_global_reference_frame(String value)
    {
        global_frame_of_reference_ = value;
    }

    public int magnification_factor()
    {
        return magnification_factor;
    }

    public void set_magnification_factor(int value)
    {
        magnification_factor = value;
    }

    public boolean has_image_path_set()
    {
        return path_set(image_path_);
    }

    public String image_path()
    {
        return image_path_;
    }

    public void set_image_path(String value)
    {
        image_path_ = value;
    }

    public boolean has_zoom_path_set()
    {
        return path_set(zoom_path_);
    }

    public String zoom_path()
    {
        return zoom_path_;
    }

    public void set_zoom_path(String value)
    {
        zoom_path_ = value;
    }

    public boolean has_data_output_path_set()
    {
        return path_set(data_output_path_);
    }

    public String data_output_path()
    {
        return data_output_path_;
    }

    public void set_data_output_path(String value)
    {
        data_output_path_ = value;
    }

    private static boolean path_set(String path)
    {
        return path != null && !path.equals(EMPTY_STRING);
    }

    public int sigma_scale()
    {
        return sigma_scale_;
    }

    public void set_sigma_scale(int value)
    {
        sigma_scale_ = value;
    }

    public int n_threads()
    {
        return n_threads_;
    }

    public void set_n_threads(int value)
    {
        n_threads_ = value;
    }

    public String parse_method()
    {
        return parse_method_.parse_method();
    }

    public ParseMethod parse_method_settings()
    {
        return parse_method_;
    }

    public void set_parse_method(ParseMethod parse_method)
    {
        parse_method_ = parse_method;
    }

    @Override
    public void write_to(Writer writer)
    {
        Helper.write_if_set_default(writer, "Image", image_path(), EMPTY_STRING);
        Helper.write_if_set_default(writer, "Camera Pixel Size (nm)", camera_pixel_size_nm(), DEFAULT_CAMERA_PIXEL_SIZE);
        Helper.write_if_set_default(writer, "Magnification Factor", magnification_factor(), DEFAULT_MAGNIFICATION_LEVEL);
        Helper.write_if_set_default(writer, "Global Frame of Reference", global_reference_frame(), DEFAULT_GLOBAL_REFERENCE_FRAME);
        Helper.write_if_set_default(writer, "Sigma Scale", sigma_scale(), DEFAULT_SIGMA_SCALE);
    }

    @Override
    public boolean has_same_settings(Object other)
    {
        if (!Helper.is_valid_for_comparison(this, other))
        {
            return false;
        }
        RenderSettings other_ = (RenderSettings) other;
        return camera_pixel_size_nm_ == other_.camera_pixel_size_nm_ &&
               global_frame_of_reference_.contentEquals(other_.global_frame_of_reference_) &&
               magnification_factor == other_.magnification_factor &&
               sigma_scale_ == other_.sigma_scale_;
    }
}

