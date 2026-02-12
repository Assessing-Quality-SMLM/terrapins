package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.io.JointImages

class JointImagesVM private constructor(
    private val model_: JointImages,
    private val label_a_: String,
    private val label_b_: String
)
{
    private var image_1_ = DiskOrImageVM.from(model_.image_1())
    private var image_2_ = DiskOrImageVM.from(model_.image_2())

    companion object
    {
        @JvmStatic
        fun from(model: JointImages, label_a: String, label_b: String): JointImagesVM
        {
            return JointImagesVM(model, label_a, label_b)
        }

        @JvmStatic
        fun default(): JointImagesVM
        {
            return from(JointImages.default(), "Image a", "Image b")
        }

        // for calls from Java
        @JvmStatic
        fun default_(): JointImagesVM
        {
            return default()
        }
    }

    fun image_a_label(): String
    {
        return label_a_
    }

    fun image_1_vm(): DiskOrImageVM
    {
        return image_1_
    }

    fun image_b_label(): String
    {
        return label_b_
    }

    fun image_2_vm(): DiskOrImageVM
    {
        return image_2_
    }
}