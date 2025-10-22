package com.coxphysics.terrapins.models.hawkman;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.GaussianBlur;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;

public class HAWKMAN implements PlugIn
{


    //Main method & start point
    @Override
    public void run(String args)
    {
        String imgTestName, imgRefName;
        double PSFsizeGD, maxScaleGD;
        double FWHMthr, FWHMsmo, FWHMoff;
        double SKELthr, SKELsmo, SKELoff;
        boolean ADialErrode, BDialErrode, DiagnoseFlag, FlattenFlag, BlurSkelFlag;

        IJ.log("HAWKMAN : Super-resolution sharpening artefact detection");
        GryphonDialog GryphonGD;
        try {
            GryphonGD = new GryphonDialog();
            GryphonGD.showDialog();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            IJ.showMessage("HAWKMAN error", "HAWKMAN requires both a Test and a Reference image!");
            IJ.log("HAWKMAN : Could not get images - Exiting");
            return;
        }

	if(!GryphonGD.wasOKed())
            return;

        GryphonGD.getParamsFromGD();

        PSFsizeGD = GryphonGD.getNextNumber();
        maxScaleGD = GryphonGD.getNextNumber();
        imgTestName = GryphonGD.getNextChoice();
        imgRefName = GryphonGD.getNextChoice();
        FWHMthr = GryphonGD.FWHMthreshold;
        FWHMsmo = GryphonGD.FWHMsmooth;
        FWHMoff = GryphonGD.FWHMoffset;
        SKELthr = GryphonGD.SKELthreshold;
        SKELsmo = GryphonGD.SKELsmooth;
        SKELoff = GryphonGD.SKELoffset;
        ADialErrode = GryphonGD.ADelAndErrode;
        BDialErrode = GryphonGD.BDelAndErrode;
        DiagnoseFlag = GryphonGD.DiagnosticWin;
        FlattenFlag = GryphonGD.FlattenImage;
        BlurSkelFlag = GryphonGD.BlurSkeleton;

        if (PSFsizeGD == 0.0) {                     //if PSF = 0 in macro use values for calcPSF checkbox and calculate
            GryphonGD.getCalculatedPSF();
        }
        if (GryphonGD.calculatedPSF > 0.01) {       // if calculatedPSF no zero then overide main dialog with calculated value
            PSFsizeGD = (double)GryphonGD.calculatedPSF;
        }
        run_with(PSFsizeGD,
                 maxScaleGD,
                 imgTestName,
                 imgRefName,
                 FWHMthr,
                 FWHMsmo,
                 FWHMoff,
                 SKELthr,
                 SKELsmo,
                 SKELoff,
                 ADialErrode,
                 BDialErrode,
                 DiagnoseFlag,
                 FlattenFlag,
                 BlurSkelFlag);
    }

    public static void run_with_settings(Settings settings)
    {
        run_with(settings.psf_size(),
                 settings.max_scale(),
                 settings.test_name(),
                 settings.ref_name(),
                 settings.fwhm_threshold(),
                 settings.fwhm_smoothing(),
                 settings.fwhm_offset(),
                 settings.skeletonise_threshold(),
                 settings.skeletonise_smoothing(),
                 settings.skeletonise_offset(),
                false,
                settings.dilate_erode_method() == DilateErode.Method.B,
                false,
                 settings.flatten_images(),
                 settings.blur_skeletons()
        );
    }

    public static void run_with(double PSFsizeGD,
                         double maxScaleGD,
                         String imgTestName,
                         String imgRefName,
                         double FWHMthr,
                         double FWHMsmo,
                         double FWHMoff,
                         double SKELthr,
                         double SKELsmo,
                         double SKELoff,
                         boolean ADialErrode,
                         boolean BDialErrode,
                         boolean DiagnoseFlag,
                         boolean FlattenFlag,
                         boolean BlurSkelFlag)
    {
        double normInt, tempDblA,tempDblB;
        int SRpixelsX, SRpixelsY, PSFsize, rConv, PSFhalf, scalecount, maxScale;
        float ThreshKernal[], HalfKernal[], ResCorr[], SkelCorr[], PlotX[], SRinfoA[], SRinfoB[] ,CorrScore[];
        boolean ThreshBool;

        ImagePlus imgTestIplus, imgRefIplus, plusTempA, plusTempB, plusTempC;
        ImageProcessor imgTestIP, imgRefIP, imgTempA, imgTempB, imgTempC;
        FloatProcessor imgTestFP, imgRefFP, imgTestHalfFP, imgRefHalfFP, imgTestThreshFP, imgRefThreshFP, imgTestFlat, imgRefFlat;
        ByteProcessor imgTestRes, imgRefRes, imgTestSkel, imgRefSkel, TestPicBP, RefPicBP;
        ColorProcessor colRes, colSkel, colConf;
        ImageStack stackOut, stackRGBRes, stackRGBSkel, stackConf;  //stack.out used for diagnostics
        Plot CorrPlot, SRIPlot;
        GaussianBlur BlurKernal = new GaussianBlur();
        Convolver ConvKernal = new Convolver();
        ImageCalculator ImgCalcKernal = new ImageCalculator();
        CoxGroupLocalCorr CoxLocCorrelator = new CoxGroupLocalCorr();   //new instance of CoxGroupLocalCorr.java


        IJ.log("HAWKMAN : Super-resolution sharpening artefact detection");

        IJ.log("Settings used :");
        IJ.log("FWHM threshold = " + String.valueOf(FWHMthr));
        IJ.log("FWHM smoothing = " + String.valueOf(FWHMsmo));
        IJ.log("FWHM offset = " + String.valueOf(FWHMoff));
        IJ.log("SKEL threshold = " + String.valueOf(SKELthr));
        IJ.log("SKEL smoothing = " + String.valueOf(SKELsmo));
        IJ.log("SKEL offset = " + String.valueOf(SKELoff));
        //IJ.log("Dialate/errode method A = " + String.valueOf(ADialErrode));
        //IJ.log("Dialate/errode method B = " + String.valueOf(BDialErrode));
        IJ.log("Dilate & erode = " + String.valueOf(BDialErrode));
        IJ.log("Diagnostic windows = " + String.valueOf(DiagnoseFlag));
        IJ.log("Flatten images = " + String.valueOf(FlattenFlag));
        IJ.log("Show blured skeleton = " + String.valueOf(BlurSkelFlag));

        IJ.log("Test image = " + imgTestName + " : Reference image = " + imgRefName);
        IJ.resetEscape();
        IJ.showStatus("HAWKMAN Analising");

        imgTestIplus = ij.WindowManager.getImage(imgTestName);
        imgRefIplus = ij.WindowManager.getImage(imgRefName);
        SRpixelsX = imgTestIplus.getWidth();
        SRpixelsY = imgTestIplus.getHeight();
        if ((SRpixelsX != imgRefIplus.getWidth()) || (SRpixelsY != imgRefIplus.getHeight())) {
            IJ.showMessage("HAWKMAN error", "Test & Reference images must be the same size!");
            return;
        }

        PSFsize = (int) Math.ceil(PSFsizeGD);
        maxScale = (int) Math.ceil(maxScaleGD);
        stackOut = ImageStack.create(SRpixelsX+(0*maxScale), SRpixelsY+(0*maxScale), 0, 32);
        stackRGBRes = ImageStack.create(SRpixelsX, SRpixelsY, 0, 24);
        stackRGBSkel = ImageStack.create(SRpixelsX, SRpixelsY, 0, 24);
        stackConf = ImageStack.create(SRpixelsX, SRpixelsY, 0, 24);
        //stackOut.deleteLastSlice();
        stackOut.setVoxel(0, 0, 0, 0.999);
        ResCorr = new float[maxScale+1];
        SkelCorr = new float[maxScale+1];
        CorrScore = new float[maxScale+1];
        PlotX = new float[maxScale+1];
        SRinfoA = new float[maxScale+1];
        SRinfoB = new float[maxScale+1];

        imgTestRes = new ByteProcessor(SRpixelsX, SRpixelsY);
        imgRefRes = new ByteProcessor(SRpixelsX, SRpixelsY);
        imgTestSkel = new ByteProcessor(SRpixelsX, SRpixelsY);
        imgRefSkel = new ByteProcessor(SRpixelsX, SRpixelsY);
        TestPicBP = new ByteProcessor(SRpixelsX, SRpixelsY);
        RefPicBP = new ByteProcessor(SRpixelsX, SRpixelsY);
        imgTempA = new FloatProcessor(SRpixelsX, SRpixelsY);
        imgTempB = new FloatProcessor(SRpixelsX, SRpixelsY);
        imgTempC = new FloatProcessor(SRpixelsX, SRpixelsY);
        imgTestFlat = new FloatProcessor(SRpixelsX, SRpixelsY);
        imgRefFlat = new FloatProcessor(SRpixelsX, SRpixelsY);
        colRes = new ColorProcessor(SRpixelsX, SRpixelsY);
        colSkel = new ColorProcessor(SRpixelsX, SRpixelsY);
        colConf = new ColorProcessor(SRpixelsX, SRpixelsY);
        IJ.log("Image size : " + String.valueOf(SRpixelsX) + " x " + String.valueOf(SRpixelsY));

        imgTestIP = imgTestIplus.getProcessor();
        imgRefIP = imgRefIplus.getProcessor();
        IJ.log("PSF pixel size : " + String.valueOf(PSFsize));

        if (FlattenFlag) {
            imgTestFlat = imgFlatten(imgTestIP.convertToFloatProcessor(), 256, DiagnoseFlag);
            imgRefFlat = imgFlatten(imgRefIP.convertToFloatProcessor(), 256, DiagnoseFlag);
        }
        else {
            imgTestFlat = imgTestIP.convertToFloatProcessor();
            imgRefFlat = imgRefIP.convertToFloatProcessor();
        }

        imgTestFP = imgTestIP.convertToFloatProcessor();
        IJ.log("min value : " + String.valueOf(imgTestFP.getMin()));
        IJ.log("max value : " + String.valueOf(imgTestFP.getMax()));

        PSFhalf = (int)(2*Math.ceil(Math.ceil(PSFsize/4.0)))-1;
        HalfKernal = new float[PSFhalf*PSFhalf];
        Arrays.fill(HalfKernal, 1F);
        imgTestHalfFP = imgTestFlat.convertToFloatProcessor();
        imgRefHalfFP = imgRefFlat.convertToFloatProcessor();
        normInt = imgTestHalfFP.getMax();
        normInt = (double)1.0/normInt;
        imgTestHalfFP.multiply(normInt);
        normInt = imgRefHalfFP.getMax();
        normInt = (double)1.0/normInt;
        imgRefHalfFP.multiply(normInt);
        imgTestHalfFP.resetMinAndMax();
        imgRefHalfFP.resetMinAndMax();
        ThreshBool = ConvKernal.convolve(imgTestHalfFP, HalfKernal, PSFhalf, PSFhalf);
        ThreshBool = ConvKernal.convolve(imgRefHalfFP, HalfKernal, PSFhalf, PSFhalf);
        imgTestHalfFP.resetMinAndMax();
        imgRefHalfFP.resetMinAndMax();
        //IJ.log("Max of halfpsf theshold image : " + String.valueOf(imgTestHalfFP.getMax()) + "PSFhalf : " + String.valueOf(PSFhalf));


        scalecount = 0;
        for (int scalenum = 1; scalenum <= maxScale; scalenum++) {
            // Begin loop for each blur scale
            IJ.log("Analysing pixel scale : " + String.valueOf(scalenum));


            imgTestFP = imgTestFlat.convertToFloatProcessor();
            imgRefFP = imgRefFlat.convertToFloatProcessor();
            BlurKernal.blurGaussian(imgTestFP, (double)scalenum/2.355);
            BlurKernal.blurGaussian(imgRefFP, (double)scalenum/2.355);
            imgTestFP.resetMinAndMax();
            imgRefFP.resetMinAndMax();

            normInt=imgTestFP.getMax();
            normInt=(double)1.0/normInt;
            imgTestFP.multiply(normInt);
            normInt=imgRefFP.getMax();
            normInt=(double)1.0/normInt;
            imgRefFP.multiply(normInt);
            imgTestFP.resetMinAndMax();
            imgRefFP.resetMinAndMax();
            //IJ.log("Normalisation factor : " + String.valueOf(normInt) + " New max : " + String.valueOf(imgTestFP.getMax()));

            rConv = (int)(2*Math.ceil(Math.ceil(scalenum/2.0)))-1;
            ThreshKernal = new float[(int)rConv*rConv];
            Arrays.fill(ThreshKernal, 1F);
            //IJ.log("Thresh kernal size : " + String.valueOf(ThreshKernal.length));
            ConvKernal.setNormalize(true);
            imgTestThreshFP = imgTestFP.convertToFloatProcessor();
            imgRefThreshFP = imgRefFP.convertToFloatProcessor();
            ThreshBool = ConvKernal.convolve(imgTestThreshFP, ThreshKernal, rConv, rConv);
            ThreshBool = ConvKernal.convolve(imgRefThreshFP, ThreshKernal, rConv, rConv);
            imgTestThreshFP.resetMinAndMax();
            imgRefThreshFP.resetMinAndMax();


            stackOut.addSlice(imgTestFP.duplicate());
            stackOut.addSlice(imgRefFP.duplicate());


            // Loop over images, set pixel binary output if threshold exceeded.
            for (int row = 0; row < (SRpixelsY-1); row++) {
                for (int col = 0; col < (SRpixelsX-1); col++) {
                    normInt = (FWHMthr*imgTestThreshFP.getf(col, row)) + (FWHMsmo*imgTestHalfFP.getf(col, row))+FWHMoff;
                    if (imgTestFP.getf(col, row) > normInt) {
                        imgTestRes.set(col, row, 255);
                    }
                    else {
                        imgTestRes.set(col, row, 0);
                    }
                    normInt = (FWHMthr*imgRefThreshFP.getf(col, row)) + (FWHMsmo*imgRefHalfFP.getf(col, row))+FWHMoff;
                    if (imgRefFP.getf(col, row) > normInt) {
                        imgRefRes.set(col, row, 255);
                    }
                    else {
                        imgRefRes.set(col, row, 0);
                    }
                    normInt = (SKELthr*imgTestThreshFP.getf(col, row)) + (SKELsmo*imgTestHalfFP.getf(col, row))+SKELoff;
                    if (imgTestFP.getf(col, row) > normInt) {
                        imgTestSkel.set(col, row, 255);
                    }
                    else {
                        imgTestSkel.set(col, row, 0);
                    }
                    normInt = (SKELthr*imgRefThreshFP.getf(col, row)) + (SKELsmo*imgRefHalfFP.getf(col, row))+SKELoff;
                    if (imgRefFP.getf(col, row) > normInt) {
                        imgRefSkel.set(col, row, 255);
                    }
                    else {
                        imgRefSkel.set(col, row, 0);
                    }
                }
            }

            // Apply dialate and erode options, will tidy
            if (ADialErrode) {
                for (int icount = 1; icount <= (int)Math.ceil(scalenum/1); icount++) {
                    imgTestSkel.dilate();
                    imgTestSkel.erode();
                    imgTestRes.dilate();
                    imgTestRes.erode();
                    imgRefSkel.dilate();
                    imgRefSkel.erode();
                    imgRefRes.dilate();
                    imgRefRes.erode();
                }

            }
            if (BDialErrode) {
                for (int icount = 1; icount <= (int)Math.ceil(scalenum/2); icount++) {
                    imgRefSkel.dilate();
                    imgRefRes.dilate();
                    imgTestSkel.dilate();
                    imgTestRes.dilate();
                }
                for (int icount = 1; icount <= (int)Math.ceil(scalenum/2); icount++) {
                    //imgRefSkel.erode();
                    imgRefRes.erode();
                    imgTestRes.erode();
                    imgRefSkel.erode();
                    imgTestSkel.erode();
                }
                imgRefSkel.dilate();
                imgTestSkel.dilate();
            }

            // Get Res image correlation
            ResCorr[scalenum] = CoxLocCorrelator.CorrelatePatch(imgTestRes, imgRefRes);
            PlotX[scalenum] = scalenum;


            // Add Res image results to colour stack
            colRes = new ColorProcessor(SRpixelsX, SRpixelsY);
            colRes.setChannel(1, imgTestRes);
            colRes.setChannel(2, imgRefRes);
            colRes.setChannel(3,XORimages(imgTestRes,imgRefRes));
            stackRGBRes.addSlice(colRes);
            stackRGBRes.setSliceLabel("Corr coef = " + String.valueOf(ResCorr[scalenum]), scalenum+1);

            // Generate Skel image result
            colSkel = new ColorProcessor(SRpixelsX, SRpixelsY);
            TestPicBP = imgTestSkel.convertToByteProcessor();
            TestPicBP.invert();
            TestPicBP.skeletonize();
            TestPicBP.invert();
            colSkel.setChannel(1, TestPicBP);
            RefPicBP = imgRefSkel.convertToByteProcessor();
            RefPicBP.invert();
            RefPicBP.skeletonize();
            RefPicBP.invert();
            colSkel.setChannel(2, RefPicBP);
            colSkel.setChannel(3,XORimages(TestPicBP, RefPicBP));

            // Get correlatio of blured Skel images, add Skel results to stack
            imgTempA = TestPicBP.convertToFloatProcessor();
            imgTempB = RefPicBP.convertToFloatProcessor();
            BlurKernal.blurGaussian(imgTempA, (double)scalenum/2.355);
            BlurKernal.blurGaussian(imgTempB, (double)scalenum/2.355);
            imgTempA.resetMinAndMax();
            imgTempB.resetMinAndMax();
            SkelCorr[scalenum] = CoxLocCorrelator.CorrelatePatch(imgTempA, imgTempB);

            //blurr skel option
            if (BlurSkelFlag == true) {
                colSkel.setChannel(1,imgTempA.convertToByteProcessor());
                colSkel.setChannel(2,imgTempB.convertToByteProcessor());
                imgTempC = XORimages(TestPicBP,RefPicBP).convertToFloatProcessor();
                BlurKernal.blurGaussian(imgTempC, (double)scalenum/2.355);
                imgTempC.resetMinAndMax();
                colSkel.setChannel(3,imgTempC.convertToByteProcessor());
            }

            stackRGBSkel.addSlice(colSkel);
            stackRGBSkel.setSliceLabel("Corr coef = " + String.valueOf(SkelCorr[scalenum]),scalenum+1);
            stackOut.addSlice(imgTempA.duplicate());
            stackOut.setSliceLabel("Blured test image skeleton scale = " + String.valueOf(scalenum),(6*(scalenum-1))+2);
            stackOut.addSlice(imgTempB.duplicate());
            stackOut.setSliceLabel("Blured Ref image skeleton scale = " + String.valueOf(scalenum),(6*(scalenum-1))+3);
            stackOut.addSlice(CoxLocCorrelator.CorrelationMap(TestPicBP, RefPicBP, (int)Math.ceil(scalenum/1.0D)));
            stackOut.setSliceLabel("Correlation map of unblured skeletons scale = " + String.valueOf(scalenum),(6*(scalenum-1))+4);
            stackOut.addSlice(CoxLocCorrelator.CorrelationMap(imgTempA, imgTempB, (int)Math.ceil(scalenum/1.0D)));
            stackOut.setSliceLabel("Correlation map of blured skeletons scale = " + String.valueOf(scalenum),(6*(scalenum-1))+5);

            //Get quantity of super-res info for plot
            SRinfoA[scalenum] = getSRinfo(TestPicBP);
            SRinfoB[scalenum] = getSRinfo(RefPicBP);

            //Generate Confidence score from localcorrelation maps, color test image by confidence (try non-lin) add to stack
            FloatProcessor ConfMapRes = new FloatProcessor(SRpixelsX, SRpixelsY);
            FloatProcessor ConfMapSkel = new FloatProcessor(SRpixelsX,SRpixelsY);
            ConfMapSkel = CoxLocCorrelator.CorrelationMap(imgTempA, imgTempB, (int)Math.ceil(2.0*scalenum/2.0D));
            ConfMapRes = CoxLocCorrelator.CorrelationMap(imgTestRes, imgRefRes, (int)Math.ceil(2.0*scalenum/1.0D));
            stackOut.addSlice(ConfMapRes.duplicate());
            stackOut.setSliceLabel("Correlation map of binarised images scale = " + String.valueOf(scalenum),(6*(scalenum-1))+6);
            stackOut.addSlice(ConfMapSkel.duplicate());
            stackOut.setSliceLabel("Correlation mape of blured skeletons scale = " + String.valueOf(scalenum),(6*(scalenum-1))+7);

            for (int icount = 0; icount < SRpixelsX; icount++) {
                for (int jcount = 0; jcount < SRpixelsY; jcount++) {


                    //  set threshold for good correlation
                    tempDblA = (0.5D/0.85D)*ConfMapSkel.getf(icount,jcount);
                    if (tempDblA > 0.5D) tempDblA = 0.5D;
                    tempDblB = (0.5D/0.85D)*ConfMapRes.getf(icount,jcount);
                    if (tempDblB >  0.5D) tempDblB = 0.5D;
                    normInt = 127.0D * imgTestFP.getf(icount,jcount) * (1.0D - tempDblA - tempDblB);
                    normInt = normInt + (127.0D * imgRefFP.getf(icount,jcount) *  (1.0D - tempDblA - tempDblB));
                    if (normInt > 255.0D) normInt = 255.0D;
                    if (normInt < 0.0D) normInt = 0.0D;
                    TestPicBP.setf(icount, jcount, (int)normInt);
                    normInt = 127.0D * imgTestFP.getf(icount,jcount) * (0.0D + tempDblA + tempDblB);
                    normInt = normInt + (127.0D * imgRefFP.getf(icount,jcount) *  (0.0D + tempDblA + tempDblB));
                    if (normInt > 255.0D) normInt = 255.0D;
                    if (normInt < 0.0D) normInt = 0.0D;
                    RefPicBP.setf(icount, jcount, (int)normInt);
                }
            }
            colConf = new ColorProcessor(SRpixelsX, SRpixelsY);
            colConf.setChannel(1, TestPicBP);
            colConf.setChannel(2, RefPicBP);
            colConf.setChannel(3, RefPicBP);
            stackConf.addSlice(colConf);

            //Calcualte score for whole image
            tempDblA = (0.5D/0.85D)*SkelCorr[scalenum];
            if (tempDblA > 0.5D) tempDblA = 0.5D;
            tempDblB = (0.5D/0.85D)*ResCorr[scalenum];
            if (tempDblB >  0.5D) tempDblB = 0.5D;
            CorrScore[scalenum]=(float)(tempDblA+tempDblB);

            //IJ.log("Analysing pixel scale : " + String.valueOf(scalenum) + " sigma : " + String.valueOf((double)scalenum/2.355) + " Conv width : " + String.valueOf(rConv));
            scalecount = scalenum;

            IJ.showProgress(scalenum, maxScale);
            if (IJ.escapePressed()) {   // finish  processing if escape pressed
                IJ.log("HAWKMAN : Finishing early at scale level : " + String.valueOf(scalenum));
                break;
            }

        }// end main loop  over scalenum
        IJ.showStatus("HAWKMAN generating results");

        // Remove initial slice, output stack to image window
        stackOut.deleteSlice(1);
        stackRGBRes.deleteSlice(1);
        stackRGBSkel.deleteSlice(1);
        stackConf.deleteSlice(1);
        //stackOut.crop(PSFsize, PSFsize, 0, SRpixelsX, SRpixelsY, PSFsize-1);
        if (DiagnoseFlag) {
            ImagePlus imgOut = new ImagePlus("HAWKMAN test results", stackOut);
            imgOut.show();
        }
        ImagePlus imgRGBRes = new ImagePlus("HAWKMAN Sharpening map", stackRGBRes);
        imgRGBRes.show();
        ImagePlus imgRGBSkel = new ImagePlus("HAWKMAN Structure map", stackRGBSkel);
        imgRGBSkel.show();
        ImagePlus imgRGBConf = new ImagePlus("HAWKMAN Confidence map", stackConf);
        imgRGBConf.show();


        PlotX=Arrays.copyOfRange(PlotX, 0, scalecount+1);
        ResCorr = Arrays.copyOfRange(ResCorr, 0, scalecount+1);
        SkelCorr = Arrays.copyOfRange(SkelCorr, 0, scalecount+1);
        SRinfoA = Arrays.copyOfRange(SRinfoA, 0, scalecount+1);
        SRinfoB = Arrays.copyOfRange(SRinfoB, 0, scalecount+1);
        CorrScore = Arrays.copyOfRange(CorrScore, 0,scalecount+1);


        IJ.log("HAWKMAN : Analysis Compleated");
        IJ.showStatus("HAWKMAN done");
    }   //End main method


    static long getSRinfo (ImageProcessor SRimg) {         //function to measure SRinfo (count no pixels in Skel image)
        long imgCount;
        int pixelsX, pixelsY;

        pixelsX = SRimg.getWidth();
        pixelsY = SRimg.getHeight();
        imgCount = 0;

        for (int pX = 0; pX < pixelsX; pX++) {
            for (int pY = 0; pY < pixelsY; pY++) {
                if (SRimg.getf(pX,pY) > 0)
                    imgCount = imgCount+1;
            }
        }

        return imgCount;
    }


    FloatProcessor imgMultiplyer(ImageProcessor imgArgA, ImageProcessor imgArgB) {      //Function to multiply images
        FloatProcessor imgResult;
        int Xsize = imgArgA.getWidth();
        int Ysize = imgArgA.getHeight();
        imgResult = new FloatProcessor(Xsize, Ysize);

        for (int icount = 0; icount < Xsize; icount++) {
            for (int jcount = 0; jcount < Ysize; jcount++) {
                imgResult.setf(icount, jcount, (float)imgArgA.getf(icount, jcount) * imgArgB.getf(icount,jcount));
            }
        }

        return imgResult;
    }


    static FloatProcessor imgFlatten(ImageProcessor imgArg, int Bins, boolean showWin) {    //Function to flattern imput images to 2nd & 98th percentile
        FloatProcessor imgResult;
        double MinPixelVal, MaxPixelVal, PixelVal;
        double MaxBinValue, MinBinValue;
        int MaxBinNum = 0, MinBinNum = 0;
        int BinNum;
        long SumValue;
        int Xsize = imgArg.getWidth();
        int Ysize = imgArg.getHeight();
        int[] HistResult = new int[Bins+1];
        long[] CumSum = new long [Bins+1];
        imgResult = new FloatProcessor(Xsize,Ysize);

        imgResult = imgArg.convertToFloatProcessor();
        imgResult.resetMinAndMax();
        imgResult.findMinAndMax();
        MaxPixelVal = (double)imgResult.getMax();
        MinPixelVal = (double)imgResult.getMin();

        //Histogram pixel values
        for (int icount = 0; icount < Xsize; icount++) {
            for (int jcount = 0; jcount < Ysize; jcount++) {
                PixelVal = (double)imgResult.getf(icount, jcount);
                if (PixelVal > 0.0D) {
                    MaxBinValue = (PixelVal - MinPixelVal) / (MaxPixelVal - MinPixelVal);
                    BinNum = (int)Math.floor((double)Bins*MaxBinValue);
                    //IJ.log("BinNum = " + String.valueOf(BinNum) + " MaxBinVal = " + String.valueOf(MaxBinValue) + " " + String.valueOf(MaxPixelVal)+ " " + String.valueOf(MinPixelVal));
                    HistResult[BinNum]++;
                }
            }
        }
        //IJ.log("Histogram " + String.valueOf(HistResult));

        // Get Comulative sum of histogram and 98th percantile
        SumValue = 0;
        for (int icount = 0; icount < Bins; icount++) {
            SumValue = SumValue + (long)HistResult[icount];
            CumSum[icount] = SumValue;
        }
        for (int icount = 0; icount < Bins; icount++) {
            if (CumSum[icount] < (0.98*CumSum[Bins-1])) {
                MaxBinNum = icount;
            }
            if (CumSum[icount] < (0.02*CumSum[Bins-1])) {
                MinBinNum = icount;
            }
        }

        //Cap image values at below max percentile
        IJ.log("MaxBinNum = " + String.valueOf(MaxBinNum) + "  MaxPixelVal = " + String.valueOf(MaxPixelVal));
        MaxBinValue = MaxPixelVal * (double)MaxBinNum / ((double)Bins);
        imgResult.max(MaxBinValue);
        IJ.log("MaxBinValue = " + String.valueOf(MaxBinValue));
        //imgResult = imgArg.convertToFloatProcessor();


        if (showWin) {
            ImagePlus DiagImg = new ImagePlus("Flattened image", imgResult.convertToFloatProcessor());

            DiagImg.show();  //Only for diagnostic so removing may prevent issue
        }

        return imgResult;
    }


    static ByteProcessor XORimages(ByteProcessor BinArgA, ByteProcessor BinArgB) {     //Function  to XOR images,  ImageJ imgCalculator  givesunstable results!
    ByteProcessor BinResult;
    int Xsize = BinArgA.getWidth();
    int Ysize = BinArgA.getHeight();
    BinResult = new ByteProcessor(Xsize, Ysize);

    for (int icount = 0; icount < Xsize; icount++) {
            for (int jcount = 0; jcount < Ysize; jcount++) {
                BinResult.set(icount, jcount, BinArgA.getPixel(icount, jcount) ^ BinArgB.getPixel(icount,jcount));
            }
        }
    return BinResult;
    }

    public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = HAWKMAN.class;
        java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        java.io.File file = new java.io.File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        ImagePlus reference = IJ.openImage("C:\\Users\\k1651658\\Documents\\support\\images\\LMC-MT-ME-Raw16.tif");
        ImagePlus test = IJ.openImage("C:\\Users\\k1651658\\Documents\\support\\images\\LMC-MT-ME-HAWK16.tif");
        reference.show();
        test.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }

}   // End Main class



