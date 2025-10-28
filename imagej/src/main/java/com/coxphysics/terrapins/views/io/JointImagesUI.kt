package com.coxphysics.terrapins.views.io

import com.coxphysics.terrapins.view_models.io.JointImagesVM
import com.coxphysics.terrapins.views.DiskOrImageUI
import com.coxphysics.terrapins.views.Message
import com.coxphysics.terrapins.views.Utils
import ij.gui.GenericDialog
import java.awt.Font

class JointImagesUI private constructor(
    private val vieW_model_: JointImagesVM,
    private val group_header: Message,
    private val image_1_header_: Message,
    private val image_1_ui_: DiskOrImageUI,
    private val image_2_header_: Message,
    private val image_2_ui_: DiskOrImageUI
)
{
    companion object
    {
        @JvmStatic
        fun add_to_dialog(dialog: GenericDialog, view_model: JointImagesVM): JointImagesUI
        {
            val group_header_1 = Utils.add_message(dialog, view_model.group_name())
            group_header_1.set_bold()

            val image_1_header = Utils.add_message(dialog, view_model.image_1_name())
            val image_1_ui = DiskOrImageUI.add_to_dialog(dialog, view_model.image_1_vm())

            val image_2_header = Utils.add_message(dialog, view_model.image_2_name())
            val image_2_ui = DiskOrImageUI.add_to_dialog(dialog, view_model.image_2_vm())

            return JointImagesUI(view_model, group_header_1, image_1_header, image_1_ui, image_2_header, image_2_ui)
        }
    }

    fun set_visibility(value: Boolean)
    {
        group_header.set_visibility(value)
        image_1_header_.set_visibility(value)
        image_1_ui_.set_visibility(value)
        image_2_header_.set_visibility(value)
        image_2_ui_.set_visibility(value)
    }

    fun reset_images()
    {
        image_1_ui_.reset_images()
        image_2_ui_.reset_images()
    }
}