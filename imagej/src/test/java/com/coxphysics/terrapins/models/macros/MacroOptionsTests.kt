package com.coxphysics.terrapins.models.macros

import ij.plugin.frame.Recorder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class MacroOptionsTests
{

    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

    @Test
    fun is_empty_test()
    {
        executor.submit { assertEquals(MacroOptions.empty(), true) }.get()
    }

    @Test
    fun has_key_test()
    {
        executor.submit {
            val options = MacroOptions.from("")
            assertEquals(options.has_key("something"), false)
        }.get()
    }

    @Test
    fun get_value_test()
    {
        executor.submit {
            MacroOptions.reset()
            Recorder.recordOption("some", "thing")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get("some"), "thing")
        }.get()
    }

    @Test
    fun get_missing_test()
    {
        executor.submit{
            MacroOptions.reset()
            Recorder.recordOption("some", "thing")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get("else"), null)
        }.get()
    }

    @Test
    fun can_parse_int()
    {
        executor.submit{
            MacroOptions.reset()
            Recorder.recordOption("some", 1.toString())
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get_int("some"), 1)
        }.get()
    }

    @Test
    fun fails_gracefully_for_non_ints()
    {
        executor.submit{
            MacroOptions.reset()
            Recorder.recordOption("some", "thing")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get_int("some"), null)
        }.get()
    }

    @Test
    fun can_parse_double()
    {
        executor.submit{
            MacroOptions.reset()
            Recorder.recordOption("some", 1.0.toString())
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get_double("some"), 1.0)
        }.get()
    }

    @Test
    fun fails_gracefully_for_non_doubles()
    {
        executor.submit{
            MacroOptions.reset()
            Recorder.recordOption("some", "thing")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get_double("some"), null)
        }.get()
    }
}