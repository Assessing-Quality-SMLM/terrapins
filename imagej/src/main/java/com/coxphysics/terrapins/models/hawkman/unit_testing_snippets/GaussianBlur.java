package com.coxphysics.terrapins.models.hawkman.unit_testing_snippets;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class GaussianBlur
{
    public static FloatProcessor blur(ImageProcessor image, double sigma)
    {
        ij.plugin.filter.GaussianBlur gb = new ij.plugin.filter.GaussianBlur();
        FloatProcessor new_image = image.convertToFloatProcessor();
        gb.blurGaussian(new_image, sigma);
        new_image.resetMinAndMax();
        return new_image;
    }

    public static FloatProcessor blur_and_normalise(ImageProcessor image, double sigma)
    {
        FloatProcessor new_image = blur(image, sigma);
        new_image.resetMinAndMax();
        normalise(new_image);
        return new_image;
    }

    private static void normalise(FloatProcessor new_image)
    {
        double normInt = new_image.getMax();
        normInt= 1.0 / normInt;
        new_image.multiply(normInt);
    }
}
