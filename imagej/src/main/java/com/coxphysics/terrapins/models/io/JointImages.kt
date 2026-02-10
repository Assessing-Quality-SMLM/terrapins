package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.macros.MacroOptions
import java.nio.file.Path

class JointImages private constructor(
    private val image_1_: DiskOrImage,
    private val image_2_: DiskOrImage)
{
    companion object
    {
        @JvmStatic
        fun new(image_1: DiskOrImage, image_2: DiskOrImage) : JointImages
        {
            return JointImages(image_1, image_2)
        }

        @JvmStatic
        fun default() : JointImages
        {
            return new(DiskOrImage.default(), DiskOrImage.default())
        }

        @JvmStatic
        fun from_macro_options_with(key_1: String, key_2: String, options: MacroOptions) : JointImages?
        {
            val image_1 = DiskOrImage.from_macro_options_with(key_1, options) ?: return null
            val image_2 = DiskOrImage.from_macro_options_with(key_2, options) ?: return null
            return new(image_1, image_2)
        }
    }

    fun is_valid(): Boolean
    {
        return image_1_.has_data() && image_2_.has_data()
    }

    fun image_1(): DiskOrImage
    {
        return image_1_
    }

    fun image_1_filepath(directory: Path) : Path?
    {
        return image_1().filepath(image_1_name_in(directory))
    }

    fun set_image_1_filename(value: String)
    {
        image_1_.set_filename_and_switch_usage(value)
    }

    fun image_2(): DiskOrImage
    {
        return image_2_
    }

    fun image_2_filepath(directory: Path) : Path?
    {
        return image_2().filepath(image_2_name_in(directory))
    }

    fun set_image_2_filename(value: String)
    {
        image_2_.set_filename_and_switch_usage(value)
    }

    private fun image_1_name_in(directory: Path): Path
    {
        return directory.resolve("image_1.tiff")
    }

    private fun image_2_name_in(directory: Path): Path
    {
        return directory.resolve("image_2.tiff")
    }

    fun to_disk_in(directory: Path): Boolean
    {
        var image_1_ok = true
        var image_2_ok = true
        if (image_1_.has_data())
        {
            image_1_ok = image_1_.to_disk_with(image_1_name_in(directory)) != null
        }
        if (image_2_.has_data())
        {
            image_2_ok = image_2_.to_disk_with(image_2_name_in(directory)) != null
        }
        return image_1_ok && image_2_ok
    }

    fun record_to_macro_with(key_1: String, key_2: String)
    {
        image_1_.record_to_macro_with(key_1)
        image_2_.record_to_macro_with(key_2)
    }
}