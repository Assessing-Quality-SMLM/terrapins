package com.coxphysics.terrapins.models.localisations

import com.coxphysics.terrapins.models.PathWrapper
import ij.plugin.frame.Recorder

class LocalisationFile private constructor(
    private var localisation_file_: PathWrapper,
    private val parse_method_: ParseMethod)
{
    companion object
    {
        @JvmStatic
        fun new(filename: String, parse_method: ParseMethod): LocalisationFile
        {
            return LocalisationFile(PathWrapper.from_string(filename), parse_method)
        }

        @JvmStatic
        fun default(): LocalisationFile
        {
            return LocalisationFile(PathWrapper.empty(), ParseMethod.default_())
        }
    }

    fun path(): PathWrapper
    {
        return localisation_file_
    }

    fun is_set(): Boolean
    {
        return localisation_file_.has_data()
    }

    fun filename_nn() : String
    {
        return localisation_file_.to_string()
    }

    fun set_filename(value: String)
    {
        localisation_file_.set_path_from_string(value)
    }

    fun parse_method(): ParseMethod
    {
        return parse_method_
    }

    fun parse_method_string(): String
    {
        return parse_method_.parse_method()
    }

    fun use_thunderstorm(): Boolean
    {
        return parse_method().use_thunderstorm()
    }

    fun set_use_thunderstorm(value: Boolean)
    {
        if (value)
            parse_method_.set_parse_method_thunderstorm()
        else
            parse_method_.set_parse_method_csv()
    }
    fun delimeter(): Char
    {
        return parse_method_.delimiter()
    }

    fun set_delimeter(value: Char)
    {
        parse_method_.set_delimiter(value)
    }

    fun n_header_lines(): Int
    {
        return parse_method_.n_header_lines()
    }

    fun set_n_header_lines(value: Int)
    {
        parse_method_.set_n_headers(value)
    }

    fun x_pos(): Int
    {
        return parse_method_.x_position()
    }

    fun set_x_pos(value: Int)
    {
        parse_method_.set_x_pos(value)
    }

    fun y_pos(): Int
    {
        return parse_method_.y_position()
    }

    fun set_y_pos(value: Int)
    {
        parse_method_.set_y_pos(value)
    }

    fun uncertainty_sigma_pos(): Int
    {
        return parse_method_.uncertainty_position()
    }

    fun set_uncertainty_sigma_pos(value: Int)
    {
        parse_method_.set_uncertainty_sigma_pos(value)
    }

    fun frame_number_pos(): Int
    {
        return parse_method_.frame_number_position()
    }

    fun set_frame_number_pos(value: Int)
    {
        parse_method_.set_frame_number_pos(value)
    }

    fun record_to_macro(key: String, parse_key: String)
    {
        val filename = filename_nn()
        Recorder.recordOption(key, filename)
        parse_method_.record_to_macro(parse_key)
    }
}