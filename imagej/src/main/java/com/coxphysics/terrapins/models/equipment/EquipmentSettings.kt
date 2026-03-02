package com.coxphysics.terrapins.models.equipment

import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.plugins.EQUIPMENT_INSTRUMENT_PSF_FWHM_NM
import com.coxphysics.terrapins.plugins.EQUIPMENT_SETTINGS_CAMERA_PIXEL_SIZE_NM
import com.coxphysics.terrapins.plugins.EQUIPMENT_SETTINGS_MAGNIFICATION
import ij.plugin.frame.Recorder

class EquipmentSettings private constructor(
    private var instrument_psf_fwmn_nm: Double,
    private var camera_pixel_size_nm: Double,
    private var magnification: Double)
{
    companion object
    {
        fun default() : EquipmentSettings
        {
            return EquipmentSettings(270.0, 160.0, 10.0)
        }

        @JvmStatic
        fun from_macro_options(options: MacroOptions): EquipmentSettings
        {
            val settings = default()

            val instrument_psf_fwmn_nm = options.get_double(EQUIPMENT_INSTRUMENT_PSF_FWHM_NM)
            if (instrument_psf_fwmn_nm != null)
                settings.set_instrument_psf_fwhm_nm(instrument_psf_fwmn_nm)

            val camera_pixel_size_nm = options.get_double(EQUIPMENT_SETTINGS_CAMERA_PIXEL_SIZE_NM)
            if (camera_pixel_size_nm != null)
                settings.set_camera_pixel_size_nm(camera_pixel_size_nm)

            val magnification = options.get_double(EQUIPMENT_SETTINGS_MAGNIFICATION)
            if (magnification != null)
                settings.set_magnification(magnification)
            return settings
        }
    }

    fun instrument_psf_fwhm_nm(): Double
    {
        return instrument_psf_fwmn_nm
    }

    fun set_instrument_psf_fwhm_nm(value: Double)
    {
        instrument_psf_fwmn_nm = value
    }

    fun camera_pixel_size_nm(): Double
    {
        return camera_pixel_size_nm
    }

    fun set_camera_pixel_size_nm(value: Double)
    {
        camera_pixel_size_nm = value
    }

    fun magnification(): Double
    {
        return magnification
    }

    fun set_magnification(value: Double)
    {
        magnification = value
    }

    fun record_to_macro()
    {
        Recorder.recordOption(EQUIPMENT_SETTINGS_CAMERA_PIXEL_SIZE_NM, camera_pixel_size_nm.toString())
        Recorder.recordOption(EQUIPMENT_SETTINGS_MAGNIFICATION, magnification.toString())
        Recorder.recordOption(EQUIPMENT_INSTRUMENT_PSF_FWHM_NM, instrument_psf_fwmn_nm.toString())
    }
}