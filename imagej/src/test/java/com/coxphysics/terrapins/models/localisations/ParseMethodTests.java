package com.coxphysics.terrapins.models.localisations;

import com.coxphysics.terrapins.models.macros.MacroOptions;
import com.coxphysics.terrapins.views.localisations.ParseMethodsUI;
import ij.gui.GenericDialog;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseMethodTests
{
    private ExecutorService executor = Executors.newSingleThreadExecutor();

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
        parse_method.set_psf_sigma_pos(2);
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
        parse_method.set_psf_sigma_pos(2);
        parse_method.set_uncertainty_pos(3);
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;2;3;-1");
    }

    @Test
    void csv_settings_set_psf_sigma_and_uncertainty_and_frame_number()
    {
        ParseMethod parse_method = ParseMethod.default_();
        parse_method.set_parse_method_csv();
        parse_method.set_psf_sigma_pos(2);
        parse_method.set_uncertainty_pos(3);
        parse_method.set_frame_number_pos(4);
        assertEquals(parse_method.parse_method(), "csv=0;,;0;1;2;3;4");
    }

    @Test
    void thunderstorm_macro_record()
    {
        try {
            executor.submit(() -> {
                ParseMethod parse_method = ParseMethod.default_();
                parse_method.set_parse_method_thunderstorm();
                MacroOptions.reset();
                parse_method.record_to_macro("something");
                MacroOptions options = MacroOptions.from_recorder_command_options();
                String desc = options.get("something");
                assertEquals(desc, "ts");
            }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void csv_macro_record()
    {
        try {
            executor.submit(() -> {
                ParseMethod parse_method = ParseMethod.default_();
                parse_method.set_parse_method_csv();
                parse_method.set_n_headers(1);
                parse_method.set_psf_sigma_pos(2);
                parse_method.set_uncertainty_pos(3);
                parse_method.set_frame_number_pos(4);
                parse_method.set_x_pos(5);
                parse_method.set_y_pos(6);

                MacroOptions.reset();
                parse_method.record_to_macro("something");

                MacroOptions options = MacroOptions.from_recorder_command_options();
                String desc = options.get("something");
                assertEquals(desc, ",,1,4,5,6,2,3");
            }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void read_ts_settings_macro()
    {
        MacroOptions options = MacroOptions.from("something=ts");
        ParseMethod method = ParseMethod.from_macro_options("something", options);
        assertEquals(method.use_thunderstorm(), true);
    }

    @Test
    void read_csv_settings_from_macro()
    {
        MacroOptions options = MacroOptions.from("something=1,4,5,6,2");
        ParseMethod method = ParseMethod.from_macro_options("something", options);

        assertEquals(method.use_thunderstorm(), false);
        assertEquals(method.n_header_lines(), 1);
        assertEquals(method.frame_number_position(), 4);
        assertEquals(method.x_position(), 5);
        assertEquals(method.y_position(), 6);
        assertEquals(method.uncertainty_position(), 2);
    }
}
