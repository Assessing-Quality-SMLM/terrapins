package com.coxphysics.terrapins.models.macros

import ij.Macro
import ij.plugin.frame.Recorder

class MacroUtils
{
    companion object
    {
        @JvmStatic
        fun is_ran_from_macro() : Boolean
        {
            return Macro.getOptions() != null
        }

        @JvmStatic
        fun is_recording(): Boolean
        {
            return Recorder.record
        }

        @JvmStatic
        fun record(key: String, value: String)
        {
            return Recorder.recordOption(key, value)
        }
    }
}