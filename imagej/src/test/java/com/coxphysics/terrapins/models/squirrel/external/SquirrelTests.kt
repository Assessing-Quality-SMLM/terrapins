package com.coxphysics.terrapins.models.squirrel.external

import com.coxphysics.terrapins.models.squirrel.SquirrelSettings
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path

class SquirrelTests
{
    private fun program_path(): Path
    {
        return File("some/program").toPath()
    }

    private fun output_data(): String
    {
        return File("some/squirrel_data").toPath().toString()
    }

    private fun output_data_parameter(): String
    {
        return "od=%s".format(output_data())
    }
    @Test
    fun registration_propogated_to_arguments()
    {
        val settings = SquirrelSettings.default_()
        settings.set_registration(true)
        val squirrel = Squirrel.custom (program_path())
        val commands = squirrel.get_commands(File("widefield").toPath(), File("super-res").toPath(), settings)
        val expected = listOf(program_path().toString(), "wf=widefield", "sr=super-res", output_data_parameter(), "sigma=200.0", "reg", "cb")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun if_not_registering_reg_flag_not_in_args()
    {
        val settings = SquirrelSettings.default_()
        settings.set_registration(false)
        val squirrel = Squirrel.custom (program_path())
        val commands = squirrel.get_commands(File("widefield").toPath(), File("super-res").toPath(), settings)
        val expected = listOf(program_path().toString(), "wf=widefield", "sr=super-res", output_data_parameter(), "sigma=200.0", "cb")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun crop_border_propogated_to_arguments()
    {
        val settings = SquirrelSettings.default_()
        settings.set_crop_borders(true)
        val squirrel = Squirrel.custom (File("some/program").toPath())
        val commands = squirrel.get_commands(File("widefield").toPath(), File("super-res").toPath(), settings)
        val expected = listOf(program_path().toString(), "wf=widefield", "sr=super-res", output_data_parameter(), "sigma=200.0", "reg", "cb")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun if_not_cropping_borders_flag_not_in_args()
    {
        val settings = SquirrelSettings.default_()
        settings.set_crop_borders(false)
        val squirrel = Squirrel.custom (program_path())
        val commands = squirrel.get_commands(File("widefield").toPath(), File("super-res").toPath(), settings)
        val expected = listOf(program_path().toString(), "wf=widefield", "sr=super-res", output_data_parameter(), "sigma=200.0", "reg")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }
}