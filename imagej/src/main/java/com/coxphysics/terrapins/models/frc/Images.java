package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.utils.IJUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.io.ImageWriter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Images
{
    public final static String IMAGE_1_NAME = "image_1";
    public final static String IMAGE_2_NAME = "image_2";
    private final ImagePlus image_1_;
    private final ImagePlus image_2_;

    private Images(ImagePlus image_1, ImagePlus image_2)
    {
        image_1_ = image_1;
        image_2_ = image_2;
    }

    public static Images from(ImagePlus image_1, ImagePlus image_2)
    {
        return new Images(image_1, image_2);
    }

    public static Images invalid()
    {
        return from(null, null);
    }

    public ImagePlus image_1()
    {
        return image_1_;
    }

    public ImagePlus image_2()
    {
        return image_2_;
    }

    public boolean is_valid()
    {
        return image_1_ != null && image_2_ != null;
    }

    public void show()
    {
        image_1().show();
        image_2().show();
    }

    public void write_to_disk(Path output_directory, FRCDialogSettings settings)
    {
        Path image_1_location = Paths.get(output_directory.toString(), IMAGE_1_NAME);
        String image_1_filename = IJUtils.get_tiff_path_from(image_1_location).toString();
        settings.set_image_1(image_1_filename);
        IJ.saveAsTiff(image_1_, image_1_filename);

        Path image_2_location = Paths.get(output_directory.toString(), IMAGE_2_NAME);
        String image_2_filename = IJUtils.get_tiff_path_from(image_2_location).toString();
        settings.set_image_2(image_2_filename);
        IJ.saveAsTiff(image_2_, image_2_filename);
    }

    private static Path get_image_path(Path output_directory, String image_name)
    {
        return Paths.get(output_directory.toString(), image_name, ".tif");
    }
}
