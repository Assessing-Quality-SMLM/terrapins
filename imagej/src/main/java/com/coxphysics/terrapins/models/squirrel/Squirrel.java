package com.coxphysics.terrapins.models.squirrel;

import com.coxphysics.terrapins.models.squirrel.tools.SQUIRRELMathTools_;
import com.coxphysics.terrapins.models.squirrel.utils.OptimisationHelper;
import com.coxphysics.terrapins.models.squirrel.utils.StackHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Squirrel
{
    public static void run(SquirrelSettings settings)
    {
        ImagePlus impRef = WindowManager.getImage(settings.reference_image());
        ImagePlus impSR = WindowManager.getImage(settings.super_res_image());

        ImageStack imsRef = impRef.getImageStack().convertToFloat();
        ImageStack imsSR = impSR.getImageStack().convertToFloat();

        int w_SR = imsSR.getWidth();
        int h_SR = imsSR.getHeight();


        int magnification = w_SR / imsRef.getWidth();
        int magnification2 = magnification * magnification;

        int nSlicesSR = impSR.getStackSize();
        int nSlicesRef = impRef.getStackSize();

        boolean borderCrop = false;
        boolean registrationCrop = false;

        long startTime = System.currentTimeMillis();

        // Check and purge homogeneous/empty slices
        if(settings.purge_empty_frames())
        {
            imsSR = StackHelper.purgeEmptyFrames(imsSR);
            nSlicesSR = imsSR.getSize();
        }

        // Crop black borders from images
        if(settings.crop_borders())
        {
            ImageStack[] cropped = StackHelper.checkAndCropBorders(imsRef, imsSR, magnification);

            if(cropped.length==2)
            {
                borderCrop = true;

                imsRef = cropped[0];
                imsSR = cropped[1];

                // update width and height
                w_SR = imsSR.getWidth();
                h_SR = imsSR.getHeight();
            }
        }

        // Run cross-correlation realignment and crop realignment borders
        if(settings.register())
        {
            IJ.showStatus("Registering and cropping images...");
            ImageStack[] realigned = StackHelper.realignImages(imsRef, imsSR, settings.misalignment(), magnification);

            if(realigned.length==2)
            {
                imsRef = realigned[0];
                imsSR = realigned[1];

                registrationCrop = true;
            }
        }

        // update width and height
        w_SR = imsSR.getWidth();
        h_SR = imsSR.getHeight();
        int nPixelsSR = w_SR*h_SR;

        // Set up reference float processors
        FloatProcessor fpRef = imsRef.getProcessor(1).convertToFloatProcessor();
        fpRef.resetRoi();

        int w_Ref = fpRef.getWidth();
        int h_Ref = fpRef.getHeight();

        // Set up pixel arrays for optimization
        float[] pixelsRef = (float[]) fpRef.getPixels();
        float[] ones = new float[nPixelsSR];
        for(int i=0; i<nPixelsSR; i++){ones[i] = 1;}

        // Set up pixel array for error map generation
        FloatProcessor fpRefScaledToSR = (FloatProcessor) fpRef.duplicate();
        fpRefScaledToSR.setInterpolationMethod(ImageProcessor.BICUBIC);
        fpRefScaledToSR = (FloatProcessor) fpRefScaledToSR.resize(w_SR, h_SR);
        float[] pixelsRefScaledToSR = (float[]) fpRefScaledToSR.getPixels();

        ImageStack imsSRConvolved = new ImageStack(w_SR, h_SR, nSlicesSR);
        ImageStack imsEMap = new ImageStack(w_SR, h_SR, nSlicesSR);
        ImageStack imsSRNormalised = new ImageStack(w_SR, h_SR, nSlicesSR);
        ResultsTable rt = new ResultsTable();

        // Convert smart sigma (nm) into pixels
        double pixelSizeNm = StackHelper.getPixelSizeUm(impRef) * 1000;
        double maxSigma = (settings.sigma_nm() / pixelSizeNm) * 5;
        if(maxSigma ==0)
        {
            maxSigma = magnification * 5;
        }

        if(maxSigma ==0)
        {
            maxSigma = magnification * 5;
        }

        long loopStart = System.nanoTime();

        // time for the main event

        OptimisationHelper optimisationHelper = new OptimisationHelper();

        for(int n=1; n<=nSlicesSR; n++)
        {

            IJ.log("-----------------------------------");
            IJ.log("Processing super-resolution frame " + n);
            IJ.log("-----------------------------------");

            // Get SR FloatProcessor
            FloatProcessor fpSR = imsSR.getProcessor(n).convertToFloatProcessor();

            // Do optimisation
            double[] parameters = optimisationHelper.linearMatching(fpSR, fpRef, maxSigma, maxSigma);
            double alpha = parameters[0];
            double beta = parameters[1];
            double sigma_linear = parameters[2];

            // POPULATE OUTPUT STACKS

            /// intensity-scaled stack
            FloatProcessor fpSRIntensityScaled = (FloatProcessor) fpSR.duplicate();
            fpSRIntensityScaled.multiply(alpha);
            fpSRIntensityScaled.add(beta);
            imsSRNormalised.setProcessor(fpSRIntensityScaled, n);

            /// intensity-scaled and convolved stack
            FloatProcessor fpSRIntensityScaledBlurred = (FloatProcessor) fpSRIntensityScaled.duplicate();
            fpSRIntensityScaledBlurred.blurGaussian(sigma_linear);
            imsSRConvolved.setProcessor(fpSRIntensityScaledBlurred, n);

            IJ.saveAsTiff(new ImagePlus("fpSRIntensityScaledBlurred", fpSRIntensityScaledBlurred), "/home/nik/Documents/repositories/hawkman/cmake_build/here/ij_new_sr.tiff");
            IJ.saveAsTiff(new ImagePlus("fpRefScaledToSR", fpRefScaledToSR), "/home/nik/Documents/repositories/hawkman/cmake_build/here/ij_big_widefield.tiff");
            // CALCULATE METRICS AND MAP

            IJ.showStatus("Calculating similarity...");

            /// metrics
            FloatProcessor fpIntensityScaledBlurred_RefSize = (FloatProcessor) fpSRIntensityScaledBlurred.resize(w_Ref, h_Ref);
            float[] pixelsIntensityScaledBlurred_RefSize = (float[]) fpIntensityScaledBlurred_RefSize.getPixels();
            double globalRMSE = Math.sqrt(SQUIRRELMathTools_.calculateMSE(pixelsIntensityScaledBlurred_RefSize, pixelsRef));
            double globalPPMCC = SQUIRRELMathTools_.calculatePPMCC(pixelsIntensityScaledBlurred_RefSize, pixelsRef, true);

            FloatProcessor error_map = StackHelper.getErrorMap(fpSRIntensityScaledBlurred, pixelsRefScaledToSR, settings.show_positive_and_negative());
            IJ.saveAsTiff(new ImagePlus("ij_error_map", error_map), "/home/nik/Documents/repositories/hawkman/cmake_build/here/ij_error_map.tiff");
            imsEMap.setProcessor(error_map, n);

            // CALCULATE METRICS AND MAP FOR BOUNDARY PROBLEM CASES
            double globalRMSEBoundary = globalRMSE;
            double globalPPMCCBoundary = globalPPMCC;

            /// put error values into table
            rt.incrementCounter();
            rt.addValue("Frame", n);
            rt.addValue("RSP (Resolution Scaled Pearson-Correlation)", globalPPMCC);
            rt.addValue("RSE (Resolution Scaled Error)", globalRMSE);


            if (nSlicesSR <= 1)
            {
                continue;
            }
            double frameTime = ((System.nanoTime() - loopStart) / n) / 1e9;
            double remainingTime = frameTime * (nSlicesSR - n);
            int _h = (int) (remainingTime / 3600);
            int _m = (int) (((remainingTime % 86400) % 3600) / 60);
            int _s = (int) (((remainingTime % 86400) % 3600) % 60);
            IJ.log("Estimated time remaining to complete analysis:");
            IJ.log("\t" + String.format("%02d:%02d:%02d", _h, _m, _s));
        }

        // Output images

        String borderString = new String();
        if(borderCrop)
        {
            borderString = "Border cropped ";
        }

        String registrationString = new String();
        if(registrationCrop)
        {
            if (borderCrop)
            {
                registrationString = "and Registered";
            }
            else {
                registrationString = "Registered";
            }
        }

        boolean noCrop = borderCrop & registrationCrop;

        if(!noCrop)
        {
            new ImagePlus(settings.reference_image()+" - " +borderString+" " +registrationString, imsRef).show();
        }
        String titleSRImage = settings.super_res_image();
        if(settings.crop_and_normalise() || !noCrop)
        {
            new ImagePlus(titleSRImage +" - " +borderString +registrationString+" - intensity-normalised", imsSRNormalised).show();
        }

        if(settings.show_rsf_convolved())
        {
            new ImagePlus(titleSRImage+" - Convolved with RSF", imsSRConvolved).show();
        }

        ImagePlus impEMap = new ImagePlus(titleSRImage+" - Resolution Scaled Error-Map", imsEMap);
        StackHelper.applyLUT(impEMap,"SQUIRREL-Errors.lut");
        impEMap.show();

        rt.show("RSP and RSE values");

        IJ.run("Tile");

        IJ.log("SQUIRREL analysis took "+(System.currentTimeMillis()-startTime)/1000+"s");
    }
}
