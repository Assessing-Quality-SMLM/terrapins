package com.coxphysics.terrapins.models.hawk

import com.coxphysics.terrapins.models.ij_wrapping.WindowManager
import ij.ImagePlus

class FakeWindowManager private constructor(
    private val map_: Map<String, ImagePlus>
): WindowManager
{
    companion object
    {
        fun from(map: Map<String, ImagePlus>) : FakeWindowManager
        {
            return FakeWindowManager(map)
        }

        fun empty(): FakeWindowManager
        {
            return from(emptyMap())
        }
    }
    override fun get_image(image_name: String): ImagePlus?
    {
        if(map_.containsKey(image_name))
            return map_[image_name]
        return null
    }

    override fun get_image_at(index: Int): ImagePlus?
    {
        return map_.values.elementAt(index)
    }
}