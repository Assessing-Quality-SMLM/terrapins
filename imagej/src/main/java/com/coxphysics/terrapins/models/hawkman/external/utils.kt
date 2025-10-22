package com.coxphysics.terrapins.models.hawkman.external

import com.coxphysics.terrapins.models.utils.IJUtils
import com.coxphysics.terrapins.models.utils.StringUtils
//import com.coxphysics.terrapins.models.write_to_disk
import ij.ImagePlus
import ij.WindowManager
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

const val REFERENCE_NAME = "ref.tiff"
const val TEST_NAME = "test.tiff"

private fun prepare_image(image: ImagePlus, directory: Path, image_name: String): Path
{
    val file_path = Paths.get(directory.toString(), image_name)
//    return image.write_to_disk(file_path);
    return IJUtils.write_to_disk(image, file_path);
}

private fun prepare_reference(image : ImagePlus, directory: Path): Path
{
    return prepare_image(image, directory, REFERENCE_NAME)
}

private fun prepare_test(image : ImagePlus, directory: Path): Path
{
    return prepare_image(image, directory, TEST_NAME)
}

fun name_to_image(image_name: String): ImagePlus
{
    return WindowManager.getImage(image_name);
}

fun prepare_images(ref: ImagePlus, test: ImagePlus, directory: Path): Pair<Path, Path>
{
    val ref_path = prepare_reference(ref, directory)
    val test_path = prepare_test(test, directory)
    return Pair(ref_path, test_path)
}

fun prepare_images_from_names(ref: String, test: String, directory: Path): Pair<Path, Path>
{
    return prepare_images(name_to_image(ref), name_to_image(test), directory)
}

fun parse_score_line(line: String): Pair<Int, Double>?
{
    val splits = line.split(",")
    if (splits.size < 2)
        return null
    val level = StringUtils.parse_unisigned_int(splits[0])
    val value = StringUtils.parse_double(splits[1])
    return Pair(level, value)
}

class Helpers
{
    companion object
    {
        @JvmStatic
        fun prepare_images(ref: String, test: String, directory: Path): Pair<Path, Path>
        {
            return prepare_images_from_names(ref, test, directory)
        }

        @JvmStatic
        fun filename(file_name: File): String
        {
             return file_name.nameWithoutExtension
        }

        @JvmStatic
        fun parse_score(line: String): Pair<Int, Double>?
        {
            return parse_score_line(line)
        }
    }
}