package com.coxphysics.terrapins.models.ij_wrapping

import ij.ImagePlus

interface WindowManager
{
    fun get_image(image_name: String) : ImagePlus?
}

class IJWindowManager private constructor(): WindowManager
{
    companion object
    {
        @JvmStatic
        fun new() : IJWindowManager
        {
            return IJWindowManager()
        }
    }
    override fun get_image(image_name: String): ImagePlus?
    {
        return ij.WindowManager.getImage(image_name)
    }

}