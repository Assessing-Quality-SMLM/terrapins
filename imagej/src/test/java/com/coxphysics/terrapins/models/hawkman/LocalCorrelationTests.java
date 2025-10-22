package com.coxphysics.terrapins.models.hawkman;

import ij.process.FloatProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalCorrelationTests
{
    @Test
    public void basic_patch_test()
    {
        float[] data_1 = new float[]{0 ,1, 2, 3, 4, 5};
        float[] data_2 = new float[]{0 ,1, 1, 3, 1, 5};
        FloatProcessor image_1 = new FloatProcessor(2, 3, data_1);
        FloatProcessor image_2 = new FloatProcessor(2, 3, data_2);
        double value = CoxGroupLocalCorr.CorrelatePatch(image_1, image_2);
        assertEquals(value, 0.7865561246871948);
    }

    @Test
    public void basic_map_test()
    {
        float[] data_1 = new float[]{0 ,1, 2, 3, 4, 5};
        float[] data_2 = new float[]{0 ,1, 1, 3, 1, 5};
        FloatProcessor image_1 = new FloatProcessor(2, 3, data_1);
        FloatProcessor image_2 = new FloatProcessor(2, 3, data_2);
        FloatProcessor map = CoxGroupLocalCorr.CorrelationMap(image_1, image_2, 1);
        float[] expected = new float[]{0.95577896f, 0.95577896f, 0.8526688f, 0.8526687f, 0.8631767f, 0.86317664f};
        assertArrayEquals((float[])map.getPixels(), expected);
    }
}