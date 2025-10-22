package com.coxphysics.terrapins.views.assessment.localisations

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import ij.gui.NonBlockingGenericDialog
import java.awt.event.ItemEvent

const val TITLE = "Assess Data"

class AssessmentDialog private constructor() : NonBlockingGenericDialog(TITLE)
{
    private var ui_: AssessmentUI? = null

    companion object Factory
    {
        @JvmStatic
        fun from(settings: AssessmentSettings): AssessmentDialog
        {
            val dialog = AssessmentDialog()
            val ui = AssessmentUI.add_controls_to_dialog(dialog, settings)
            dialog.set_ui(ui)
            return dialog
        }
    }

    fun set_ui(ui: AssessmentUI)
    {
        ui_ = ui
    }

    fun create_settings_record() : AssessmentSettings?
    {
        return ui_?.create_settings_record(this)
    }

    @Override
    override fun itemStateChanged(e: ItemEvent?)
    {
        if (e != null)
        {
            ui_?.handle_event(e)
        }
    }
}