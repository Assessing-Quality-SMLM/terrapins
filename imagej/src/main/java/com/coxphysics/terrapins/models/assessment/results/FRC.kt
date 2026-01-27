package com.coxphysics.terrapins.models.assessment.results

import com.coxphysics.terrapins.models.frc.FRCResult
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import java.nio.file.Path

class FRC private constructor(private val path: Path)
{
    companion object
    {
        @JvmStatic
        fun from(path: Path): FRC
        {
            return FRC(path)
        }
    }

    private fun name_prefix(): String
    {
        return path.fileName.toString()
    }

    private fun images_path(): Path
    {
        return path.resolve("image")
    }

    private fun image_a_path(): Path
    {
        return images_path().resolve("a.tiff")
    }

    private fun image_a_data_path(): Path
    {
        return images_path().resolve("a_data")
    }

    fun image_a(): ImagePlus?
    {
        return IJUtils.load_image_with_prefix(image_a_path(), name_prefix())
    }

    private fun image_a_data(): String?
    {
        return FsUtils.read_to_string_utf8(image_a_data_path())
    }

    private fun image_b_path(): Path
    {
        return images_path().resolve("b.tiff")
    }

    private fun image_b_data_path(): Path
    {
        return images_path().resolve("b_data")
    }

    fun image_b(): ImagePlus?
    {
        return IJUtils.load_image_with_prefix(image_b_path(), name_prefix())
    }

    private fun image_b_data(): String?
    {
        return FsUtils.read_to_string_utf8(image_b_data_path())
    }

    fun info(): String
    {
        val a_text = image_a_data()
        val b_text = image_b_data()
        val builder = StringBuilder()
        if (a_text != null)
            builder.append(a_text)
        if (b_text != null)
            builder.append(b_text)
        return builder.toString()
    }

    fun results(): FRCResult?
    {
        return FRCResult.from(path)
    }
}