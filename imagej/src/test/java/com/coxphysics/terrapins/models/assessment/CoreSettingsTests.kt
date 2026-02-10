package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.assessment.images.Settings
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_IMAGE_STACK
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_SETTINGS_FILE
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_WIDEFIELD
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_WORKING_DIRECTORY
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CoreSettingsTests
{
    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

    @Test
    fun can_set_working_directory()
    {
        val settings = Settings.default()
        assertNotEquals(settings.working_directory(), Paths.get("something"))
        settings.set_working_directory("something")
        assertEquals(settings.working_directory(), Paths.get("something"))
    }

    @Test
    fun widefield_path()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.from(working_directory)
        settings.widefield().set_use_image(true)
        val expected = working_directory.resolve("widefield.tiff")
        assertEquals(settings.widefield_path(), expected)
    }

    @Test
    fun widefield_changes_usage_on_filename_setting()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.from(working_directory)
        settings.widefield().set_use_image(true)
        assertEquals(settings.widefield_path(), working_directory.resolve("widefield.tiff"))

        settings.set_widefield_filename("something.tiff")
        assertEquals(settings.widefield_path(), Paths.get("something.tiff"))
    }

    @Test
    fun image_stack_path()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.from(working_directory)
        settings.image_stack().set_use_disk(false)
        val expected = working_directory.resolve("image_stack.tiff")
        assertEquals(settings.image_stack_path(), expected)
    }

    @Test
    fun image_stack_changes_usage_on_filename_setting()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.from(working_directory)
        settings.image_stack().set_use_image(true)

        assertEquals(settings.image_stack_path(), working_directory.resolve("image_stack.tiff"))

        settings.set_image_stack_filename("something.tiff")
        assertEquals(settings.image_stack_path(), Paths.get("something.tiff"))
    }

//    @Test
//    fun to_disk_fails_if_widefield_cannot_write_to_disk()
//    {
//        val working_directory = Paths.get("some", "thing")
//        val settings = CoreSettings.from(working_directory)
//
//        val widefield = DiskOrImage.default()
//        widefield.set_use_disk(true)
//        assertEquals(widefield.use_disk(), true)
//        assertEquals(widefield.filename(), null)
//
//        settings.set_widefield(widefield)
//        assertEquals(settings.to_disk_in(working_directory), false)
//    }

//    @Test
//    fun to_disk_fails_if_image_stack_cannot_write_to_disk()
//    {
//        val working_directory = Paths.get("some", "thing")
//        val settings = CoreSettings.from(working_directory)
//
//        val widefield = DiskOrImage.from_filename("an_image.tiff")
//        widefield.set_use_disk(true)
//        assertEquals(widefield.use_disk(), true)
//        assertEquals(widefield.filename(), "an_image.tiff")
//        settings.set_widefield(widefield)
//
//        val image_stack = DiskOrImage.default()
//        image_stack.set_use_disk(true)
//        assertEquals(image_stack.use_disk(), true)
//        assertEquals(image_stack.filename(), null)
//        settings.set_image_stack(image_stack)
//
//        assertEquals(settings.to_disk_in(working_directory), false)
//    }

    @Test
    fun to_disk_passes_if_both_images_write_to_disk()
    {
        val working_directory = Paths.get("some", "thing")
        val settings = CoreSettings.from(working_directory)

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

        assertEquals(settings.to_disk_in(working_directory) != null, true)
    }

    @Test
    fun macro_recording_records_working_directory()
    {
        executor.submit{
            val working_directory = Paths.get("something")
            val settings = CoreSettings.from(working_directory)
            settings.record_to_macro()

            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get(CORE_SETTINGS_WORKING_DIRECTORY), "something")
        }
    }

    @Test
    fun macro_recording_records_settings_file()
    {
        executor.submit {
            val working_directory = Paths.get("something")
            val settings = CoreSettings.from(working_directory)
            settings.set_settings_file("else")
            settings.record_to_macro()

            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get(CORE_SETTINGS_SETTINGS_FILE), "else")
        }
    }

    @Test
    fun macro_recording_records_widefield()
    {
        executor.submit {
            val working_directory = Paths.get("something")
            val settings = CoreSettings.from(working_directory)
            settings.set_widefield(DiskOrImage.from_filename("else"))

            settings.record_to_macro()

            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get(CORE_SETTINGS_WIDEFIELD), "else")
        }
    }

    @Test
    fun macro_recording_records_image_stack()
    {
        executor.submit {
            val working_directory = Paths.get("something")
            val settings = CoreSettings.from(working_directory)
            settings.set_image_stack(DiskOrImage.from_filename("else"))

            settings.record_to_macro()

            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get(CORE_SETTINGS_IMAGE_STACK), "else")
        }
    }
}