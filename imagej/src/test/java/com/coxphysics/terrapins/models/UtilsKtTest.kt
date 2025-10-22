package com.coxphysics.terrapins.models

import ij.process.FloatProcessor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UtilsKtTest
{
    @Test
    fun arange_test()
    {
        val data = arange_floats(5);
        assertEquals(data[0], 0f)
        assertEquals(data[1], 1f)
        assertEquals(data[2], 2f)
        assertEquals(data[3], 3f)
    }

    @Test
    fun clones_are_immutable()
    {
        val data = arange_floats(25)
        val image = FloatProcessor(5, 5, data)
        val new_image = immutable_clone(image)
        new_image.set(0, 0, 10)
        assertEquals(image.get(0, 0), 0)
        assertEquals(new_image.get(0, 0), 10)
    }

    @Test
    fun pixel_iterator_test()
    {
        val data = arange_floats(6)
        val image = FloatProcessor(2, 3, data)
        val pixels = image.pixel_iterator();
        val pixel_values = pixels.toList()
        assertEquals(pixel_values[0], 0f)
        assertEquals(pixel_values[1], 2f)
        assertEquals(pixel_values[2], 4f)
        assertEquals(pixel_values[3], 1f)
        assertEquals(pixel_values[4], 3f)
        assertEquals(pixel_values[5], 5f)
    }
}