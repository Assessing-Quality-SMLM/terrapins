package com.coxphysics.terrapins.models.hawkman.snippets;

import com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.Threshold;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import org.junit.jupiter.api.Test;

import static com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.Helpers.create_test_image;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThresholdTests
{
    private static final byte IMAGE_J_1 = -1;
    @Test
    public void byte_conversion_test()
    {
//        assertEquals(255, (byte)255);
        assertEquals(IMAGE_J_1, (byte)255);
    }

    @Test
    public void basic_test()
    {
        FloatProcessor half_psf = create_test_image(10).convertToFloatProcessor();
        FloatProcessor scale_blur = create_test_image(10).convertToFloatProcessor();
        FloatProcessor image = create_test_image(10).convertToFloatProcessor();
        ByteProcessor result = Threshold.threshold(half_psf, scale_blur, image, 1, 1, -1);
        byte[] expected = new byte[]{IMAGE_J_1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        assertArrayEquals((byte[])result.getPixels(), expected);
    }
}
