package com.coxphysics.terrapins.models.hawkman.unit_testing_snippets;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Morphology
{
    public static ByteProcessor erode(ByteProcessor image)
    {
        ByteProcessor new_image = image.convertToByteProcessor();
        new_image.erode();
        return new_image;
    }

    public static ByteProcessor dilate(ByteProcessor image)
    {
        ByteProcessor new_image = image.convertToByteProcessor();
        new_image.dilate();
        return new_image;
    }

    public static ByteProcessor skeletonise(ByteProcessor image)
    {
        ByteProcessor new_image = image.convertToByteProcessor();
        new_image.invert();
        new_image.skeletonize();
        new_image.invert();
        return new_image;
    }
}
