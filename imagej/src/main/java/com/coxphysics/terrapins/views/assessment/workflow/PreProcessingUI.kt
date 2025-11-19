package com.coxphysics.terrapins.views.assessment.workflow

import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.hawk.PStreamFilter
import com.coxphysics.terrapins.views.*
import com.coxphysics.terrapins.views.hawk.HawkUI
import ij.WindowManager
import ij.gui.GenericDialog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent

private val IMAGE_J = "I'm using ImageJ"
private val NOT_IMAGE_J = "I'm using an external fitter"

class PreProcessingUI private constructor(
    private val options_: RadioButtons,
    private val load_message_: Message,
    private val localise_message_: Message,
    private val hawk_message_: Message,
    private val save_hawk_to_disk_message_: Message,
    private val image_selector_: ImageSelector,
    private val hawk_: HawkUI,
    private val run_hawk_: Button,
    private val localise_hawk_message_: Message
): ActionListener
{
    companion object
    {
        @JvmStatic
        fun add_to_dialog(dialog: GenericDialog, settings: Settings): PreProcessingUI
        {
            val options = Utils.add_radio_buttons(dialog, "Pre-processing", arrayOf(IMAGE_J, NOT_IMAGE_J), 2, 1, IMAGE_J)

            val load_data_message = Utils.add_message(dialog, "Load your data into ImageJ (image stack)")
            load_data_message.set_bold()

            val localise_message = Utils.add_message(dialog, "Localise your data (Thunder Storm - TS)")
            localise_message.set_bold()

            val hawk_message = Utils.add_message(dialog, "HAWK your image stack")
            hawk_message.set_bold()

            val save_hawk_to_disk_message = Utils.add_message(dialog, "Save the HAWK output to disk and use your fitter of choice")
            save_hawk_to_disk_message.set_bold()

            val image_selector = ImageSelector.add_to_dialog(dialog, settings.hawk_stack_image_selector_settings())
            val hawk = HawkUI.add_to_dialog(dialog, settings.hawk_settings())
            val run_hawk_button = Utils.add_button(dialog, "Run HAWK", null)

            val localise_hawk_message = Utils.add_message(dialog, "Now localise your HAWK stack")
            localise_hawk_message.set_bold()
            val ui = PreProcessingUI(options, load_data_message, localise_message, hawk_message, save_hawk_to_disk_message, image_selector, hawk, run_hawk_button, localise_hawk_message)
            ui.run_hawk_.add_listener(ui)
            return ui
        }
    }

    fun create_settings_record(dialog: GenericDialog)
    {
        Utils.extract_radio_buttons(dialog)
        image_selector_.extract_image_names_recorded(dialog)
        hawk_.create_settings_recorded(dialog)
    }

    fun is_event_source(source: Any): Boolean
    {
        return options_.is_button_group(source)
    }

    fun handle_event(event: ItemEvent)
    {
        if (options_.is_button_group(event.source))
            re_draw_ui()
    }

    private fun re_draw_ui()
    {
        if (options_.is_checked(IMAGE_J))
        {
            show_image_j()
            hide_external()
        }

        if (options_.is_checked(NOT_IMAGE_J))
        {
            show_external()
            hide_image_j()
        }
    }

    private fun show_image_j()
    {
        set_image_j(true)
    }

    private fun hide_image_j()
    {
        set_image_j(false)
    }

    private fun set_image_j(value: Boolean)
    {
        load_message_.set_visibility(value)
        localise_message_.set_visibility(value)
    }

    private fun show_external()
    {
        set_extenal_visibility(true )
    }

    private fun hide_external()
    {
        set_extenal_visibility(false)
    }

    private fun set_extenal_visibility(value: Boolean)
    {
        save_hawk_to_disk_message_.set_visibility(value)
        hawk_.save_to_disk(value)
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        val image_names = image_selector_.read_image_names()
        val config = hawk_.read_config()
        val image = WindowManager.getImage(image_names[0])
        val hawk_filter = PStreamFilter.from(image, config)
        if (config.has_output_filename_set())
        {
            hawk_filter.write_to_disk()
        }
        else
        {
            hawk_filter.get_image_plus()?.show()
        }
    }

    fun set_visibility(value: Boolean)
    {
        options_.set_visibility(value)
        load_message_.set_visibility(value)
        localise_message_.set_visibility(value)
        hawk_message_.set_visibility(value)
        save_hawk_to_disk_message_.set_visibility(value)
        image_selector_.set_visibility(value)
        hawk_.set_visibility(value)
        run_hawk_.set_visibility(value)
        localise_hawk_message_.set_visibility(value)
    }

    fun reset_images()
    {
        image_selector_.reset_images()
    }
}