package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.SquirrelInputs

class SquirrelInputsVM private constructor(private val model_: SquirrelInputs)
{
    private val widefield_vm_ = DiskOrImageVM.from(model_.widefield())
    private val image_stack_vm_ = DiskOrImageVM.from(model_.image_stack())

    companion object
    {
        @JvmStatic
        fun from(model: SquirrelInputs): SquirrelInputsVM
        {
            return SquirrelInputsVM(model)
        }

        @JvmStatic
        fun default() : SquirrelInputsVM
        {
            return from(SquirrelInputs.default())
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