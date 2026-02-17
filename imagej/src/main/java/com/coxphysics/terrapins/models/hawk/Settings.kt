package com.coxphysics.terrapins.models.hawk

import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.PathWrapper
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.macros.MacroUtils
import com.coxphysics.terrapins.plugins.*
import ij.ImagePlus
import ij.WindowManager

enum class OutputStyle
{
    SEQUENTIAL,
    TEMPORAL
}

enum class NegativeValuesPolicy
{
    ABSOLUTE,
    SEPARATE
}

class Settings private constructor(
    private var image_: Image,
    private var filename_: PathWrapper,
    private var n_levels_: Int,
    private var negative_handling_: NegativeValuesPolicy,
    private var output_style_: OutputStyle)
{
    companion object
    {
        @JvmStatic
        fun from(n_levels: Int, negative_handling: NegativeValuesPolicy, output_style: OutputStyle) : Settings
        {
            return Settings(Image.empty(), PathWrapper.empty(), n_levels, negative_handling, output_style)
        }

        @JvmStatic
        fun default() : Settings
        {
            return from(3, NegativeValuesPolicy.ABSOLUTE, OutputStyle.SEQUENTIAL)
        }

        // for Java to use
        @JvmStatic
        fun default_() : Settings
        {
            return default()
        }

        @JvmStatic
        fun to_negative_value(value: String): NegativeValuesPolicy?
        {
            if (value == ABSOLUTE)
            {
                return NegativeValuesPolicy.ABSOLUTE
            }
            if(value == SEPARATE)
            {
                return NegativeValuesPolicy.SEPARATE
            }
            return null
        }

        @JvmStatic
        fun to_output_style(value: String): OutputStyle?
        {
            if (value == SEQUENTIAL)
            {
                return OutputStyle.SEQUENTIAL
            }
            if(value == TEMPORALLY)
            {
                return OutputStyle.TEMPORAL
            }
            return null
        }

        @JvmStatic
        fun from_macro_options(options: MacroOptions): Settings
        {
            val settings = default()

            val image_name = options.get(HAWK_IMAGE_NAME)
            val image = WindowManager.getImage(image_name)
            settings.set_image(image)

            val n_level_s = options.get(HAWK_N_LEVELS)
            val n_levels = n_level_s?.toIntOrNull()
            if (n_levels != null)
                settings.set_n_levels(n_levels)

            val negative_value_s = options.get(HAWK_NEGATIVE_VALUES)
            val negative_value = negative_value_s?.let { parse_recorded_negative_value(it) }
            if (negative_value != null)
                settings.set_negative_handling(negative_value)

            val output_style_s = options.get(HAWK_OUTPUT_STYLE)
            val output_style = output_style_s?.let { parse_recorded_output_style(it) }
            if (output_style != null)
                settings.set_output_style(output_style)

            val maybe_filename_s = options.get(HAWK_SAVE_TO_DISK)
            if (maybe_filename_s != null)
                settings.set_filename(maybe_filename_s)
            return settings
        }

        @JvmStatic
        fun extract_from_macro(): Settings
        {
            val options = MacroOptions.default()
            return if (options == null)  default() else from_macro_options(options)
        }

        @JvmStatic
        private fun parse_recorded_negative_value(value: String): NegativeValuesPolicy?
        {
            if (value.equals(HAWK_NEGATIVE_VALUE_ABSOLUTE))
                return NegativeValuesPolicy.ABSOLUTE
            if (value.equals(HAWK_NEGATIVE_VALUE_SEPARATE))
                return NegativeValuesPolicy.SEPARATE
            return null
        }

        @JvmStatic
        private fun parse_recorded_output_style(value: String): OutputStyle?
        {
            if (value == HAWK_OUTPUT_STYLE_SEQUENTIAL)
                return OutputStyle.SEQUENTIAL
            if (value == HAWK_OUTPUT_STYLE_TEMPORAL)
                return OutputStyle.TEMPORAL
            return null
        }
    }

    fun inner_image(): Image
    {
        return image_
    }

    fun file_path_wrapper(): PathWrapper
    {
        return filename_
    }

    fun filename(): String
    {
        return filename_.to_string()
    }

    fun set_filename(value: String)
    {
        filename_.set_path_from_string(value)
    }

    fun image(): ImagePlus?
    {
        return image_.to_image_plus()
    }

    fun image_name(): String
    {
        val ip = image()
        return if (ip == null) "" else ip.title
    }

    fun set_image(image: ImagePlus?)
    {
        image_.set_inner(image)
    }

    fun n_levels(): Int
    {
        return n_levels_
    }

    fun set_n_levels(value: Int)
    {
        n_levels_ = value
    }

    fun negative_handling(): String
    {
        return negative_handling_.toString()
    }

    fun output_style(): String
    {
        return output_style_.toString()
    }

    fun is_absolute(): Boolean
    {
        return negative_handling_ == NegativeValuesPolicy.ABSOLUTE
    }

    fun is_separate(): Boolean
    {
        return negative_handling_ == NegativeValuesPolicy.SEPARATE
    }

    fun set_negative_handling(value: NegativeValuesPolicy)
    {
        negative_handling_ = value
    }

    fun is_sequential(): Boolean
    {
        return output_style_ == OutputStyle.SEQUENTIAL
    }

    fun is_temporal(): Boolean
    {
        return output_style_ == OutputStyle.TEMPORAL
    }

    fun set_output_style(value: OutputStyle)
    {
        output_style_ = value
    }

    fun error_string(): String?
    {
        val config = Config.from(this)
        val n_frames = n_frames()
        if (n_frames == null)
            return null
        return config.get_validation_errors(n_frames)
    }

    fun n_frames(): Int?
    {
        return image_.to_image_plus()?.stack?.size
    }

    fun record_values()
    {
        MacroUtils.record(HAWK_IMAGE_NAME, image_name())
        MacroUtils.record(HAWK_N_LEVELS, n_levels().toString())
        MacroUtils.record(HAWK_NEGATIVE_VALUES, negative_values_recording_key())
        MacroUtils.record(HAWK_OUTPUT_STYLE, output_style_recording_key())
        if (filename_.has_data())
            MacroUtils.record(HAWK_SAVE_TO_DISK, filename_.to_string())
    }

    private fun negative_values_recording_key(): String
    {
        if (is_separate())
        {
            return HAWK_NEGATIVE_VALUE_SEPARATE
        }
        return HAWK_NEGATIVE_VALUE_ABSOLUTE
    }

    private fun output_style_recording_key(): String
    {
        if(is_temporal())
        {
            return HAWK_OUTPUT_STYLE_TEMPORAL
        }
        return HAWK_OUTPUT_STYLE_SEQUENTIAL
    }
}