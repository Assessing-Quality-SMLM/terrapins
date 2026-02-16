package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.CoreSettings

class SquirrelInputsVM private constructor(private val model_: CoreSettings)
{
    private val widefield_vm_ = DiskOrImageVM.from(model_.widefield())
    private val image_stack_vm_ = DiskOrImageVM.from(model_.image_stack())

    companion object
    {
        @JvmStatic
        fun from(model: CoreSettings): SquirrelInputsVM
        {
            return SquirrelInputsVM(model)
        }

        @JvmStatic
        fun default() : SquirrelInputsVM
        {
            return from(CoreSettings.default())
        }

        @JvmStatic
        fun default_() : SquirrelInputsVM
        {
            return default()
        }

    }

    fun widefield_vm(): DiskOrImageVM
    {
        return widefield_vm_
    }

    fun image_stack_vm(): DiskOrImageVM
    {
        return image_stack_vm_
    }
}