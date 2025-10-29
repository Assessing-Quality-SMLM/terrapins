package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.assertEquals

class JointImagesTests
{
    @Test
    fun to_disk_fails_if_image_1_fails()
    {
        val image_1 = DiskOrImage.default()
        image_1.set_use_disk(true)
        assertEquals(image_1.use_disk(), true)
        assertEquals(image_1.filename(), null)

        val settings = JointImages.new(image_1, DiskOrImage.default())
        val result = settings.to_disk_in(Paths.get("some", "thing"))
        assertEquals(result, false)
    }

    @Test
    fun to_disk_fails_if_image_2_fails()
    {
        val image_1 = DiskOrImage.from_filename("an_image.tiff")
        image_1.set_use_disk(true)
        assertEquals(image_1.use_disk(), true)
        assertEquals(image_1.filename(), "an_image.tiff")

        val image_2 = DiskOrImage.default()
        image_2.set_use_disk(true)
        assertEquals(image_2.use_disk(), true)
        assertEquals(image_2.filename(), null)

        val settings = JointImages.new(image_1, image_2)
        val result = settings.to_disk_in(Paths.get("some", "thing"))
        assertEquals(result, false)
    }

    @Test
    fun to_disk_ok_if_all_images_ok()
    {
        val image_1 = DiskOrImage.from_filename("an_image.tiff")
        image_1.set_use_disk(true)
        assertEquals(image_1.use_disk(), true)
        assertEquals(image_1.filename(), "an_image.tiff")

        val image_2 = DiskOrImage.from_filename("another_image.tiff")
        image_2.set_use_disk(true)
        assertEquals(image_2.use_disk(), true)
        assertEquals(image_2.filename(), "another_image.tiff")

        val settings = JointImages.new(image_1, image_2)
        val result = settings.to_disk_in(Paths.get("some", "thing"))
        assertEquals(result, true)
    }
}