package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.assessment.images.Settings
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path

class ImagesTests
{
    private fun exe_path(): Path
    {
        return Path("a/program")
    }

    private fun working_directory_path(): Path
    {
        return Path("a/smlm_assessment")
    }

    private fun working_directory(): String
    {
        return working_directory_path().toString()
    }

    @Test
    fun default_arguments_test()
    {
        val settings = Settings.with(working_directory_path())
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--extract", "image", "--magnification", "10.0", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_reference_image()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_reference_filename("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--extract", "image", "--magnification", "10.0", "--reference-image", "some.thing", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_hawk_image()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_hawk_filename("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--extract", "image", "--magnification", "10.0", "--hawk-image", "some.thing", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun only_half_split_a_no_setting()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_half_split_a("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--extract", "image", "--magnification", "10.0", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun only_half_split_b_no_setting()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_half_split_b("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--extract", "image", "--magnification", "10.0", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_half_split()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_half_split_a("some.thing")
        settings.set_half_split_b("some.thing.else")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--extract", "image", "--magnification", "10.0", "--half-split-a", "some.thing", "--half-split-b", "some.thing.else", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun only_zip_split_a_no_setting()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_zip_split_a("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--extract", "image", "--magnification", "10.0", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun only_zip_split_b_no_setting()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_zip_split_b("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--extract", "image", "--magnification", "10.0", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_zip_split()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_zip_split_a("some.thing")
        settings.set_zip_split_b("some.thing.else")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--extract", "image", "--magnification", "10.0", "--zip-split-a", "some.thing", "--zip-split-b", "some.thing.else", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_widefield()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_widefield_filename("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--widefield", "some.thing", "--extract", "image", "--magnification", "10.0", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_image_stack()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_image_stack_filename("some.thing")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--image-stack", "some.thing", "--extract", "image", "--magnification", "10.0","--pixel-size-nm", "160.0",  "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_settings_file()
    {
        val settings = Settings.with(working_directory_path())
        settings.set_settings_file("settings.file")
        val commands = Assessment.custom(exe_path()).get_images_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(),"--settings", "settings.file", "--extract", "image", "--magnification", "10.0", "--pixel-size-nm", "160.0", "--psf-px", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }
}