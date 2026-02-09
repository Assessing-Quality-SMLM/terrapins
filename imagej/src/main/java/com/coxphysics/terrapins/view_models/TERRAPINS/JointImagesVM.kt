package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.io.JointImages

class JointImagesVM private constructor(private val model_: JointImages)
{
    private var image_1_ = DiskOrImageVM.from(model_.image_1())
    private var image_2_ = DiskOrImageVM.from(model_.image_2())

    companion object
    {
        @JvmStatic
        fun from(model: JointImages): JointImagesVM
        {
            return JointImagesVM(model)
        }

        @JvmStatic
        fun default(): JointImagesVM
        {
            return from(JointImages.default())
        }

        // for calls from Java
        @JvmStatic
        fun default_(): JointImagesVM
        {
            return default()
        }
    }

    fun image_1_vm(): DiskOrImageVM
    {
        return image_1_
    }

    fun image_2_vm(): DiskOrImageVM
    {
        return image_2_
    }
}