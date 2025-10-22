package com.coxphysics.terrapins.models.squirrel.external

import com.coxphysics.terrapins.models.squirrel.utils.StackHelper
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.IJ
import java.nio.file.Path

class Results private constructor(private val data_path_: Path)
{
    companion object Factory
    {
        @JvmStatic
        fun from(data_path: Path): Results
        {
            return Results(data_path)
        }
    }

    public fun error_map_path(): Path
    {
        return data_path_.resolve("error_map.tiff")
    }

    fun display_error_map()
    {
        val map = IJUtils.load_image(error_map_path())
        if (map == null)
            return
        StackHelper.applyLUT(map,"SQUIRREL-Errors.lut");
        map.show()
    }


    fun display()
    {
        display_error_map()
    }
}