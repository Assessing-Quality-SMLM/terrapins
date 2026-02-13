package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.*
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.processing.WidefieldGenerator
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_IMAGE_STACK
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_SETTINGS_FILE
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_WIDEFIELD
import com.coxphysics.terrapins.plugins.CORE_SETTINGS_WORKING_DIRECTORY
import ij.ImagePlus
import ij.Prefs
import ij.plugin.frame.Recorder
import java.nio.file.Path

class CoreSettings private constructor(
    private var working_directory_: Path,
    private var widefield_: DiskOrImage,
    private var image_stack_: DiskOrImage,
    private var settings_file_: PathWrapper
    )
{
    private var n_threads_ = Prefs.getThreads();

    companion object
    {
        @JvmStatic
        fun new(working_directory: Path, widefield: DiskOrImage, image_stack: DiskOrImage, settings_file: String?): CoreSettings
        {
            return CoreSettings(working_directory, widefield, image_stack, PathWrapper.from_optional_string(settings_file))
        }

        @JvmStatic
        fun from(working_directory: Path): CoreSettings
        {
            return new(working_directory, DiskOrImage.default(), DiskOrImage.default(), null)
        }

        @JvmStatic
        fun default(): CoreSettings
        {
            return from(default_working_directory())
        }

        private fun default_working_directory(): Path = FsUtils.temp_directory().resolve("smlm_assessment")

        fun from_macro_options(options: MacroOptions): CoreSettings
        {
            var widefield = DiskOrImage.from_macro_options_with(CORE_SETTINGS_WIDEFIELD, options)
            if (widefield == null)
                widefield = DiskOrImage.default()

            var image_stack = DiskOrImage.from_macro_options_with(CORE_SETTINGS_IMAGE_STACK, options)
            if (image_stack == null)
                image_stack = DiskOrImage.default()

            val working_directory = options.get(CORE_SETTINGS_WORKING_DIRECTORY)
            val working_directory_path = working_directory.to_path_or_default(default_working_directory())
            val settings_file = options.get(CORE_SETTINGS_SETTINGS_FILE)
            return new(working_directory_path, widefield, image_stack, settings_file)
        }
    }

    fun working_directory():Path
    {
        return working_directory_
    }

    fun set_working_directory(value: Path)
    {
        working_directory_ = value
    }

    fun has_widefield(): Boolean
    {
        return widefield_.has_data()
    }

    fun widefield(): DiskOrImage
    {
        return widefield_
    }

    fun widefield_path() : Path?
    {
        return widefield_path_in(working_directory())
    }

    private fun widefield_path_in(directory: Path) : Path?
    {
        val image_path = directory.resolve("widefield.tiff")
        return widefield_.filepath(image_path)
    }

    fun image_stack(): DiskOrImage
    {
        return image_stack_
    }

    fun image_stack_path() : Path?
    {
        return image_stack_path_in(working_directory())
    }

    private fun image_stack_path_in(directory: Path) : Path?
    {
        val image_path = directory.resolve("image_stack.tiff")
        return image_stack_.filepath(image_path)
    }

    fun set_widefield(value: DiskOrImage)
    {
        widefield_ = value
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

    fun set_image_stack_filename(value: String)
    {
        image_stack_.set_filename_and_switch_usage(value)
    }

    fun n_threads(): Int
    {
        return n_threads_
    }

    fun set_n_threads(value: Int)
    {
        n_threads_ = value
    }

    fun settings_file(): PathWrapper
    {
        return settings_file_
    }

    fun has_settings_file(): Boolean
    {
        return settings_file_.has_data()
    }

    fun settings_file_nn(): String
    {
        return settings_file_.to_string()
    }

    fun set_settings_file(value: String)
    {
        settings_file_.set_path_from_string(value)
    }

    fun to_disk_in(directory: Path) : CoreSettings?
    {
        var widefield_ok = true
        var widefield_path : Path? = null
        var image_stack_ok = true
        var image_stack_path : Path? = null
        if (widefield_.has_data())
        {
            val new_widefield_path = widefield_path_in(directory)?.let { p -> widefield().to_disk_with(p) }
            widefield_path = new_widefield_path
            widefield_ok = new_widefield_path != null
        }
        if (image_stack_.has_data())
        {
            if (image_stack_.use_image())
            {
                val p = image_stack_path_in(directory)
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
        if (widefield_ok && image_stack_ok)
        {
            val settings = from(working_directory_)
            settings.n_threads_ = n_threads_
            settings.settings_file_ = settings_file_
            settings.widefield_ = widefield_
            settings.image_stack_ = image_stack_
            if (widefield_path != null)
                settings.set_widefield_filename(widefield_path.toString())
            if (image_stack_path != null)
                settings.set_image_stack_filename(image_stack_path.toString())
            return settings
        }
        return null
    }

    fun record_to_macro()
    {
        Recorder.recordOption(CORE_SETTINGS_WORKING_DIRECTORY, working_directory_.toString())
        widefield_.record_to_macro_with(CORE_SETTINGS_WIDEFIELD)
        image_stack_.record_to_macro_with(CORE_SETTINGS_IMAGE_STACK)
        if(settings_file_.has_data())
            Recorder.recordOption(CORE_SETTINGS_SETTINGS_FILE, settings_file_.to_string())
    }
}