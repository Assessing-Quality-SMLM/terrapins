package com.coxphysics.terrapins.models;

import com.coxphysics.terrapins.models.hawk.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ConfigTest 
{
    @Test
    public void sequential_output_style()
    {
        int value = NativeHAWK.output_style_sequential();
        assertEquals(value, 0);
    }

    @Test
    public void interleaved_output_style()
    {
        int value = NativeHAWK.output_style_interleaved();
        assertEquals(value, 1);
    }

    @Test
    public void get_metadata()
    {
        short negative_handling = NativeHAWK.negative_handling_absolute();
        short output_style = NativeHAWK.output_style_sequential();
        long config_ptr = NativeHAWK.config_new(3, negative_handling, output_style);
        try {
            String metadata = NativeHAWK.get_metadata(config_ptr);
            assertEquals(metadata, "hawk_core:\nVersion: 0.2.0\nconfig: {\"Version1\":{\"threading\":\"Parallel\",\"memory\":\"Contiguous\",\"run_style\":\"InMemory\",\"algorithm\":{\"n_levels\":3,\"output_style\":\"Sequential\",\"negative_handling\":\"Absolute\"},\"validation\":{\"LimitOutputsToUnder32Bits\":true}}}");
        }
        finally {
            NativeHAWK.config_free(config_ptr);
        }
    }

    @Test
    public void validation_errors()
    {
        Config config = Config.from(Settings.from(2, NegativeValuesPolicy.ABSOLUTE, OutputStyle.SEQUENTIAL));
        String errors = config.get_validation_errors(1);
        String expected = "Validation Error: data length of 1 < kernel size 4";
        assertEquals(errors,  expected);
    }
}
