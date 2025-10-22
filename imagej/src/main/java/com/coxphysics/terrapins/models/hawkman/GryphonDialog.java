package com.coxphysics.terrapins.models.hawkman;


import com.coxphysics.terrapins.views.psf.PSFDialog;
import ij.gui.GenericDialog;

import java.awt.event.ItemEvent;


// Main Dialog box for Gryphon
class GryphonDialog extends GenericDialog {
    double FWHMthreshold, FWHMsmooth, FWHMoffset;
    double SKELthreshold, SKELsmooth, SKELoffset;
    boolean ADelAndErrode, BDelAndErrode, DiagnosticWin, FlattenImage, BlurSkeleton;
    double calculatedPSF;
    GryphonSettingsDialog SettingsGD;
    PSFDialog PSFcalcGD;

    //Main constructor for generic dialog
    GryphonDialog() {
        super ("HAWKMAN");
        String[] imgWinList;

        calculatedPSF = 0.0;
        addNumericField("PSF FWHM in super-res pixels", 13, 0);
        addNumericField("No. of scales to analyse", 10,0);
        imgWinList = ij.WindowManager.getImageTitles();
        addChoice("Test image (No HAWK)", imgWinList,imgWinList[1]);
        addChoice("Reference image (with HAWK)", imgWinList, imgWinList[1]);
        addCheckbox("Show settings",false);
        addCheckbox("Calc PSF size",false);
        SettingsGD = new GryphonSettingsDialog();
        PSFcalcGD = PSFDialog.create();
    }


    @Override  // Listen for settings checkbox
    public void itemStateChanged(ItemEvent e) {
        //IJ.log(e.paramString());
        //IJ.log(e.getItem().toString());
        //IJ.log(String.valueOf(e.getStateChange()));
        if ((e.getItem().toString() == "Show settings") && (e.getStateChange() == ItemEvent.SELECTED)) {
            //SettingsGD = new GryphonSettingsDialog();
            SettingsGD.doLayout();
            SettingsGD.showDialog();
            if (SettingsGD.wasOKed()) {
                //getParamsFromGD();
            }
        }
        if ((e.getItem().toString() == "Calc PSF size") && (e.getStateChange() == ItemEvent.SELECTED)) {
            PSFcalcGD.showDialog();
            if (PSFcalcGD.wasOKed()) {
                //update numeric field in main dialogue with calulated value
                getCalculatedPSF();
            }
            else {
                calculatedPSF = 0.0;
            }
        }
        if ((e.getItem().toString() == "Show settings") && (e.getStateChange() == ItemEvent.DESELECTED)) {
            SettingsGD.dispose();
            SettingsGD = null;
            SettingsGD = new GryphonSettingsDialog();
        }
        if ((e.getItem().toString() == "Calc PSF size") && (e.getStateChange() == ItemEvent.DESELECTED)) {
            calculatedPSF = 0.0;
            PSFcalcGD.dispose();
            PSFcalcGD = null;
            PSFcalcGD = PSFDialog.create();
        }
    }

    //Pass parameters from settings Dialog
    void getParamsFromGD() {
        this.FWHMthreshold = SettingsGD.getNextNumber();
        this.FWHMsmooth = SettingsGD.getNextNumber();
        this.FWHMoffset = SettingsGD.getNextNumber();
        this.SKELthreshold = SettingsGD.getNextNumber();
        this.SKELsmooth = SettingsGD.getNextNumber();
        this.SKELoffset = SettingsGD.getNextNumber();
        //this.ADelAndErrode = SettingsGD.getNextBoolean();
        this.BDelAndErrode = SettingsGD.getNextBoolean();
        //this.DiagnosticWin = SettingsGD.getNextBoolean();
        this.ADelAndErrode = false;
        //this.BDelAndErrode = false;
        this.DiagnosticWin = false;
        this.FlattenImage = SettingsGD.getNextBoolean();
        this.BlurSkeleton = SettingsGD.getNextBoolean();
    }

    //Pass calulated value of PSF size in SRpixels
    void getCalculatedPSF() {
        double instPSFnm = PSFcalcGD.getNextNumber();
        double camPixnm = PSFcalcGD.getNextNumber();
        double SRmag = PSFcalcGD.getNextNumber();
        calculatedPSF = (instPSFnm * SRmag / camPixnm);
    }
}


// Settings Dialog Box
class GryphonSettingsDialog extends GenericDialog {

    //Main constructor for advanced settings dialog
    GryphonSettingsDialog() {
        super ("HAWKMAN : Settings");

        addNumericField("FWHM_threshold",0.7,3);
        addNumericField("FWHM_smoothing threshold",0.1,3);
        addNumericField("FWHM_offset",0.04,3);
        addNumericField("Skel_threshold",0.85,3);
        addNumericField("Skel_smoothing threshold",0.1,3);
        addNumericField("Skel_offset",0.02,3);
        //addCheckbox("Dilate_&_errode_mathod_A",false);
        //addCheckbox("Dilate_&_errode_method_B", false);
        addCheckbox("Dilate_&_erode?", false);
        //addCheckbox("Show_diagnostic_windows?", false);
        addCheckbox("Flatten_images?", true);
        addCheckbox("Show_blured_skeleton?", true);
    }
}
