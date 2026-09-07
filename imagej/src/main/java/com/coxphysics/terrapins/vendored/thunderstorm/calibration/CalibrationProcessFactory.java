package com.coxphysics.terrapins.vendored.thunderstorm.calibration;

import com.coxphysics.terrapins.vendored.thunderstorm.detectors.ui.IDetectorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.AstigmaticBiplaneCalibrationEstimatorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.AstigmatismCalibrationEstimatorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.BiplaneCalibrationEstimatorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.ICalibrationEstimatorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.filters.ui.IFilterUI;
import ij.ImagePlus;
import ij.gui.Roi;

public final class CalibrationProcessFactory {

    public static AbstractCalibrationProcess create(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, ICalibrationEstimatorUI calibrationEstimatorUI,
                                                   DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp1, ImagePlus imp2, Roi roi1, Roi roi2) {
        if (calibrationEstimatorUI instanceof BiplaneCalibrationEstimatorUI) {
            return new BiplaneCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (BiplaneCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2);
        } else if (calibrationEstimatorUI instanceof AstigmaticBiplaneCalibrationEstimatorUI) {
            return new AstigmaticBiplaneCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (AstigmaticBiplaneCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2);
        } else {
            throw new IllegalArgumentException("Unknown instance of astigmatic calibration estimator!");
        }
    }

    public static AbstractCalibrationProcess create(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, ICalibrationEstimatorUI calibrationEstimatorUI,
                                                      DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp, Roi roi) {
        if (calibrationEstimatorUI instanceof AstigmatismCalibrationEstimatorUI) {
            return new AstigmaticCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (AstigmatismCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp, roi);
        } else {
            throw new IllegalArgumentException("Unknown instance of astigmatic calibration estimator!");
        }
    }
}
