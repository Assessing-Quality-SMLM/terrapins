package com.coxphysics.terrapins.models.calibration

enum class Unit
{
    NANO,
    MICRO;

    companion object
    {
        @JvmStatic
        fun parse(unit: String) : Unit?
        {
            if (unit == "nm")
            {
                return Unit.NANO
            }
            else if (unit == "micron")
            {
                return Unit.MICRO
            }
            return null
        }
    }

    fun is_nanometers(): Boolean
    {
        return this == NANO
    }

    fun is_micrometers(): Boolean
    {
        return this == MICRO
    }
}