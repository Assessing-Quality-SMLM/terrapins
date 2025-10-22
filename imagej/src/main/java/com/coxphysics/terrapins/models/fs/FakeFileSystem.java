package com.coxphysics.terrapins.models.fs;

import java.nio.file.Path;

public class FakeFileSystem implements FileSystem
{
    private boolean value_ = true;

    private FakeFileSystem(boolean value)
    {
        value_ = value;
    }
    public static FakeFileSystem True()
    {
        return new FakeFileSystem(true);
    }

    public static FakeFileSystem False()
    {
        return new FakeFileSystem(false);
    }

    @Override
    public boolean prepare_directory(Path directory)
    {
        return value_;
    }
}
