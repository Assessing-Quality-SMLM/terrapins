package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.utils.StringUtils
import ij.ImagePlus
import ij.plugin.frame.Recorder
import ij.process.FloatProcessor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiskOrImageTests
{
    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

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
    fun filename_is_not_null_on_default()
    {
        val disk_or_image = DiskOrImage.default()
        assertEquals(disk_or_image.use_image(), false)
        assertEquals(disk_or_image.filename(), StringUtils.EMPTY_STRING)
    }

    @Test
    fun filename_set_have_data()
    {
        val disk_or_image = DiskOrImage.from_filename("something")
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
        val disk_or_image = DiskOrImage.from_image(Image.from(image))
        assertEquals(disk_or_image.has_data(), true)
    }

    @Test
    fun set_use_disk_test()
    {
        val disk_or_image = DiskOrImage.from_image(Image.empty())
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_use_disk(true)
        assertEquals(disk_or_image.use_image(), false)
        assertEquals(disk_or_image.use_disk(), true)
    }

    @Test
    fun set_use_image_test()
    {
        val disk_or_image = DiskOrImage.from_image(Image.empty())
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_use_image(false)
        assertEquals(disk_or_image.use_image(), false)
        assertEquals(disk_or_image.use_disk(), true)
    }

    @Test
    fun usage_is_not_switched_on_filename_change()
    {
        val disk_or_image = DiskOrImage.from_image(Image.empty())
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_filename("something")
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)
    }

    @Test
    fun change_usage_on_filename_setting()
    {
        val disk_or_image = DiskOrImage.from_image(Image.empty())
        assertEquals(disk_or_image.use_image(), true)
        assertEquals(disk_or_image.use_disk(), false)

        disk_or_image.set_filename_and_switch_usage("something")
        assertEquals(disk_or_image.use_image(), false)
        assertEquals(disk_or_image.use_disk(), true)
    }

    @Test
    fun use_image_title_for_recording()
    {
        executor.submit{
            val data = listOf(1.0)
            val image = ImagePlus("some", FloatProcessor(1, 1, data.toDoubleArray()))
            val disk_or_image = DiskOrImage.from_image(Image.from(image))
            MacroOptions.reset()
            disk_or_image.record_to_macro_with("thing")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get("thing"), "some")
        }.get()
    }

    @Test
    fun use_filepath_for_recording()
    {
        executor.submit {
            val disk_or_image = DiskOrImage.from_filename("some")
            MacroOptions.reset()
            disk_or_image.record_to_macro_with("thing")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get("thing"), "some")
        }.get()
    }

    @Test
    fun strings_that_can_be_paths_are_treated_as_disk_images()
    {
        executor.submit {
            val options = MacroOptions.from("a=thing")
            val disk_or_image = DiskOrImage.from_macro_options_with("a", options)
            assertEquals(disk_or_image!!.filename_nn(), "thing")
        }.get()
    }

    @Test
    fun empty_strings_are_not_recorded()
    {
        executor.submit {
            val disk_or_image = DiskOrImage.default()
            assertEquals(disk_or_image.filename_nn(), StringUtils.EMPTY_STRING)
            MacroOptions.reset()
            disk_or_image.record_to_macro_with("a")
            val options = MacroOptions.from_recorder_command_options()
            val value = options.get("a")
            assertNull(value)
        }.get()
    }

}