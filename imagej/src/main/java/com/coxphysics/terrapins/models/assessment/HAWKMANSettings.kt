package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.plugins.HAWKMAN_SETTINGS_N_LEVELS
import ij.plugin.frame.Recorder

const val DEFAULT_N_LEVELS = 20

class HAWKMANSettings private constructor(private var n_levels_: Int)
{
    companion object
    {
        @JvmStatic
        fun new(n_levels: Int): HAWKMANSettings
        {
            return HAWKMANSettings(n_levels)
        }

        @JvmStatic
        fun default(): HAWKMANSettings
        {
            return new(DEFAULT_N_LEVELS)
        }

        fun from_macro_options(options: MacroOptions) : HAWKMANSettings
        {
            val n_levels = options.get_int(HAWKMAN_SETTINGS_N_LEVELS) ?: DEFAULT_N_LEVELS
            return new(n_levels)
        }
    }

    fun n_levels(): Int
    {
        return n_levels_
    }

    fun set_n_levels(value: Int)
    {
        n_levels_ = value
    }

    fun record_to_macro()
    {
        Recorder.recordOption(HAWKMAN_SETTINGS_N_LEVELS, n_levels_.toString())
    }
}