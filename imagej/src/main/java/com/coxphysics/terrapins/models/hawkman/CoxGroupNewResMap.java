/*
 * Copyright (C) 2020 Susan Cox
 * This software is free but covered by the following restrictions
 * GNU AFFERO GENERAL PUBLIC LICENSE
 *      Version 3, 19 November 2007
 *      Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *      See contained 'LICENCE' file for details
 */
package com.coxphysics.terrapins.models.hawkman;

/**
 *
 * @author CoxGroup
 */


import static ij.plugin.filter.PlugInFilter.NO_CHANGES;
import static ij.plugin.filter.PlugInFilter.STACK_REQUIRED;
import static ij.plugin.filter.PlugInFilter.DOES_RGB;

import ij.*;
//import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
//import ij.process.FloatProcessor;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.gui.GenericDialog;
//import ij.gui.Plot;
//import ij.gui.PlotWindow;


public class CoxGroupNewResMap implements PlugInFilter{
    
    ImagePlus ConfMapWin;
    String args;
    
    //Setup to get stack and set allowable types
    @Override
    public int setup(String arg_, ImagePlus img) {
		ConfMapWin = img;
		args=arg_;

        return STACK_REQUIRED +NO_CHANGES + DOES_RGB;
    }
    
    
    //Main method & start point
    @Override
    public void run(ImageProcessor ip) {
        ImageStack ConfMapStack;
        //ColorProcessor ResMapImg, tempConfMap;
        //byte[] ImgRed, ImgGreen, ImgBlue;
        //byte[] ImgBright, ImgCol, ImgSat;
        //ByteProcessor ImgRes;
        double colratio, threshold;
        int pixelsX, pixelsY, Nframes, resLevel;
        int conscales;
        
        IJ.log("HAWKMAN : Converting confidence to artefact scale map");
        NewResMapDialog ResMapGD = new NewResMapDialog();
        // coment the nest 3 lines to disable settings dialogue
        ResMapGD.showDialog();
        if(!ResMapGD.wasOKed())
            return;
        threshold = (double)ResMapGD.getNextNumber();
        conscales = (int)ResMapGD.getNextNumber();
        
        ConfMapStack = ConfMapWin.getStack();        
        pixelsX = ConfMapStack.getWidth();
        pixelsY = ConfMapStack.getHeight();
        Nframes = ConfMapStack.getSize();
        
        IJ.log("Number of lengthscales = " + String.valueOf(Nframes));
        //IJ.log("Confidence threshold = " + String.valueOf(threshold));
        ImagePlus MapWin = NewCalcResMap(ConfMapStack, threshold, conscales);
        ImagePlus KeyWin = NewResMapKey(pixelsX,pixelsY,Nframes);
        
        IJ.run("Combine...", "stack1=[HAWKMAN : Scale map] stack2=[HAWKMAN : Key]");
        (WindowManager.getCurrentWindow()).setTitle("HAWKMAN artefact scale map");
        (WindowManager.getCurrentWindow()).setName("HAWKMAN artefact scale map");
        
        
        //ImgRed = new byte[pixelsX * pixelsY];
        //ImgGreen = new byte[pixelsX * pixelsY];
        //ImgBlue = new byte[pixelsX * pixelsY];
        //ResMapImg = new ColorProcessor(pixelsX ,pixelsY);
        //ImgRes = new ByteProcessor(pixelsX, pixelsY);
        //ImgBright = new byte[pixelsX * pixelsY];
        //ImgCol = new byte[pixelsX * pixelsY];
        //ImgSat = new byte[pixelsX * pixelsY];
        
       
        //for (int scale = 1; scale <= Nframes; scale++) {
        //    //tempConfMap = new ColorProcessor(pixelsX, pixelsY);
        //    tempConfMap = (ConfMapStack.getProcessor(scale)).convertToColorProcessor();
        //    tempConfMap.resetMinAndMax();
        //    tempConfMap.getRGB(ImgRed, ImgGreen, ImgBlue);
        //    
        //    for (int row = 0; row < pixelsY; row++) {
        //        for (int col = 0; col < pixelsX; col++) {
        //            resLevel = Byte.toUnsignedInt(ImgRed[(row*pixelsX)+col]);
        //            resLevel = resLevel + Byte.toUnsignedInt(ImgGreen[(row*pixelsX)+col]);
        //            colratio = Byte.toUnsignedInt(ImgRed[(row*pixelsX)+col])/(1.0*resLevel);
        //            if ((scale==1) && (resLevel>0)) {
        //                ImgRes.set(col, row, 1);
        //                ImgBright[(row*pixelsX)+col] = (byte)resLevel;
        //                ImgCol[(row*pixelsX)+col] = (byte)(1*196/Nframes);
        //                ImgSat[(row*pixelsX)+col] = (byte)127;
        //            }
        //            resLevel = ImgRes.get(col,row);
        //            if((scale>1) && (resLevel > 0)) {
        //                resLevel = (int)ImgRed[(row*pixelsX)+col];
        //                //if (resLevel > 0) {
        //                if (colratio > threshold) {
        //                    ImgRes.set(col,row,scale);
        //                    ImgCol[(row*pixelsX)+col] = (byte)(scale*196.0/Nframes);
        //                }
        //                    
        //            }
        //         
        //        }
        //    }
        //}
        //ResMapImg.setHSB(ImgCol, ImgSat, ImgBright);
        //ImagePlus ResMapWin = new ImagePlus("HAWKMAN resolution map", ResMapImg);
        //ResMapWin.show();
        //ImagePlus ScaleMapWin = new ImagePlus("HAWKMAN lengthscale map", ImgRes);
        //ScaleMapWin.show();
        IJ.log("Done generating Artefact scale map");
    }
    
    //Functio to calculate and display the resolution map frome given stack and threshold
    ImagePlus NewCalcResMap(ImageStack inputIS, double inputThresh, int inputConscales) {
        ImageStack ConfMapStack;
        ColorProcessor ResMapImg, tempConfMap;
        byte[] ImgRed, ImgGreen, ImgBlue;
        byte[] ImgBright, ImgCol, ImgSat;
        int[] ImgFlag;
        ByteProcessor ImgRes;
        double colratio, threshold;
        int pixelsX, pixelsY, Nframes, resLevel;
        int tempint, conScales;
        
        ConfMapStack = inputIS.duplicate();
        pixelsX = ConfMapStack.getWidth();
        pixelsY = ConfMapStack.getHeight();
        Nframes = ConfMapStack.getSize();
        threshold = inputThresh;
        conScales = inputConscales;
        
        ImgRed = new byte[pixelsX * pixelsY];
        ImgGreen = new byte[pixelsX * pixelsY];
        ImgBlue = new byte[pixelsX * pixelsY];
        ResMapImg = new ColorProcessor(pixelsX ,pixelsY);
        ImgRes = new ByteProcessor(pixelsX, pixelsY);
        ImgBright = new byte[pixelsX * pixelsY];
        ImgCol = new byte[pixelsX * pixelsY];
        ImgSat = new byte[pixelsX * pixelsY];
        ImgFlag = new int[pixelsX * pixelsY];
        
       
        for (int scale = 1; scale <= Nframes; scale++) {
            //tempConfMap = new ColorProcessor(pixelsX, pixelsY);
            tempConfMap = (ConfMapStack.getProcessor(scale)).convertToColorProcessor();
            tempConfMap.resetMinAndMax();
            tempConfMap.getRGB(ImgRed, ImgGreen, ImgBlue);
            
            for (int row = 0; row < pixelsY; row++) {
                for (int col = 0; col < pixelsX; col++) {
                    resLevel = Byte.toUnsignedInt(ImgRed[(row*pixelsX)+col]);
                    resLevel = resLevel + Byte.toUnsignedInt(ImgGreen[(row*pixelsX)+col]);
                    colratio = Byte.toUnsignedInt(ImgRed[(row*pixelsX)+col])/(1.0*resLevel);
                    if ((scale==1) && (resLevel>0)) {
                        ImgRes.set(col, row, 1);
                        ImgBright[(row*pixelsX)+col] = (byte)resLevel;
                        ImgCol[(row*pixelsX)+col] = (byte)(1*196/Nframes);
                        ImgSat[(row*pixelsX)+col] = (byte)127;
                        ImgFlag[(row*pixelsX)+col] = conScales;
                    }
                    resLevel = ImgRes.get(col,row);
                    if((scale>1) && (resLevel > 0)) {
                        //resLevel = (int)ImgRed[(row*pixelsX)+col];
                        //if (resLevel > 0) {
                        if ((colratio > threshold) && (ImgFlag[(row*pixelsX)+col] >0)) {
                            ImgRes.set(col,row,scale);
                            ImgCol[(row*pixelsX)+col] = (byte)(scale*196.0/Nframes);
                            //tempint=(int)scale+ImgFlag[(row*pixelsX)+col]-3;
                            //ImgCol[(row*pixelsX)+col] = (byte)(tempint*296/Nframes);
                        }
                        else {
                            ImgFlag[(row*pixelsX)+col] = ImgFlag[(row*pixelsX)+col]-1;
                        }
                            
                    }
                 
                }
            }
        }
        ResMapImg.setHSB(ImgCol, ImgSat, ImgBright);
        ImagePlus ResMapWin = new ImagePlus("HAWKMAN : Scale map", ResMapImg);
        ResMapWin.show();
        //ImagePlus ScaleMapWin = new ImagePlus("HAWKMAN lengthscale map", ImgRes);
        //ScaleMapWin.show();
        //IJ.log("Done generating resolution map");
        return ResMapWin;
    }
    
    
    //Function to display a key to the colour res map
    ImagePlus NewResMapKey(int pixX, int pixY,int NLevels) {
        ColorProcessor ResMapKey;
        byte[] KeyCol, KeySat, KeyBright;
        int keyPixX;
        
        keyPixX = (int)Math.ceil(0.05*pixX);
        if (keyPixX < 20) {
            keyPixX = 20;
        }
        ResMapKey = new ColorProcessor(keyPixX,pixY);
        KeyCol = new byte[pixY*keyPixX];
        KeySat = new byte[pixY*keyPixX];
        KeyBright = new byte[pixY*keyPixX];
        
        for (int row = 0; row < pixY; row++) {
            for (int col = 0; col < keyPixX; col++) {
                KeySat[(row*keyPixX)+col] = (byte)127;
                KeyBright[(row*keyPixX)+col] = (byte)255;
                KeyCol[(row*keyPixX)+col] = (byte)((int)(196.0*Math.ceil((double)NLevels*(row+1.0)/pixY)/(double)NLevels));                
            }
        }
 
        ResMapKey.setHSB(KeyCol, KeySat, KeyBright);
        ResMapKey.drawString("1",2,15);
        ResMapKey.drawString(String.valueOf(NLevels),2,pixY-3);
        ImagePlus ResKeyWin = new ImagePlus("HAWKMAN : Key", ResMapKey);
        ResKeyWin.show();
        
        return ResKeyWin;
    }

        public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = CoxGroupNewResMap.class;
        java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        java.io.File file = new java.io.File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        ImagePlus [] files = new ImagePlus[]{
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/1.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/2.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/3.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/4.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/5.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/6.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/7.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/8.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/9.tiff"),
                IJ.openImage("/home/nik/Documents/repositories/hawkman/cpp/tools_build/output/confidence_map/10.tiff"),
        };
        ImageStack stack = new ImageStack(files[0].getWidth(), files[0].getHeight());
        for (ImagePlus f : files)
        {
            stack.addSlice(f.getProcessor());
        }
        ImagePlus stack_image = new ImagePlus("HAWKMAN Sharpening map", stack);
        stack_image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}


//Dialogue box to enter the threshold for res calcuation
class NewResMapDialog extends GenericDialog {
    double ResThreshold;
    
    NewResMapDialog() {
        super("HAWKMAN");
        addNumericField("Artefact map threshold : ", 0.1, 3);
        addNumericField("Consecutive OK scales : ", 3, 0);
    }
}
