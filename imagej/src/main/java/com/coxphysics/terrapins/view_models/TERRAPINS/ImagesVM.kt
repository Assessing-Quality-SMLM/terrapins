package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.images.Settings

class ImagesVM private constructor(private var settings_: Settings)
{
    private val drift_split_vm_ = JointImagesVM.from(settings_.drift_split_model())
    private val half_split_vm_ = JointImagesVM.from(settings_.half_split_model())
    private val zip_split_vm_ = JointImagesVM.from(settings_.zip_split_model())

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
    fun drift_split_vm() : JointImagesVM
    {
        return drift_split_vm_
    }

    fun half_split_vm() : JointImagesVM
    {
        return half_split_vm_
    }

    fun zip_split_vm() : JointImagesVM
    {
        return zip_split_vm_
    }
}