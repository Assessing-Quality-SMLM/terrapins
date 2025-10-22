package com.coxphysics.terrapins.models.renderer

import com.coxphysics.terrapins.models.renderer.from_lines
import com.coxphysics.terrapins.models.renderer.line_to_value
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RendererOutputTests
{
    @Test
    fun from_lines_test()
    {
        val lines = listOf("somethig", "pixel_size", "pixel size (nm)= 123.456")
        val data = from_lines(lines.asSequence());
        assertEquals(data, 123.456)
    }

    @Test
    fun basic_line_parse()
    {
        val data = line_to_value("something=123.456");
        assertEquals(data, 123.456)
    }

    @Test
    fun leading_ws_parse()
    {
        val data = line_to_value("something= 123.456");
        assertEquals(data, 123.456)
    }

    @Test
    fun trailing_ws_parse()
    {
        val data = line_to_value("something=123.456 ");
        assertEquals(data, 123.456)
    }

    @Test
    fun bad_parse()
    {
        val data = line_to_value("something123.456");
        assertEquals(data, null)
    }
}