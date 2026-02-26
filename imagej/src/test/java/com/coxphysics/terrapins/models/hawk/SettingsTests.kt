package com.coxphysics.terrapins.models.hawk

import com.coxphysics.terrapins.models.ij_wrapping.WindowManager
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.utils.StringUtils
import ij.ImagePlus
import ij.process.FloatProcessor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FakeWindowManager private constructor(
    private val map_: Map<String, ImagePlus>
): WindowManager
{
    companion object
    {
        fun from(map: Map<String, ImagePlus>) : FakeWindowManager
        {
            return FakeWindowManager(map)
        }

        fun empty(): FakeWindowManager
        {
            return from(emptyMap())
        }
    }
    override fun get_image(image_name: String): ImagePlus?
    {
        if(map_.containsKey(image_name))
            return map_[image_name]
        return null
    }
}

class SettingsTests
{
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
}