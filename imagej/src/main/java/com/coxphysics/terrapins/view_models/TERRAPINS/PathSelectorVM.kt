package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.io.PathSelector
import java.nio.file.Path

class PathSelectorVM private constructor(private var settings_: PathSelector)
{
    companion object
    {
        @JvmStatic
        fun from(settings: PathSelector) : PathSelectorVM
        {
            return PathSelectorVM(settings)
        }

        @JvmStatic
        fun default() : PathSelectorVM
        {
            return from(PathSelector.default())
        }

        // for calls from Java
        @JvmStatic
        fun default_() : PathSelectorVM
        {
            return default()
        }
    }

    fun find_path(): Path
    {
        settings_.find()
        return settings_.current_path()
    }
}