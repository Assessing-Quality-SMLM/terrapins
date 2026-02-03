package com.coxphysics.terrapins.models.macros

import ij.plugin.frame.Recorder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MacroOptionsTests
{
    @Test
    fun is_empty_test()
    {
        assertEquals(MacroOptions.empty(), true)
    }

    @Test
    fun get_value_test()
    {
        Recorder.recordOption("some", "thing")
        val options = MacroOptions.from_recorder_command_options()
        assertEquals(options.get("some"), "thing")
    }

    @Test
    fun get_missing_test()
    {
        Recorder.recordOption("some", "thing")
        val options = MacroOptions.from_recorder_command_options()
        assertEquals(options.get("else"), null)
    }
}