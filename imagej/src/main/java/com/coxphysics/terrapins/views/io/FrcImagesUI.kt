package com.coxphysics.terrapins.views.io

import com.coxphysics.terrapins.models.io.FrcImages
import com.coxphysics.terrapins.view_models.io.FrcImagesVM
import com.coxphysics.terrapins.views.Recordable
import com.coxphysics.terrapins.views.RecordableElement
import com.coxphysics.terrapins.views.UIElement
import ij.gui.GenericDialog

class FrcImagesUI private constructor(
    private val view_model_: FrcImagesVM,
    private val half_split_ui_: JointImagesUI,
    private val zip_split_ui_: JointImagesUI,
    ): Recordable<FrcImages>, UIElement, RecordableElement<FrcImages>
{
    companion object
    {
        @JvmStatic
        fun add_to_dialog(dialog: GenericDialog, view_model: FrcImagesVM): FrcImagesUI
        {
            val half_split_ui = JointImagesUI.add_to_dialog(dialog, view_model.half_split_vm())
            val zip_split_ui = JointImagesUI.add_to_dialog(dialog, view_model.zip_split_vm())
            return FrcImagesUI(view_model, half_split_ui, zip_split_ui)
        }
    }

    override fun extract_from(dialog: GenericDialog): FrcImages
    {
        val half_split = half_split_ui_.extract_from(dialog)
        val zip_split = zip_split_ui_.extract_from(dialog)
        return FrcImages.new(half_split, zip_split)
    }

    override fun set_visibility(value: Boolean)
    {
        half_split_ui_.set_visibility(value)
        zip_split_ui_.set_visibility(value)
    }

    fun reset_images()
    {
        half_split_ui_.reset_images()
        zip_split_ui_.reset_images()
    }
}