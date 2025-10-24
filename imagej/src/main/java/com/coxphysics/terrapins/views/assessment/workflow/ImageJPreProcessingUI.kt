package com.coxphysics.terrapins.views.assessment.workflow

import com.coxphysics.terrapins.views.Message
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.hawk.HawkUI
import ij.gui.GenericDialog

class ImageJPreProcessingUI private constructor(
    private val load_message_: Message,
    private val localise_message_: Message,
    private val hawk_message_: Message,
    private val hawk_: HawkUI
)
{
    companion object
    {
        @JvmStatic
        fun add_to_dialog(dialog: GenericDialog): ImageJPreProcessingUI
        {
            val load_data_message = Utils.add_message(dialog, "Load your data into ImageJ (image stack)")
            val localise_message = Utils.add_message(dialog, "Localise your data (Thunder Storm - TS)")
            val hawk_message = Utils.add_message(dialog, "HAWK your image stack")
            val hawk = HawkUI.add_to_dialog(dialog)
            return ImageJPreProcessingUI(load_data_message, localise_message, hawk_message, hawk)
        }
    }

    fun set_visibility(value: Boolean)
    {
        load_message_.set_visibility(value)
        localise_message_.set_visibility(value)
        hawk_message_.set_visibility(value)
        hawk_.set_visibility(value)
    }
}