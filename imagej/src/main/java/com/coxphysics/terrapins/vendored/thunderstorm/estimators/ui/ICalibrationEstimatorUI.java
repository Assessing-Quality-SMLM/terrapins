package com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.calibration.DefocusFunction;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IEstimator;

public interface ICalibrationEstimatorUI {

    IEstimator getThreadLocalImplementation();
    void resetThreadLocal();

    int getFitradius();
    void setAngle(double angle);
    void setDefocusModel(DefocusFunction defocusModel);
}
