package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage
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
        val image_1_path = image_1_.to_disk_with(image_1_name_in(directory))
        val image_2_path = image_2_.to_disk_with(image_2_name_in(directory))
        return image_1_path != null && image_2_path != null
    }
}