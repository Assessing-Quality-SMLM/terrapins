package com.coxphysics.terrapins.models.calibration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import ij.measure.Calibration as ImageJCalibration

class CalibrationTests
{
    @Test
    fun can_extract_nm_unit()
    {
        val ij_calibration = ImageJCalibration()
        ij_calibration.unit = "nm"
        assertEquals(ij_calibration.unit, "nm")
        assertEquals(Calibration.new(ij_calibration).unit(), Unit.NANO)
    }

    @Test
    fun can_extract_micron_unit()
    {
        val ij_calibration = ImageJCalibration()
        ij_calibration.unit = "micron"
        assertEquals(ij_calibration.unit, "micron")
        assertEquals(Calibration.new(ij_calibration).unit(), Unit.MICRO)
    }

    @Test
    fun can_extract_pixel_size()
    {
        val ij_calibration = ImageJCalibration()
        ij_calibration.unit = "micron"
        ij_calibration.pixelWidth = 10.0
        assertEquals(ij_calibration.unit, "micron")
        assertEquals(ij_calibration.pixelWidth, 10.0)
        assertEquals(Calibration.new(ij_calibration).pixel_size(), Size.from(10.0, Unit.MICRO))
    }

    @Test
    fun can_extract_pixel_size_or_default()
    {
        val ij_calibration = ImageJCalibration()
        ij_calibration.unit = "junk"
        ij_calibration.pixelWidth = 10.0
        assertEquals(ij_calibration.unit, "junk")
        assertEquals(ij_calibration.pixelWidth, 10.0)
        val calibration = Calibration.new(ij_calibration)
        assertEquals(calibration.pixel_size(), null)
        assertEquals(calibration.pixel_size_nm_or(100.0), 100.0)
    }
}