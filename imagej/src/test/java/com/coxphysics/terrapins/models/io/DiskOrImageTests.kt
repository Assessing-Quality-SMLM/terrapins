package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage
import ij.ImagePlus
import ij.process.FloatProcessor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DiskOrImageTests
{
    @Test
    fun nothing_set_means_no_data()
    {
        val disk_or_image = DiskOrImage.default()
        assertEquals(disk_or_image.has_data(), false)
    }

    @Test
    fun can_get_non_null_filename()
    {
        val disk_or_image = DiskOrImage.default()
        assertEquals(disk_or_image.filename_nn(), "")
    }

    @Test
    fun filename_is_null_so_is_filepath()
    {
        val disk_or_image = DiskOrImage.default()
        assertEquals(disk_or_image.filepath(), null)
    }

    @Test
    fun filename_set_have_data()
    {
        val disk_or_image = DiskOrImage.new("something", null, false)
        assertEquals(disk_or_image.has_data(), true)
    }

    @Test
    fun image_set_have_data()
    {
        val processor = FloatProcessor(0, 0)
        val image = ImagePlus("An image", processor)
        val disk_or_image = DiskOrImage.new(null, image, false)
        assertEquals(disk_or_image.has_data(), true)
    }
}