package com.coxphysics.terrapins.models.hawkman.snippets;

import com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.Helpers;
import com.coxphysics.terrapins.models.hawkman.unit_testing_snippets.Morphology;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

public class MorphologyTests
{
    @Test
    public void erode_test()
    {
        ByteProcessor image = Helpers.create_binary_test_image(10, 5, (byte)255);
        image.invert();
        ByteProcessor result = Morphology.erode(image);
        result.invert();
        byte[] expected = new byte[]{0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0};
        assertArrayEquals((byte[])result.getPixels(), expected);
    }

    @Test
    public void dilate_test()
    {
        ByteProcessor image = Helpers.create_binary_test_image(10, 5, (byte)255);
        image.invert();
        ByteProcessor result = Morphology.dilate(image);
        result.invert();
        byte[] expected = new byte[]{0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, 0};
        assertArrayEquals((byte[])result.getPixels(), expected);
    }

        @Test
    public void skeleton_test()
    {
        ByteProcessor image = Helpers.create_binary_test_image(10, 5, (byte)255);
        ByteProcessor result = Morphology.skeletonise(image);
        byte[] expected = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        assertArrayEquals((byte[])result.getPixels(), expected);
    }
}
