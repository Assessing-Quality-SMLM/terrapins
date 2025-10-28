package com.coxphysics.terrapins.views.assessment.images

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.images.Settings
import com.coxphysics.terrapins.models.io.JointImages
import com.coxphysics.terrapins.view_models.DiskOrImageVM
import com.coxphysics.terrapins.view_models.io.JointImagesVM
import com.coxphysics.terrapins.views.Button
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.DiskOrImageUI
import com.coxphysics.terrapins.views.FileField
import com.coxphysics.terrapins.views.Utils
import com.coxphysics.terrapins.views.equipment.EquipmentUI
import com.coxphysics.terrapins.views.io.JointImagesUI
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
    private val widefield_ : DiskOrImageUI,
    private val image_stack_ : DiskOrImageUI,
    private val reference_ : DiskOrImageUI,
    private val hawk_image_ : DiskOrImageUI,

    private val frc_data_available_: Checkbox,
    private val half_split_: JointImagesUI,
    private val zip_split_: JointImagesUI,
    private val advanced_settings_visible_: Checkbox,
    private val settings_file_field_: FileField,
    private var reset_images_button_: Button?,
) : ActionListener, ItemListener {
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: Settings): UI
        {
            val equipment = EquipmentUI.add_controls_to_dialog(dialog, settings.equipment_settings());

            val wf_disk_or_image = DiskOrImageVM.with(WIDEFIELD, DiskOrImage.from_filename(settings.widefield_nn()))
            wf_disk_or_image.set_draw_reset_button(false)
            val widefield = DiskOrImageUI.add_to_dialog(dialog, wf_disk_or_image)

            val is_disk_or_image = DiskOrImageVM.with(IMAGE_STACK, DiskOrImage.from_filename(settings.image_stack_nn()))
            is_disk_or_image.set_draw_reset_button(false)
            val image_stack = DiskOrImageUI.add_to_dialog(dialog, is_disk_or_image)

            val ref_disk_or_image = DiskOrImageVM.with("Reference", DiskOrImage.from_filename(settings.reference_image_nn()))
            ref_disk_or_image.set_draw_reset_button(false)
            val reference = DiskOrImageUI.add_to_dialog(dialog, ref_disk_or_image)

            val hawk_disk_or_image = DiskOrImageVM.with("HAWK", DiskOrImage.from_filename(settings.hawk_image_nn()))
            hawk_disk_or_image.set_draw_reset_button(false)
            val hawk_image = DiskOrImageUI.add_to_dialog(dialog, hawk_disk_or_image)

            val frc_data_available = Utils.add_checkbox(dialog, "I have FRC splits", false)

            val half_split_model = settings.half_split_model()
            val half_split_vm = JointImagesVM.from(half_split_model)
            half_split_vm.set_group_name("Half split")
            half_split_vm.set_image_1_name("Split A")
            half_split_vm.set_image_2_name("Split B")
            val half_split = JointImagesUI.add_to_dialog(dialog, half_split_vm)
            half_split.set_visibility(frc_data_available.is_checked)

            val zip_split_model = settings.zip_split_model()
            val zip_split_vm = JointImagesVM.from(zip_split_model)
            zip_split_vm.set_group_name("Zip split")
            zip_split_vm.set_image_1_name("Split A")
            zip_split_vm.set_image_2_name("Split B")
            val zip_split = JointImagesUI.add_to_dialog(dialog, zip_split_vm)
            zip_split.set_visibility(frc_data_available.is_checked)

            val is_visible = false
            val advanced_settings_checkbox = Utils.add_checkbox(dialog, "Advanced Settings", is_visible)
            val settings_file_field = Utils.add_file_field(dialog, "Settings File", settings.settings_file_nn())
            settings_file_field.set_visibility(is_visible)

            val ui = UI(dialog, equipment, widefield, image_stack, reference, hawk_image, frc_data_available, half_split, zip_split, advanced_settings_checkbox, settings_file_field, null)

            val reset_images_button = Utils.add_button(dialog, "Reset Images", ui)
            ui.reset_images_button_ = reset_images_button

            frc_data_available.add_item_listener(ui)
            advanced_settings_checkbox.add_item_listener(ui)
            ui.re_draw_ui()
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
        if (frc_data_available_.is_checkbox(source))
        {
            set_frc_visibility()
            return
        }
        if (advanced_settings_visible_.is_checkbox(source))
        {
            flip_advanced_settings_visibility()
        }
    }

    private fun re_draw_ui()
    {
        set_frc_visibility()
    }

    private fun set_frc_visibility()
    {
        val visibility = frc_data_available_.is_checked
        half_split_.set_visibility(visibility)
        zip_split_.set_visibility(visibility)
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
        frc_data_available_.set_visibility(value)
        half_split_.set_visibility(value)
        zip_split_.set_visibility(value)
//        half_split_a_.set_visibility(value)
//        half_split_b_.set_visibility(value)
//        zip_split_a_.set_visibility(value)
//        zip_split_b_.set_visibility(value)
        advanced_settings_visible_.set_visibility(value)
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        widefield_.reset_images()
        image_stack_.reset_images()
        reference_.reset_images()
        hawk_image_.reset_images()
        half_split_.reset_images()
        zip_split_.reset_images()
    }
}