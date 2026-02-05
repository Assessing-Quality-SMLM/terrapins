package com.coxphysics.terrapins.models.hawk

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

    fun get_hawk_image() : ImagePlus?
    {
        val p_stream = PStream.from(settings_)
        if (p_stream == null)
            return null
        val view = ImagePlus("JHAWK pstream", p_stream)
        view.calibration = get_calibration(settings_)
        val metadata = p_stream._metadata;
        view.setProp("hawk_metadata", metadata)
        return view
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
}