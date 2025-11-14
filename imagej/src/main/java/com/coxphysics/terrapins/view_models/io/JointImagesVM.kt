package com.coxphysics.terrapins.view_models.io

import com.coxphysics.terrapins.models.io.JointImages
import com.coxphysics.terrapins.view_models.DiskOrImageVM

class JointImagesVM private constructor(
    private val model_: JointImages,
    private var group_name_: String,
    private var image_1_name_: String,
    private val image_1_vm_: DiskOrImageVM,
    private var image_2_name_: String,
    private val image_2_vm_: DiskOrImageVM)
{
    companion object
    {
        @JvmStatic
        fun from(model: JointImages) : JointImagesVM
        {
            val image_1 = DiskOrImageVM.with("Filename", model.image_1())
            image_1.set_draw_reset_button(false)
            val image_2 = DiskOrImageVM.with("Filename", model.image_2())
            image_2.set_draw_reset_button(false)
            return JointImagesVM(model, "Group", "A", image_1, "B", image_2)
        }
    }

    fun group_name(): String
    {
        return group_name_
    }

    fun set_group_name(value: String)
    {
        group_name_ = value
    }

    fun image_1_vm(): DiskOrImageVM
    {
        return image_1_vm_
    }

    fun image_1_name(): String
    {
        return image_1_name_
    }

    fun set_image_1_name(value: String)
    {
        image_1_name_ = value
    }

    fun image_2_vm(): DiskOrImageVM
    {
        return image_2_vm_
    }

    fun image_2_name(): String
    {
        return image_2_name_
    }

    fun set_image_2_name(value: String)
    {
        image_2_name_ = value
    }
}