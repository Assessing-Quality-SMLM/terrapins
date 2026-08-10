package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.PathWrapper
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.view_models.hawk.HAWKVM

class PreProcessingVM private constructor(
    private val settings_: Settings,
    private val joint_last_path: PathWrapper)
{
    private var hawk_vm_ : HAWKVM = HAWKVM.from(settings_.hawk_settings(), joint_last_path)

    companion object
    {
        @JvmStatic
        fun from(settings: Settings, joint_last_path: PathWrapper) : PreProcessingVM
        {
            return PreProcessingVM(settings, joint_last_path)
        }

        @JvmStatic
        fun default() : PreProcessingVM
        {
            return from(Settings.default(), PathWrapper.empty())
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