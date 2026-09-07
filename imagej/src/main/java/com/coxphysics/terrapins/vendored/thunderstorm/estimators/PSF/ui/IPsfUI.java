package com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.IModuleUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel;
import static com.coxphysics.terrapins.vendored.thunderstorm.util.MathProxy.sqrt;
import com.coxphysics.terrapins.vendored.thunderstorm.util.Range;
import static org.apache.commons.math3.util.FastMath.log;

public abstract class IPsfUI extends IModuleUI<PSFModel> {
    
    public static final double FWHM_FACTOR = 2*sqrt(2*log(2));
    
    public static double fwhm2sigma(double fwhm) {
        return fwhm / FWHM_FACTOR;
    }
    
    public static double sigma2fwhm(double sigma) {
        return FWHM_FACTOR * sigma;
    }
    
    @Override
    protected String getPreferencesPrefix() {
        return "terrapins.thunderstorm.datagen.psf";
    }
    
    abstract public double getAngle();
    abstract public Range getZRange();
    abstract public double getSigma1(double z);
    abstract public double getSigma2(double z);
    abstract public boolean is3D();
    
}
