package com.coxphysics.terrapins.models.utils

import com.coxphysics.terrapins.models.PathWrapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PathUtilsTests
{
    @Test
    fun can_swap_path_seperator_for_forward_slash()
    {
        val p = PathWrapper.from_string("some\\path\\pointing\\here")
        val result = PathUtils.path_string_with_forward_slash(p.path()!!)
        assertEquals(result, "some/path/pointing/here")
    }

    @Test
    fun no_slash_input_ok()
    {
        val p = PathWrapper.from_string("somepathpointinghere")
        val result = PathUtils.path_string_with_forward_slash(p.path()!!)
        assertEquals(result, "somepathpointinghere")
    }

    @Test
    fun all_forward_slash()
    {
        val p = PathWrapper.from_string("some/path/pointing/here")
        val result = PathUtils.path_string_with_forward_slash(p.path()!!)
        assertEquals(result, "some/path/pointing/here")
    }
}