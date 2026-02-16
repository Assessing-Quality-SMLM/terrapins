package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.PathWrapper
import com.coxphysics.terrapins.models.io.PathSelector
import com.coxphysics.terrapins.models.to_nullable_path
import java.awt.Color
import java.nio.file.Path
import javax.swing.JTextField
import kotlin.io.path.exists

class PathSelectorVM private constructor(
    private var title_: String,
    private var path_: PathSelector)
{
    companion object
    {
        @JvmStatic
        fun from(title: String, settings: PathSelector) : PathSelectorVM
        {
            return PathSelectorVM(title, settings)
        }

        @JvmStatic
        fun with_path_and_title(title: String, path: PathWrapper) : PathSelectorVM
        {
            return from(title, PathSelector.default_with(path))
        }

        @JvmStatic
        fun with_directory_path_and_title(title: String, path: PathWrapper) : PathSelectorVM
        {
            return from(title, PathSelector.directory_from(path))
        }

        @JvmStatic
        fun with(path: PathWrapper) : PathSelectorVM
        {
            return with_path_and_title("File", path)
        }

        @JvmStatic
        fun default() : PathSelectorVM
        {
            return from("File", PathSelector.default())
        }

        // for calls from Java
        @JvmStatic
        fun default_() : PathSelectorVM
        {
            return default()
        }
    }

    fun title(): String
    {
        return title_
    }

    fun current_path(): Path?
    {
        return path_.current_path()
    }

    fun set_current_path(value: String) : Color
    {
        val path = value.to_nullable_path()
        if (path == null)
            return error_colour()
        path_.set_current_path(path)
        return if(path.exists()) default_background_colour() else error_colour()
    }

    private fun error_colour(): Color = Color.RED

    private fun default_background_colour(): Color = JTextField().background

    fun find_path(): Path?
    {
        path_.find()
        return current_path()
    }

    fun set_is_files_only(value: Boolean)
    {
        path_.set_is_files_only(value)
    }
}