package com.coxphysics.terrapins.view_models.io

import com.coxphysics.terrapins.models.non_null

class FileFieldVM private constructor(
    private var name_: String,
    private var filename_: String?)
{
    companion object
    {
        @JvmStatic
        fun from(filename: String) : FileFieldVM
        {
            return FileFieldVM("Filename",  filename)
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

    fun filename(): String
    {
        return filename_.non_null()
    }

}