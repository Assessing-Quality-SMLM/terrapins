package com.coxphysics.terrapins.models.hawkman;

import com.coxphysics.terrapins.models.psf.PsfCalculator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PsfCalculatorTest
{
    @Test
    public void defaults_test()
    {
        PsfCalculator settings = PsfCalculator.default_();
        assertEquals(settings.camera_pixel_size_nm(), 160);
        assertEquals(settings.instrument_psf_fwhm_nm(), 270);
        assertEquals(settings.reconstruction_magnification_factor(), 10);
    }

    @Test
    public void calculation_test()
    {
        assertEquals(PsfCalculator.default_().calculate_psf(), 16.875);
    }
}
