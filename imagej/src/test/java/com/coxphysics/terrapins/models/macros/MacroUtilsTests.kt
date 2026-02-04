package com.coxphysics.terrapins.models.macros

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MacroUtilsTests
{
    @Test
    fun is_ran_from_macro_test()
    {
        assertEquals(MacroUtils.is_ran_from_macro(), false)
    }

}