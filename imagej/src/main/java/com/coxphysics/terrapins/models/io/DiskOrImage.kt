package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.IJUtils
import com.coxphysics.terrapins.models.utils.StringUtils
import ij.ImagePlus
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class DiskOrImage private constructor(
    private var filename_: String?,
    private var image_: ImagePlus?,
    private var use_image_: Boolean
)
{
    companion object
    {
        @JvmStatic
        fun new(filename: String?, image: ImagePlus?, use_image: Boolean) : DiskOrImage
        {
            return DiskOrImage(filename, image, use_image)
        }

        @JvmStatic
        fun from_filename(filename: String?) : DiskOrImage
        {
            return new(filename, null, false)
        }

        @JvmStatic
        fun from_image(image: ImagePlus) : DiskOrImage
        {
            return new(null, image, true)
        }

        @JvmStatic
        fun default() : DiskOrImage
        {
            return new(null, null, false)
        }
    }

    fun has_data(): Boolean
    {
        if (use_disk())
            return StringUtils.path_set(filename_)
        if (use_image())
            return image_ != null
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

    fun flip_mode()
    {
        set_use_image(!use_image_)
    }

    fun filename_nn(): String
    {
        return filename_.non_null()
    }

    fun filename(): String?
    {
        if (use_disk())
        {
            return filename_
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
        return filename()?.let{s -> Paths.get(s) }
    }

    fun image(): ImagePlus?
    {
        if(use_image())
        {
            return image_
        }
        return null
    }

    fun load_image(): ImagePlus?
    {
        if (use_image())
            return image_
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
        filename_ = filename
    }

    fun set_image(image: ImagePlus)
    {
        image_ = image
    }

    fun to_disk_with(image_path: Path): Path?
    {
        if (use_disk())
        {
            if (StringUtils.path_set(filename_))
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
            if (image_ == null)
                return null
            else
            {
                return IJUtils.write_to_disk(image_, image_path)
            }
        }
        return null
    }
}