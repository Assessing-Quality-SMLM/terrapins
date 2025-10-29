package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.localisations.LocalisationFile
import com.coxphysics.terrapins.models.localisations.ParseMethod
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path

class AssessmentTests
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
        val settings = AssessmentSettings.with(working_directory_path())
        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--extract", "localisation", "--magnification", "10.0", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_localisation_file()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_localisation_file(LocalisationFile.new("localisations.file", ParseMethod.default_()))
        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--extract", "localisation", "--magnification", "10.0", "--locs", "localisations.file", "--locs-format", "ts", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_hawk_localisation_file()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_hawk_localisation_file(LocalisationFile.new("hawk.file", ParseMethod.default_()))

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--extract", "localisation", "--magnification", "10.0", "--locs-hawk", "hawk.file", "--locs-hawk-format", "ts", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_widefield()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_widefield_filename("some.thing")

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--widefield", "some.thing", "--extract", "localisation", "--magnification", "10.0", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_image_stack()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_image_stack_filename("some.thing")

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--image-stack", "some.thing", "--extract", "localisation", "--magnification", "10.0","--camera-pixel-size-nm", "160.0",  "--instrument-psf-fwhm-nm", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_settings_file()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_settings_file("settings.file")

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--settings", "settings.file", "--extract", "localisation", "--magnification", "10.0", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }
}