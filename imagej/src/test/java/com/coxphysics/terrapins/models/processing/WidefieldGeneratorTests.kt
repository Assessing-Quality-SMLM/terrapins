package com.coxphysics.terrapins.models.processing

import ij.ImageStack
import ij.process.FloatProcessor
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class WidefieldGeneratorTests
{
    @Test
    fun basic_aof_tests()
    {
        val frame_1 = FloatProcessor(1, 3, arrayOf(1.0, 2.0, 3.0).toDoubleArray())
        val frame_2 = FloatProcessor(1, 3, arrayOf(4.0, 5.0, 6.0).toDoubleArray())
        val stack = ImageStack(1, 3, 2)
        stack.setProcessor(frame_1, 1)
        stack.setProcessor(frame_2, 2)
        val aof = WidefieldGenerator.average_of_frames(stack)
        val aof_data = aof.floatArray[0]
        val expected = arrayOf(2.5f, 3.5f, 4.5f).toFloatArray()
        assertArrayEquals(aof_data, expected)
    }
}