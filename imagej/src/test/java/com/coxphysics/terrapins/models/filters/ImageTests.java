package com.coxphysics.terrapins.models.filters;

import com.coxphysics.terrapins.models.filters.image;
import ij.process.FloatProcessor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ImageTests
{
    @Test
    public void non_zero_pixels_test()
    {
        float[] data = new float[]{0.000001f, 0f, 0f, 1f, 2f, 3f, 4f, 0f, 5f};
        FloatProcessor processor = new FloatProcessor(3, 3, data);
        assertEquals(image.non_zero_pixels(processor), 6);
    }

    @Test
    public void normalisation_test()
    {
        float[] data = arange(4);
        FloatProcessor processor = new FloatProcessor(2, 2, data);
        image.normalise(processor);
        float [] new_data = (float[]) processor.getPixels();
        assertEquals(new_data[0], 0);
        assertEquals(new_data[1], (float) 1 / 3);
        assertEquals(new_data[2], (float) 2 / 3);
        assertEquals(new_data[3], 1);
    }

    @Test
    public void gaussian_filter_test()
    {
        float[] data = arange(25);
        FloatProcessor processor = new FloatProcessor(5, 5, data);
        int sigma = 1;
        FloatProcessor new_image = image.immutable_gaussian_blur(processor, sigma);
        float [] filter_data = (float[]) new_image.getPixels();
        float [] expected_data = new float[]
{
2.1827369f, 2.8820727f, 3.8189473f, 4.7558227f, 5.455158f,
5.6794167f, 6.3787527f, 7.315627f, 8.252502f, 8.9518385f,
10.3637905f, 11.063126f, 12.000001f, 12.936875f, 13.63621f,
15.048163f, 15.747499f, 16.684374f, 17.621248f, 18.320585f,
18.544844f, 19.24418f, 20.181053f, 21.117928f, 21.817265f
};
        assertArrayEquals(data, arange(25));
        assertArrayEquals(filter_data, expected_data);
    }

    @Test
    public void box_filter_test()
    {
        float[] data = arange(25);
        FloatProcessor processor = new FloatProcessor(5, 5, data);
        int kernel_size = 3;
        FloatProcessor new_image = (FloatProcessor) image.immutable_box_filter(processor, kernel_size);
        float [] filter_data = (float[]) new_image.getPixels();
        float [] expected_data = new float[]
{
2.0f, 2.6666667f, 3.6666667f, 4.6666665f, 5.3333335f,
5.3333335f, 6.0f, 7.0f, 8.0f, 8.666667f,
10.333333f, 11.0f, 12.0f, 13.0f, 13.666667f,
15.333333f, 16.0f, 17.0f, 18.0f, 18.666666f,
18.666666f, 19.333334f, 20.333334f, 21.333334f, 22.0f
};
        assertArrayEquals(data, arange(25));
        assertArrayEquals(filter_data, expected_data);
    }

        @Test
    public void mean_threshold_test()
    {
        float[] data = arange(25);
        FloatProcessor processor = new FloatProcessor(5, 5, data);
        int kernel_size = 3;
        FloatProcessor new_image = image.immutable_mean_threshold(processor, kernel_size);
        float [] filter_data = (float[]) new_image.getPixels();
        float [] expected_data = new float[]
{
2.0f, 2.6666667f, 3.6666667f, 4.6666665f, 5.3333335f,
5.3333335f, 6.0f, 7.0f, 8.0f, 8.666667f,
10.333333f, 11.0f, 12.0f, 13.0f, 13.666667f,
15.333333f, 16.0f, 17.0f, 18.0f, 18.666666f,
18.666666f, 19.333334f, 20.333334f, 21.333334f, 22.0f
};
        assertArrayEquals(data, arange(25));
        assertArrayEquals(filter_data, expected_data);
    }

    float [] arange(int size)
    {
        float [] data = new float[size];
        for(int idx = 0; idx < size; idx++)
        {
            data[idx] = idx;
        }
        return data;
    }
}
