package com.coxphysics.terrapins.view_models

class OptionalInputVM private constructor(
    private val model_: Boolean,
    private var name_: String)
{
    companion object
    {
        @JvmStatic
        fun from(model: Boolean) : OptionalInputVM
        {
            return OptionalInputVM(model, "")
        }
    }

    fun name(): String
    {
        return name_
    }

    fun set_name(value: String)
    {
        name_ = value
    }

    fun available(): Boolean
    {
        return model_
    }
}