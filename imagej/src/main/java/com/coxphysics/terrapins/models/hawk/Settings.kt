package com.coxphysics.terrapins.models.hawk

class Settings private constructor(
    private val n_levels_: Int,
    private val negative_handling_: String,
    private val output_style_: String)
{
    companion object
    {
        @JvmStatic
        fun from(n_levels: Int, negative_handling: String, output_style: String) : Settings
        {
            return Settings(n_levels, negative_handling, output_style)
        }

        @JvmStatic
        fun default() : Settings
        {
            return from(3, ABSOLUTE, SEQUENTIAL)
        }

        // for Java to use
        @JvmStatic
        fun default_() : Settings
        {
            return default()
        }
    }

    fun n_levels(): Int
    {
        return n_levels_
    }

    fun negative_handling(): String
    {
        return negative_handling_
    }

    fun output_style(): String
    {
        return output_style_
    }

    fun is_absolute(): Boolean
    {
        return negative_handling_ == ABSOLUTE
    }

    fun is_separate(): Boolean
    {
        return negative_handling_ == SEPARATE
    }

    fun is_sequential(): Boolean
    {
        return output_style_ == SEQUENTIAL
    }

    fun is_temporal(): Boolean
    {
        return output_style_ == TEMPORALLY
    }

}