package com.coxphysics.terrapins.views.assessment.workflow

import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.Message
import com.coxphysics.terrapins.views.RadioButtons
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.assessment.localisations.AssessmentUI
import ij.gui.GenericDialog
import java.awt.event.ItemEvent
import com.coxphysics.terrapins.views.assessment.images.UI as ImagesUI

private val IMAGE_J = "I'm using ImageJ"
private val NOT_IMAGE_J = "I'm using an external fitter"

private val LOCALISATIONS = "Localisations"
private val IMAGES = "Images"

class Ui private constructor(
    private val pre_processing_completed_: Checkbox,
    private val pre_processing_: RadioButtons,
    private val processing_: RadioButtons,
    private val imagej_preprocessing_ : ImageJPreProcessingUI,
    private val external_preprocessing_: Message,
    private val localisation_ui_: AssessmentUI,
    private val images_ui_ : ImagesUI)
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: Settings): Ui
        {
            val pre_processing = Utils.add_radio_buttons(dialog, "Pre-processing", arrayOf(IMAGE_J, NOT_IMAGE_J), 2, 1, IMAGE_J)

            val pre_processing_completed = Utils.add_checkbox(dialog, "Super resolution images created", false)

            val processing = Utils.add_radio_buttons(dialog, "Processing", arrayOf(LOCALISATIONS, IMAGES), 2, 1, LOCALISATIONS)

            val imagej_pre_processing = ImageJPreProcessingUI.add_to_dialog(dialog, settings)
            val external_fitter_ui = Utils.add_message(dialog, "Follow the instructions of your fitter")
            val localisation_ui = AssessmentUI.add_controls_to_dialog(dialog, settings.localisation_settings())
            val images_ui = ImagesUI.add_controls_to_dialog(dialog, settings.images_settings())

            val ui = Ui(pre_processing_completed, pre_processing, processing, imagej_pre_processing, external_fitter_ui, localisation_ui, images_ui)
            ui.re_draw_ui()
            return ui
        }
    }

    fun handle_event(event: ItemEvent)
    {
        val source = event.source
        if (pre_processing_completed_.is_checkbox(source))
        {
            re_draw_ui()
            return
        }
        if (pre_processing_.is_button_group(source))
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

    private fun pre_processing_completed(): Boolean
    {
        return pre_processing_completed_.is_checked
    }

    private fun pre_processing_enabled(): Boolean
    {
        return !pre_processing_completed()
    }

    private fun processing_enabled(): Boolean
    {
        return pre_processing_completed()
    }

    private fun localisation_selected(): Boolean
    {
        return processing_.is_checked(LOCALISATIONS)
    }

    private fun images_selected(): Boolean
    {
        return processing_.is_checked(IMAGES)
    }

    private fun display_image_j_pre_processing(): Boolean
    {
        return pre_processing_enabled() && pre_processing_.is_checked(IMAGE_J)
    }

    private fun display_external_pre_processing(): Boolean
    {
        return pre_processing_enabled() && pre_processing_.is_checked(NOT_IMAGE_J)
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
            pre_processing_.set_enabled(false)
            processing_.set_enabled(true)
        }
        else
        {
            pre_processing_.set_enabled(true)
            processing_.set_enabled(false)
        }
        if (display_image_j_pre_processing()) {
            return draw_imagej_ui()
        }
        if (display_external_pre_processing()) {
            return draw_external_ui()
        }
        if (display_localisation_ui()) {
            return draw_localisation_ui()
        }
        if (display_images_ui()) {
            return draw_images_ui()
        }
    }

    private fun draw_imagej_ui()
    {
        imagej_preprocessing_.set_visibility(true)
        external_preprocessing_.set_visibility(false)
        hide_localisation_ui()
        hide_images_ui()
    }

    private fun draw_external_ui()
    {
        imagej_preprocessing_.set_visibility(false)
        external_preprocessing_.set_visibility(true)
        hide_localisation_ui()
        hide_images_ui()
    }

    private fun draw_images_ui()
    {
        imagej_preprocessing_.set_visibility(false)
        external_preprocessing_.set_visibility(false)
        hide_localisation_ui()
        show_images_ui()
    }

    private fun draw_localisation_ui()
    {
        imagej_preprocessing_.set_visibility(false)
        external_preprocessing_.set_visibility(false)
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