package com.coxphysics.terrapins.models.hawk

import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.utils.StringUtils
import com.coxphysics.terrapins.plugins.*
import ij.ImagePlus
import ij.process.FloatProcessor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class SettingsTests
{
    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

    @Test
    fun can_parse_image_name()
    {
        val options = MacroOptions.from("image_name=something")
        val image = ImagePlus("else", FloatProcessor(1, 1))
        val window_manager = FakeWindowManager.from(mapOf(Pair("something", image)))
        val settings = Settings.from_macro_options_with(options, window_manager)
        assertEquals(settings.image_name(), "else")
    }

    @Test
    fun if_image_not_in_manager_nothing_set()
    {
        val options = MacroOptions.from("image_name=something")
        val settings = Settings.from_macro_options_with(options, FakeWindowManager.empty())
        assertEquals(settings.image_name(), StringUtils.EMPTY_STRING)
    }

    @Test
    fun can_parse_n_levels()
    {
        val options = MacroOptions.from("n_levels=10")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.n_levels(), 10)
    }

    @Test
    fun n_levels_not_set_on_parse_failure()
    {
        val options = MacroOptions.from("n_levels=junk")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.n_levels(), 3)
    }

    @Test
    fun n_levels_not_set_for_missing_key()
    {
        val options = MacroOptions.from("")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.n_levels(), 3)
    }

    @Test
    fun negative_value_abs_policy_can_be_set()
    {
        val options = MacroOptions.from("negative_values=ABS")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.is_absolute(), true)
        assertEquals(settings.is_separate(), false)
    }

    @Test
    fun negative_value_separate_policy_can_be_set()
    {
        val options = MacroOptions.from("negative_values=separate")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.is_absolute(), false)
        assertEquals(settings.is_separate(), true)
    }

    @Test
    fun output_style_sequential_policy_can_be_set()
    {
        val options = MacroOptions.from("output_style=sequential")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.is_sequential(), true)
        assertEquals(settings.is_temporal(), false)
    }

    @Test
    fun output_style_temporal_policy_can_be_set()
    {
        val options = MacroOptions.from("output_style=temporal")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.is_sequential(), false)
        assertEquals(settings.is_temporal(), true)
    }

    @Test
    fun can_set_file_to_save_to()
    {
        val options = MacroOptions.from("save_to_disk=something")
        val settings = Settings.from_macro_options(options)
        assertEquals(settings.filename(), "something")
    }

    @Test
    fun round_trip_macro_io()
    {
        executor.submit {
            val settings = Settings.default()
            val image = ImagePlus("an_image", FloatProcessor(1, 1))
            settings.set_image(image)
            settings.set_filename("something")
            settings.set_n_levels(100)
            settings.set_output_style(OutputStyle.TEMPORAL)
            settings.set_negative_handling(NegativeValuesPolicy.SEPARATE)
            settings.record_values()

            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get(HAWK_IMAGE_NAME), "an_image")
            assertEquals(options.get(HAWK_SAVE_TO_DISK), "something")
            assertEquals(options.get(HAWK_N_LEVELS), "100")
            assertEquals(options.get(HAWK_OUTPUT_STYLE), "temporal")
            assertEquals(options.get(HAWK_NEGATIVE_VALUES), "separate")

            val window_manager = FakeWindowManager.from(mapOf(Pair("an_image", image)))
            val new_settings = Settings.from_macro_options_with(options, window_manager)
            assertEquals(new_settings.image(), image)
            assertEquals(new_settings.filename(), settings.filename())
            assertEquals(new_settings.n_levels(), settings.n_levels())
            assertEquals(new_settings.is_absolute(), settings.is_absolute())
            assertEquals(new_settings.is_separate(), settings.is_separate())
            assertEquals(new_settings.is_temporal(), settings.is_temporal())
            assertEquals(new_settings.is_sequential(), settings.is_sequential())
        }.get()

    }
}