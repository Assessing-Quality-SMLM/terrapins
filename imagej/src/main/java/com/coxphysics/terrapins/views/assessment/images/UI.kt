package com.coxphysics.terrapins.views.assessment.images

import com.coxphysics.terrapins.models.assessment.images.Settings
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.FileField
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import java.awt.event.ItemEvent

private const val WIDEFIELD = "Widefield"
private const val IMAGE_STACK = "Image Stack"

class UI private constructor(
    private val dialog_: Dialog,
    private val advanced_settings_visible_: Checkbox,
    private val settings_file_field_: FileField
)
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: Dialog, settings: Settings): UI
        {
            val equipment = EquipmentUI.add_controls_to_dialog(dialog, settings.equipment_settings());

            val widefield = Utils.add_file_field(dialog, WIDEFIELD, settings.widefield_nn())
            val image_stack = Utils.add_file_field(dialog, IMAGE_STACK, settings.image_stack_nn())

            val reference = Utils.add_file_field(dialog, "Reference", settings.reference_image_nn())
            val hawk_image = Utils.add_file_field(dialog, "HAWK", settings.hawk_image_nn())

            val half_split_a = Utils.add_file_field(dialog, "Half split image a", settings.half_split_image_a_nn())
            val half_split_b = Utils.add_file_field(dialog, "Half split image b", settings.half_split_image_b_nn())

            val zip_split_a = Utils.add_file_field(dialog, "Zip split image a", settings.zip_split_image_a_nn())
            val zip_split_b = Utils.add_file_field(dialog, "Zip split image b", settings.zip_split_image_b_nn())

            val is_visible = false
            val advanced_settings_checkbox = Utils.add_checkbox(dialog, "Advanced Settings", is_visible)
            val settings_file_field = Utils.add_file_field(dialog, "Settings File", settings.settings_file_nn())
            settings_file_field.set_visibility(is_visible)

            val ui = UI(dialog, advanced_settings_checkbox, settings_file_field)
            return ui
        }
    }

    fun create_settings_record(dialog: Dialog) : Settings
    {
        val settings = Settings.default()

        val equipment = EquipmentUI.create_settings_record(dialog)
        settings.set_equipment_settings(equipment)

        val widefield = Utils.extract_file_field(dialog)
        settings.set_widefield(widefield)

        val image_stack = Utils.extract_file_field(dialog)
        settings.set_image_stack(image_stack)

        val reference = Utils.extract_file_field(dialog)
        settings.set_reference(reference)

        val hawk = Utils.extract_file_field(dialog)
        settings.set_hawk(hawk)

        val half_split_a = Utils.extract_file_field(dialog)
        settings.set_half_split_a(half_split_a)

        val half_split_b = Utils.extract_file_field(dialog)
        settings.set_half_split_b(half_split_b)

        val zip_split_a = Utils.extract_file_field(dialog)
        settings.set_zip_split_a(zip_split_a)

        val zip_split_b = Utils.extract_file_field(dialog)
        settings.set_zip_split_b(zip_split_b)

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