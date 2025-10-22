package com.coxphysics.terrapins.models.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FsUtils
{
    public static boolean prepare_directory(Path directory)
    {
        boolean ok = delete_directory_recursive(directory);
        create_directories(directory);
        return ok;
    }

    public static void create_directories(Path directory)
    {
        if (!Files.exists(directory))
        {
            try
            {
                Files.createDirectories(directory);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean delete_directory_recursive(Path filepath)
    {
        return delete_directory_recursive(filepath.toFile());
    }

    private static boolean delete_directory_recursive(File filepath)
    {
        if (!filepath.exists())
            return true;
        File[] files = filepath.listFiles();
        if (files == null)
            return true;
        return remove_sub_parts(files) && filepath.delete();
    }

    private static boolean remove_sub_parts(File[] files)
    {
        boolean all_ok = true;
        for (File file : files)
        {
            if (file.isDirectory())
            {
                boolean ok = delete_directory_recursive(file);
                if (!ok)
                    all_ok = false;
            }
            else
            {
                boolean ok = file.delete();
                if (!ok)
                    all_ok = false;
            }
        }
        return all_ok;
    }

    public static boolean exists(Path path)
    {
        return Files.exists(path);
    }
}