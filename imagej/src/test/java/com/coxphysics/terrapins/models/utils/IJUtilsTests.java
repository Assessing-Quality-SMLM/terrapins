package com.coxphysics.terrapins.models.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.coxphysics.terrapins.models.ffi.is_windows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IJUtilsTests
{
    @Test
    public void tiff_path_test()
    {
        Path dir = Paths.get("here");
        if (is_windows())
        {
            assertEquals(IJUtils.get_tiff_path(dir, "something").toString(), "here\\something.tif");
        }
        else
        {
            assertEquals(IJUtils.get_tiff_path(dir, "something").toString(), "here/something.tif");
        }
    }
}
