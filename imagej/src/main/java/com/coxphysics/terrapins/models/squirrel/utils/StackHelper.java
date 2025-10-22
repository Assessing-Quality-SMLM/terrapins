package com.coxphysics.terrapins.models.squirrel.utils;

import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_CCMPeak;
import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_CrossCorrelationMap;
import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_GetFileFromResource;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.LutLoader;
import ij.plugin.filter.Binary;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

import org.apache.commons.math3.stat.StatUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.coxphysics.terrapins.models.squirrel.utils.ArrayUtils.toDoubleArray;
import static ij.process.ImageProcessor.NO_LUT_UPDATE;
import static java.lang.Math.*;

public class StackHelper {

    public static ImageStack purgeEmptyFrames(ImageStack ims){

        ArrayList<Integer> slicesToDelete = new ArrayList<Integer>();

        int nSlices = ims.getSize();

        for (int n = 1; n <= nSlices; n++) {

            IJ.showStatus("Checking stack, frame " + n);
            IJ.showProgress(n, nSlices);

            FloatProcessor fp = ims.getProcessor(n).convertToFloatProcessor();
            float[] pixels = (float[]) fp.getPixels();

            boolean deleteFlag = true;

            float firstVal = pixels[0];
            for (int p = 1; p < pixels.length; p++) {
                if (pixels[p] != firstVal) {
                    deleteFlag = false;
                    break;
                }
            }

            if (deleteFlag) slicesToDelete.add(n);
        }

        IJ.log("Empty Frame Check:");

        if (!slicesToDelete.isEmpty()) {
            int nSlicesToDelete = slicesToDelete.size();
            int[] slicesToDeleteArray = new int[nSlicesToDelete];
            for (int i = 0; i < nSlicesToDelete; i++) {
                slicesToDeleteArray[i] = slicesToDelete.get(i);
            }

            IJ.log("\t Frames purged: " + Arrays.toString(slicesToDeleteArray));

            for (int i = nSlicesToDelete - 1; i >= 0; i--) {
                ims.deleteSlice(slicesToDeleteArray[i]);
            }

        } else {
            IJ.log("\t All frames contain data!");
        }

        return ims;

    }

    public static ImageStack[] checkAndCropBorders(ImageStack imsRef, ImageStack imsSR, int magnification) {

        //TODO: doesn't work if borders are NaN rather than 0 - don't know if this is ever the case but worth bearing in mind

        int nSlicesRef = imsRef.getSize();
        int w_Ref = imsRef.getWidth();
        int h_Ref = imsRef.getHeight();

        int nSlicesSR = imsSR.getSize();
        int w_SR = imsSR.getWidth();
        int h_SR = imsSR.getHeight();

        int maxTopBorder = 0, maxRightBorder = 0, maxBottomBorder = 0, maxLeftBorder = 0;

        for(int n=1; n<=nSlicesSR; n++) {

            IJ.showStatus("Checking borders of frame "+n);
            IJ.showProgress(n, nSlicesSR);

            FloatProcessor fpSR = imsSR.getProcessor(n).convertToFloatProcessor();

            int topBorderCounter = 0, rightBorderCounter = 0, bottomBorderCounter = 0, leftBorderCounter = 0;
            float topBorderValue = 0, rightBorderValue = 0, bottomBorderValue = 0, leftBorderValue = 0;

            outerloop:
            while(topBorderValue==0){
                for(int x=0; x<w_SR; x++){
                    topBorderValue = fpSR.getPixelValue(x, topBorderCounter);
                    if(topBorderValue>0 && topBorderValue!=Float.NaN) break outerloop;
                }
                topBorderCounter++;
            }

            outerloop:
            while(rightBorderValue==0){
                for(int y=0; y<h_SR; y++){
                    rightBorderValue = fpSR.getPixelValue(w_SR - rightBorderCounter -1, y);
                    if(rightBorderValue>0 && rightBorderValue!=Float.NaN) break outerloop;
                }
                rightBorderCounter++;
            }

            outerloop:
            while(bottomBorderValue==0){
                for(int x=0; x<w_SR; x++){
                    bottomBorderValue = fpSR.getPixelValue(x, h_SR - bottomBorderCounter-1);
                    if(bottomBorderValue>0 && bottomBorderValue!=Float.NaN) break outerloop;
                }
                bottomBorderCounter++;
            }

            outerloop:
            while(leftBorderValue==0){
                for(int y=0; y<h_SR; y++){
                    leftBorderValue = fpSR.getPixelValue(leftBorderCounter, y);
                    if(leftBorderValue>0 && leftBorderValue!=Float.NaN) break outerloop;
                }
                leftBorderCounter++;
            }

            //Check if largest border in stacks
            maxTopBorder = max(topBorderCounter, maxTopBorder);
            maxRightBorder = max(rightBorderCounter, maxRightBorder);
            maxBottomBorder = max(bottomBorderCounter, maxBottomBorder);
            maxLeftBorder = max(leftBorderCounter, maxLeftBorder);

        }

        IJ.log("Border Control:");

        if(maxTopBorder==0 && maxRightBorder==0 && maxBottomBorder==0 && maxLeftBorder==0){
            IJ.log("\t No borders to crop!");
            return new ImageStack[]{};
        }

        // Convert borders to reference image scale
        int roundedTop = (int) (ceil(maxTopBorder/magnification));
        int roundedLeft = (int) (ceil(maxLeftBorder/magnification));
        int roundedBottom = (int) (ceil(maxBottomBorder/magnification));
        int roundedRight = (int) (ceil(maxRightBorder/magnification));


        IJ.log("\t Rounded border widths (reference scale): Top = "+roundedTop+", Right = "+roundedRight+", Bottom = "+roundedBottom+", Left = "+roundedLeft);

        int newWidth = w_Ref - roundedRight - roundedLeft;
        int newHeight = h_Ref - roundedBottom - roundedTop;

        imsRef = imsRef.duplicate().crop(roundedLeft, roundedTop, 0, newWidth, newHeight, nSlicesRef);
        imsSR = imsSR.crop(roundedLeft*magnification, roundedTop*magnification, 0, newWidth*magnification, newHeight*magnification, nSlicesSR);

        return new ImageStack[]{imsRef, imsSR};

    }

    public static ImageStack[] realignImages(ImageStack imsRef, ImageStack imsSR, double maxExpectedMisalignment, int magnification){

        int w_Ref = imsRef.getWidth();
        int h_Ref = imsRef.getHeight();
        int nSlicesRef = imsRef.getSize();
        int w_SR = imsSR.getWidth();
        int h_SR = imsSR.getHeight();
        int nSlicesSR = imsSR.getSize();

        // Magnify ref image
        ImageStack imsRefResized = magnify(imsRef, w_SR, h_SR);
        FloatProcessor fpRef = imsRefResized.getProcessor(1).convertToFloatProcessor();

        // Cross-correlation realignment
        ImageStack imsSRTranslated = new ImageStack(w_SR, h_SR, nSlicesSR);
        ResultsTable rt = new ResultsTable();

        double maxShiftX = 0, maxShiftY = 0, absMaxShiftX = 0, absMaxShiftY = 0;

        for(int n=1; n<=nSlicesSR; n++) {
            IJ.showStatus("Checking registration of slice "+n);
            IJ.showProgress(n, nSlicesSR);

            FloatProcessor fpSR = imsSR.getProcessor(n).convertToFloatProcessor();

            IJ.showStatus("calculating cross-correlation...");
            FloatProcessor ipCCM = (FloatProcessor) SQUIRREL_CrossCorrelationMap.calculateCrossCorrelationMap(fpRef, fpSR, true);

            if (maxExpectedMisalignment != 0) ipCCM = SQUIRREL_CrossCorrelationMap.cropCCM(ipCCM, (int) maxExpectedMisalignment);

            IJ.showStatus("calculating cross-correlation peaks...");

            float[] shift = SQUIRREL_CCMPeak.getShiftFromCrossCorrelationPeak(ipCCM, 2);
            float shiftX = shift[1];
            float shiftY = shift[2];
            rt.incrementCounter();
            rt.addValue("Frame", n);
            rt.addValue("x-shift (pixels)", shiftX);
            rt.addValue("y-shift (pixels)", shiftY);

            if(abs(shiftX)>absMaxShiftX) {maxShiftX = shiftX; absMaxShiftX = abs(shiftX);}
            if(abs(shiftY)>absMaxShiftY) {maxShiftY = shiftY; absMaxShiftY = abs(shiftY);}

            fpSR.setInterpolationMethod(ImageProcessor.BICUBIC);
            fpSR.resetRoi();
            fpSR.translate(shiftX, shiftY);
            imsSRTranslated.setProcessor(fpSR, n);

        }

        //rt.show("Cross-correlation results");

        IJ.log("Registration:");

        //IJ.log("maxShiftX = "+maxShiftX+" maxShiftY = "+maxShiftY);
        if(absMaxShiftX<0.5) maxShiftX = 0;
        if(absMaxShiftY<0.5) maxShiftY = 0;

        if(maxShiftX==0 && maxShiftY==0){
            IJ.log("\t Images are already registered");
            return new ImageStack[]{};
        }

        // Crop shifty borders from images
        int roundedMaxShiftX = (int) (ceil(absMaxShiftX/magnification)*signum(maxShiftX));
        int roundedMaxShiftY = (int) (ceil(absMaxShiftY/magnification)*signum(maxShiftY));

        IJ.log("\t Maximum misalignment (on reference pixel scale): x = "+roundedMaxShiftX+", y = "+roundedMaxShiftY+". Cropping registration borders.");

        fpRef.resetRoi();

        imsRef = imsRef.duplicate().crop(max(roundedMaxShiftX, 0), max(roundedMaxShiftY, 0), 0,
                w_Ref - abs(roundedMaxShiftX), h_Ref - abs(roundedMaxShiftY), nSlicesRef);

        imsSR = imsSRTranslated.crop(max(roundedMaxShiftX*magnification, 0), max(roundedMaxShiftY*magnification, 0), 0,
                w_SR - abs(roundedMaxShiftX*magnification), h_SR - abs(roundedMaxShiftY*magnification), nSlicesSR);

        return new ImageStack[]{imsRef, imsSR};
    }

    public static ImageStack magnify(ImageStack ims, int w, int h) {

        ImageStack imsResized = new ImageStack(w, h, ims.getSize());
        for (int s = 1; s <= ims.getSize(); s++) {
            FloatProcessor fpRefResized = ims.getProcessor(s).convertToFloatProcessor();
            fpRefResized.setInterpolationMethod(ImageProcessor.BICUBIC);
            fpRefResized = (FloatProcessor) fpRefResized.resize(w, h);
            imsResized.setProcessor(fpRefResized, s);
        }
        return imsResized;
    }

    public static FloatProcessor getErrorMap(FloatProcessor fpSRIntensityScaledBlurred, float[] pixelsRefScaledToSR, boolean showPositiveNegative){

        int w_SR = fpSRIntensityScaledBlurred.getWidth();
        int h_SR = fpSRIntensityScaledBlurred.getHeight();

        /// error map
        float[] pixelsSRC = (float[]) fpSRIntensityScaledBlurred.getPixels();
        int nPixelsSR = pixelsSRC.length;
        float[] pixelsEMap = new float[nPixelsSR];

        for(int p=0; p<nPixelsSR; p++){
            float vRef = pixelsRefScaledToSR[p];
            float vSRC = pixelsSRC[p];

            if(showPositiveNegative) pixelsEMap[p] = (vRef - vSRC);
            else pixelsEMap[p] = abs(vRef-vSRC);
        }
         return new FloatProcessor(w_SR, h_SR, pixelsEMap);
    }

    public static void applyLUT(ImagePlus imp, String path) {
        File temp = null;
        try {
            temp = SQUIRREL_GetFileFromResource.getLocalFileFromResource("/"+path);
        } catch (IOException e) {
            IJ.log("Couldn't find resource: "+path);
        }
        if (temp != null) {
            LUT lut = LutLoader.openLut(temp.getAbsolutePath());
            imp.setLut(lut);
        }
    }

    public static boolean checkPixelSumZero(FloatProcessor fp){

        float[] pixels = (float[]) fp.getPixels();
        int nPixels = pixels.length;
        float sumPixels = 0;
        for(int i=0; i<nPixels; i++){
            sumPixels += pixels[i];
        }

        if(sumPixels>0) return false;

        return true;
    }

    public static boolean checkProportionPixelsNonZero(FloatProcessor fp, double proportion){

        int nPixels = fp.getPixelCount();
        int nPixelsTarget = (int) (proportion*nPixels);
        int nPixelsNonZero = 0;

        for(int i=0; i<nPixels; i++){
            if(fp.getf(i)!=0) nPixelsNonZero++;
            if(nPixelsNonZero>nPixelsTarget) return true;
        }
        return false;
    }

    public static double getPixelSizeUm(ImagePlus imp){
        String pixelUnitRef = imp.getCalibration().getUnit();
        double pixelSizeUm;

        if (pixelUnitRef.equals("nm")) {
            IJ.log("Detected nm!");
            pixelSizeUm = imp.getCalibration().pixelWidth/1000;
        } else if (pixelUnitRef.equals("micron") || pixelUnitRef.equals("microns") || pixelUnitRef.equals("µm") || pixelUnitRef.equals("um")) {
            IJ.log("Detected µm!");
            pixelSizeUm = imp.getCalibration().pixelWidth;
        } else {
            //assign arbitrary pixel size of 100nm
            IJ.log("WARNING: no pixel calibration data associated with image; assuming pixel size = 100nm");
            pixelSizeUm = 0.1;
        }

        return pixelSizeUm;

    }

    public static FloatProcessor transferPixels(FloatProcessor fp1, FloatProcessor fp2, Roi roi){
        Rectangle rectangle = roi.getBounds();
        int xStart = rectangle.x;
        int yStart = rectangle.y;
        int width = rectangle.width;
        int height = rectangle.height;

        int cropWidth = fp1.getWidth();
        int cropHeight = fp1.getHeight();

        for(int y=0; y<height; y++){
            if(y>=cropHeight) continue;
            for(int x=0; x<width; x++){
                if(x>=cropWidth) continue;
                float val = fp1.getf(x, y);
                fp2.setf(x+xStart, y+yStart, val);
            }
        }
        return fp2;
    }

    public static double getProportionStructure(ImageProcessor ip, double sigma, boolean invertLUT){
        Binary binary = new Binary();

        if(sigma!=0){
            ip.blurGaussian(sigma);
        }
        ip.setAutoThreshold("Default", true, NO_LUT_UPDATE);
        ip.autoThreshold();
        ImageProcessor ipMask = ip.createMask();
        if(invertLUT) ipMask.invertLut();

        binary.setup("skeletonize", null);
        binary.run(ipMask);

        //new ImagePlus("skeleton", ipMask).show();

        double nPixels = ip.getWidth()*ip.getHeight();
        double sumIntensePixels = 0;

        for(int i=0; i<nPixels; i++){
            if(ipMask.get(i)>0) sumIntensePixels++;
        }

        double fraction = sumIntensePixels/nPixels;

        if(fraction>0.5) return 1-fraction; // just in case of stupid invert problems
        return fraction;
    }

    public static int getNumPositiveNonZeroPixels(FloatProcessor fp){
        float[] pixels = (float[]) fp.getPixels();
        int v=0;
        for(int i=0; i<pixels.length; i++){
            if(pixels[i]>0) v++;;
        }
        return v;
    }

    public static double[] estimateSignalAndBG(ImageProcessor ip, boolean invertLUT){

        FloatProcessor fp = ip.convertToFloatProcessor();

        ip.setAutoThreshold("Triangle", true, NO_LUT_UPDATE);
        ip.autoThreshold();
        ImageProcessor ipMask = ip.createMask();
        if(invertLUT) ipMask.invertLut();

        ArrayList<Float> signalPixels = new ArrayList<>();
        ArrayList<Float> bgPixels = new ArrayList<>();

        for(int i=0; i<ipMask.getPixelCount(); i++){
            if(ipMask.get(i)==0) bgPixels.add(fp.getf(i));
            else signalPixels.add(fp.getf(i));
        }

        int nSignal = signalPixels.size();
        double maxSignal = 0;
        double meanSignal = 0;
        int nBG = bgPixels.size();
        double meanBG = 0;


        for(int i=0; i<nSignal; i++){
            meanSignal += signalPixels.get(i)/nSignal;
            maxSignal = max(maxSignal, signalPixels.get(i));
        }
        for(int i=0; i<nBG; i++) meanBG += bgPixels.get(i)/nBG;

        double stdSignal = 0;
        for(int i=0; i<nSignal; i++) stdSignal += pow(signalPixels.get(i)-meanSignal, 2)/nSignal;
        double stdBG = 0;
        for(int i=0; i<nBG; i++) stdBG += pow(bgPixels.get(i)-meanBG, 2)/nBG;

        stdSignal = sqrt(stdSignal);
        stdBG = sqrt(stdBG);

        return new double[]{maxSignal, meanSignal, stdSignal, meanBG, stdBG};


    }

    public static FloatProcessor normaliseByMax(ImageProcessor ip){
        FloatProcessor fp = ip.convertToFloatProcessor();

        double maxVal = getMaxOfImage(ip);

        for(int i=0; i<fp.getPixelCount(); i++) fp.setf(i, fp.getf(i)/(float)maxVal);

        return fp;
    }

    public static double getMaxOfImage(ImageProcessor ip){
        double imageMaxVal = 0;
        for(int i=0; i<ip.getPixelCount(); i++) imageMaxVal = max(imageMaxVal, ip.getf(i));
        return imageMaxVal;
    }

    public static double[] getPixelsAsDoubleArray(FloatProcessor fp){
        float[] pixelsF = (float[]) fp.getPixels();
        double[] pixelsD = new double[pixelsF.length];
        for(int i=0; i<pixelsF.length; i++) pixelsD[i] = (double) pixelsF[i];
        return pixelsD;
    }

    public static double[] getPixelsAsDoubleArrayNoNans(FloatProcessor fp){
        float[] pixelsF = (float[]) fp.getPixels();
        ArrayList<Double> pixelsDNoNans = new ArrayList<>();
        for(int i=0; i<pixelsF.length; i++){
            float val = pixelsF[i];
            if(Float.isInfinite(val)|| Float.isNaN(val)) continue;
            pixelsDNoNans.add((double) pixelsF[i]);
        }
        return toDoubleArray(pixelsDNoNans);
    }

    public static FloatProcessor subtractFp(FloatProcessor fp1, FloatProcessor fp2){
        FloatProcessor fpDiff = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
        for(int i=0; i<fpDiff.getPixelCount(); i++){
            float v1 = fp1.getf(i);
            float v2 = fp2.getf(i);
            fpDiff.setf(i, v1-v2);
        }
        return fpDiff;
    }

    public static FloatProcessor addFp(FloatProcessor fp1, FloatProcessor fp2){
        FloatProcessor fpSum = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
        for(int i=0; i<fpSum.getPixelCount(); i++){
            float v1 = fp1.getf(i);
            float v2 = fp2.getf(i);
            fpSum.setf(i, v1+v2);
        }
        return fpSum;
    }

    public static FloatProcessor divideFp(FloatProcessor fp1, FloatProcessor fp2){
        FloatProcessor fp = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
        for(int p=0; p<fp.getPixelCount(); p++){
            fp.setf(p, fp1.getf(p)/fp2.getf(p));
        }
        return fp;
    }

    public static double getFpMax(ImageProcessor ip){
        double[] pixels = getPixelsAsDoubleArray(ip.convertToFloatProcessor());
        return StatUtils.max(pixels);
    }
    public static double getFpMin(ImageProcessor ip){
        double[] pixels = getPixelsAsDoubleArray(ip.convertToFloatProcessor());
        return StatUtils.min(pixels);
    }

    public static double getFpMinNonZero(ImageProcessor ip){
        double[] pixels = getPixelsAsDoubleArray(ip.convertToFloatProcessor());
        for(int p=0; p<pixels.length; p++){
            if(pixels[p]==0) pixels[p] = Double.MAX_VALUE;
        }
        return StatUtils.min(pixels);
    }

}
