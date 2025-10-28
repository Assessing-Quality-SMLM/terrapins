package com.coxphysics.terrapins.views.assessment.images

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.images.Settings
import com.coxphysics.terrapins.models.io.FrcImages
import com.coxphysics.terrapins.view_models.DiskOrImageVM
import com.coxphysics.terrapins.view_models.OptionalInputVM
import com.coxphysics.terrapins.view_models.io.FrcImagesVM
import com.coxphysics.terrapins.view_models.io.JointImagesVM
import com.coxphysics.terrapins.views.Button
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.DiskOrImageUI
import com.coxphysics.terrapins.views.FileField
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import com.coxphysics.terrapins.views.io.*
import ij.gui.GenericDialog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

private const val WIDEFIELD = "Widefield"
private const val IMAGE_STACK = "Image Stack"

class UI private constructor(
    private val dialog_: GenericDialog,
    private val equipment_ui_: EquipmentUI,

    private val widefield_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,
    private val image_stack_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,

    private val reference_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,
    private val hawk_image_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,

    private val frc_images_: OptionalInputUI<FrcImagesUI, FrcImages>,

    private val advanced_settings_visible_: Checkbox,
    private val settings_file_field_: FileField,
    private var reset_images_button_: Button?,
) : ActionListener, ItemListener
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: Settings): UI
        {
            val equipment = EquipmentUI.add_controls_to_dialog(dialog, settings.equipment_settings());

            val widefield_vm = DiskOrImageVM.with(WIDEFIELD, settings.widefield())
            widefield_vm.set_draw_reset_button(false)

            val optional_widefield = OptionalInputVM.from(false)
            optional_widefield.set_name("I have a widefield")
            val widefield = OptionalInputUI.add_to_dialog(dialog, optional_widefield, FileFactory.from(widefield_vm))

            val image_stack_vm = DiskOrImageVM.with(IMAGE_STACK, settings.image_stack())
            image_stack_vm.set_draw_reset_button(false)

            val optional_image_stack = OptionalInputVM.from(false)
            optional_image_stack.set_name("I have an image stack")
            val image_stack = OptionalInputUI.add_to_dialog(dialog, optional_image_stack, FileFactory.from(image_stack_vm))

            val reference_vm = DiskOrImageVM.with("Reference", settings.reference_image())
            reference_vm.set_draw_reset_button(false)

            val optional_reference = OptionalInputVM.from(false)
            optional_reference.set_name("I have a super resolution image")
            val reference = OptionalInputUI.add_to_dialog(dialog, optional_reference, FileFactory.from(reference_vm))

            val hawk_vm = DiskOrImageVM.with("HAWK", settings.hawk_image())
            hawk_vm.set_draw_reset_button(false)

            val optional_hawk = OptionalInputVM.from(false)
            optional_hawk.set_name("I Have a HAWK image")
            val hawk_image = OptionalInputUI.add_to_dialog(dialog, optional_hawk, FileFactory.from(hawk_vm))

            val frc_vm = FrcImagesVM.from(settings.frc_model())

            val optional_frc = OptionalInputVM.from(false)
            optional_frc.set_name("I have FRC splits")
            val frc_images = OptionalInputUI.add_to_dialog(dialog, optional_frc, FrcFactory.from(frc_vm))

            val is_visible = false
            val advanced_settings_checkbox = Utils.add_checkbox(dialog, "Advanced Settings", is_visible)
            val settings_file_field = Utils.add_file_field(dialog, "Settings File", settings.settings_file_nn())
            settings_file_field.set_visibility(is_visible)

            val ui = UI(dialog, equipment, widefield, image_stack, reference, hawk_image, frc_images, advanced_settings_checkbox, settings_file_field, null)

            val reset_images_button = Utils.add_button(dialog, "Reset Images", ui)
            ui.reset_images_button_ = reset_images_button

            advanced_settings_checkbox.add_item_listener(ui)
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

    override fun itemStateChanged(e: ItemEvent?)
    {
        if (e == null)
            return
        handle_event(e)
        dialog_.pack()
    }

    fun handle_event(event: ItemEvent)
    {
        val source = event.source
        if (advanced_settings_visible_.is_checkbox(source))
        {
            flip_advanced_settings_visibility()
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
        equipment_ui_.set_visibility(value)
        widefield_.set_visibility(value)
        image_stack_.set_visibility(value)
        reference_.set_visibility(value)
        hawk_image_.set_visibility(value)
        frc_images_.set_visibility(value)
        advanced_settings_visible_.set_visibility(value)
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        widefield_.ui_element().reset_images()
        image_stack_.ui_element().reset_images()
        reference_.ui_element().reset_images()
        hawk_image_.ui_element().reset_images()
        frc_images_.ui_element().reset_images()
    }
}