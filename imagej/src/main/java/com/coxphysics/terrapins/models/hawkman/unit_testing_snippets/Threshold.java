package com.coxphysics.terrapins.models.hawkman.unit_testing_snippets;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

public class Threshold
{

    public static ByteProcessor threshold(FloatProcessor half_psf, FloatProcessor gauss_blur, FloatProcessor scale_blur, double thresh, double smooth, double offset)
    {
        int SRpixelsX = gauss_blur.getWidth();
        int SRpixelsY = gauss_blur.getHeight();
        ByteProcessor result = new ByteProcessor(SRpixelsX, SRpixelsY);
        for (int row = 0; row < (SRpixelsY-1); row++)
        {
            for (int col = 0; col < (SRpixelsX-1); col++)
            {
                double normInt = (thresh * scale_blur.getf(col, row)) + (smooth * half_psf.getf(col, row)) + offset;
                if (gauss_blur.getf(col, row) > normInt)
                {
                    result.set(col, row, 255);
                }
                else
                {
                    result.set(col, row, 0);
                }
            }
        }
        return result;
    }
}
