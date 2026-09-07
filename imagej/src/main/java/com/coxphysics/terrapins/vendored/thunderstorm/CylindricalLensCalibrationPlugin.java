package com.coxphysics.terrapins.vendored.thunderstorm;

import com.coxphysics.terrapins.vendored.thunderstorm.UI.AstigmatismCalibrationDialog;
import com.coxphysics.terrapins.vendored.thunderstorm.UI.GUI;
import com.coxphysics.terrapins.vendored.thunderstorm.calibration.*;
import com.coxphysics.terrapins.vendored.thunderstorm.detectors.ui.IDetectorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.AstigmatismCalibrationEstimatorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.IEstimatorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.filters.ui.IFilterUI;
import com.coxphysics.terrapins.vendored.thunderstorm.thresholding.Thresholder;
import com.coxphysics.terrapins.vendored.thunderstorm.util.UI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CylindricalLensCalibrationPlugin implements PlugIn {

    DefocusFunction defocusModel;
    IFilterUI selectedFilterUI;
    IDetectorUI selectedDetectorUI;
    AstigmatismCalibrationEstimatorUI calibrationEstimatorUI;
    String savePath;
    double stageStep;
    double zRangeLimit;//in nm
    ImagePlus imp;
    Roi roi;

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        //
        imp = IJ.getImage();
        if(imp == null) {
            IJ.error("No image open.");
            return;
        }
        if(imp.getImageStackSize() < 2) {
            IJ.error("Requires a stack.");
            return;
        }
        try {
            //load modules
            calibrationEstimatorUI = new AstigmatismCalibrationEstimatorUI();
            List<IFilterUI> filters = ModuleLoader.getUIModules(IFilterUI.class);
            List<IDetectorUI> detectors = ModuleLoader.getUIModules(IDetectorUI.class);
            List<IEstimatorUI> estimators = Arrays.asList(new IEstimatorUI[]{calibrationEstimatorUI}); // only one estimator can be used
            List<DefocusFunction> defocusFunctions = ModuleLoader.getUIModules(DefocusFunction.class);
            Thresholder.loadFilters(filters);

            // get user options
            try {
                GUI.setLookAndFeel();
            } catch(Exception e) {
                IJ.handleException(e);
            }
            AstigmatismCalibrationDialog dialog;
            dialog = new AstigmatismCalibrationDialog(imp, filters, detectors, estimators, defocusFunctions);
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return;
            }
            selectedFilterUI = dialog.getActiveFilterUI();
            selectedDetectorUI = dialog.getActiveDetectorUI();
            savePath = dialog.getSavePath();
            stageStep = dialog.getStageStep();
            zRangeLimit = dialog.getZRangeLimit();
            defocusModel = dialog.getActiveDefocusFunction();

            roi = imp.getRoi() != null ? imp.getRoi() : new Roi(0, 0, imp.getWidth(), imp.getHeight());

            // perform the calibration
            final AstigmaticCalibrationProcess process = (AstigmaticCalibrationProcess) CalibrationProcessFactory.create(
                    new CalibrationConfig(),
                    selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI,
                    defocusModel, stageStep, zRangeLimit, imp, roi);

            try {
                process.runCalibration();
            } catch(NoMoleculesFittedException ex) {
                // if no beads were succesfully fitted, draw localizations anyway
                process.drawOverlay();
                IJ.handleException(ex);
                return;
            }
            process.drawOverlay();
            process.drawSigmaPlots();

            try {
                process.getCalibration(defocusModel).saveToFile(savePath);
            } catch(IOException ex) {
                UI.showAnotherLocationDialog(ex, process.getCalibration(defocusModel));
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }
}
