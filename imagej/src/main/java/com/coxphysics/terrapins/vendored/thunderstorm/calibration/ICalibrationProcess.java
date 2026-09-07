package com.coxphysics.terrapins.vendored.thunderstorm.calibration;

public interface ICalibrationProcess {
    void runCalibration();
    DefocusCalibration getCalibration(DefocusFunction defocusModel);
    void drawOverlay();
    void drawSigmaPlots();
}
