package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.PathWrapper
import com.coxphysics.terrapins.models.assessment.SquirrelInputs

class SquirrelInputsVM private constructor(
    private val model_: SquirrelInputs,
    private val joint_last_path: PathWrapper)
{
    private val widefield_vm_ = DiskOrImageVM.from(model_.widefield(), joint_last_path)
    private val image_stack_vm_ = DiskOrImageVM.from(model_.image_stack(), joint_last_path)

    companion object
    {
        @JvmStatic
        fun from(model: SquirrelInputs, joint_last_path: PathWrapper): SquirrelInputsVM
        {
            return SquirrelInputsVM(model, joint_last_path)
        }

        @JvmStatic
        fun default() : SquirrelInputsVM
        {
            return from(SquirrelInputs.default(), PathWrapper.empty())
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

    fun register_images(): Boolean
    {
        return model_.perform_registration()
    }
    fun set_registration(value: Boolean)
    {
        model_.set_regisration(value)
    }
}