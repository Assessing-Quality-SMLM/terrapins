package com.coxphysics.terrapins.views.assessment.workflow

import com.coxphysics.terrapins.models.assessment.workflow.Settings
import ij.gui.NonBlockingGenericDialog
import java.awt.event.ItemEvent

class Dialog private constructor() : NonBlockingGenericDialog("Workflow")
{
    private var ui_ : Ui? = null
    companion object
    {
        @JvmStatic
        fun new(): Dialog
        {
            val dialog = Dialog()
            val ui = Ui.add_controls_to_dialog(dialog, Settings.default())
            dialog.set_ui(ui)
            return dialog
        }
    }

    fun create_settings_record(): Settings?
    {
        return ui_?.create_settings_record(this)
    }

    fun pre_processing_complete(): Boolean?
    {
        return ui_?.pre_processing_completed()
    }

    fun use_localisations(): Boolean?
    {
        return ui_?.localisation_selected()
    }

    private fun set_ui(ui: Ui)
    {
        ui_ = ui
    }

    @Override
    override fun itemStateChanged(e: ItemEvent?)
    {
        if (e != null)
        {
            ui_?.handle_event(e)
            pack()
        }
    }
}