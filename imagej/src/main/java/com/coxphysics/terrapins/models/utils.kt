package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.StringUtils
import ij.process.ImageProcessor
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

fun <T: ImageProcessor> immutable_clone(image_processor: T): T
{
   return image_processor.duplicate() as T
}

fun arange_floats(n_elements: Int): FloatArray
{
   return (0..<n_elements).map { idx -> idx.toFloat() }
                                     .toFloatArray()
}

fun <T: ImageProcessor> T.pixel_iterator(): Sequence<Float>
{
   return (0..<this.width).asSequence()
                              .flatMap {
                                 col -> (0..<this.height).asSequence()
                                                             .map {
                                                                row -> this.getf(col, row) } }
}

fun <T: ImageProcessor> T.non_zero_pixels(): Int
{
   return this.pixel_iterator().count { value -> value > 0.0 }
}

fun String?.non_null(): String
{
    if (this == null)
    {
        return StringUtils.EMPTY_STRING
    }
    return this
}

fun String?.to_nullable_path(): Path?
{
    if (this == null)
    {
        return null
    }
    try
    {
        return Paths.get(this)
    }
    catch (e: InvalidPathException)
    {
        return null
    }
}


fun Path?.to_string_non_null(): String
{
    return if(this == null) StringUtils.EMPTY_STRING else this.toString()
}

fun String?.to_path_or_temp(): Path
{
    return to_path_or_default(FsUtils.temp_directory())
}

fun String?.to_path_or_default(default: Path): Path
{
    return to_nullable_path() ?: default
}

//fun ImagePlus.write_to_disk(image_path: Path): Path
//{
//    if (!image_path.parent.exists())
//    {
//        image_path.createParentDirectories()
//    }
//    IJ.saveAsTiff(this, image_path.toString())
//    return image_path
//}