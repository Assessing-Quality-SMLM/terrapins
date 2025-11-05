package com.coxphysics.terrapins.views.assessment.workflow

import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.hawk.PStreamFilter
import com.coxphysics.terrapins.views.Button
import com.coxphysics.terrapins.views.ImageSelector
import com.coxphysics.terrapins.views.Message
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.hawk.HawkUI
import ij.WindowManager
import ij.gui.GenericDialog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ImageJPreProcessingUI private constructor(
    private val dialog_: GenericDialog,
    private val load_message_: Message,
    private val localise_message_: Message,
    private val hawk_message_: Message,
    private val image_selector_: ImageSelector,
    private val hawk_: HawkUI,
    private val run_hawk_ : Button,
    private val localise_hawk_message_: Message) : ActionListener
{
    companion object
    {
        @JvmStatic
        fun add_to_dialog(dialog: GenericDialog, settings: Settings): ImageJPreProcessingUI
        {
            val load_data_message = Utils.add_message(dialog, "Load your data into ImageJ (image stack)")
            val localise_message = Utils.add_message(dialog, "Localise your data (Thunder Storm - TS)")
            val hawk_message = Utils.add_message(dialog, "HAWK your image stack")
            val image_selector = ImageSelector.add_to_dialog(dialog, settings.hawk_stack_image_selector_settings())
            val hawk = HawkUI.add_to_dialog(dialog, settings.hawk_settings())
            val run_hawk_button = Utils.add_button(dialog, "Run HAWK", null)
            val localise_hawk_message = Utils.add_message(dialog, "Now localise your HAWK stack")
            val ui = ImageJPreProcessingUI(dialog, load_data_message, localise_message, hawk_message, image_selector, hawk, run_hawk_button, localise_hawk_message)
            ui.run_hawk_.add_listener(ui)
            return ui
        }
    }

    fun create_settings_record(dialog: GenericDialog)
    {
        image_selector_.extract_image_names_recorded(dialog)
        hawk_.create_settings_recorded(dialog)
    }


    override fun actionPerformed(e: ActionEvent?)
    {
        val image_names = image_selector_.read_image_names()
        val config = hawk_.read_config()
        val image = WindowManager.getImage(image_names[0])
        val hawk_filter = PStreamFilter.from(image, config)
        hawk_filter.get_image_plus()?.show()
    }

    fun set_visibility(value: Boolean)
    {
        load_message_.set_visibility(value)
        localise_message_.set_visibility(value)
        hawk_message_.set_visibility(value)
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