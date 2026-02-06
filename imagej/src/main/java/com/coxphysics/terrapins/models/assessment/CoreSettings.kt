package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.non_null
import com.coxphysics.terrapins.models.processing.WidefieldGenerator
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import ij.Prefs
import ij.process.FloatProcessor
import java.nio.file.Path

class CoreSettings private constructor(private var working_directory_: Path)
{
    private var widefield_: DiskOrImage = DiskOrImage.default()
    private var image_stack_: DiskOrImage = DiskOrImage.default()
    private var n_threads_ = Prefs.getThreads();
    private var settings_file_: String? = null

    companion object
    {
        @JvmStatic
        fun new(working_directory: Path): CoreSettings
        {
            return CoreSettings(working_directory)
        }

        @JvmStatic
        fun default(): CoreSettings
        {
            return new(FsUtils.temp_directory().resolve("smlm_assessment"))
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

    fun has_settings_file(): Boolean
    {
        return !settings_file_.isNullOrEmpty()
    }

    fun settings_file_nn(): String
    {
        return settings_file_.non_null()
    }

    fun set_settings_file(value: String)
    {
        settings_file_ = value
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
            val settings = new(working_directory_)
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
}