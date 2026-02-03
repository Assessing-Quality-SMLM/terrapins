package com.coxphysics.terrapins.view_models.io

import com.coxphysics.terrapins.models.io.FrcImages
import com.coxphysics.terrapins.views.io.JointImagesUI

class FrcImagesVM private constructor(
    private val model_: FrcImages,
    private val half_split_: JointImagesVM,
    private val zip_split_: JointImagesVM,
    private val drift_split_: JointImagesVM,
    )
{
    companion object
    {
        @JvmStatic
        fun from(model: FrcImages): FrcImagesVM
        {
            val half_split_vm = JointImagesVM.from(model.half_split())
            half_split_vm.set_group_name("Half split")
            half_split_vm.set_image_1_name("Split A")
            half_split_vm.set_image_2_name("Split B")

            val zip_split_model = model.zip_split()
            val zip_split_vm = JointImagesVM.from(zip_split_model)
            zip_split_vm.set_group_name("Zip split")
            zip_split_vm.set_image_1_name("Split A")
            zip_split_vm.set_image_2_name("Split B")

            val drift_split_model = model.drift_split()
            val drift_split_vm = JointImagesVM.from(drift_split_model)
            drift_split_vm.set_group_name("Drift split")
            drift_split_vm.set_image_1_name("Split A")
            drift_split_vm.set_image_2_name("Split B")
            return FrcImagesVM(model, half_split_vm, zip_split_vm, drift_split_vm)
        }
    }

    fun half_split_vm(): JointImagesVM
    {
        return half_split_
    }

    fun zip_split_vm(): JointImagesVM
    {
        return zip_split_
    }

    fun drift_split_vm(): JointImagesVM
    {
        return drift_split_
    }
}