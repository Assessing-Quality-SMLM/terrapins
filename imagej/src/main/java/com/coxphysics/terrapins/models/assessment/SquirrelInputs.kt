package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.ij_wrapping.WindowManager
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.processing.WidefieldGenerator
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_IMAGE_STACK
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_WIDEFIELD
import com.coxphysics.terrapins.plugins.SQUIRREL_PERFORM_REGISTRATION
import ij.ImagePlus
import ij.plugin.frame.Recorder
import java.nio.file.Path

class SquirrelInputs private constructor(
    private var widefield_: DiskOrImage,
    private var image_stack_: DiskOrImage,
    private var perform_registration_: Boolean)
{
    companion object
    {
        @JvmStatic
        fun new(widefield: DiskOrImage, image_stack: DiskOrImage, perform_registration: Boolean): SquirrelInputs
        {
            return SquirrelInputs(widefield, image_stack, perform_registration)
        }

        @JvmStatic
        fun default(): SquirrelInputs
        {
            return new(DiskOrImage.default(), DiskOrImage.default(), true)
        }


        fun from_macro_options(options: MacroOptions, window_manager: WindowManager): SquirrelInputs
        {
            var widefield = DiskOrImage.from_macro_options_with(CORE_SETTINGS_WIDEFIELD, options, window_manager)
            if (widefield == null)
                widefield = DiskOrImage.default()

            var image_stack = DiskOrImage.from_macro_options_with(CORE_SETTINGS_IMAGE_STACK, options, window_manager)
            if (image_stack == null)
                image_stack = DiskOrImage.default()

            var perform_registration = options.get_bool(SQUIRREL_PERFORM_REGISTRATION)
            if (perform_registration == null)
                perform_registration = true

            return new(widefield, image_stack, perform_registration)
        }
    }
    fun has_widefield(): Boolean
    {
        return widefield_.has_data()
    }

    fun widefield(): DiskOrImage
    {
        return widefield_
    }

    fun widefield_path_in(directory: Path?) : Path?
    {
        val image_path = directory?.resolve("widefield.tiff")
        if (image_path == null)
            return null
        return widefield_.filepath(image_path)
    }

    fun image_stack(): DiskOrImage
    {
        return image_stack_
    }

    fun image_stack_path_in(directory: Path?) : Path?
    {
        val image_path = directory?.resolve("image_stack.tiff")
        if (image_path == null)
            return null
        return image_stack_.filepath(image_path)
    }

    fun set_widefield(value: DiskOrImage)
    {
        widefield_ = value
    }

    fun set_widefield_path(value: Path)
    {
        widefield_.set_path_and_switch_usage(value)
    }

    fun set_widefield_filename(value: String)
    {
        widefield_.set_filename_and_switch_usage(value)
    }

    fun has_image_stack(): Boolean
    {
        return image_stack_.has_data()
    }

    fun set_image_stack(value: DiskOrImage)
    {
        image_stack_ = value
    }

    fun set_image_stack_path(value: Path)
    {
        image_stack_.set_path_and_switch_usage(value)
    }

    fun set_image_stack_filename(value: String)
    {
        image_stack_.set_filename_and_switch_usage(value)
    }

    fun perform_registration(): Boolean
    {
        return perform_registration_
    }

    fun set_regisration(value: Boolean)
    {
        perform_registration_ = value
    }

    fun to_disk_in(working_directory: Path) : SquirrelInputs?
    {
        var widefield_ok = true
        var widefield_path : Path? = null
        var image_stack_ok = true
        var image_stack_path : Path? = null
        if (widefield_.has_data())
        {
            val new_widefield_path = widefield_path_in(working_directory)?.let { p -> widefield().to_disk_with(p) }
            widefield_path = new_widefield_path
            widefield_ok = widefield_path != null
        }
        if (image_stack_.has_data())
        {
            if (image_stack_.use_disk())
            {
                image_stack_path = image_stack_.filename_path()
                image_stack_ok = image_stack_path != null
            }
            else
            {
                val p = image_stack_path_in(working_directory)
                if (p != null)
                {
                    val image_stack = image_stack_.image()?.stack
                    if (image_stack != null)
                    {
                        val aof = WidefieldGenerator.average_of_frames(image_stack)
                        val aof_image = Image.from(ImagePlus("average_of_frames", aof))
                        image_stack_path = DiskOrImage.from_image(aof_image).to_disk_with(p)
                        image_stack_ok = image_stack_path != null
                    }
                }
            }
        }
        if (!widefield_ok || !image_stack_ok)
        {
            return null
        }

        val inputs = default()
        if (widefield_path != null)
            inputs.set_widefield_path(widefield_path)
        if (image_stack_path != null)
            inputs.set_image_stack_path(image_stack_path)
        inputs.set_regisration(perform_registration())
        return inputs

    }

    fun record_to_macro()
    {
        widefield_.record_to_macro_with(CORE_SETTINGS_WIDEFIELD)
        image_stack_.record_to_macro_with(CORE_SETTINGS_IMAGE_STACK)
        Recorder.recordOption(SQUIRREL_PERFORM_REGISTRATION, perform_registration_.toString())
    }
}