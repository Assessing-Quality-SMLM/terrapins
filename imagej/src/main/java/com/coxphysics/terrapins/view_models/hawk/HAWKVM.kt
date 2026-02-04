package com.coxphysics.terrapins.view_models.hawk

import com.coxphysics.terrapins.models.hawk.NegativeValuesPolicy
import com.coxphysics.terrapins.models.hawk.OutputStyle
import com.coxphysics.terrapins.models.hawk.Settings
import ij.ImagePlus
import java.awt.Color

class HAWKVM private constructor(private var settings_: Settings)
{
    private var n_levels_default_colour: Color? = null
    private var n_levels_error_colour: Color = Color.RED

    companion object
    {
        @JvmStatic
        fun from(settings: Settings): HAWKVM
        {
            return HAWKVM(settings)
        }

        @JvmStatic
        fun default(): HAWKVM
        {
            return from(Settings.default())
        }
    }

    fun image_name() : String
    {
        return settings_.image_name()
    }

    fun set_image(image: ImagePlus)
    {
        settings_.set_image(image)
    }

    fun n_levels() : Int
    {
        return settings_.n_levels()
    }

    fun set_n_levels(value: String) : Boolean
    {
        val i = value.toIntOrNull()
        if (i == null)
        {
            return false
        }
        settings_.set_n_levels(i)
        return true
    }

    fun set_output_style(value: OutputStyle)
    {
        settings_.set_output_style(value)
    }

    fun set_negative_value_policy(value: NegativeValuesPolicy)
    {
        settings_.set_negative_handling(value)
    }

    fun n_levels_colour(): Color?
    {
        val error_string = settings_.error_string()
        if (error_string == null)
            return n_levels_default_colour
        if (error_string == "")
            return n_levels_default_colour
        return n_levels_error_colour
    }

    fun n_levels_error_colour(): Color
    {
        return n_levels_error_colour
    }

    fun set_n_levels_default_colour(colour: Color)
    {
        n_levels_default_colour = colour
    }
}