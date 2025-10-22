package com.coxphysics.terrapins.models.renderer

import java.io.File

const val PIXEL_SIZE_NM_ID = "pixel size (nm)"

fun line_to_value(line: String): Double?
{
    val splits = line.split("=")
    if (splits.size < 2)
        return null;
    val value = splits[1]
    return value.toDoubleOrNull()
}

fun from_lines(lines: Sequence<String>): Double?
{
    val line = lines.firstOrNull { s -> s.startsWith(PIXEL_SIZE_NM_ID) }
    return when (line)
    {
        null -> null
        else -> line_to_value(line)
    }
}

private fun from_filename(filename: String): Double?
{
    return File(filename).bufferedReader().useLines { l -> from_lines(l) }
}

class RendererOutput private constructor(private val pixel_size_nm: Double)
{
    fun pixel_size_nm(): Double
    {
        return pixel_size_nm
    }
    companion object
    {
        @JvmStatic
        fun from_file(filename: String): RendererOutput?
        {
            return when (val pixel_size_nm = from_filename(filename))
            {
                null -> null
                else -> RendererOutput(pixel_size_nm)
            }
        }
    }
}