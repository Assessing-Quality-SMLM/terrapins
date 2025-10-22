package com.coxphysics.terrapins.models.renderer;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class RenderSettingsTests
{
    @Test
    public void global_reference_frame_optional()
    {
        RenderSettings settings = RenderSettings.default_();
        assertTrue(settings.global_reference_frame_set());
        settings.set_global_reference_frame("");
        assertFalse(settings.global_reference_frame_set());
    }

    @Test
    public void write_settings_if_none_changed()
    {
        StringWriter writer = new StringWriter();
        RenderSettings settings = RenderSettings.default_();
        settings.write_to(writer);
        String expected = "";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void write_pixel_size()
    {
        StringWriter writer = new StringWriter();
        RenderSettings settings = RenderSettings.default_();
        settings.set_camera_pixel_size(2);
        settings.write_to(writer);
        String expected = "Camera Pixel Size (nm): 2.0";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void write_magnification_factor_level()
    {
        StringWriter writer = new StringWriter();
        RenderSettings settings = RenderSettings.default_();
        settings.set_magnification_factor(2);
        settings.write_to(writer);
        String expected = "Magnification Factor: 2";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void write_sigma_scale()
    {
        StringWriter writer = new StringWriter();
        RenderSettings settings = RenderSettings.default_();
        settings.set_sigma_scale(2);
        settings.write_to(writer);
        String expected = "Sigma Scale: 2";
        assertEquals(writer.toString(), expected);
    }
}