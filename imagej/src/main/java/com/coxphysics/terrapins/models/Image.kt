package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.ij_wrapping.ImageSelector
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import java.nio.file.Path

class Image private constructor(private var image_: ImagePlus?)
{
    companion object
    {
        @JvmStatic
        fun with(image: ImagePlus?) : Image
        {
            return Image(image)
        }

        @JvmStatic
        fun from(image: ImagePlus) : Image
        {
            return with(image)
        }

        @JvmStatic
        fun from_title(title: String) : Image
        {
            val image = ImageSelector.get_image_from_title(title)
            return if (image == null) empty() else  with(image)
        }

        @JvmStatic
        fun empty() : Image
        {
            return with(null)
        }

        @JvmStatic
        fun default_from_manager() : Image
        {
            val image = ImageSelector.get_image_from_index(0)
            return with(image)
        }
    }

    fun has_data(): Boolean
    {
        return image_ != null
    }

    fun to_image_plus(): ImagePlus?
    {
        return image_
    }

    fun title(): String?
    {
        return image_?.title
    }

    fun title_nn(): String
    {
        return title().non_null()
    }

    fun set_inner(ij_image: ImagePlus?)
    {
        image_ = ij_image
    }

    fun write_to_disk(image_path: Path): Path?
    {
        return if (image_ == null) null else IJUtils.write_to_disk(image_, image_path)
    }
}