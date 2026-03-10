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
        settings.set_working_directory(Paths.get("something"))
        assertEquals(settings.working_directory(), Paths.get("something"))
    }

    @Test
    fun can_detect_settings_file()
    {
        val settings = CoreSettings.default()
        assertEquals(settings.has_settings_file(), false)
        assertEquals(settings.settings_file_nn(), "")

        settings.set_settings_file("something")
        assertEquals(settings.has_settings_file(), true)
        assertEquals(settings.settings_file_nn(), "something")
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
        }.get()
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
        }.get()
    }
}