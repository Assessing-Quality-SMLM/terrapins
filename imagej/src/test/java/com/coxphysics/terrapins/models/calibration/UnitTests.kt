package com.coxphysics.terrapins.models.calibration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UnitTests
{
    @Test
    fun can_parse_nano()
    {
        assertEquals(Unit.parse("nm"), Unit.NANO)
    }

    @Test
    fun can_parse_micron()
    {
        assertEquals(Unit.parse("micron"), Unit.MICRO)
    }

    @Test
    fun is_micrometers()
    {
        assertEquals(Unit.NANO.is_micrometers(), false)
        assertEquals(Unit.MICRO.is_micrometers(), true)
    }
}