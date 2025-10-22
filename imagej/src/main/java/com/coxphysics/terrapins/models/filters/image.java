package com.coxphysics.terrapins.models.filters;

import com.coxphysics.terrapins.models.UtilsKt;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;

import java.util.Arrays;

import static com.coxphysics.terrapins.models.UtilsKt.immutable_clone;


public class image
{
     public static long non_zero_pixels(ImageProcessor image)
     {
         return UtilsKt.non_zero_pixels(image);
     }

    public static <T extends ImageProcessor> void normalise(T image)
    {
        double normaliser = 1.0 / image.getMax();
        image.multiply(normaliser);
        image.resetMinAndMax();
    }

    public static <T extends ImageProcessor> T immutable_gaussian_blur(T image, double sigma)
    {
        T blurred_image = immutable_clone(image);
        new GaussianBlur().blurGaussian(blurred_image, sigma);
        return blurred_image;
    }

    public static  <T extends ImageProcessor> T immutable_mean_threshold(T image, int kernel_size)
    {
        return immutable_box_filter(image, kernel_size);
    }

    public static  <T extends ImageProcessor> T immutable_box_filter(T image, int kernel_size)
    {
        T blurred_image = immutable_clone(image);
        return box_filter(blurred_image, kernel_size);
    }

    public static  <T extends ImageProcessor> T box_filter(T image, int kernel_size)
    {
        float[] kernel = create_kernel(kernel_size);
        Convolver ConvKernel = new Convolver();
        ConvKernel.setNormalize(true);
        // false here indicates user cancelled operation - not success or failure
        // https://imagej.net/ij/developer/api/ij/ij/plugin/filter/Convolver.html#convolve(ij.process.ImageProcessor,float%5B%5D,int,int)
        boolean result = ConvKernel.convolve(image, kernel, kernel_size, kernel_size);
        return result ? image: null;
    }

    private static float[] create_kernel(int kernel_size)
    {
        float[] kernel  = new float[kernel_size * kernel_size];
        Arrays.fill(kernel, 1f);
        return kernel;
    }
}
