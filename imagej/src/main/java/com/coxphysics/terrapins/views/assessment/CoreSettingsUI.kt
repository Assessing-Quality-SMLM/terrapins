package com.coxphysics.terrapins.views.assessment

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.view_models.DiskOrImageVM
import com.coxphysics.terrapins.view_models.OptionalInputVM
import com.coxphysics.terrapins.views.DiskOrImageUI
import com.coxphysics.terrapins.views.io.DiskOrImageFactory
import com.coxphysics.terrapins.views.io.OptionalInputUI
import ij.gui.GenericDialog

private const val WIDEFIELD = "Widefield"
private const val IMAGE_STACK = "Image Stack"

class CoreSettingsUI private constructor(
    private val widefield_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,
    private val image_stack_ : OptionalInputUI<DiskOrImageUI, DiskOrImage>,
)

{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, settings: CoreSettings): CoreSettingsUI
        {
            val widefield_vm = DiskOrImageVM.with(WIDEFIELD, settings.widefield())
            widefield_vm.set_draw_reset_button(false)

            val optional_widefield = OptionalInputVM.from(false)
            optional_widefield.set_name("I have a widefield")
            val widefield = OptionalInputUI.add_to_dialog(dialog, optional_widefield, DiskOrImageFactory.from(widefield_vm))

            val image_stack_vm = DiskOrImageVM.with(IMAGE_STACK, settings.image_stack())
            image_stack_vm.set_draw_reset_button(false)

            val optional_image_stack = OptionalInputVM.from(false)
            optional_image_stack.set_name("I have an image stack")
            val image_stack = OptionalInputUI.add_to_dialog(dialog, optional_image_stack, DiskOrImageFactory.from(image_stack_vm))

            return CoreSettingsUI(widefield, image_stack)
        }
    }

    fun create_settings_record(dialog: GenericDialog) : CoreSettings
    {
        val settings = CoreSettings.default()

        val widefield = widefield_.extract_from(dialog)
        settings.set_widefield(widefield)

        val image_stack = image_stack_.extract_from(dialog)
        settings.set_image_stack(image_stack)

        return settings
    }

    fun set_visibility(value: Boolean)
    {
        widefield_.set_visibility(value)
        image_stack_.set_visibility(value)
    }

    fun reset_images()
    {
        widefield_.ui_element().reset_images()
        image_stack_.ui_element().reset_images()
    }
}