package com.coxphysics.terrapins.vendored.thunderstorm.estimators;

import com.coxphysics.terrapins.vendored.thunderstorm.UI.StoppedByUserException;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.Molecule;
import com.coxphysics.terrapins.vendored.thunderstorm.util.Point;
import ij.process.FloatProcessor;

import java.util.List;

public class CylindricalLensZEstimator implements IEstimator {

    IEstimator estimator;

    public CylindricalLensZEstimator(IEstimator estimator) {
        this.estimator = estimator;
    }

    @Override
    public List<Molecule> estimateParameters(FloatProcessor image, List<Point> detections) throws StoppedByUserException {
        return estimator.estimateParameters(image, detections);
    }
}
