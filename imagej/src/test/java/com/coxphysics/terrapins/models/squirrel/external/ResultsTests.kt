package com.coxphysics.terrapins.models.squirrel.external

import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals

class ResultsTests
{
    private fun data_path(): Path
    {
        return File("some/data").toPath()
    }

    @Test
    fun widefield_path()
    {
        assertEquals(get_results().widefield_path(false).toString(), File("some/widefield.tiff").toPath().toString())
    }

    @Test
    fun widefield_path_adjusted_for_non_linear_data()
    {
        assertEquals(get_results().widefield_path(true).toString(), File("some/aof_widefield.tiff").toPath().toString())
    }

    @Test
    fun error_map_path()
    {
        assertEquals(get_results().error_map_path().toString(), File("some/data/error_map.tiff").toPath().toString())
    }

    @Test
    fun optimiser_data_path()
    {
        assertEquals(get_results().optimiser_data_path().toString(), File("some/data/optimiser_data").toPath().toString())
    }

    @Test
    fun big_widefield()
    {
        assertEquals(get_results().big_widefield_path().toString(), File("some/data/big_widefield.tiff").toPath().toString())
    }

    @Test
    fun squirrel_results_image_path()
    {
        assertEquals(get_results().sr_transform_path().toString(), File("some/data/sr_affine_blurred.tiff").toPath().toString())
    }

    private fun get_results() = Results.from(data_path())
}