package com.coxphysics.terrapins.vendored.thunderstorm.detectors;

import com.coxphysics.terrapins.vendored.thunderstorm.FormulaParser.FormulaParserException;
import com.coxphysics.terrapins.vendored.thunderstorm.UI.StoppedByUserException;
import com.coxphysics.terrapins.vendored.thunderstorm.detectors.ui.IDetectorUI;
import com.coxphysics.terrapins.vendored.thunderstorm.util.Point;
import ij.process.FloatProcessor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is actually never used. The only purpose is for MeasurementProtocol, when no protocol is loaded.
 */
public class EmptyDetector extends IDetectorUI implements IDetector {

    private final String name = "No detector";

    @Override
    public List<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException, StoppedByUserException {
        return new ArrayList<Point>();
    }

    @Override
    public String getThresholdFormula() {
        return "";
    }

    @Override
    public float getThresholdValue() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        return new JPanel();
    }

    @Override
    public IDetector getImplementation() {
        return null;
    }
}
