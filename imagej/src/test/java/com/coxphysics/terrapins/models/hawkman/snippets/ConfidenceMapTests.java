package com.coxphysics.terrapins.models.hawkman.snippets;

import com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.ConfidenceMap;
import com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.Helpers;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfidenceMapTests
{
    @Test
    public void basic_test()
    {
        FloatProcessor image_a = Helpers.create_test_image(10).convertToFloatProcessor();
        FloatProcessor image_b = Helpers.create_test_image(10).convertToFloatProcessor();
        ByteProcessor skeleton = Helpers.create_binary_test_image(10, 5, (byte)255);
        ByteProcessor confidence_map = Helpers.create_binary_test_image(10, 5, (byte)255);
        ByteProcessor[] images = ConfidenceMap.generate(image_a, image_b, skeleton, confidence_map);
        byte[] expected_a = new byte[]{0, -2, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1};
        byte[] expected_b = new byte[]{0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0};
        assertArrayEquals((byte[])images[0].getPixels(), expected_a);
        assertArrayEquals((byte[])images[1].getPixels(), expected_b);

    }

    @Test
    public void conversion_test()
    {
        byte a = -1;
        int i = 255;
        int value = a & i;
        assertEquals(value, 255);
        int value_b = value ^ value;
        int value_c = value ^ 0;
        assertEquals(value_b, 0);
        assertEquals(value_c, 255);
    }
}
