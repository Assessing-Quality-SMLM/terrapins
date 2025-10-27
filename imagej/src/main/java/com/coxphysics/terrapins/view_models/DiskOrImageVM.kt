package com.coxphysics.terrapins.view_models

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.views.ImageSelectorSetttings
import ij.ImagePlus

class DiskOrImageVM private constructor(
    private var model_: DiskOrImage,
    private var name_: String,
    private var image_selector_settings_ : ImageSelectorSetttings,
    private var draw_reset_button_: Boolean)
{
    companion object
    {
        @JvmStatic
        fun new(name: String, model: DiskOrImage, image_selector_settings: ImageSelectorSetttings, draw_reset_button: Boolean) : DiskOrImageVM
        {
            return DiskOrImageVM(model, name, image_selector_settings, draw_reset_button)
        }

        @JvmStatic
        fun with(name: String, model: DiskOrImage) : DiskOrImageVM
        {
            val settings = ImageSelectorSetttings.default_()
            settings.set_n_images(1)
            settings.set_image_names(listOf("Image").toTypedArray())
            settings.set_visible(true)
            return new(name, model, settings, true)
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

    fun image_selector_settings(): ImageSelectorSetttings
    {
        return image_selector_settings_
    }

    fun draw_reset_button(): Boolean
    {
        return draw_reset_button_
    }

    fun set_filename(filename: String)
    {
        model_.set_filename(filename)
    }

    fun set_image(image: ImagePlus)
    {
        model_.set_image(image)
    }

    fun set_draw_reset_button(value: Boolean)
    {
        draw_reset_button_ = value
    }
}