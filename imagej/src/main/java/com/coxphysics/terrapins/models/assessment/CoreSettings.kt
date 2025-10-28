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
        fun default(): CoreSettings
        {
            return CoreSettings(FsUtils.temp_directory())
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

    fun image_stack(): DiskOrImage
    {
        return image_stack_
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
        widefield_.set_filename(value)
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
        image_stack_.set_filename(value)
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
}