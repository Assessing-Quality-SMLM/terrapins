package com.coxphysics.terrapins.models.assessment.results

import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import java.nio.file.Path

class Recon private constructor(private val path: Path)
{
    companion object
    {
        @JvmStatic
        fun from(path: Path): Recon
        {
            return Recon(path)
        }
    }

    private fun image_path(): Path
    {
        return path.resolve("image.tiff")
    }

    fun image(): ImagePlus?
    {
        return IJUtils.load_image(image_path())
    }

    private fun data_path(): Path
    {
        return path.resolve("data")
    }

    fun data(): String?
    {
        return FsUtils.read_to_string_utf8(data_path())
    }

    private fun localisation_path(): Path
    {
        return path.resolve("localisation_data")
    }

    fun localisation_list(): String?
    {
        return FsUtils.read_to_string_utf8(localisation_path())
    }
}