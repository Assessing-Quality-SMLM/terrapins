package com.coxphysics.terrapins.vendored.thunderstorm.detectors.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.IModuleUI;
import com.coxphysics.terrapins.vendored.thunderstorm.detectors.IDetector;

public abstract class IDetectorUI extends IModuleUI<IDetector> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".detectors";
    }
    //
    
}
