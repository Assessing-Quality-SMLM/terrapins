package com.coxphysics.terrapins.models.hawkman.unit_testing_snippets;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

import java.util.Arrays;

public class Helpers
{
    public static ImageProcessor create_test_image(int size)
    {
        float[] pixels = new float[size * size];
        for (int idx = 0; idx < (size * size); idx++)
        {
            pixels[idx] = (float)(idx);
        }
        return new FloatProcessor(size, size, pixels);
    }

    public static ByteProcessor create_binary_test_image(int size, int width, byte true_value)
    {
        byte[] pixels = new byte[size * size];
        Arrays.fill(pixels, (byte)0);
        ByteProcessor image = new ByteProcessor(size, size, pixels);
        int half = width / 2;
        int centre_col = size / 2;
        int start = centre_col - half;
        int end = start + width;
        for (int row = 0; row < image.getHeight(); row++)
        {
            for(int col = start; col < end; col++)
            {
                image.set(col, row, true_value);
            }
        }
        return image;
    }
}
