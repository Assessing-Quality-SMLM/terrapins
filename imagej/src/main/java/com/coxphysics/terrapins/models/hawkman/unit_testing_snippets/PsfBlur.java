package com.coxphysics.terrapins.models.hawkman.unit_testing_snippets;

import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;

public class PsfBlur
{
    public static FloatProcessor blur_to(ImageProcessor image, double psf)
    {
        int PSFhalf = get_half_psf(psf);
        float[] HalfKernal = new float[PSFhalf*PSFhalf];
        Arrays.fill(HalfKernal, 1F);
        FloatProcessor image_float_processor = image.convertToFloatProcessor();
        double normInt = image_float_processor.getMax();
        normInt = (double)1.0/normInt;
        image_float_processor.multiply(normInt);
        image_float_processor.resetMinAndMax();
        Convolver ConvKernal = new Convolver();
        boolean ThreshBool = ConvKernal.convolve(image_float_processor, HalfKernal, PSFhalf, PSFhalf);
        image_float_processor.resetMinAndMax();
        return image_float_processor;
    }

    public static int get_half_psf(double psf)
    {
        return (int)(2*Math.ceil(Math.ceil(psf /4.0)))-1;
    }
}
