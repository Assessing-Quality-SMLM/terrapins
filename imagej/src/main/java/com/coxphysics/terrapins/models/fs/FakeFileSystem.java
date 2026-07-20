package com.coxphysics.terrapins.models.fs;

import java.nio.file.Path;
import java.util.Arrays;

public class FakeFileSystem implements FileSystem
{
    private final Path[] paths_;

    private FakeFileSystem(Path[] paths)
    {
        paths_ = paths;
    }


    public static FakeFileSystem with(Path[] paths)
    {
        return new FakeFileSystem( paths);
    }

    @Override
    public boolean exists(Path path)
    {
        return Arrays.asList(paths_).contains(path);
    }
}
