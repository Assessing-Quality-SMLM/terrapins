package com.coxphysics.terrapins.models.localisations;


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

    public void set_uncertainty_sigma_pos(int value)
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
}