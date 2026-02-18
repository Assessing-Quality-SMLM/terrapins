package com.coxphysics.terrapins.view_models.hawk

import com.coxphysics.terrapins.models.hawk.*
import com.coxphysics.terrapins.models.io.PathSelector
import com.coxphysics.terrapins.view_models.TERRAPINS.ImageSelectorVM
import com.coxphysics.terrapins.view_models.TERRAPINS.PathSelectorVM
import ij.ImagePlus
import java.awt.Color

class HAWKVM private constructor(private var settings_: Settings)
{
    private var image_selector_vm_ : ImageSelectorVM = ImageSelectorVM.with_image(settings_.inner_image())
    private var output_file_vm_: PathSelectorVM = PathSelectorVM.with(settings_.file_path_wrapper())
    private var n_levels_default_colour_: Color? = null
    private var n_levels_error_colour_: Color = Color.RED

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

        // For Java
        @JvmStatic
        fun default_(): HAWKVM
        {
            return default()
        }
    }

    fun image_selector_vm(): ImageSelectorVM
    {
        return image_selector_vm_
    }

    fun image_name() : String
    {
        return settings_.image_name()
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
            return n_levels_default_colour()
        if (error_string == "")
            return n_levels_default_colour()
        return n_levels_error_colour()
    }

    fun n_levels_error_colour(): Color
    {
        return n_levels_error_colour_
    }

    fun n_levels_default_colour(): Color?
    {
        return n_levels_default_colour_
    }

    fun set_n_levels_default_colour(colour: Color?)
    {
        n_levels_default_colour_ = colour
    }

    fun propogate_image_selection()
    {
        val image = image_selector_vm_.get_image();
        settings_.set_image(image)
    }

    fun output_file_vm(): PathSelectorVM
    {
        return output_file_vm_
    }

    fun save_to_disk(): Boolean
    {
        PathSelectorVM.with(settings_.file_path_wrapper()).find_path()
        return HAWK.from(settings_).save_to_disk()
    }
}