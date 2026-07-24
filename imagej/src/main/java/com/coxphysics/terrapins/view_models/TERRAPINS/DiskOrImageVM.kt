package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.PathWrapper
import com.coxphysics.terrapins.models.ij_wrapping.ImageSelector

class DiskOrImageVM private constructor(
    private var model_: DiskOrImage,
    private val joint_last_path: PathWrapper)
{
    private var path_selector_vm_ : PathSelectorVM = PathSelectorVM.with(model_.path_wrapper(), joint_last_path)
    private var image_selector_vm_ : ImageSelectorVM = ImageSelectorVM.from(ImageSelector.from(model_.image_wrapper()))

    companion object
    {
        @JvmStatic
        fun from(model: DiskOrImage, joint_last_path: PathWrapper): DiskOrImageVM
        {
            return DiskOrImageVM(model, joint_last_path)
        }

        @JvmStatic
        fun default(): DiskOrImageVM
        {
            return from(DiskOrImage.default(), PathWrapper.empty())
        }

        // For Java
        @JvmStatic
        fun default_(): DiskOrImageVM
        {
            return default()
        }
    }

    fun path_selector_vm(): PathSelectorVM
    {
        return path_selector_vm_
    }

    fun image_selector_vm(): ImageSelectorVM
    {
        return image_selector_vm_
    }

    fun use_image(): Boolean
    {
        return model_.use_image()
    }

    fun set_use_images(value: Boolean)
    {
        model_.set_use_image(value)
    }

}