package com.coxphysics.terrapins.models.hawkman.unit_testing_snippets;

import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;

public class BlurToScale
{
    public static FloatProcessor blur(ImageProcessor image, int scale)
    {
        int kernel_size = get_kernel_size(scale);
        float[] kernel = new float[kernel_size * kernel_size];
        Arrays.fill(kernel, 1F);
        Convolver convolver = new Convolver();
        convolver.setNormalize(true);
        FloatProcessor image_copy = image.convertToFloatProcessor();
        convolver.convolve(image_copy, kernel, kernel_size, kernel_size);
        image_copy.resetMinAndMax();
        return image_copy;
    }

    private static int get_kernel_size(int scale)
    {
        return (int)(2 * Math.ceil(Math.ceil(scale / 2.0))) - 1;
    }
}
