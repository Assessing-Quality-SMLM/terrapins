package com.coxphysics.terrapins.vendored.thunderstorm.calibration;

import com.coxphysics.terrapins.vendored.thunderstorm.detectors.ui.IDetectorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.AstigmatismCalibrationEstimatorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.filters.ui.IFilterUI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

public class AstigmaticCalibrationProcess extends AbstractCalibrationProcess {

    // processing
    ImagePlus imp;
    Roi roi;

    // results
    private PSFSeparator beadFits;

    public AstigmaticCalibrationProcess(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI,
                                        AstigmatismCalibrationEstimatorUI calibrationEstimatorUI, DefocusFunction defocusModel,
                                        double stageStep, double zRangeLimit, ImagePlus imp, Roi roi) {
        super(config, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit);
        this.imp = imp;
        this.roi = roi;
    }

    @Override
    public void runCalibration() {
        angle = estimateAngle(imp, roi);
        IJ.log("angle = " + angle);

        beadFits = fitFixedAngle(angle, imp, roi, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, config.showResultsTable);
        fitQuadraticPolynomials(beadFits.getPositions());
        IJ.log("s1 = " + polynomS1Final.toString());
        IJ.log("s2 = " + polynomS2Final.toString());
    }

    public DefocusCalibration getCalibration(DefocusFunction defocusModel) {
        return defocusModel.getCalibration(angle, null, polynomS1Final, polynomS2Final);
    }

    @Override
    public void drawOverlay() {
        drawOverlay(imp, roi, beadFits.getAllFits(), usedPositions);
    }
}
