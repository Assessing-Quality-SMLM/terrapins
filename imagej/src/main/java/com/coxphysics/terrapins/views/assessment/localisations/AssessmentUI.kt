package com.coxphysics.terrapins.views.assessment.localisations

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.view_models.OptionalInputVM
import com.coxphysics.terrapins.view_models.io.FileFieldVM
import com.coxphysics.terrapins.views.*
import com.coxphysics.terrapins.views.assessment.CoreSettingsUI
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import com.coxphysics.terrapins.views.io.FileFactory
import com.coxphysics.terrapins.views.io.OptionalInputUI
import com.coxphysics.terrapins.views.localisations.LocalisationFileUI
import ij.gui.GenericDialog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.nio.file.Paths

private const val LOCALISATION_FILE = "Localisation File"
private const val HAWKED_LOCALISATION_FILE = "HAWK Localisation File"

class AssessmentUI private constructor(
    private val dialog_: GenericDialog,
    private val working_directory_: DirectoryField,
    private val equipment_: EquipmentUI,
    private val localisation_file_ : LocalisationFileUI,
    private val hawked_localisation_file_: LocalisationFileUI,
    private val core_settings_ui_ : CoreSettingsUI,
    private val settings_file_field_: OptionalInputUI<FileField, String>,

    private var display_reset_button_: Boolean,
    private var reset_images_button_: Button?,
): ActionListener
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: AssessmentSettings, display_reset_button: Boolean): AssessmentUI
        {
            val working_directory = Utils.add_directory_field(dialog, "Working directory", settings.working_directory().toString())
            val equipment = EquipmentUI.add_controls_to_dialog(dialog, settings.equipment());

            val localisation_file =
                LocalisationFileUI.add_to_dialog(dialog, settings.localisation_file(), LOCALISATION_FILE)

            val hawked_localisation_file =
                LocalisationFileUI.add_to_dialog(dialog, settings.hawk_localisation_file(), HAWKED_LOCALISATION_FILE)

            val core_settings = CoreSettingsUI.add_controls_to_dialog(dialog, settings.core_settings())

            val settings_vm = FileFieldVM.from(settings.settings_file_nn())
            settings_vm.set_name("Settings file")
            val optional_settings = OptionalInputVM.from(false)
            optional_settings.set_name("Advanced settings")
            val settings_file_field = OptionalInputUI.add_to_dialog(dialog, optional_settings, FileFactory.from(settings_vm))

            val ui = AssessmentUI(dialog, working_directory, equipment, localisation_file, hawked_localisation_file, core_settings, settings_file_field, display_reset_button, null)

            val reset_images_button = Utils.add_button(dialog, "Reset Images", ui)
            if (!display_reset_button)
                reset_images_button.set_visibility(false)
            ui.reset_images_button_ = reset_images_button

            return ui
        }
    }

    fun create_settings_record(dialog: GenericDialog) : AssessmentSettings
    {
        val settings = AssessmentSettings.default()

        val working_directory = Utils.extract_directory_field(dialog)
        settings.set_working_directory(Paths.get(working_directory))

        val equipment = EquipmentUI.create_settings_record(dialog)
        settings.set_equipment_settings(equipment)

        val localisation_file = LocalisationFileUI.create_settings_record(dialog)
        settings.set_localisation_file(localisation_file)

        val hawked_locaoisation_file = LocalisationFileUI.create_settings_record(dialog)
        settings.set_hawk_localisation_file(hawked_locaoisation_file)

        val core_settings = core_settings_ui_.create_settings_record(dialog)
        settings.set_image_stack(core_settings.image_stack())
        settings.set_widefield(core_settings.widefield())

        // advanced visible
        val settings_file = settings_file_field_.extract_from(dialog)
        settings.set_settings_file(settings_file)

        return settings
    }

    fun set_visibility(value: Boolean)
    {
        working_directory_.set_visibility(value)
        equipment_.set_visibility(value)
        localisation_file_.set_visibility(value)
        hawked_localisation_file_.set_visibility(value)
        core_settings_ui_.set_visibility(value)
        settings_file_field_.set_visibility(value)

        reset_images_button_?.set_visibility(display_reset_button_ && value)
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        reset_images()
    }

    fun reset_images()
    {
        core_settings_ui_.reset_images()
    }
}