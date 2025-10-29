package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.non_null
import com.coxphysics.terrapins.models.utils.FsUtils
import java.nio.file.Path

class CoreSettings private constructor(private var working_directory_: Path)
{
    private var widefield_: DiskOrImage = DiskOrImage.default()
    private var image_stack_: DiskOrImage = DiskOrImage.default()
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

    fun widefield_nn(): String
    {
        return widefield_.filename_nn()
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

    fun image_stack_nn(): String
    {
        return image_stack_.filename_nn()
    }

    fun set_image_stack(value: DiskOrImage)
    {
        image_stack_ = value
    }

    fun set_image_stack_filename(value: String)
    {
        image_stack_.set_filename_and_switch_usage(value)
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

    fun to_disk_in(directory: Path) : Boolean
    {
        val widefield_path = widefield_path_in(directory)?.let { p -> widefield().to_disk_with(p) }
        val image_stack_path = image_stack_path_in(directory)?.let{p -> image_stack().to_disk_with(p)}
        return widefield_path != null && image_stack_path != null
    }
}