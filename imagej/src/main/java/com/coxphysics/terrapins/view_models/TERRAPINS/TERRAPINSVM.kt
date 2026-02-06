package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.workflow.Settings

class TERRAPINSVM private constructor(private val settings_: Settings)
{
    private val pre_processing_vm_: PreProcessingVM = PreProcessingVM.from(settings_)
    companion object
    {
        @JvmStatic
        fun from(settings: Settings) : TERRAPINSVM
        {
            return TERRAPINSVM(settings)
        }

        @JvmStatic
        fun default() : TERRAPINSVM
        {
            return from(Settings.default())
        }
    }

    fun pre_processing_vm(): PreProcessingVM
    {
        return pre_processing_vm_
    }
}