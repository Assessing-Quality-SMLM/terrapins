package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.localisations.SplitSettings;
import com.coxphysics.terrapins.models.renderer.RenderSettings;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FRCDialogSettingsTests
{
    @Test
    public void write_settings_if_none_changed()
    {
        StringWriter writer = new StringWriter();
        FRCDialogSettings settings = FRCDialogSettings.default_();
        settings.write_to(writer);
        String expected = "";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void write_split_settings_if_changed()
    {
        StringWriter writer = new StringWriter();
        FRCDialogSettings settings = FRCDialogSettings.default_();
        SplitSettings new_settings = SplitSettings.default_();
        new_settings.set_zip_split();
        settings.set_split_settings(new_settings);
        settings.write_to(writer);
        String expected = "Split Settings: Method: zip";
        assertEquals(writer.toString(), expected);
        assertNotEquals(new_settings, SplitSettings.default_());
    }

    @Test
    public void write_split_settings_only_if_structurally_changed()
    {
        StringWriter writer = new StringWriter();
        FRCDialogSettings settings = FRCDialogSettings.default_();
        SplitSettings new_settings = SplitSettings.default_();
        settings.set_split_settings(new_settings);
        settings.write_to(writer);
        String expected = "";
        assertEquals(writer.toString(), expected);
        assertNotEquals(new_settings, SplitSettings.default_());
    }

    @Test
    public void write_render_settings_if_changed()
    {
        StringWriter writer = new StringWriter();
        FRCDialogSettings settings = FRCDialogSettings.default_();
        RenderSettings new_settings = RenderSettings.default_();
        new_settings.set_camera_pixel_size(1);
        settings.set_render_settings(new_settings);
        settings.write_to(writer);
        String expected = "Render Settings: Camera Pixel Size (nm): 1.0";
        assertEquals(writer.toString(), expected);
        assertNotEquals(new_settings, RenderSettings.default_());
    }

    @Test
    public void write_render_settings_only_if_structurally_changed()
    {
        StringWriter writer = new StringWriter();
        FRCDialogSettings settings = FRCDialogSettings.default_();
        RenderSettings new_settings = RenderSettings.default_();
        new_settings.set_camera_pixel_size(100);
        settings.set_render_settings(new_settings);
        settings.write_to(writer);
        String expected = "";
        assertEquals(writer.toString(), expected);
        assertNotEquals(new_settings, RenderSettings.default_());
    }
}