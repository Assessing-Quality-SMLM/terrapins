package com.coxphysics.terrapins.models.hawkman.unit_testing_snippets;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

public class ConfidenceMap
{
    public static ByteProcessor[] generate(FloatProcessor ref_gb, FloatProcessor test_gb, ByteProcessor skeleton, ByteProcessor confidence_map)
    {
        double normInt = 0.0;
        double tempDblA = 0.0;
        double tempDblB = 0.0;
        int SRpixelsX = skeleton.getHeight();
        int SRpixelsY = skeleton.getWidth();
        ByteProcessor test_skeleton = new ByteProcessor(SRpixelsY, SRpixelsX);
        ByteProcessor ref_ekeleton = new ByteProcessor(SRpixelsY, SRpixelsX);
        for (int icount = 0; icount < SRpixelsX; icount++)
        {
            for (int jcount = 0; jcount < SRpixelsY; jcount++)
            {
                tempDblA = (0.5D / 0.85D) * skeleton.getf(icount, jcount);
                if (tempDblA > 0.5D)
                    tempDblA = 0.5D;
                tempDblB = (0.5D / 0.85D) * confidence_map.getf(icount, jcount);
                if (tempDblB > 0.5D)
                    tempDblB = 0.5D;
                normInt = 127.0D * test_gb.getf(icount, jcount) * (1.0D - tempDblA - tempDblB);
                normInt = normInt + (127.0D * ref_gb.getf(icount, jcount) * (1.0D - tempDblA - tempDblB));
                if (normInt > 255.0D)
                    normInt = 255.0D;
                if (normInt < 0.0D)
                    normInt = 0.0D;
                test_skeleton.setf(icount, jcount, (int) normInt);
                 normInt = 127.0D * test_gb.getf(icount,jcount) * (0.0D + tempDblA + tempDblB);
                 normInt = normInt + (127.0D * ref_gb.getf(icount,jcount) *  (0.0D + tempDblA + tempDblB));
                 if (normInt > 255.0D)
                     normInt = 255.0D;
                 if (normInt < 0.0D)
                     normInt = 0.0D;
                 ref_ekeleton.setf(icount, jcount, (int)normInt);
            }
        }
        return new ByteProcessor[]{test_skeleton, ref_ekeleton};
    }
}
