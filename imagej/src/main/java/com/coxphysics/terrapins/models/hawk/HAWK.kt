package com.coxphysics.terrapins.models.hawk

import com.coxphysics.terrapins.models.macros.MacroUtils
import ij.IJ
import ij.ImagePlus
import ij.measure.Calibration

class HAWK private constructor(private var settings_: Settings)
{
    companion object
    {
        @JvmStatic
        fun from(settings: Settings): HAWK
        {
            return HAWK(settings)
        }
    }

    fun save_to_disk(): Boolean
    {
        return recordable_action { inner_save_to_disk() }
    }

    private fun inner_save_to_disk(): Boolean
    {
        val p_stream = PStream.from(settings_)
        if (p_stream == null)
            return false
        return p_stream.write_to_disk(settings_.filename())
    }

    fun get_hawk_image() : ImagePlus?
    {
        return recordable_action { inner_get_hawk_image() }
    }

    private fun inner_get_hawk_image() : ImagePlus?
    {
        val p_stream = PStream.from(settings_)
        if (p_stream == null)
            return null
        val view = ImagePlus(get_hawk_image_name(), p_stream)
        view.calibration = get_calibration(settings_)
        val metadata = p_stream._metadata;
        view.setProp("hawk_metadata", metadata)
        return view
    }

    private fun get_hawk_image_name(): String
    {
        return String.format("%s-HAWK-stream", settings_.image_name())
    }

    private fun get_calibration(settings: Settings): Calibration?
    {
        val image = settings.image()
        if (image == null)
            return null
        val base = image.getCalibration().copy()
        base.frameInterval = 0.0
        return base;
    }

    private fun<T> recordable_action(action: () -> T) : T
    {
        val value = action()
        if (MacroUtils.is_recording())
        {
            settings_.record_values()
        }
        return value
    }
}