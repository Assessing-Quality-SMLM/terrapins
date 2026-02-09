package com.coxphysics.terrapins.models.equipment

import com.coxphysics.terrapins.models.macros.MacroOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EquipmentSettingsTests
{
    @Test
    fun to_macro_test()
    {
        val settings = EquipmentSettings.default()
        settings.set_magnification(1.0)
        settings.set_instrument_psf_fwhm_nm(2.0)
        settings.set_camera_pixel_size_nm(3.0)
        assertEquals(settings.magnification(), 1.0)
        assertEquals(settings.instrument_psf_fwhm_nm(), 2.0)
        assertEquals(settings.camera_pixel_size_nm(), 3.0)

        settings.record_to_macro()

        val options = MacroOptions.from_recorder_command_options()
        val new_settings = EquipmentSettings.from_macro_options(options)
        assertEquals(new_settings.magnification(), 1.0)
        assertEquals(new_settings.instrument_psf_fwhm_nm(), 2.0)
        assertEquals(new_settings.camera_pixel_size_nm(), 3.0)
    }

}