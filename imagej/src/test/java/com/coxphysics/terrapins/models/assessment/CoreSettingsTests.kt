package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.DiskOrImage
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.assertEquals

class CoreSettingsTests
{
    @Test
    fun widefield_path()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.new(working_directory)
        val expected = working_directory.resolve("widefield.tiff")
        assertEquals(settings.widefield_path(), expected)
    }

    @Test
    fun widefield_changes_usage_on_filename_setting()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.new(working_directory)
        assertEquals(settings.widefield_path(), working_directory.resolve("widefield.tiff"))

        settings.set_widefield_filename("something.tiff")
        assertEquals(settings.widefield_path(), Paths.get("something.tiff"))
    }

    @Test
    fun image_stack_path()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.new(working_directory)
        val expected = working_directory.resolve("image_stack.tiff")
        assertEquals(settings.image_stack_path(), expected)
    }

    @Test
    fun image_stack_changes_usage_on_filename_setting()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.new(working_directory)
        assertEquals(settings.image_stack_path(), working_directory.resolve("image_stack.tiff"))

        settings.set_image_stack_filename("something.tiff")
        assertEquals(settings.image_stack_path(), Paths.get("something.tiff"))
    }

    @Test
    fun to_disk_fails_if_widefield_cannot_write_to_disk()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.new(working_directory)

        val widefield = DiskOrImage.default()
        widefield.set_use_disk(true)
        assertEquals(widefield.use_disk(), true)
        assertEquals(widefield.filename(), null)

        settings.set_widefield(widefield)
        assertEquals(settings.to_disk_in(working_directory), false)
    }

    @Test
    fun to_disk_fails_if_image_stack_cannot_write_to_disk()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.new(working_directory)

        val widefield = DiskOrImage.from_filename("an_image.tiff")
        widefield.set_use_disk(true)
        assertEquals(widefield.use_disk(), true)
        assertEquals(widefield.filename(), "an_image.tiff")
        settings.set_widefield(widefield)

        val image_stack = DiskOrImage.default()
        image_stack.set_use_disk(true)
        assertEquals(image_stack.use_disk(), true)
        assertEquals(image_stack.filename(), null)
        settings.set_image_stack(image_stack)

        assertEquals(settings.to_disk_in(working_directory), false)
    }

    @Test
    fun to_disk_passes_if_both_images_write_to_disk()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.new(working_directory)

        val widefield = DiskOrImage.from_filename("an_image.tiff")
        widefield.set_use_disk(true)
        assertEquals(widefield.use_disk(), true)
        assertEquals(widefield.filename(), "an_image.tiff")
        settings.set_widefield(widefield)

        val image_stack = DiskOrImage.from_filename("another_image.tiff")
        image_stack.set_use_disk(true)
        assertEquals(image_stack.use_disk(), true)
        assertEquals(image_stack.filename(), "another_image.tiff")
        settings.set_image_stack(image_stack)

        assertEquals(settings.to_disk_in(working_directory), true)
    }
}