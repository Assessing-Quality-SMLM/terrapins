package com.coxphysics.terrapins.models.localisations

import com.coxphysics.terrapins.models.non_null
import com.coxphysics.terrapins.models.utils.StringUtils

class LocalisationFile private constructor(
    private var localisation_file_: String?,
    private val parse_method_: ParseMethod)
{
    companion object
    {
        @JvmStatic
        fun new(filename: String, parse_method: ParseMethod): LocalisationFile
        {
            return LocalisationFile(filename, parse_method)
        }

        @JvmStatic
        fun default(): LocalisationFile
        {
            return LocalisationFile(null, ParseMethod.default_())
        }
    }

    fun is_set(): Boolean
    {
        return localisation_file_ != null && localisation_file_ != StringUtils.EMPTY_STRING
    }

    fun filename_nn() : String
    {
        return localisation_file_.non_null()
    }

    fun set_filename(value: String)
    {
        localisation_file_ = value
    }

    fun parse_method(): ParseMethod
    {
        return parse_method_
    }

    fun parse_method_string(): String
    {
        return parse_method_.parse_method()
    }

    fun set_delimeter(value: Char)
    {
        parse_method_.set_delimiter(value)
    }

    fun set_n_header_lines(value: Int)
    {
        parse_method_.set_n_headers(value)
    }

    fun set_x_pos(value: Int)
    {
        parse_method_.set_x_pos(value)
    }

    fun set_y_pos(value: Int)
    {
        parse_method_.set_y_pos(value)
    }

    fun set_uncertainty_sigma_pos(value: Int)
    {
        parse_method_.set_uncertainty_sigma_pos(value)
    }

    fun set_frame_number_pos(value: Int)
    {
        parse_method_.set_frame_number_pos(value)
    }
}