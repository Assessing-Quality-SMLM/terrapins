package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.StringUtils
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

class PathWrapper private constructor(private var path_: Path?)
{
    companion object
    {
        @JvmStatic
        fun empty(): PathWrapper
        {
            return PathWrapper(null)
        }

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
        fun from_optional_string(path: String?): PathWrapper
        {
            if (path == null)
                return empty()
            return PathWrapper(Paths.get(path))
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

    fun path_valid() : Boolean
    {
        return if (path_ == null) false else path_!!.exists()
    }

    fun to_string(): String
    {
        return path_.to_string_non_null()
    }
}