package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.localisations.LocalisationFile
import com.coxphysics.terrapins.models.localisations.ParseMethod
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.io.path.Path
import kotlin.test.assertEquals

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
        settings.set_n_threads(4)
        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings.core_settings(), settings, null)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--n-threads", "4", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "10.0", "localisation")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_localisation_file()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_n_threads(4)
        settings.set_localisation_file(LocalisationFile.new("localisations.file", ParseMethod.default_()))
        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings.core_settings(), settings, null)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--n-threads", "4", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "10.0", "localisation", "--locs", "localisations.file", "--locs-format", "ts")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_hawk_localisation_file()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_n_threads(4)
        settings.set_hawk_localisation_file(LocalisationFile.new("hawk.file", ParseMethod.default_()))

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings.core_settings(), settings, null)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--n-threads", "4", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "10.0", "localisation", "--locs-hawk", "hawk.file", "--locs-hawk-format", "ts")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_widefield()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_n_threads(4)
        settings.set_widefield(DiskOrImage.from_filename("some.thing"))

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings.core_settings(), settings, null)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--widefield", "some.thing", "--n-threads", "4", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "10.0", "localisation")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_image_stack()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_n_threads(4)
        settings.set_image_stack(DiskOrImage.from_filename("some.thing"))

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings.core_settings(), settings, null)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--image-stack", "some.thing", "--n-threads", "4", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "10.0", "localisation")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_use_settings_file()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_n_threads(4)
        settings.set_settings_file("settings.file")

        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings.core_settings(), settings, null)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--n-threads", "4", "--settings", "settings.file", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "10.0", "localisation")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun can_set_magnification()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_n_threads(4)
        settings.set_magnification(123.0)

        val core_settings = CoreSettings.new(working_directory_path())
        core_settings.set_n_threads(4)
        val commands = Assessment.custom(exe_path()).get_localisations_arguments(core_settings, settings, null)
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--n-threads", "4", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "123.0", "localisation")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

    @Test
    fun date_time_as_file_path()
    {
        val assessment = Assessment.custom(working_directory_path())
        val date = LocalDate.of(2025, 11, 19)
        val time = LocalTime.of(12, 5, 12)
        val date_time = LocalDateTime.of(date, time)
        assertEquals(assessment.date_time_to_file_path(date_time), "2025_11_19_12_05_12")
    }

    @Test
    fun can_set_data_name()
    {
        val settings = AssessmentSettings.with(working_directory_path())
        settings.set_n_threads(4)
        val commands = Assessment.custom(exe_path()).get_localisations_arguments(settings.core_settings(), settings, "some_thing")
        val expected = listOf(exe_path().toString(), "--working-directory", working_directory(), "--data-name", "some_thing", "--n-threads", "4", "--extract", "--camera-pixel-size-nm", "160.0", "--instrument-psf-fwhm-nm", "270.0", "--magnification", "10.0", "localisation")
        assertArrayEquals(commands.toTypedArray(), expected.toTypedArray())
    }

}