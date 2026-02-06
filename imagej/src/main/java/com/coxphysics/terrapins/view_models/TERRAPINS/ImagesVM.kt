package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.images.Settings

class ImagesVM private constructor(private var settings_: Settings)
{
    companion object
    {
        @JvmStatic
        fun from(settings: Settings): ImagesVM
        {
            return ImagesVM(settings)
        }

        @JvmStatic
        fun default(): ImagesVM
        {
            return from(Settings.default())
        }

        // for calls from Java
        @JvmStatic
        fun default_(): ImagesVM
        {
            return default()
        }
    }
}