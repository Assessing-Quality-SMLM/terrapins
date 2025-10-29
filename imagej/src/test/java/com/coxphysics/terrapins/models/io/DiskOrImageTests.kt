package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage
import ij.ImagePlus
import ij.process.FloatProcessor
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
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
    fun filename_is_null()
    {
        val disk_or_image = DiskOrImage.default()
        assertEquals(disk_or_image.filename(), null)
    }

    @Test
    fun filename_set_have_data()
    {
        val disk_or_image = DiskOrImage.new("something", null, false)
        assertEquals(disk_or_image.has_data(), true)
    }

    @Test
    fun if_using_disk_return_disk_filepath()
    {
        val disk_or_image = DiskOrImage.from_filename("something.tiff" )
        assertEquals(disk_or_image.use_disk(), true)
        assertEquals(disk_or_image.filepath(Paths.get("else.tiff")), Paths.get("something.tiff"))
    }

    @Test
    fun if_using_image_return_image_filepath()
    {
        val disk_or_image = DiskOrImage.default()
        disk_or_image.set_use_image(true)
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.filepath(Paths.get("else.tiff")), Paths.get("else.tiff"))
    }

    @Test
    fun image_set_have_data()
    {
        val processor = FloatProcessor(0, 0)
        val image = ImagePlus("An image", processor)
        val disk_or_image = DiskOrImage.new(null, image, false)
        assertEquals(disk_or_image.has_data(), true)
    }

    @Test
    fun set_use_disk_test()
    {
        val disk_or_image = DiskOrImage.new(null, null, true)
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_use_disk(true)
        assertEquals(disk_or_image.use_image(), false)
        assertEquals(disk_or_image.use_disk(), true)
    }

    @Test
    fun set_use_image_test()
    {
        val disk_or_image = DiskOrImage.new(null, null, true)
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_use_image(false)
        assertEquals(disk_or_image.use_image(), false)
        assertEquals(disk_or_image.use_disk(), true)
    }

    @Test
    fun usage_is_not_switched_on_filename_change()
    {
        val disk_or_image = DiskOrImage.new(null, null, true)
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_filename("something")
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)
    }

    @Test
    fun change_usage_on_filename_setting()
    {
        val disk_or_image = DiskOrImage.new(null, null, true)
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_filename_and_switch_usage("something")
        assertEquals(disk_or_image.use_image(), false)
        assertEquals(disk_or_image.use_disk(), true)
    }
}