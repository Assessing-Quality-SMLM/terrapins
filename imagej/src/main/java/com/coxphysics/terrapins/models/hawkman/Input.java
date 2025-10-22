package com.coxphysics.terrapins.models.hawkman;

import ij.process.FloatProcessor;

public class Input
{
    private final FloatProcessor test_image_;

    private final FloatProcessor ref_image_;

    public Input(FloatProcessor test_image, FloatProcessor ref_image) {
        test_image_ = test_image;
        ref_image_ = ref_image;
    }

    public FloatProcessor test_image()
    {
        return test_image_;
    }

    public FloatProcessor ref_image()
    {
        return ref_image_;
    }

    public int width()
    {
        return test_image().getWidth();
    }

    public int height()
    {
        return test_image().getHeight();
    }

}
