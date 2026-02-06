package com.coxphysics.terrapins.views.localisations;

import com.coxphysics.terrapins.views.*;
import com.coxphysics.terrapins.models.localisations.ParseMethod;
import ij.gui.GenericDialog;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static com.coxphysics.terrapins.views.Utils.*;

public class ParseMethodsUI
{
    private final GenericDialog dialog_;

    private final Message message_;
    private final ParseMethod settings_;

    private final Checkbox thunderstorm_checkbox_;
    private final NumericField header_lines_;
    private final StringField delimiter_;
    private final NumericField x_pos_;
    private final NumericField y_pos_;
    private final NumericField psf_sigma_pos_;
    private final NumericField uncertainty_pos_;
    private final NumericField frame_number_pos_;

    private ParseMethodsUI(GenericDialog dialog,
                           Message message,
                           ParseMethod settings,
                           Checkbox ts_checkbox,
                           NumericField header_lines,
                           StringField delimiter,
                           NumericField x_pos,
                           NumericField y_pos,
                           NumericField psf_sigma_pos,
                           NumericField uncertainty_pos,
                           NumericField frame_number_pos)
    {
        dialog_ = dialog;
        this.message_ = message;
        settings_ = settings;
        thunderstorm_checkbox_ = ts_checkbox;
        header_lines_ = header_lines;
        delimiter_ = delimiter;
        x_pos_ = x_pos;
        y_pos_ = y_pos;
        psf_sigma_pos_ = psf_sigma_pos;
        uncertainty_pos_ = uncertainty_pos;
        frame_number_pos_ = frame_number_pos;
    }

    public static ParseMethodsUI add_to_dialog(GenericDialog dialog, ParseMethod settings)
    {
        Message message = Utils.add_message(dialog,"~~~Localisation Parsing Options~~~");
        Checkbox ts_checkbox = add_checkbox(dialog, "ThunderStorm", settings.use_thunderstorm());
        NumericField header_lines = add_numeric_field(dialog, "Number of header lines", settings.n_header_lines(), 0);
        StringField delimiter = add_string_field(dialog, "Delimiter", String.valueOf(settings.delimiter()));
        NumericField x_pos = add_numeric_field(dialog, "X position", settings.x_position(), 0);
        NumericField y_pos = add_numeric_field(dialog, "Y position", settings.y_position(), 0);
        NumericField psf_sigma_pos = add_numeric_field(dialog, "PSF sigma position", settings.psf_sigma_position(), 0);
        NumericField uncertainty_sigma_pos = add_numeric_field(dialog, "Uncertainty position", settings.uncertainty_position(), 0);
        NumericField frame_number_pos = add_numeric_field(dialog, "Frame number position", settings.frame_number_position(), 0);
        ParseMethodsUI ui = new ParseMethodsUI(dialog, message, settings, ts_checkbox, header_lines, delimiter, x_pos, y_pos, psf_sigma_pos, uncertainty_sigma_pos, frame_number_pos);
        ui.synchronise_csv_visibility();
        ts_checkbox.add_item_listener(new IsThunderStormListener(ui));
        return ui;
    }
    public static ParseMethod create_settings_recorded(GenericDialog dialog)
    {
        ParseMethod parse_method = ParseMethod.default_();
        populate_settings_recorded(dialog, parse_method);
        return parse_method;
    }

    public static void populate_settings_recorded(GenericDialog dialog, ParseMethod parse_method)
    {
        boolean use_thunderstorm = Utils.extract_checkbox_value(dialog);
        if (use_thunderstorm)
        {
            parse_method.set_parse_method_thunderstorm();
        }
        else
        {
            parse_method.set_parse_method_csv();
        }

        int header_lines = Utils.extract_numeric_field_as_int(dialog);
        parse_method.set_n_headers(header_lines);
        String delimiter = Utils.extract_string_field(dialog);
        if (!delimiter.isEmpty())
        {
            char delim_char = delimiter.charAt(0);
            parse_method.set_delimiter(delim_char);
        }
        int x_pos = Utils.extract_numeric_field_as_int(dialog);
        parse_method.set_x_pos(x_pos);
        int y_pos = Utils.extract_numeric_field_as_int(dialog);
        parse_method.set_y_pos(y_pos);
        int sigma_pos = Utils.extract_numeric_field_as_int(dialog);
        parse_method.set_uncertainty_sigma_pos(sigma_pos);

        int uncertainty_pos = Utils.extract_numeric_field_as_int(dialog);
        parse_method.set_uncertainty_pos(uncertainty_pos);

        int frame_number_pos = Utils.extract_numeric_field_as_int(dialog);
        parse_method.set_frame_number_pos(frame_number_pos);
    }


    public void update_settings()
    {
        populate_settings(settings_);
    }

    public ParseMethod create_settings()
    {
        ParseMethod settings = ParseMethod.default_();
        populate_settings(settings);
        return settings;
    }

    public void populate_settings(ParseMethod settings)
    {
        if (thunderstorm_checkbox_.is_checked())
        {
            settings.set_parse_method_thunderstorm();
        }
        else
        {
            settings.set_parse_method_csv();
        }
        Integer n_header = header_lines_.get_nullable_value();
        if (n_header != null)
            settings.set_n_headers(n_header);

        Character delimiter = delimiter_.char_value();
        if (delimiter != null)
            settings.set_delimiter(delimiter);

        Integer x_pos = x_pos_.get_nullable_value();
        if (x_pos != null)
            settings.set_x_pos(x_pos);

        Integer y_pos = y_pos_.get_nullable_value();
        if (y_pos != null)
            settings.set_y_pos(y_pos);

        Integer psf_sigma_pos = psf_sigma_pos_.get_nullable_value();
        if (psf_sigma_pos != null)
            settings.set_uncertainty_sigma_pos(psf_sigma_pos);

        Integer uncertainty_pos = uncertainty_pos_.get_nullable_value();
        if (uncertainty_pos != null)
            settings.set_uncertainty_pos(uncertainty_pos);

        Integer frame_number_pos = frame_number_pos_.get_nullable_value();
        if (frame_number_pos != null)
            settings.set_frame_number_pos(frame_number_pos);
    }

    private void synchronise_csv_visibility()
    {
        boolean is_visible = !thunderstorm_checkbox_.is_checked();
        set_csv_visibility(is_visible);
    }

    private void set_csv_visibility(boolean value)
    {
        header_lines_.set_visible(value);
        delimiter_.set_visible(value);
        x_pos_.set_visible(value);
        y_pos_.set_visible(value);
        psf_sigma_pos_.set_visible(value);
        uncertainty_pos_.set_visible(value);
        frame_number_pos_.set_visible(value);
    }

    // this is for tests do not use
    public Checkbox thunderstorm_checkbox()
    {
        return thunderstorm_checkbox_;
    }

    // this is for tests do not use
    public NumericField n_headers()
    {
        return header_lines_;
    }

    public void set_visibility(boolean value)
    {
        message_.set_visibility(value);
        thunderstorm_checkbox_.set_visibility(value);
        if (value)
        {
            synchronise_csv_visibility();
        }
        else
        {
            set_csv_visibility(value);
        }
    }

    private static class IsThunderStormListener implements ItemListener
    {
        private final ParseMethodsUI ui_;

        public IsThunderStormListener(ParseMethodsUI dialog)
        {
            ui_ = dialog;
        }

        @Override
        public void itemStateChanged(ItemEvent e)
        {
            ui_.synchronise_csv_visibility();
            ui_.dialog_.pack();
        }
    }
}
