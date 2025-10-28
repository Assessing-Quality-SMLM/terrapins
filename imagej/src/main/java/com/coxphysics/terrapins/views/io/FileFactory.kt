package com.coxphysics.terrapins.views.io

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.view_models.DiskOrImageVM
import com.coxphysics.terrapins.views.DiskOrImageUI
import com.coxphysics.terrapins.views.RecordableUIElement
import ij.gui.GenericDialog

class FileFactory private constructor(
    private val view_model_: DiskOrImageVM
)
    : RecordableUIElement<DiskOrImage, DiskOrImageUI>
{
    companion object
    {
        @JvmStatic
        fun from(view_model: DiskOrImageVM) : FileFactory
        {
            return FileFactory(view_model)
        }
    }

    override fun add_to_dialog(dialog: GenericDialog): DiskOrImageUI
    {
        return DiskOrImageUI.add_to_dialog(dialog, view_model_)
    }
}