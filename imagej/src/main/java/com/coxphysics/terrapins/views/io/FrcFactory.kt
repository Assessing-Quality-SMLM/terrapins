package com.coxphysics.terrapins.views.io

import com.coxphysics.terrapins.models.io.FrcImages
import com.coxphysics.terrapins.view_models.io.FrcImagesVM
import com.coxphysics.terrapins.views.RecordableUIElement
import ij.gui.GenericDialog

class FrcFactory private constructor(
    private val view_model_: FrcImagesVM
)
    : RecordableUIElement<FrcImages, FrcImagesUI>
{
    companion object
    {
        @JvmStatic
        fun from(view_model: FrcImagesVM) : FrcFactory
        {
            return FrcFactory(view_model)
        }
    }

    override fun add_to_dialog(dialog: GenericDialog): FrcImagesUI
    {
        return FrcImagesUI.add_to_dialog(dialog, view_model_)
    }
}