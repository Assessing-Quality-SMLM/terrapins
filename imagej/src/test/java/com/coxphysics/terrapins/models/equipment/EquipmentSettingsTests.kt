package com.coxphysics.terrapins.models.equipment

import com.coxphysics.terrapins.models.macros.MacroOptions
import ij.plugin.frame.Recorder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class EquipmentSettingsTests
{
    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

    @Test
    fun to_macro_test()
    {
        executor.submit{
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

    @Test
    fun magnification_from_macro_test()
    {
        executor.submit{
            Recorder.recordOption("magnification", "1.234")
            val options = MacroOptions.from_recorder_command_options()
            val new_settings = EquipmentSettings.from_macro_options(options)
            assertEquals(new_settings.magnification(), 1.234)
        }
    }

    @Test
    fun camera_pixel_size_test()
    {
        executor.submit{
            Recorder.recordOption("camera_pixel_size_nm", "1.234")
            val options = MacroOptions.from_recorder_command_options()
            val new_settings = EquipmentSettings.from_macro_options(options)
            assertEquals(new_settings.camera_pixel_size_nm(), 1.234)
        }
    }

    @Test
    fun instrument_psf_fwhm()
    {
        executor.submit{
            Recorder.recordOption("instrument_psf_fwhm_nm", "1.234")
            val options = MacroOptions.from_recorder_command_options()
            val new_settings = EquipmentSettings.from_macro_options(options)
            assertEquals(new_settings.instrument_psf_fwhm_nm(), 1.234)
        }
    }

}