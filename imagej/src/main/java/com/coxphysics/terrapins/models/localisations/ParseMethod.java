package com.coxphysics.terrapins.models.localisations;


import com.coxphysics.terrapins.models.macros.MacroOptions;
import ij.plugin.frame.Recorder;

public class ParseMethod
{
    private static class CsvSettings
    {
        private int n_headers_ = 0;
        private char delimiter_ = ',';
        private int x_pos_ = 0;
        private int y_pos_ = 1;

        private int psf_sigma_pos_ = -1;

        private int uncertainty_pos_ = -1;

        private int frame_number_pos_ = -1;

        public CsvSettings()
        {

        }

        public int n_headers()
        {
            return n_headers_;
        }

        public char delimiter()
        {
            return delimiter_;
        }

        public int x_position()
        {
            return x_pos_;
        }

        public int y_position()
        {
            return y_pos_;
        }

        public int psf_sigma_position()
        {
            return psf_sigma_pos_;
        }

        public int uncertainty_position()
        {
            return uncertainty_pos_;
        }

        public int frame_number_position()
        {
            return frame_number_pos_;
        }

        public void set_n_headers(int value)
        {
            n_headers_ = value;
        }

        public void set_delimiter(char value)
        {
            delimiter_ = value;
        }

        public void set_x_pos(int value)
        {
            x_pos_ = value;
        }

        public void set_y_pos(int value)
        {
            y_pos_ = value;
        }

        public void set_psf_sigma_pos(int value)
        {
            psf_sigma_pos_ = value;
        }

        public void set_uncertainty_pos(int value)
        {
            uncertainty_pos_ = value;
        }

        public void set_frame_number_pos(int value)
        {
            frame_number_pos_ = value;
        }

        public String to_parse_method()
        {
            return String.format("csv=%d;%c;%d;%d;%d;%d;%d", n_headers_, delimiter_, x_pos_, y_pos_, psf_sigma_pos_, uncertainty_pos_, frame_number_pos_);
        }

    }

    private final static String PARSE_METHOD_THUNDERSTORM = "ts";

    private final CsvSettings csv_settings_ = new CsvSettings();

    private boolean is_thunderstorm_ = true;

    private ParseMethod()
    {

    }

    public static ParseMethod default_()
    {
        return new ParseMethod();
    }

    private static Integer nullable_parse(String desc)
    {
        try
        {
            return Integer.parseInt(desc);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
    public static ParseMethod from_macro_options(String key, MacroOptions options)
    {
        String description = options.get(key);
        if (description == null)
            return null;
        ParseMethod method = default_();
        if (description.equals("ts"))
        {
            method.set_parse_method_thunderstorm();
            return method;
        }
        String[] splits = description.split(",");
        if (splits.length < 5)
            return null;
        Integer n_header_lines = nullable_parse(splits[0]);
        if (n_header_lines == null)
            return null;
        Integer frame_number =nullable_parse(splits[1]);
        if (frame_number == null)
            return null;
        Integer x_pos =nullable_parse(splits[2]);
        if (x_pos == null)
            return null;
        Integer y_pos =nullable_parse(splits[3]);
        if (y_pos == null)
            return null;
        Integer uncertainty_sigma =nullable_parse(splits[4]);
        if (uncertainty_sigma == null)
            return null;
        method.set_parse_method_csv();
        method.set_n_headers(n_header_lines);
        method.set_frame_number_pos(frame_number);
        method.set_x_pos(x_pos);
        method.set_y_pos(y_pos);
        method.set_uncertainty_pos(uncertainty_sigma);
        return method;
    }

    public boolean use_thunderstorm()
    {
        return is_thunderstorm_;
    }

    public int n_header_lines()
    {
        return csv_settings_.n_headers();
    }

    public char delimiter()
    {
        return csv_settings_.delimiter();
    }

    public int x_position()
    {
        return csv_settings_.x_position();
    }

    public int y_position()
    {
        return csv_settings_.y_position();
    }

    public int psf_sigma_position()
    {
        return csv_settings_.psf_sigma_position();
    }

    public int uncertainty_position()
    {
        return csv_settings_.uncertainty_position();
    }

    public int frame_number_position()
    {
        return csv_settings_.frame_number_position();
    }

    public String parse_method()
    {
        if (use_thunderstorm())
            return PARSE_METHOD_THUNDERSTORM;
        return csv_settings_.to_parse_method();
    }

    public void set_parse_method_thunderstorm()
    {
        is_thunderstorm_ = true;
    }

    public void set_parse_method_csv()
    {
        is_thunderstorm_ = false;
    }

    public void set_n_headers(int value)
    {
        csv_settings_.set_n_headers(value);
    }

    public void set_delimiter(char value)
    {
        csv_settings_.set_delimiter(value);
    }

    public void set_x_pos(int value)
    {
        csv_settings_.set_x_pos(value);
    }

    public void set_y_pos(int value)
    {
        csv_settings_.set_y_pos(value);
    }

    public void set_psf_sigma_pos(int value)
    {
        csv_settings_.set_psf_sigma_pos(value);
    }

    public void set_uncertainty_pos(int value)
    {
        csv_settings_.set_uncertainty_pos(value);
    }

    public void set_frame_number_pos(int value)
    {
        csv_settings_.set_frame_number_pos(value);
    }

    public void record_to_macro(String key)
    {
        if (is_thunderstorm_)
            Recorder.recordOption(key, "ts");
        else
        {
            Recorder.recordOption(key, csv_macro_string());
        }
    }

    private String csv_macro_string()
    {
        String[] data = new String[]{
            String.valueOf(delimiter()),
            String.valueOf(n_header_lines()),
            String.valueOf(frame_number_position()),
            String.valueOf(x_position()),
            String.valueOf(y_position()),
            String.valueOf(psf_sigma_position()),
            String.valueOf(uncertainty_position())
        };
        return String.join(",", data);
    }
}