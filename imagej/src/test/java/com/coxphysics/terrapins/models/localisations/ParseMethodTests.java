package com.coxphysics.terrapins.models.localisations;

import com.coxphysics.terrapins.views.localisations.ParseMethodsUI;
import ij.gui.GenericDialog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseMethodTests
{
    @Test
    void populate_settings_picks_up_thunderstorm_checkbox()
    {
        GenericDialog dialog = new GenericDialog("");
        ParseMethod settings = ParseMethod.default_();
        ParseMethodsUI ui =  ParseMethodsUI.add_to_dialog(dialog, settings);
        assertEquals(settings.use_thunderstorm(), true);
        ui.thunderstorm_checkbox().set_checked(false);
        assertEquals(settings.use_thunderstorm(), true);
        ui.update_settings();
        assertEquals(settings.use_thunderstorm(), false);
    }

    @Test
    void populate_settings_handles_bad_text()
    {
        GenericDialog dialog = new GenericDialog("");
        ParseMethod settings = ParseMethod.default_();
        ParseMethodsUI ui =  ParseMethodsUI.add_to_dialog(dialog, settings);
        ui.n_headers().set_text("2");
        assertEquals(settings.n_header_lines(), 0);
        ui.update_settings();
        assertEquals(settings.n_header_lines(), 2);
        ui.n_headers().set_text("junk");
        ui.update_settings();
        assertEquals(ui.n_headers().text(), "junk");
        assertEquals(settings.n_header_lines(), 2);
    }
    
    @Test
    void minimal_csv_settings()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;-1;-1;-1");
    }

    @Test
    void csv_settings_set_header_position()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_n_headers(10);
        assertEquals(parse_method.parse_method(), "csv=10;,;0;1;-1;-1;-1");
    }

    @Test
    void csv_settings_set_psf_sigma()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_uncertainty_sigma_pos(2);
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;2;-1;-1");
    }

    @Test
    void csv_settings_set_uncertainty()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_uncertainty_pos(2);
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;-1;2;-1");
    }

    @Test
    void csv_settings_set_frame_number()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_frame_number_pos(2);
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;-1;-1;2");
    }

    @Test
    void csv_settings_set_psf_sigma_and_uncertainty()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_uncertainty_sigma_pos(2);
        parse_method.set_uncertainty_pos(3);
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;2;3;-1");
    }

    @Test
    void csv_settings_set_psf_sigma_and_uncertainty_and_frame_number()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_uncertainty_sigma_pos(2);
        parse_method.set_uncertainty_pos(3);
        parse_method.set_frame_number_pos(4);
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;2;3;4");
    }
}
