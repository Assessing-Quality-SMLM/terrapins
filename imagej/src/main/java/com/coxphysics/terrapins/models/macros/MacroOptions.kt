package com.coxphysics.terrapins.models.macros

import ij.Macro
import ij.plugin.frame.Recorder

class MacroOptions private constructor(private var options: String)
{
    companion object
    {
        @JvmStatic
        fun from(options: String) : MacroOptions
        {
            return MacroOptions(options)
        }

        @JvmStatic
        fun from_recorder_command_options() : MacroOptions
        {
            return from(Recorder.getCommandOptions())
        }

        @JvmStatic
        fun default_() : MacroOptions?
        {
            val options = Macro.getOptions()
            if (options == null)
            {
                return null
            }
            return from(options)
        }

        @JvmStatic
        fun empty() : Boolean
        {
            return Macro.getOptions() == null
        }
    }

    fun get(key: String) : String?
    {
        return Macro.getValue(options, key, null)
    }
}