package com.coxphysics.terrapins.models.localisations
import com.coxphysics.terrapins.models.macros.MacroOptions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class LocalisationFileTests
{
    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

    @Test
    fun can_record_filename()
    {
        executor.submit {
            val settings = LocalisationFile.new("something", ParseMethod.default_())
            MacroOptions.reset()
            settings.record_to_macro("else", "parse_key")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get("else"), "something")
        }.get()
    }

    @Test
    fun can_record_parse_method()
    {
        executor.submit {
            val parse_method = ParseMethod.default_()
            parse_method.set_parse_method_csv()
            parse_method.set_n_headers(0)
            parse_method.set_frame_number_pos(1)
            parse_method.set_x_pos(2)
            parse_method.set_y_pos(3)
            parse_method.set_psf_sigma_pos(4)
            parse_method.set_uncertainty_pos(5)
            val settings = LocalisationFile.new("something", parse_method)
            MacroOptions.reset()
            settings.record_to_macro("f_key", "else")
            val options = MacroOptions.from_recorder_command_options()
            assertEquals(options.get("else"), ",a0a1a2a3a4a5")
        }.get()
    }

    @Test
    fun can_read_path_from_options()
    {
        executor.submit{
            val options = MacroOptions.from("f_name=something")
            val loc_file = LocalisationFile.from_macro_options("f_name", "parse_key", options)
            assertEquals(loc_file.filename_nn(), "something")
            assertEquals(loc_file.use_thunderstorm(), true)
        }.get()
    }

    @Test
    fun can_read_parse_method_options()
    {
        executor.submit{
            val options = MacroOptions.from("f_name=[something], p_key=[,a0a1a2a3a4a5]")
            val loc_file = LocalisationFile.from_macro_options("f_name", "p_key", options)
            assertEquals(loc_file.filename_nn(), "something")
            val parse_method = loc_file.parse_method()
            assertEquals(parse_method.n_header_lines(), 0)
            assertEquals(parse_method.frame_number_position(), 1)
            assertEquals(parse_method.x_position(), 2)
            assertEquals(parse_method.y_position(), 3)
            assertEquals(parse_method.psf_sigma_position(), 4)
            assertEquals(parse_method.uncertainty_position(), 5)
        }.get()
    }
}