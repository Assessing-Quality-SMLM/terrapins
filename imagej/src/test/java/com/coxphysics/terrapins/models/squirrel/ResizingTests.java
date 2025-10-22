package com.coxphysics.terrapins.models.squirrel;

import com.coxphysics.terrapins.models.UtilsKt;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ResizingTests {
    @Test
    public void downsample_test()
    {
        float[] data = UtilsKt.arange_floats(25);
        FloatProcessor image = new FloatProcessor(5,5, data);
//        image.setInterpolationMethod(ImageProcessor.BILINEAR);
        float[] result_data = (float[]) image.resize(3, 3).getPixels();
        float[] expected = new float[]{0.0f, 1.0f, 3.0f, 5.0f, 6.0f, 8.0f, 15.0f, 16.0f, 18.0f};
        assertArrayEquals(expected, result_data);
    }
}
