package com.coxphysics.terrapins.models.squirrel;

import com.coxphysics.terrapins.models.squirrel.utils.OptimisationHelper;
import ij.process.FloatProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SquirrelTests
{
    @Test
    public void basic_alpha_beta_test()
    {
        double[] wf_data = new double[]{0 ,1, 2, 3, 4, 5, 6, 7, 8};
        double[] sr_data = new double[]{0 ,1, 2, 3, 4, 5, 6, 7, 8};
        double[] ones_data = new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1};
        double[] parameters = OptimisationHelper.calculateAlphaBeta(sr_data, wf_data, ones_data);
        assertEquals(parameters[0], 1.0);
        assertEquals(parameters[1], 0.0);
    }

    @Test
    public void downscale()
    {
        double[] sr_data = create_image(5);
        FloatProcessor sr = new FloatProcessor(5, 5, sr_data);
        FloatProcessor result = (FloatProcessor)sr.resize(3, 3);
        float[] result_data = (float[])result.getPixels();
        float[] expected = new float[]{0.0f, 1.0f, 3.0f, 5.0f, 6.0f, 8.0f, 15.0f, 16.0f, 18.0f};
        assertArrayEquals(result_data, expected);
    }

    @Test
    public void blur_and_downscale()
    {
        double[] wf_data = create_image(3);
        double[] sr_data = create_image(5);
        FloatProcessor wf = new FloatProcessor(3, 3, wf_data);
        FloatProcessor sr = new FloatProcessor(5,5, sr_data);
        FloatProcessor result = new OptimisationHelper.SigmaOptimiseFunction(sr, wf).blurAndDownscale(sr, 5);
        float[] result_data = (float[])result.getPixels();
        float[] expected = new float[]{8.350962f, 8.649266f, 9.269003f, 9.8424835f, 10.140789f, 10.760526f, 12.94117f, 13.239473f, 13.85921f};
//        float[] expected = create_image_float(3);
        assertArrayEquals(result_data, expected);
    }

    double [] create_image(int size)
    {
        double[] data = new double[size * size];
        for (int idx = 0; idx < (size * size); idx++)
        {
            data[idx] = (double)idx;
        }
        return data;
    }

    float [] create_image_float(int size)
    {
        float[] data = new float[size * size];
        for (int idx = 0; idx < (size * size); idx++)
        {
            data[idx] = (float)idx;
        }
        return data;
    }
}
