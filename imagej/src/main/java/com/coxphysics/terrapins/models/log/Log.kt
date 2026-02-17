package com.coxphysics.terrapins.models.log

import ij.IJ

interface Log<T>
{
    fun log(item: T)
}

class IJLog private constructor(): Log<String>
{
    companion object
    {
        fun new() : IJLog
        {
            return IJLog()
        }
    }
    override fun log(item: String)
    {
        IJ.log(item)
    }
}
