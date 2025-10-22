package com.coxphysics.terrapins.models.fs;

import java.nio.file.Path;

public interface FileSystem
{
    boolean prepare_directory(Path directory);
}
