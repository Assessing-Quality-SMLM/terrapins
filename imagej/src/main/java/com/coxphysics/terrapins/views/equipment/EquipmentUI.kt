package com.coxphysics.terrapins.views.equipment

import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.views.NumericField
import com.coxphysics.terrapins.views.Utils
import ij.gui.GenericDialog

class EquipmentUI private constructor(
    private val instrument_psf_: NumericField,
    private val camera_pixel_size_: NumericField,
    private val magnification_: NumericField)
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: EquipmentSettings): EquipmentUI
        {
            val instrument_psf =
                Utils.add_numeric_field(dialog, "Instrument Psf fwhm (nm)", settings.instrument_psf_fwhm_nm(), 2)
            val camera_pixel_size =
                Utils.add_numeric_field(dialog, "Camera Pixel Size (nm)", settings.camera_pixel_size_nm(), 2)
            val magnification = Utils.add_numeric_field(dialog, "Magnification", settings.magnification(), 0)

            val ui = EquipmentUI(instrument_psf, camera_pixel_size, magnification)
            return ui
        }

        @JvmStatic
        fun create_settings_record(dialog: GenericDialog): EquipmentSettings
        {
            val settings = EquipmentSettings.default()
            val instrument_psf = Utils.extract_numeric_field(dialog)
            settings.set_instrument_psf_fwhm_nm(instrument_psf)

            val camera_pixel_size = Utils.extract_numeric_field(dialog)
            settings.set_camera_pixel_size_nm(camera_pixel_size)

            val magnification = Utils.extract_numeric_field(dialog)
            settings.set_magnification(magnification)

            return settings
        }
    }

    fun set_visibility(value: Boolean)
    {
        instrument_psf_.set_visibility(value)
        camera_pixel_size_.set_visibility(value)
        magnification_.set_visibility(value)
    }
}