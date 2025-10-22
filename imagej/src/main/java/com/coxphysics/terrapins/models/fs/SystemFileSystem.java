package com.coxphysics.terrapins.models.fs;

import com.coxphysics.terrapins.models.utils.FsUtils;

import java.nio.file.Path;

public class SystemFileSystem implements FileSystem
{
    @Override
    public boolean prepare_directory(Path directory)
    {
        return FsUtils.prepare_directory(directory);
    }
}
