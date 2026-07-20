package com.coxphysics.terrapins.view_models

import com.coxphysics.terrapins.models.DiskOrImage
import ij.ImagePlus

class DiskOrImageVM private constructor(
    private var model_: DiskOrImage,
    private var name_: String,
    private var draw_reset_button_: Boolean)
{
    companion object
    {
        @JvmStatic
        fun new(name: String, model: DiskOrImage, draw_reset_button: Boolean) : DiskOrImageVM
        {
            return DiskOrImageVM(model, name, draw_reset_button)
        }

        @JvmStatic
        fun with(name: String, model: DiskOrImage) : DiskOrImageVM
        {

            return new(name, model, true)
        }
    }

    fun name(): String
    {
        return name_
    }

    fun use_disk(): Boolean
    {
        return model_.use_image()
    }

    fun filename_nn(): String
    {
        return model_.filename_nn()
    }

    fun draw_reset_button(): Boolean
    {
        return draw_reset_button_
    }

    fun set_filename(filename: String)
    {
        model_.set_filename(filename)
    }

    fun set_name(value: String)
    {
        name_ = value
    }

    fun set_image(image: ImagePlus)
    {
        model_.set_image(image)
    }

    fun set_use_disk(value: Boolean)
    {
        model_.set_use_disk(value)
    }

    fun set_draw_reset_button(value: Boolean)
    {
        draw_reset_button_ = value
    }
}