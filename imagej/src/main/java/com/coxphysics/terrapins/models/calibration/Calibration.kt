package com.coxphysics.terrapins.models.calibration

import ij.ImagePlus
import ij.measure.Calibration as ImageJCalibration

class Calibration private constructor(
    private val calibration_: ImageJCalibration
)
{
    companion object
    {
        @JvmStatic
        fun new(calibration: ImageJCalibration) : Calibration
        {
            return Calibration(calibration)
        }

        @JvmStatic
        fun from_image(image: ImagePlus) : Calibration
        {
            return new(image.calibration)
        }
    }

    fun pixel_size_nm_or(default_value: Double) : Double
    {
        return pixel_size_nm() ?: return default_value
    }

    fun pixel_size_nm() : Double?
    {
        return pixel_size()?.to_nm()
    }

    fun pixel_size() : Size?
    {
        val unit = unit();
        if (unit == null)
            return null
        return Size.from(calibration_.pixelWidth, unit)
    }

    fun unit(): Unit?
    {
        return calibration_.unit?.let{ s -> Unit.parse(s)}
    }
}