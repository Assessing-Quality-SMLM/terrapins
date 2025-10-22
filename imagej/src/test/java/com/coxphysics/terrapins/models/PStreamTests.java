package com.coxphysics.terrapins.models;

import static org.junit.jupiter.api.Assertions.*;

import com.coxphysics.terrapins.models.hawk.Config;
import com.coxphysics.terrapins.models.hawk.NativeHAWK;
import com.coxphysics.terrapins.models.hawk.PStream;
import ij.ImageStack;
import org.junit.jupiter.api.Test;

public class PStreamTests
{
    @Test
    public void basic_test()
    {
        short nh = NativeHAWK.negative_handling_absolute();
        short os = NativeHAWK.output_style_interleaved();
        Config config = new Config(3, nh, os);

        float[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ImageStack stack = new ImageStack(1, 1, data.length);
        int count = 1;
        for (float value : data)
        {
            float [] pixel_data = {value};
            stack.setPixels(pixel_data, count);
            count++;
        }

        try(PStream stream = PStream.from(stack, config, 15, 1))
        {
            test_value(stream, 1,1);
            test_value(stream, 2,4);
            test_value(stream, 3,16);
            test_value(stream, 6, 16);
            test_value(stream, 16, 1);
        }
        catch (Exception e)
        {
            fail();
        }
    }

    @Test
    public void basic_test_sequential()
    {
        short nh = NativeHAWK.negative_handling_absolute();
        short os = NativeHAWK.output_style_sequential();
        Config config = new Config(3, nh, os);

        float[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        ImageStack stack = new ImageStack(1, 1, data.length);
        int count = 1;
        for (float value : data)
        {
            float [] pixel_data = {value};
            stack.setPixels(pixel_data, count);
            count++;
        }

        try(PStream stream = PStream.from(stack, config, 15, 1))
        {
            test_value(stream, 1,1);
            test_value(stream, 8,1);
            test_value(stream, 9,4);
            test_value(stream, 14,4);
            test_value(stream, 15, 16);
            test_value(stream, 16, 16);
        }
        catch (Exception e)
        {
            fail();
        }
    }

    private void test_value(PStream stream, int frame, int value)
    {
        float[] output_data = (float[]) stream.getPixels(frame);
        float[] expected = {value};
        assertArrayEquals(expected, output_data);
    }
}
