package com.coxphysics.terrapins.views.assessment.localisations

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.FileField
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import com.coxphysics.terrapins.views.localisations.LocalisationFileUI
import ij.gui.GenericDialog
import java.awt.event.ItemEvent

private const val LOCALISATION_FILE = "Localisation File"
private const val HAWKED_LOCALISATION_FILE = "HAWK Localisation File"
private const val WIDEFIELD = "Widefield"
private const val IMAGE_STACK = "Image Stack"

class AssessmentUI private constructor(
    private val dialog_: GenericDialog,
    private val equipment_: EquipmentUI,
    private val localisation_file_ : LocalisationFileUI,
    private val hawked_localisation_file_: LocalisationFileUI,
    private val widefield_: FileField,
    private val image_stack: FileField,
    private val advanced_settings_visible_: Checkbox,
    private val settings_file_field_: FileField
)
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: AssessmentSettings): AssessmentUI
        {
            val equipment = EquipmentUI.add_controls_to_dialog(dialog, settings.equipment());

            val localisation_file =
                LocalisationFileUI.add_to_dialog(dialog, settings.localisation_file(), LOCALISATION_FILE)

            val hawked_localisation_file =
                LocalisationFileUI.add_to_dialog(dialog, settings.hawk_localisation_file(), HAWKED_LOCALISATION_FILE)

            val widefield = Utils.add_file_field(dialog, WIDEFIELD, settings.widefield_nn())
            val image_stack = Utils.add_file_field(dialog, IMAGE_STACK, settings.image_stack_nn())

            val is_visible = false
            val advanced_settings_checkbox = Utils.add_checkbox(dialog, "Advanced Settings", is_visible)
            val settings_file_field = Utils.add_file_field(dialog, "Settings File", settings.settings_file_nn())
            settings_file_field.set_visibility(is_visible)

            val ui = AssessmentUI(dialog, equipment, localisation_file, hawked_localisation_file, widefield, image_stack, advanced_settings_checkbox, settings_file_field)
            return ui
        }
    }

    fun create_settings_record(dialog: AssessmentDialog) : AssessmentSettings
    {
        val settings = AssessmentSettings.default()

        val equipment = EquipmentUI.create_settings_record(dialog)
        settings.set_equipment_settings(equipment)

        val localisation_file = LocalisationFileUI.create_settings_record(dialog)
        settings.set_localisation_file(localisation_file)

        val hawked_locaoisation_file = LocalisationFileUI.create_settings_record(dialog)
        settings.set_hawk_localisation_file(hawked_locaoisation_file)

        val widefield = Utils.extract_file_field(dialog)
        settings.set_widefield_filename(widefield)

        val image_stack = Utils.extract_file_field(dialog)
        settings.set_image_stack_filename(image_stack)

        // advanced visible
        val _advanced_checkbox = Utils.extract_checkbox_value(dialog)
        val settings_file = Utils.extract_file_field(dialog)
        settings.set_settings_file(settings_file)

        return settings
    }

    fun handle_event(event: ItemEvent)
    {
        val source = event.source
        if (advanced_settings_visible_.is_checkbox(source))
        {
            flip_advanced_settings_visibility()
            dialog_.pack()
        }
    }

    private fun flip_advanced_settings_visibility()
    {
        val new_visibility = !current_visible()
        settings_file_field_.set_visibility(new_visibility)
    }

    private fun current_visible() : Boolean
    {
        return settings_file_field_.is_visible
    }

    fun set_visibility(value: Boolean)
    {
        equipment_.set_visibility(value)
        localisation_file_.set_visibility(value)
        hawked_localisation_file_.set_visibility(value)
        widefield_.set_visibility(value)
        image_stack.set_visibility(value)
        advanced_settings_visible_.set_visibility(value)
    }
}