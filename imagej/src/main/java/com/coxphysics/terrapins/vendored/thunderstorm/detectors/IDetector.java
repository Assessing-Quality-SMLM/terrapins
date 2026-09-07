package com.coxphysics.terrapins.vendored.thunderstorm.detectors;

import com.coxphysics.terrapins.vendored.thunderstorm.FormulaParser.FormulaParserException;
import com.coxphysics.terrapins.vendored.thunderstorm.IModule;
import com.coxphysics.terrapins.vendored.thunderstorm.UI.StoppedByUserException;
import com.coxphysics.terrapins.vendored.thunderstorm.util.Point;
import ij.process.FloatProcessor;

import java.util.List;
import java.util.Vector;

/**
 * The interface every detector has to implement.
 */
public interface IDetector extends IModule{
    /**
     * Detect molecules in {@code image} and return list of their X,Y positions.
     * All detectors should support detection in ROI (Region Of Interest).
     * If the ROI is non-rectangle shape, then mask is not null and contains
     * an input image masked by the ROI.
     *
     * @param image an input (filtered) image
     * 
     * @see Point
     */
    public List<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException, StoppedByUserException;
    
    public String getThresholdFormula();
    
    public float getThresholdValue();
}
