package com.coxphysics.terrapins.models.utils;

import ij.IJ;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FsUtils
{
    private static final String JAVA_TEMP_DIRECTORY = "java.io.tmpdir";

    public static Path temp_directory()
    {
        String temp_location = System.getProperty(JAVA_TEMP_DIRECTORY);
        return Paths.get(temp_location);
    }

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

    public static String read_to_string_utf8(Path filepath)
    {
        return read_to_string(filepath, StandardCharsets.UTF_8);
    }
    public static String read_to_string(Path filepath, Charset encoding)
    {
        if (!exists(filepath))
            return null;
        try
        {
            byte[] encoded = Files.readAllBytes(filepath);
            return new String(encoded, encoding);
        }
        catch (IOException e)
        {
            String message = String.format("Could not read %s to string: %s", filepath, e);
            IJ.log(message);
            return null;
        }
    }

    public static void copy_folder(Path source, Path target, CopyOption... options) throws IOException
    {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.copy(file, target.resolve(source.relativize(file).toString()), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}