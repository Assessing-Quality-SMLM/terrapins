package com.coxphysics.terrapins.models.utils

import java.io.File
import java.nio.file.Path

class PathUtils
{
    companion object
    {
        @JvmStatic
        fun path_string_delimit_with_forward_slash(value: String): String
        {
            val old_delim = '\\'
            return value.replace(old_delim, '/')
        }

        @JvmStatic
        fun path_string_with_forward_slash(value: Path): String
        {
            return path_string_delimit_with_forward_slash(value.toString())
        }
    }
}