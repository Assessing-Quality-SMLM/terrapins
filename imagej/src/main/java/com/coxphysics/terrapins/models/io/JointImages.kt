package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage

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

    fun image_1_filename_nn(): String
    {
        return image_1_.filename_nn()
    }

    fun set_image_1_filename(value: String)
    {
        image_1_.set_filename(value)
    }

    fun image_2(): DiskOrImage
    {
        return image_2_
    }

    fun image_2_filename_nn(): String
    {
        return image_2_.filename_nn()
    }

    fun set_image_2_filename(value: String)
    {
        image_2_.set_filename(value)
    }
}