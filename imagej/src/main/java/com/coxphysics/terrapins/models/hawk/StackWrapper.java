package com.coxphysics.terrapins.models.hawk;

import ij.ImageStack;
import ij.process.ImageProcessor;

public class StackWrapper
{
    ImageStack stack_;

    private StackWrapper(ImageStack stack)
    {
        stack_ = stack;
    }

    public static StackWrapper from_stack(ImageStack stack)
    {
        return new StackWrapper(stack);
    }

    // Used by backend in jni land
    public int get_stack_size()
    {
        int n_slices =  stack_.getSize();
        return n_slices;
    }

    // Used by backend in jni land
    public float[] get_float_frame(int frame)
    {
        ImageProcessor float_frame = stack_.getProcessor(frame + 1).convertToFloat();
        return (float[]) float_frame.getPixels();
    }



}
