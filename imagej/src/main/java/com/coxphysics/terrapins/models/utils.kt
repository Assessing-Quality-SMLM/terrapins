package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.StringUtils
import ij.process.ImageProcessor

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

//fun ImagePlus.write_to_disk(image_path: Path): Path
//{
//    if (!image_path.parent.exists())
//    {
//        image_path.createParentDirectories()
//    }
//    IJ.saveAsTiff(this, image_path.toString())
//    return image_path
//}