package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.StringUtils
import java.nio.file.Path
import java.nio.file.Paths

class PathWrapper private constructor(private var path_: Path?)
{
    companion object
    {
        @JvmStatic
        fun from(path: Path): PathWrapper
        {
            return PathWrapper(path)
        }

        @JvmStatic
        fun from_string(path: String): PathWrapper
        {
            return PathWrapper(Paths.get(path))
        }

        @JvmStatic
        fun empty(): PathWrapper
        {
            return PathWrapper(null)
        }
    }

    fun has_data(): Boolean
    {
        return path_ != null
    }

    fun path(): Path?
    {
        return path_
    }

    fun set_path(path: Path)
    {
        path_ = path
    }

    fun set_path_from_string(value: String)
    {
        set_path(Paths.get(value))
    }

    fun to_string(): String
    {
        return if (path_ != null) return path_.toString() else StringUtils.EMPTY_STRING
    }
}