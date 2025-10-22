package com.coxphysics.terrapins.models.hawkman;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class CoxGroupLocalCorr implements PlugIn{
    private boolean WholeFrame = false;
    
    @Override
    public void run(String args) {
        String imgAName, imgBName;
        ImagePlus imgAPlus, imgBPlus;
        ImageProcessor imgAIP, imgBIP;
        ImagePlus CorrMAP;
        int PatchSize = 5;
        
                
        IJ.log("HAWKMAN : Image Correlation Map");
        
        LocalCorrDialog CMAPGD = new LocalCorrDialog(); 
        CMAPGD.showDialog();

	if(!CMAPGD.wasOKed())
            return;
        
        imgAName = CMAPGD.getNextChoice();
        imgBName = CMAPGD.getNextChoice();
        PatchSize = (int)CMAPGD.getNextNumber();
        WholeFrame = CMAPGD.getNextBoolean();

        imgAPlus = ij.WindowManager.getImage(imgAName);
        imgBPlus = ij.WindowManager.getImage(imgBName);
        imgAIP = imgAPlus.getProcessor();
        imgBIP = imgBPlus.getProcessor();
        
        if (WholeFrame) {
            IJ.showMessage("Image correlation", "Correlation coefficent  = " + String.valueOf(CorrelatePatch(imgAIP,imgBIP)));
        }
        else
        {
            CorrMAP = new ImagePlus("Correlation Map",CorrelationMap(imgAIP,imgBIP,PatchSize));
            CorrMAP.show();
        }
    }

    static FloatProcessor CorrelationMap(ImageProcessor CorrImgA, ImageProcessor CorrImgB, int CorrRange) {
        int pixelsX, pixelsY;
        FloatProcessor MAP;
        ImageProcessor PaddedA,PaddedB,PatchA,PatchB;
        
        pixelsX = CorrImgA.getWidth();
        pixelsY = CorrImgA.getHeight();
        MAP = new FloatProcessor(pixelsX, pixelsY);
        PaddedA = new FloatProcessor(pixelsX+(2*CorrRange),pixelsY+(2*CorrRange));
        PaddedB = new FloatProcessor(pixelsX+(2*CorrRange),pixelsY+(2*CorrRange));
        PatchA = new FloatProcessor(CorrRange,CorrRange);
        PatchB = new FloatProcessor(CorrRange,CorrRange);
        //IJ.log("size before paste : " + String.valueOf(PaddedA.getWidth()));
        
        for (int Xcount = 0; Xcount < pixelsX; Xcount++) {
            for (int Ycount = 0; Ycount < pixelsY; Ycount++) {
                PaddedA.setf(Xcount+CorrRange, Ycount+CorrRange, CorrImgA.getf(Xcount, Ycount));
                PaddedB.setf(Xcount+CorrRange, Ycount+CorrRange, CorrImgB.getf(Xcount, Ycount));
            }
        }
       
        //IJ.log("size affter paste : " + String.valueOf(PaddedA.getWidth()));
        for (int Xcount = 0; Xcount < pixelsX; Xcount++) {
            for (int Ycount = 0; Ycount < pixelsY; Ycount++) {
                //PaddedA.setRoi(Xcount+CorrRange,Ycount+CorrRange,CorrRange,CorrRange);
                //PaddedB.setRoi(Xcount+CorrRange,Ycount+CorrRange,CorrRange,CorrRange);
                PaddedA.setRoi(Xcount,Ycount,(2*CorrRange)+1,(2*CorrRange)+1);
                PaddedB.setRoi(Xcount,Ycount,(2*CorrRange)+1,(2*CorrRange)+1);
                PatchA = PaddedA.crop();
                PatchB = PaddedB.crop();
                MAP.setf(Xcount, Ycount, CorrelatePatch(PatchA,PatchB));
            }
            //IJ.log("X = " + String.valueOf(Xcount));
        }
        MAP.resetMinAndMax();
        return MAP;
    }


    static float CorrelatePatch (ImageProcessor picA, ImageProcessor picB)
    {
        float CorrCoef = 0;
        int Xsize, Ysize;
        float meanA, meanB, varA, varB, valA, valB;
        
        Xsize = picA.getWidth();
        Ysize = picA.getHeight();
        meanA = 0;
        meanB = 0;
        varA = 0;
        varB = 0;
        //IJ.log("size of patch " + String.valueOf(Xsize));
        for (int pX = 1; pX <= Xsize; pX++) {
            for (int pY = 1; pY <= Ysize; pY++) {
                meanA = meanA + picA.getf(pX-1,pY-1);
                meanB = meanB + picB.getf(pX-1,pY-1);
            }
        }
        meanA = meanA / (Xsize*Ysize);
        meanB = meanB / (Xsize*Ysize);
        //IJ.log("means " + String.valueOf(meanA) + " & " + String.valueOf(meanB));
        for (int pX = 0; pX < Xsize; pX++) {
            for (int pY = 0; pY < Ysize; pY++) {
                valA = (picA.getf(pX,pY)-meanA);
                valB = (picB.getf(pX,pY)-meanB);
                CorrCoef = CorrCoef + (valA * valB);
                varA = varA + (valA*valA);
                varB = varB + (valB*valB);
            }
        }

        if (varA==0 || varB==0)
            return 0;

        if (meanA==0 || meanB==0)
            return 0;

        return CorrCoef / ((float)Math.sqrt(varA*varB));
    }
}

class LocalCorrDialog extends GenericDialog {
    
    //Main constructor for generic dialog
    LocalCorrDialog() {
        super ("Local Correlation Map");
        String[] imgWinList;
        
        imgWinList = ij.WindowManager.getImageTitles();
        addChoice("First image", imgWinList,imgWinList[1]);
        addChoice("Second image", imgWinList, imgWinList[1]);
        addNumericField("Pixel range", 5, 0);
        addCheckbox("Whole image", false);
    }
}