package com.coxphysics.terrapins.models.hawk

import com.coxphysics.terrapins.models.log.ListLog
import ij.ImagePlus
import ij.ImageStack
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HAWKTests
{

    @Test
    fun calibration_data_preserved()
    {
        val data = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8).toIntArray()
        val stack = ImageStack(1, 1, data.size)
        var count = 1;
        for (value in data)
        {
            val pixel_data = listOf(value.toFloat()).toFloatArray()
            stack.setPixels(pixel_data, count)
            count++;
        }
        val image = ImagePlus("image", stack)
        image.calibration.unit = "fake_unit"
        val settings = Settings.from(1, NegativeValuesPolicy.ABSOLUTE, OutputStyle.SEQUENTIAL)
        settings.set_image(image)
        val hawk = HAWK.new(settings, ListLog())
        val filtered_image = hawk.get_hawk_image()
        assertEquals(filtered_image!!.calibration.unit, "fake_unit");
    }

    @Test
    fun frame_interval_zeroed_on_calibration_data()
    {
        val data = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8).toIntArray()
        val stack = ImageStack(1, 1, data.size)
        var count = 1;
        for (value in data)
        {
            val pixel_data = listOf(value.toFloat()).toFloatArray()
            stack.setPixels(pixel_data, count)
            count++
        }
        val image = ImagePlus("image", stack)
        image.getCalibration().setUnit("fake_unit")
        image.getCalibration().frameInterval = 100.0
        assertEquals(image.getCalibration().frameInterval, 100.0);
        val settings = Settings.from(1, NegativeValuesPolicy.ABSOLUTE, OutputStyle.SEQUENTIAL)
        settings.set_image(image)
         val hawk = HAWK.new(settings, ListLog())
        val filtered_image = hawk.get_hawk_image()
        assertEquals(filtered_image!!.calibration.unit, "fake_unit")
        assertEquals(filtered_image.calibration.frameInterval, 0.0)
    }
}