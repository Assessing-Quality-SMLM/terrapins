package com.coxphysics.terrapins.models.hawk;

import ij.IJ;
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
        // If the underlying image stack window is closed it deletes all the data - even if you are holding a reference
        // But it retains size info so cannot check by frame number - just have to catch the exception
        // because size info is retained we can use it to return a big block of zero
        try
        {
            ImageProcessor processor = stack_.getProcessor(frame + 1);
            ImageProcessor float_frame = processor.convertToFloat();
            return (float[]) float_frame.getPixels();
        }
        catch (IllegalArgumentException e)
        {
            IJ.log(String.format("Somethings gone wrong: %s. Have you closed your underlying image? If so don't it needs to be open", e));
            return new float[stack_.getHeight() * stack_.getWidth()];
        }
    }



}
