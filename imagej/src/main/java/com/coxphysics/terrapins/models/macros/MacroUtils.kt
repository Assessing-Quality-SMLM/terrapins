package com.coxphysics.terrapins.models.macros

import ij.Macro

class MacroUtils
{
    companion object
    {
        @JvmStatic
        fun is_ran_from_macro() : Boolean
        {
            return Macro.getOptions() != null
        }
    }
}