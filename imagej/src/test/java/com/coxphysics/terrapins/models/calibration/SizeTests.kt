package com.coxphysics.terrapins.models.calibration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SizeTests
{
    @Test
    fun can_convert_micro_to_nm()
    {
        assertEquals(Size.from(10.0, Unit.MICRO).to_nm(), 10000.0)
    }

    @Test
    fun if_nano_no_conversion()
    {
        assertEquals(Size.from(10.0, Unit.NANO).to_nm(), 10.0)
    }

    @Test
    fun reference_equality()
    {
        val size = Size.from(10.0, Unit.NANO)
        assertEquals(size, size)
    }

    @Test
    fun structural_equality()
    {
        val size_1 = Size.from(10.0, Unit.NANO)
        val size_2 = Size.from(10.0, Unit.NANO)
        assertEquals(size_1, size_2)
    }

    @Test
    fun unequal_on_unit()
    {
        val size_1 = Size.from(10.0, Unit.NANO)
        val size_2 = Size.from(10.0, Unit.MICRO)
        assertNotEquals(size_1, size_2)
    }

    @Test
    fun unequal_on_value()
    {
        val size_1 = Size.from(10.0, Unit.NANO)
        val size_2 = Size.from(10.1, Unit.NANO)
        assertNotEquals(size_1, size_2)
    }
}