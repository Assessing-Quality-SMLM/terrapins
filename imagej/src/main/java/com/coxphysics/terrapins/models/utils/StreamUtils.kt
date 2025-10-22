package com.coxphysics.terrapins.models.utils

import java.io.BufferedInputStream
import java.io.InputStream

private fun get_lines_from(stream: InputStream) = sequence {
    BufferedInputStream(stream).use {
        var builder = StringBuilder()
        var c = it.read()
        while (c >= 0)
        {
            val character = c.toChar()
            if (character == '\n')
            {
                yield(builder.toString())
                builder = StringBuilder()
            }
            else
            {
                builder.append(character)
            }
            c = it.read();
        }
    }
}


class StreamUtils
{
    companion object
    {
        @JvmStatic
        fun get_lines(stream: InputStream): Sequence<String>
        {
            return get_lines_from(stream)
        }
    }
}