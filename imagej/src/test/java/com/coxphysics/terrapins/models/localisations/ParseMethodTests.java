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
}
