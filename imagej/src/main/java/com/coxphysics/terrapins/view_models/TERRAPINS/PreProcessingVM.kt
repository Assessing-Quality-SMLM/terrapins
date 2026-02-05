package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.view_models.hawk.HAWKVM

class PreProcessingVM private constructor(private var settings_: Settings)
{
    private var hawk_vm_ : HAWKVM = HAWKVM.from(settings_.hawk_settings())

    companion object
    {
        @JvmStatic
        fun from(settings: Settings) : PreProcessingVM
        {
            return PreProcessingVM(settings)
        }

        @JvmStatic
        fun default() : PreProcessingVM
        {
            return from(Settings.default())
        }

        // for calls from Java
        @JvmStatic
        fun default_() : PreProcessingVM
        {
            return default()
        }
    }

    fun hawk_vm() : HAWKVM
    {
        return hawk_vm_
    }
}