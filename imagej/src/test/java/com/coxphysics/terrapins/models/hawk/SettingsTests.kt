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
}