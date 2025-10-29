package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import java.io.File
import java.nio.file.Path

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
        fun default() : DiskOrImage
        {
            return new(null, null, true)
        }
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

    fun has_data(): Boolean
    {
        return filename_ != null || image_ != null
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
        return filename()?.let{s -> File(s).toPath() }
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

    fun set_filename(filename: String)
    {
        filename_ = filename
    }

    fun set_image(image: ImagePlus)
    {
        image_ = image
    }

    fun to_disk_in(image_path: Path): Path?
    {
        if (use_disk())
            return filename_path()
        if (image_ == null)
            return null
        return IJUtils.write_to_disk(image_, image_path)
    }
}