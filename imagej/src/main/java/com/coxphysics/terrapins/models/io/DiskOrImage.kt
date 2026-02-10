package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.ij_wrapping.ImageSelector
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import ij.plugin.frame.Recorder
import java.nio.file.Path
import kotlin.io.path.exists

class DiskOrImage private constructor(
    private var filename_: PathWrapper,
    private var image_: Image,
    private var use_image_: Boolean
)
{
    companion object
    {
        @JvmStatic
        fun new(filepath: PathWrapper, image: Image, use_image: Boolean) : DiskOrImage
        {
            return DiskOrImage(filepath, image, use_image)
        }

        @JvmStatic
        fun from_filepath(path_wrapper: PathWrapper) : DiskOrImage
        {
            return new(path_wrapper, Image.empty(), false)
        }

        @JvmStatic
        fun from_filename(filename: String?) : DiskOrImage
        {
            return from_filepath(PathWrapper.from_optional_string(filename))
        }

        @JvmStatic
        fun from_path(path: Path) : DiskOrImage
        {
            return from_filepath(PathWrapper.from(path))
        }

        @JvmStatic
        fun from_image(image: Image) : DiskOrImage
        {
            return new(PathWrapper.empty(), image, true)
        }

        @JvmStatic
        fun default() : DiskOrImage
        {
            return new(PathWrapper.empty(), Image.empty(), false)
        }

        @JvmStatic
        fun from_macro_options_with(key: String, options: MacroOptions) : DiskOrImage?
        {
            val name = options.get(key)
            if (name == null)
                return null
            val name_as_title = ImageSelector.get_image_from_title(name)
            if(name_as_title != null)
                return from_image(Image.from(name_as_title))

            val name_as_path = name.to_nullable_path()
            if (name_as_path != null) // && name_as_path.exists()) -> can fix with filesystem interface - this exists needs adapting
                return from_path(name_as_path)
            return null
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
            return filename_path()
        }
        if (use_image())
        {
            return image_.write_to_disk(image_path)
        }
        return null
    }

    fun macro_string() : String
    {
        return if (use_disk()) filename_nn() else image_.title_nn()
    }

    fun record_to_macro_with(key: String)
    {
        Recorder.recordOption(key, macro_string() )
    }
}