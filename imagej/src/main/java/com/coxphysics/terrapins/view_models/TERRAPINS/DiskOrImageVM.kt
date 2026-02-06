package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.DiskOrImage

class DiskOrImageVM private constructor(private var model_: DiskOrImage)
{
    companion object
    {
        @JvmStatic
        fun from(model: DiskOrImage): DiskOrImageVM
        {
            return DiskOrImageVM(model)
        }

        @JvmStatic
        fun default(): DiskOrImageVM
        {
            return from(DiskOrImage.default())
        }

        // For Java
        @JvmStatic
        fun default_(): DiskOrImageVM
        {
            return default()
        }
    }

    fun use_image(): Boolean
    {
        return model_.use_image()
    }

    fun flip_mode()
    {
        model_.flip_mode()
    }

}