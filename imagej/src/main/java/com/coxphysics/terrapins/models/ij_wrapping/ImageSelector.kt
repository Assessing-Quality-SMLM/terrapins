package com.coxphysics.terrapins.models.ij_wrapping

import ij.ImagePlus
import ij.WindowManager

// combo box will return -1 for unset or invalid option
private const val INVALID_INDEX: Int = -1

class ImageSelector private constructor(private var current_image_: Int)
{
    companion object
    {
        @JvmStatic
        fun default(): ImageSelector
        {
            return ImageSelector(INVALID_INDEX)
        }
    }

    fun set_current_image_index(index: Int)
    {
        current_image_ = index
    }

    fun image_titles(): Array<String>?
    {
        return WindowManager.getImageTitles()
    }

    fun get_image(): ImagePlus?
    {
        if (current_image_ == INVALID_INDEX)
            return null
        val selected_image = current_image_
        val id = WindowManager.getNthImageID(selected_image + 1)
        return WindowManager.getImage(id)
    }
}