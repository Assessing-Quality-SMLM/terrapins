package com.coxphysics.terrapins.views.assessment.localisations

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.FileField
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import com.coxphysics.terrapins.views.localisations.LocalisationFileUI
import java.awt.event.ItemEvent

private const val LOCALISATION_FILE = "Localisation File"
private const val HAWKED_LOCALISATION_FILE = "HAWK Localisation File"
private const val WIDEFIELD = "Widefield"
private const val IMAGE_STACK = "Image Stack"

class AssessmentUI private constructor(
    private val dialog_: AssessmentDialog,
    private val advanced_settings_visible_: Checkbox,
    private val settings_file_field_: FileField
)
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: AssessmentDialog, settings: AssessmentSettings): AssessmentUI
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

            val ui = AssessmentUI(dialog, advanced_settings_checkbox, settings_file_field)
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
        settings.set_widefield(widefield)

        val image_stack = Utils.extract_file_field(dialog)
        settings.set_image_stack(image_stack)

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

}