package com.coxphysics.terrapins.views

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.view_models.DiskOrImageVM
import ij.WindowManager
import ij.gui.GenericDialog
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

class DiskOrImageUI(
    private val file_field_: FileField,
    private val checkbox_: Checkbox,
    private val image_selector_: ImageSelector,
    private val reset_images_button_: Button?)
    : ItemListener, Recordable<DiskOrImage>, UIElement, RecordableElement<DiskOrImage>
{
    companion object
    {
        @JvmStatic
        fun add_to_dialog(dialog: GenericDialog, view_model: DiskOrImageVM) : DiskOrImageUI
        {
            val file_field = Utils.add_file_field(dialog, view_model.name(),  view_model.filename_nn())
            val checkbox = Utils.add_checkbox(dialog, "Use Image", view_model.use_disk())
            val image_selector = ImageSelector.add_to_dialog(dialog, view_model.image_selector_settings())
            var reset_images_button: Button? = null
            if (view_model.draw_reset_button())
            {
                reset_images_button = Utils.add_button(dialog, "Reset Images", image_selector.reset_images_listener())
            }
            val ui = DiskOrImageUI(file_field, checkbox, image_selector, reset_images_button)
            ui.draw_ui()
            checkbox.add_item_listener(ui)
            return ui
        }
    }

    override fun extract_from(dialog: GenericDialog): DiskOrImage
    {
        return extract_model(dialog)
    }

    fun extract_model(dialog: GenericDialog): DiskOrImage
    {
        val file = Utils.extract_file_field(dialog)
        val use_image = Utils.extract_checkbox_value(dialog)
        val image_name = image_selector_.extract_image_names_recorded(dialog)
        val image  = WindowManager.getImage(image_name[0])
        return DiskOrImage.new(file, Image.from(image), use_image)
    }

    override fun set_visibility(value: Boolean)
    {
        file_field_.set_visibility(value)
        checkbox_.set_visibility(value)
        image_selector_.set_visibility(value)
        reset_images_button_?.set_visibility(value)
    }

    fun reset_images()
    {
        image_selector_.reset_images()
    }

    override fun itemStateChanged(e: ItemEvent?)
    {
        if (!checkbox_.is_checkbox(e?.source))
            return
        draw_ui()
    }

    private fun draw_ui()
    {
        if (checkbox_.is_checked)
        {
            file_field_.set_enabled(false)
            image_selector_.set_enabled(true)
        }
        else
        {
            file_field_.set_enabled(true)
            image_selector_.set_enabled(false)
        }
    }
}