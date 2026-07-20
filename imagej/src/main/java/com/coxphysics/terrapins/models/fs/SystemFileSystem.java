package com.coxphysics.terrapins.models.fs;

import com.coxphysics.terrapins.models.utils.FsUtils;

import java.nio.file.Path;

public class SystemFileSystem implements FileSystem
{

    @Override
    public boolean exists(Path value)
    {
        return FsUtils.exists(value);
    }
}
