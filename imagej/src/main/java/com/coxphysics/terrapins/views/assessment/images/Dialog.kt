package com.coxphysics.terrapins.views.assessment.images

import com.coxphysics.terrapins.models.assessment.images.Settings
import ij.gui.NonBlockingGenericDialog
import java.awt.event.ItemEvent


const val TITLE = "Assess Data"

class  Dialog private constructor() : NonBlockingGenericDialog(TITLE)
{
    private var ui_: UI? = null

    companion object
    {
        @JvmStatic
        fun from(settings: Settings):  Dialog
        {
            val dialog =  Dialog()
            val ui = UI.add_controls_to_dialog(dialog, settings)
            dialog.set_ui(ui)
            return dialog
        }
    }

    private fun set_ui(ui: UI)
    {
        ui_ = ui
    }

    fun create_settings_record() : Settings?
    {
        return ui_?.create_settings_record(this)
    }
}