package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.assessment.HAWKMANSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import java.awt.Color
import javax.swing.JTextField

class AuxiallySettingsVM private constructor(private val model_: EquipmentSettings, private val hawkman_settings: HAWKMANSettings)
{
    private val default_colour_: Color = JTextField().background
    private val error_colour_ : Color = Color.RED

    companion object
    {
        @JvmStatic
        fun from(model: EquipmentSettings, hawkman_settings: HAWKMANSettings): AuxiallySettingsVM
        {
            return AuxiallySettingsVM(model, hawkman_settings)
        }

        @JvmStatic
        fun default(): AuxiallySettingsVM
        {
            return AuxiallySettingsVM(EquipmentSettings.default(), HAWKMANSettings.default())
        }

        // for Java
        @JvmStatic
        fun default_(): AuxiallySettingsVM
        {
            return default()
        }
    }

    fun camera_pixel_size_nm(): Double
    {
        return model_.camera_pixel_size_nm()
    }

    fun set_camera_pixel_size_nm(value: String): Boolean
    {
        val new_value = value.toDoubleOrNull()
        if (new_value == null)
            return false
        model_.set_camera_pixel_size_nm(new_value)
        return true
    }

    fun instrument_psf_fwhm_nm(): Double
    {
        return model_.instrument_psf_fwhm_nm()
    }

    fun set_instrument_psf_fwhm_nm(value: String): Boolean
    {
        val new_value = value.toDoubleOrNull()
        if (new_value == null)
            return false
        model_.set_instrument_psf_fwhm_nm(new_value)
        return true
    }

    fun magnification(): Double
    {
        return model_.magnification()
    }

    fun set_magnification(value: String): Boolean
    {
        val new_value = value.toDoubleOrNull()
        if (new_value == null)
            return false
        model_.set_magnification(new_value)
        return true
    }

    fun hawkman_n_levels(): Int
    {
        return hawkman_settings.n_levels()
    }

    fun set_hawkman_n_levels(value: String): Boolean
    {
        val new_value = value.toIntOrNull()
        if (new_value == null)
            return false
        hawkman_settings.set_n_levels(new_value)
        return true
    }

    fun default_colour(): Color
    {
        return default_colour_
    }

    fun error_colour(): Color
    {
        return error_colour_
    }
}