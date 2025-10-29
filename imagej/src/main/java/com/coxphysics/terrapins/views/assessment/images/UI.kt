package com.coxphysics.terrapins.views.assessment.images

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.images.Settings
import com.coxphysics.terrapins.models.io.FrcImages
import com.coxphysics.terrapins.view_models.DiskOrImageVM
import com.coxphysics.terrapins.view_models.OptionalInputVM
import com.coxphysics.terrapins.view_models.io.FileFieldVM
import com.coxphysics.terrapins.view_models.io.FrcImagesVM
import com.coxphysics.terrapins.views.Button
import com.coxphysics.terrapins.views.DirectoryField
import com.coxphysics.terrapins.views.DiskOrImageUI
import com.coxphysics.terrapins.views.FileField
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.assessment.CoreSettingsUI
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import com.coxphysics.terrapins.views.io.*
import ij.gui.GenericDialog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

private const val WIDEFIELD = "Widefield"
private const val IMAGE_STACK = "Image Stack"

class UI private constructor(
    private val dialog_: GenericDialog,

    private val working_directory_: DirectoryField,

    private val equipment_ui_: EquipmentUI,

    private val core_settings_ui_: CoreSettingsUI,

    private val reference_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,
    private val hawk_image_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,

    private val frc_images_: OptionalInputUI<FrcImagesUI, FrcImages>,

    private val settings_file_field_: OptionalInputUI<FileField, String>,

    private var display_reset_button_: Boolean,
    private var reset_images_button_: Button?,
) : ActionListener
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: Settings, display_reset_button: Boolean): UI
        {
            val working_directory = Utils.add_directory_field(dialog, "Working directory", settings.working_directory().toString())
            val equipment = EquipmentUI.add_controls_to_dialog(dialog, settings.equipment_settings());

            val core_settings_ui = CoreSettingsUI.add_controls_to_dialog(dialog, settings.core_settings())

            val reference_vm = DiskOrImageVM.with("Reference", settings.reference_image())
            reference_vm.set_draw_reset_button(false)

            val optional_reference = OptionalInputVM.from(false)
            optional_reference.set_name("I have a super resolution image")
            val reference = OptionalInputUI.add_to_dialog(dialog, optional_reference, DiskOrImageFactory.from(reference_vm))

            val hawk_vm = DiskOrImageVM.with("HAWK", settings.hawk_image())
            hawk_vm.set_draw_reset_button(false)

            val optional_hawk = OptionalInputVM.from(false)
            optional_hawk.set_name("I Have a HAWK image")
            val hawk_image = OptionalInputUI.add_to_dialog(dialog, optional_hawk, DiskOrImageFactory.from(hawk_vm))

            val frc_vm = FrcImagesVM.from(settings.frc_model())

            val optional_frc = OptionalInputVM.from(false)
            optional_frc.set_name("I have FRC splits")
            val frc_images = OptionalInputUI.add_to_dialog(dialog, optional_frc, FrcFactory.from(frc_vm))


            val settings_vm = FileFieldVM.from(settings.settings_file_nn())
            settings_vm.set_name("Settings file")
            val optional_settings = OptionalInputVM.from(false)
            optional_settings.set_name("Advanced settings")
            val settings_file = OptionalInputUI.add_to_dialog(dialog, optional_settings, FileFactory.from(settings_vm))

            val ui = UI(dialog, working_directory, equipment, core_settings_ui, reference, hawk_image, frc_images, settings_file,  display_reset_button, null)

            val reset_images_button = Utils.add_button(dialog, "Reset Images", ui)
            if (!display_reset_button)
                reset_images_button.set_visibility(false)
            ui.reset_images_button_ = reset_images_button

            return ui
        }
    }

    fun create_settings_record(dialog: Dialog) : Settings
    {
        val settings = Settings.default()

        val working_directory = Utils.extract_directory_field(dialog)
        settings.set_working_directory(working_directory)

        val equipment = EquipmentUI.create_settings_record(dialog)
        settings.set_equipment_settings(equipment)

        val core_settings = core_settings_ui_.create_settings_record(dialog)
        settings.set_core_settings(core_settings)

        val reference = reference_.extract_from(dialog)
        settings.set_reference(reference)

        val hawk = hawk_image_.extract_from(dialog)
        settings.set_hawk(hawk)

        val frc_images = frc_images_.extract_from(dialog)
        settings.set_frc_images(frc_images)

        // advanced visible
        val settings_file = settings_file_field_.extract_from(dialog)
        settings.set_settings_file(settings_file)

        return settings
    }

    fun set_visibility(value: Boolean)
    {
        working_directory_.set_visibility(value)
        equipment_ui_.set_visibility(value)
        core_settings_ui_.set_visibility(value)
        reference_.set_visibility(value)
        hawk_image_.set_visibility(value)
        frc_images_.set_visibility(value)
        settings_file_field_.set_visibility(value)

        val button_value = display_reset_button_ && value
        reset_images_button_?.set_visibility(button_value)
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        reset_images()
    }

    fun reset_images()
    {
        core_settings_ui_.reset_images()
        reference_.ui_element().reset_images()
        hawk_image_.ui_element().reset_images()
        frc_images_.ui_element().reset_images()
    }
}