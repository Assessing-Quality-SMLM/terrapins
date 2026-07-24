package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.PathWrapper
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.assertEquals

class PathSelectorTests
{

    @Test
    fun use_current_path_as_starting_location_if_its_set()
    {
        val current_path = PathWrapper.from(Paths.get("current", "path"))
        val last_path = PathWrapper.from(Paths.get("last", "path"))
        val is_both = false
        val is_files = false
        val selector = PathSelector.from(current_path, last_path, is_both, is_files)
        val starting_location = selector.starting_location()
        assertEquals(starting_location!!, Paths.get("current", "path"))
    }

    @Test
    fun if_current_path_empty_starting_location_is_last_path()
    {
        val current_path = PathWrapper.empty()
        val last_path = PathWrapper.from(Paths.get("some", "thing"))
        val is_both = false
        val is_files = false
        val selector = PathSelector.from(current_path, last_path, is_both, is_files)
        val starting_location = selector.starting_location()
        assertEquals(starting_location!!, Paths.get("some", "thing"))
    }

    @Test
    fun if_no_path_set_get_null()
    {
        val current_path = PathWrapper.empty()
        val last_path = PathWrapper.empty()
        val is_both = false
        val is_files = false
        val selector = PathSelector.from(current_path, last_path, is_both, is_files)
        val starting_location = selector.starting_location()
        assertEquals(starting_location, null)
    }
}