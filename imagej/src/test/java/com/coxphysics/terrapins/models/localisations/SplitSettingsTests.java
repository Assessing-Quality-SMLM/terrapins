package com.coxphysics.terrapins.models.localisations;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplitSettingsTests
{
    @Test
    public void write_settings_even_if_none_changed()
    {
        StringWriter writer = new StringWriter();
        SplitSettings settings = SplitSettings.default_();
        settings.write_to(writer);
        String expected = "Method: half";
        assertEquals(writer.toString(), expected);
    }

    @Test
    public void method_written_correctly()
    {
        StringWriter writer = new StringWriter();
        SplitSettings settings = SplitSettings.default_();
        settings.write_to(writer);
        String expected = "Method: half";
        assertEquals(writer.toString(), expected);
    }
}