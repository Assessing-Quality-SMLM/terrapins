package com.coxphysics.terrapins.views;

import com.coxphysics.terrapins.views.renderer.RendererDialogSettings;
import com.coxphysics.terrapins.views.renderer.RendererUI;
import ij.gui.GenericDialog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RendererUITests
{
    @Test
    public void recorded_settings_capture_ts_settings()
    {
        GenericDialog dialog = new GenericDialog("something");
        RendererDialogSettings settings = RendererDialogSettings.default_();
        RendererUI.add_to_dialog(dialog, settings, true);
        RendererDialogSettings new_settings = RendererUI.create_settings_recorded(dialog);
        assertTrue(new_settings.parse_method_settings().use_thunderstorm());
    }
}