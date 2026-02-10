package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.ij_wrapping.ImageSelector
import ij.ImagePlus

class ImageSelectorVM private constructor(private val selector_: ImageSelector)
{
    companion object
    {
        @JvmStatic
        fun from(selector: ImageSelector): ImageSelectorVM
        {
            return ImageSelectorVM(selector)
        }

        @JvmStatic
        fun with_image(image: Image): ImageSelectorVM
        {
            val selector = ImageSelector.from(image)
            return from(selector)
        }

        @JvmStatic
        fun default(): ImageSelectorVM
        {
            return from(ImageSelector.default())
        }

        // for Java
        @JvmStatic
        fun default_(): ImageSelectorVM
        {
            return default()
        }
    }

    fun image_titles(): Array<String>?
    {
        return selector_.get_image_titles()
    }

    fun get_image(): ImagePlus?
    {
        return selector_.get_image()
    }

    fun set_image(index: Int)
    {
        selector_.set_current_image_index(index)
    }
}