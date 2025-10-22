package com.coxphysics.terrapins.views.equipment

import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.views.Utils
import ij.gui.GenericDialog

class EquipmentUI private constructor()
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
            val ui = EquipmentUI()
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

            return settings
        }
    }
}