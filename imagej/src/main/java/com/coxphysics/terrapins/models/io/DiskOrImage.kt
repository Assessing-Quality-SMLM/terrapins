package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import java.nio.file.Path

class DiskOrImage private constructor(
    private var filename_: PathWrapper,
    private var image_: Image,
    private var use_image_: Boolean
)
{
    companion object
    {
        @JvmStatic
        fun new(filename: String?, image: Image, use_image: Boolean) : DiskOrImage
        {
            return DiskOrImage(PathWrapper.from_optional_string(filename), image, use_image)
        }

        @JvmStatic
        fun from_filename(filename: String?) : DiskOrImage
        {
            return new(filename, Image.empty(), false)
        }

        @JvmStatic
        fun from_image(image: Image) : DiskOrImage
        {
            return new(null, image, true)
        }

        @JvmStatic
        fun default() : DiskOrImage
        {
            return new(null, Image.empty(), false)
        }
    }

    fun path_wrapper(): PathWrapper
    {
        return filename_
    }

    fun image_wrapper(): Image
    {
        return image_
    }

    fun has_data(): Boolean
    {
        if (use_disk())
            return filename_.has_data()
        if (use_image())
            return image_.has_data()
        return false
    }

    fun use_image(): Boolean
    {
        return use_image_
    }

    fun use_disk(): Boolean
    {
        return !use_image()
    }

    fun set_use_image(value: Boolean)
    {
        use_image_ = value
    }

    fun set_use_disk(value: Boolean)
    {
        set_use_image(!value)
    }

    fun filename_nn(): String
    {
        return filename_.to_string()
    }

    fun filename(): String?
    {
        if (use_disk())
        {
            return filename_nn()
        }
        return null
    }

    fun filepath(image_path: Path): Path?
    {
        if (use_image())
            return image_path
        return filename_path()
    }

    private fun filename_path(): Path?
    {
        return filename_.path()
    }

    fun image(): ImagePlus?
    {
        if(use_image())
        {
            return image_.to_image_plus()
        }
        return null
    }

    fun load_image(): ImagePlus?
    {
        if (use_image())
            return image_.to_image_plus()
        return filename_path()?.let{p -> IJUtils.load_image(p)}
    }

    fun set_filename_and_switch_usage(filename: String)
    {
        set_filename(filename)
        if (!use_disk())
            set_use_disk(true)
    }

    fun set_filename(filename: String)
    {
        filename_.set_path_from_string(filename)
    }

    fun set_image(image: ImagePlus)
    {
        image_.set_inner(image)
    }

    fun to_disk_with(image_path: Path): Path?
    {
        if (use_disk())
        {
            if (filename_.path_valid())
            {
                return filename_path()
            }
            else
            {
                return null
            }
        }
        if (use_image())
        {
            return image_.write_to_disk(image_path)
        }
        return null
    }
}