package com.coxphysics.terrapins.views.io

import com.coxphysics.terrapins.view_models.OptionalInputVM
import com.coxphysics.terrapins.views.*
import ij.gui.GenericDialog
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

class OptionalInputUI<UI: RecordableElement<T>, T> private constructor(
    private val dialog_: GenericDialog,
    private val checkbox_: Checkbox,
    private val view_model_: OptionalInputVM,
    private val ui_element_: UI): Recordable<T>, ItemListener {
    companion object
    {
        @JvmStatic
        fun <T, UI : RecordableElement<T>> add_to_dialog(dialog: GenericDialog, view_model: OptionalInputVM, factory: RecordableUIElement<T, UI>) : OptionalInputUI<UI, T>
        {
            val checkbox = Utils.add_checkbox(dialog, view_model.name(), view_model.available())
            val ui_element = factory.add_to_dialog(dialog)
            val ui = OptionalInputUI(dialog, checkbox, view_model, ui_element)
            checkbox.add_item_listener(ui)
            ui.re_draw()
            return ui
        }
    }

    fun ui_element(): UI
    {
        return ui_element_
    }

    override fun extract_from(dialog: GenericDialog): T
    {
        return ui_element_.extract_from(dialog)
    }

    override fun itemStateChanged(e: ItemEvent?)
    {
        if (e == null)
            return
        if (checkbox_.is_checkbox(e.source))
        {
            re_draw()
        }
    }

    fun set_visibility(value: Boolean)
    {
        checkbox_.set_visibility(value)
        if (value && checkbox_.is_checked)
            ui_element_.set_visibility(value)
    }

    private fun re_draw()
    {
        ui_element_.set_visibility(checkbox_.is_checked)
    }
}