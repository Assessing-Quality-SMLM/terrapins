package com.coxphysics.terrapins.models.fs;

import java.nio.file.Path;
import java.util.Arrays;

public class FakeFileSystem implements FileSystem
{
    private boolean value_ = true;
    private Path[] paths_;

    private FakeFileSystem(boolean value, Path[] paths)
    {
        value_ = value;
        paths_ = paths;
    }

    public static FakeFileSystem True()
    {
        return new FakeFileSystem(true, new Path[]{});
    }

    public static FakeFileSystem False()
    {
        return new FakeFileSystem(false, new Path[]{} );
    }

    public static FakeFileSystem with(Path[] paths)
    {
        return new FakeFileSystem(true, paths);
    }

    @Override
    public boolean prepare_directory(Path directory)
    {
        return value_;
    }

    @Override
    public boolean exists(Path path)
    {
        return Arrays.asList(paths_).contains(path);
    }
}
