package com.coxphysics.terrapins.models.squirrel.external

import com.coxphysics.terrapins.models.squirrel.utils.StackHelper
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
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

    private fun optimiser_data_path(): Path
    {
        return data_path_.resolve("optimiser_data")
    }

    fun load_error_map(): ImagePlus?
    {
        val map = IJUtils.load_image(error_map_path())
        if (map == null)
            return null
        StackHelper.applyLUT(map,"SQUIRREL-Errors.lut");
        return map
    }

    fun display_error_map()
    {
        load_error_map()?.show()
    }

    fun display()
    {
        display_error_map()
    }

    fun optimiser_output(): String?
    {
        return FsUtils.read_to_string_utf8(optimiser_data_path())
    }
}