package com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.IModuleUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IEstimator;

public abstract class IEstimatorUI extends IModuleUI<IEstimator> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".estimators";
    }
}
