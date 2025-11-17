package com.coxphysics.terrapins.models.equipment

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
}