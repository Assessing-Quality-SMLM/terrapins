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

    private fun assessment_data_dir(): Path
    {
        return data_path_.parent
    }

    private fun widefield_path(is_non_linear: Boolean): Path
    {
        val image_name = if (is_non_linear) "aof_widefield.tiff" else "widefield.tiff"
        return assessment_data_dir().resolve(image_name)
    }

    private fun big_widefield_path(): Path
    {
        return data_path_.resolve("big_widefield.tiff")
    }

    private fun sr_transform_path(): Path
    {
        return data_path_.resolve("sr_affine_blurred.tiff")
    }

    private fun error_map_path(): Path
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

    fun load_widefield(is_non_linear: Boolean): ImagePlus?
    {
        return IJUtils.load_image(widefield_path(is_non_linear))
    }

    fun load_big_widefield(): ImagePlus?
    {
        return IJUtils.load_image(big_widefield_path())
    }

    fun load_sr_transform(): ImagePlus?
    {
        return IJUtils.load_image(sr_transform_path())
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