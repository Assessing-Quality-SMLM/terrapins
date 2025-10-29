package com.coxphysics.terrapins.views.assessment.localisations

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.view_models.DiskOrImageVM
import com.coxphysics.terrapins.view_models.OptionalInputVM
import com.coxphysics.terrapins.view_models.io.FileFieldVM
import com.coxphysics.terrapins.views.*
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import com.coxphysics.terrapins.views.io.FileFactory
import com.coxphysics.terrapins.views.io.OptionalInputUI
import com.coxphysics.terrapins.views.localisations.LocalisationFileUI
import ij.gui.GenericDialog
import java.awt.event.ItemEvent

private const val LOCALISATION_FILE = "Localisation File"
private const val HAWKED_LOCALISATION_FILE = "HAWK Localisation File"
private const val WIDEFIELD = "Widefield"
private const val IMAGE_STACK = "Image Stack"

class AssessmentUI private constructor(
    private val dialog_: GenericDialog,
    private val working_directory_: DirectoryField,
    private val equipment_: EquipmentUI,
    private val magnification_: NumericField,
    private val localisation_file_ : LocalisationFileUI,
    private val hawked_localisation_file_: LocalisationFileUI,
    private val widefield_: DiskOrImageUI,
    private val image_stack: DiskOrImageUI,
    private val settings_file_field_: OptionalInputUI<FileField, String>,
)
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: AssessmentSettings): AssessmentUI
        {
            val working_directory = Utils.add_directory_field(dialog, "Working directory", settings.working_directory().toString())
            val equipment = EquipmentUI.add_controls_to_dialog(dialog, settings.equipment());

            val magnification = Utils.add_numeric_field(dialog, "Magnification", settings.magnification(), 0)

            val localisation_file =
                LocalisationFileUI.add_to_dialog(dialog, settings.localisation_file(), LOCALISATION_FILE)

            val hawked_localisation_file =
                LocalisationFileUI.add_to_dialog(dialog, settings.hawk_localisation_file(), HAWKED_LOCALISATION_FILE)

            val widefield_vm = DiskOrImageVM.with(WIDEFIELD, settings.widefield())
            val widefield = DiskOrImageUI.add_to_dialog(dialog, widefield_vm)

            val image_stack_vm = DiskOrImageVM.with(IMAGE_STACK,settings.image_stack())
            val image_stack = DiskOrImageUI.add_to_dialog(dialog, image_stack_vm)

            val settings_vm = FileFieldVM.from(settings.settings_file_nn())
            settings_vm.set_name("Settings file")
            val optional_settings = OptionalInputVM.from(false)
            optional_settings.set_name("Advanced settings")
            val settings_file_field = OptionalInputUI.add_to_dialog(dialog, optional_settings, FileFactory.from(settings_vm))

            val ui = AssessmentUI(dialog, working_directory, equipment, magnification, localisation_file, hawked_localisation_file, widefield, image_stack, settings_file_field)
            return ui
        }
    }

    fun create_settings_record(dialog: AssessmentDialog) : AssessmentSettings
    {
        val settings = AssessmentSettings.default()

        val working_directory = Utils.extract_directory_field(dialog)
        settings.set_working_directory(working_directory)

        val equipment = EquipmentUI.create_settings_record(dialog)
        settings.set_equipment_settings(equipment)

        val magnification = Utils.extract_numeric_field(dialog)
        settings.set_magnification(magnification)

        val localisation_file = LocalisationFileUI.create_settings_record(dialog)
        settings.set_localisation_file(localisation_file)

        val hawked_locaoisation_file = LocalisationFileUI.create_settings_record(dialog)
        settings.set_hawk_localisation_file(hawked_locaoisation_file)

        val widefield = widefield_.extract_from(dialog)
        settings.set_widefield(widefield)

        val image_stack = image_stack.extract_from(dialog)
        settings.set_image_stack(image_stack)

        // advanced visible
        val settings_file = settings_file_field_.extract_from(dialog)
        settings.set_settings_file(settings_file)

        return settings
    }

    fun set_visibility(value: Boolean)
    {
        working_directory_.set_visibility(value)
        equipment_.set_visibility(value)
        magnification_.set_visibility(value)
        localisation_file_.set_visibility(value)
        hawked_localisation_file_.set_visibility(value)
        widefield_.set_visibility(value)
        image_stack.set_visibility(value)
        settings_file_field_.set_visibility(value)
    }
}