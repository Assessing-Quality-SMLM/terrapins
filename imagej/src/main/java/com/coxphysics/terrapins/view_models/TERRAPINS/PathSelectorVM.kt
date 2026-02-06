package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.PathWrapper
import com.coxphysics.terrapins.models.io.PathSelector
import java.nio.file.Path

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
        fun with(path: PathWrapper) : PathSelectorVM
        {
            return from("File", PathSelector.default_with(path))
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

    fun set_current_path(value: Path)
    {
        path_.set_current_path(value)
    }

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