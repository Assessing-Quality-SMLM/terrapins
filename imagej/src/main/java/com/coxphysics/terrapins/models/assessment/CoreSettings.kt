package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.non_null

class CoreSettings
{
    private var widefield_: String? = null
    private var image_stack_: String? = null
    private var settings_file_: String? = null

    companion object
    {
        @JvmStatic
        fun default(): CoreSettings
        {
            return CoreSettings()
        }
    }

    fun has_widefield(): Boolean
    {
        return !widefield_.isNullOrEmpty()
    }

    fun widefield_nn(): String
    {
        return widefield_.non_null()
    }

    fun set_widefield(value: String)
    {
        widefield_ = value
    }

    fun has_image_stack(): Boolean
    {
        return !image_stack_.isNullOrEmpty()
    }

    fun image_stack_nn(): String
    {
        return image_stack_.non_null()
    }

    fun set_image_stack(value: String)
    {
        image_stack_ = value
    }

    fun has_settings_file(): Boolean
    {
        return !settings_file_.isNullOrEmpty()
    }

    fun settings_file_nn(): String
    {
        return settings_file_.non_null()
    }

    fun set_settings_file(value: String)
    {
        settings_file_ = value
    }
}