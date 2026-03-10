package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.*
import com.coxphysics.terrapins.models.ij_wrapping.WindowManager
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
    private var working_directory_: PathWrapper,
    private var settings_file_: PathWrapper
    )
{
    private var n_threads_ = Prefs.getThreads();

    companion object
    {
        @JvmStatic
        fun new(working_directory: Path?, settings_file: String?): CoreSettings
        {
            return CoreSettings(PathWrapper.from_optional(working_directory), PathWrapper.from_optional_string(settings_file))
        }

        @JvmStatic
        fun from(working_directory: Path?): CoreSettings
        {
            return new(working_directory, null)
        }

        @JvmStatic
        fun default(): CoreSettings
        {
            return from(default_working_directory())
        }

        private fun default_working_directory(): Path = FsUtils.temp_directory().resolve("smlm_assessment")

        fun from_macro_options(options: MacroOptions, window_manager: WindowManager): CoreSettings
        {
            val working_directory = options.get(CORE_SETTINGS_WORKING_DIRECTORY)
            val working_directory_path = working_directory.to_path_or_default(default_working_directory())
            val settings_file = options.get(CORE_SETTINGS_SETTINGS_FILE)
            return new(working_directory_path, settings_file)
        }
    }

    fun working_directory():PathWrapper
    {
        return working_directory_
    }

    fun working_directory_path(): Path?
    {
        return working_directory_.path()
    }

    fun set_working_directory(value: Path)
    {
        working_directory_.set_path(value)
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

    fun record_to_macro()
    {
        Recorder.recordOption(CORE_SETTINGS_WORKING_DIRECTORY, working_directory_.to_string())
        if(settings_file_.has_data())
            Recorder.recordOption(CORE_SETTINGS_SETTINGS_FILE, settings_file_.to_string())
    }
}