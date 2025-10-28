package com.coxphysics.terrapins.views.io

import com.coxphysics.terrapins.view_models.io.FileFieldVM
import com.coxphysics.terrapins.views.FileField
import com.coxphysics.terrapins.views.RecordableUIElement
import com.coxphysics.terrapins.views.Utils
import ij.gui.GenericDialog

class FileFactory private constructor(
    private val view_model_: FileFieldVM
): RecordableUIElement<String, FileField>
{
    companion object
    {
        @JvmStatic
        fun from(view_model: FileFieldVM) : FileFactory
        {
            return FileFactory(view_model)
        }
    }

    override fun add_to_dialog(dialog: GenericDialog): FileField
    {
        return Utils.add_file_field(dialog, view_model_.name(), view_model_.filename())
    }
}