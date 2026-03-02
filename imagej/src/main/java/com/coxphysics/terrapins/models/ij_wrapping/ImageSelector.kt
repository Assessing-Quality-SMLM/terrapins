package com.coxphysics.terrapins.models.ij_wrapping

import com.coxphysics.terrapins.models.Image
import ij.ImagePlus
import ij.WindowManager

// combo box will return -1 for unset or invalid option
private const val INVALID_INDEX: Int = -1

private fun get_image_from_manager(index: Int): ImagePlus?
{
    if (index == INVALID_INDEX)
        return null
    val id = WindowManager.getNthImageID(index + 1)
    return WindowManager.getImage(id)
}

private fun get_image_index_from_manager(image: ImagePlus): Int?
{
    val n_images = WindowManager.getImageCount()
    for (idx in 0..n_images)
    {
        val id = WindowManager.getNthImageID(idx)
        val test_image = WindowManager.getImage(id)
        if (test_image == null)
            continue
        if (test_image == image)
            return idx
    }
    return null
}

// Image.with(get_image_from_manager(current_image_))
class ImageSelector private constructor(
    private val window_manager_: com.coxphysics.terrapins.models.ij_wrapping.WindowManager,
    private var current_image_: Int,
    private var image_ : Image
)
{

    companion object
    {
        @JvmStatic
        fun default(): ImageSelector
        {
            return ImageSelector(IJWindowManager.new(), INVALID_INDEX, Image.empty())
        }

        @JvmStatic
        fun from(image: Image): ImageSelector
        {
            val ip = image.to_image_plus()
            if (ip == null)
                return ImageSelector(IJWindowManager.new(), INVALID_INDEX, image)
            val idx = get_image_index_from_manager(ip)
            if (idx == null)
                return ImageSelector(IJWindowManager.new(), INVALID_INDEX, image)
            return ImageSelector(IJWindowManager.new(), idx, image)
        }

        @JvmStatic
        fun image_titles(): Array<String>?
        {
            return WindowManager.getImageTitles()
        }

        @JvmStatic
        fun get_image_from_title(title: String): ImagePlus?
        {
            return WindowManager.getImage(title)
        }

        @JvmStatic
        fun get_image_from_index(index: Int): ImagePlus?
        {
            return get_image_from_manager(index)
        }
    }

    fun set_current_image_index(index: Int)
    {
        current_image_ = index
        val ip = get_image_from_manager(current_image_)
        image_.set_inner(ip)
    }

    fun get_image_titles(): Array<String>?
    {
        return image_titles()
    }

    fun get_image(): ImagePlus?
    {
        if (current_image_ == INVALID_INDEX)
            return null
        val selected_image = current_image_

        return window_manager_.get_image_at(selected_image)
    }
}