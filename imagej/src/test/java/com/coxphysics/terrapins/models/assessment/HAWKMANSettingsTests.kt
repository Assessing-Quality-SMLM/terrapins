package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.plugins.HAWKMAN_SETTINGS_N_LEVELS
import org.junit.jupiter.api.AfterEach
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HAWKMANSettingsTests
{
    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

    @Test
    fun macro_recording_records_n_levels()
    {
        executor.submit{
            val settings = HAWKMANSettings.new(50)
            settings.record_to_macro()

            val options = MacroOptions.from_recorder_command_options()

            val new_settings = HAWKMANSettings.from_macro_options(MacroOptions.from_recorder_command_options())
            assertEquals(new_settings.n_levels(), 50)
        }.get()
    }
}