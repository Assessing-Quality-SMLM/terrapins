package com.coxphysics.terrapins.models.utils;

import ij.IJ;
import ij.ImagePlus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IJUtils
{
    public static  final String IJ_TIF_EXTENSION = ".tif";

    public static Path get_tiff_path(Path output_directory, String image_name)
    {
        Path path = Paths.get(output_directory.toString(), image_name);
        return get_tiff_path_from(path);
    }

    public static Path get_tiff_path_from(Path path)
    {
        return path.resolveSibling(path.getFileName() + IJ_TIF_EXTENSION);
    }

    public static ImagePlus load_image(Path image_path)
    {
        if (FsUtils.exists(image_path))
        {
            return IJ.openImage(image_path.toString());
        }
        return null;
    }

    public static Path write_to_disk(ImagePlus image, Path image_path)
    {
        Path parent = image_path.getParent();
        if (!Files.exists(parent))
        {
            try
            {
                Files.createDirectories(parent);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        IJ.saveAsTiff(image, image_path.toString());
        return image_path;
    }
}
