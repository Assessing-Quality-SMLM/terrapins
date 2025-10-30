package com.coxphysics.terrapins.models.calibration

class Size private constructor(
    private val value_: Double,
    private val unit_: Unit
)
{
    companion object
    {
        @JvmStatic
        fun from(value: Double, unit: Unit) : Size
        {
            return Size(value, unit)
        }
    }

    fun to_nm() : Double
    {
        if (unit_.is_micrometers())
        {
            return value_ * 1000;
        }
        return value_
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
            return true
        if (other !is Size)
            return false
        return this.unit_ == other.unit_ &&
                this.value_ == other.value_
    }
}