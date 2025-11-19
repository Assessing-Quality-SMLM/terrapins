package com.coxphysics.terrapins.views.assessment.workflow

import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.views.Button
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.Message
import com.coxphysics.terrapins.views.RadioButtons
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.assessment.localisations.AssessmentUI
import ij.gui.GenericDialog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import com.coxphysics.terrapins.views.assessment.images.UI as ImagesUI

private val LOCALISATIONS = "Localisations"
private val IMAGES = "Images"

class Ui private constructor(
    private val pre_processing_: PreProcessingUI,
    private val pre_processing_completed_: Checkbox,
    private val processing_: RadioButtons,
    private val localisation_ui_: AssessmentUI,
    private val images_ui_ : ImagesUI,
    private var reset_images_button_: Button?
): ActionListener
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: Settings): Ui
        {
            val pre_processing = PreProcessingUI.add_to_dialog(dialog, settings)

            val pre_processing_completed = Utils.add_checkbox(dialog, "Super resolution localisations / images created", false)

            val processing = Utils.add_radio_buttons(dialog, "Processing", arrayOf(LOCALISATIONS, IMAGES), 2, 1, LOCALISATIONS)

            val localisation_ui = AssessmentUI.add_controls_to_dialog(dialog, settings.localisation_settings(), false)
            val images_ui = ImagesUI.add_controls_to_dialog(dialog, settings.images_settings(), false)

            val ui = Ui(pre_processing, pre_processing_completed, processing, localisation_ui, images_ui, null)

            val reset_images_button = Utils.add_button(dialog, "Reset Images", ui)
            ui.reset_images_button_ = reset_images_button
            ui.re_draw_ui()
            return ui
        }
    }

    fun create_settings_record(dialog: GenericDialog): Settings
    {
        val settings = Settings.default()

        pre_processing_.create_settings_record(dialog)

        val pre_processing_completed = Utils.extract_checkbox_value(dialog)

        val processing = Utils.extract_radio_buttons(dialog)

        val localisation_settings = localisation_ui_.create_settings_record(dialog)
        settings.set_localisations(localisation_settings)

        val images_settings = images_ui_.create_settings_record(dialog)
        settings.set_images(images_settings)

        return settings

    }

    override fun actionPerformed(e: ActionEvent?)
    {
        pre_processing_.reset_images()
        localisation_ui_.reset_images()
        images_ui_.reset_images()
    }

    fun handle_event(event: ItemEvent)
    {
        val source = event.source
        if (pre_processing_.is_event_source(source))
        {
            pre_processing_.handle_event(event)
        }
        if (pre_processing_completed_.is_checkbox(source))
        {
            re_draw_ui()
            return
        }
        if (processing_.is_button_group(source))
        {
            re_draw_ui()
            return
        }
    }

    fun pre_processing_completed(): Boolean
    {
        return pre_processing_completed_.is_checked
    }

    private fun processing_enabled(): Boolean
    {
        return pre_processing_completed()
    }

    fun localisation_selected(): Boolean
    {
        return processing_.is_checked(LOCALISATIONS)
    }

    private fun images_selected(): Boolean
    {
        return processing_.is_checked(IMAGES)
    }

    private fun display_pre_processing(): Boolean
    {
        return !pre_processing_completed()
    }

    private fun display_localisation_ui(): Boolean
    {
        return pre_processing_completed() && localisation_selected()
    }

    private fun display_images_ui(): Boolean
    {
        return pre_processing_completed() && images_selected()
    }


    private fun re_draw_ui()
    {
        if (processing_enabled())
        {
            pre_processing_.set_visibility(false)
            processing_.set_enabled(true)
        }
        else
        {
            pre_processing_.set_visibility(true)
            processing_.set_enabled(false)
        }
        if (display_pre_processing())
        {
            return draw_pre_processing()
        }

        if (display_localisation_ui())
        {
            return draw_localisation_ui()
        }
        if (display_images_ui())
        {
            return draw_images_ui()
        }
    }

    private fun draw_pre_processing()
    {
        pre_processing_.set_visibility(true)
        hide_localisation_ui()
        hide_images_ui()
    }

    private fun draw_images_ui()
    {
        pre_processing_.set_visibility(false)
        hide_localisation_ui()
        show_images_ui()
    }

    private fun draw_localisation_ui()
    {
        pre_processing_.set_visibility(false)
        show_localisation_ui()
        hide_images_ui()
    }


    private fun show_localisation_ui()
    {
        localisation_ui_.set_visibility(true)
    }

    private fun hide_localisation_ui()
    {
        localisation_ui_.set_visibility(false)
    }

    private fun show_images_ui()
    {
        images_ui_.set_visibility(true)
    }

    private fun hide_images_ui()
    {
        images_ui_.set_visibility(false)
    }
}