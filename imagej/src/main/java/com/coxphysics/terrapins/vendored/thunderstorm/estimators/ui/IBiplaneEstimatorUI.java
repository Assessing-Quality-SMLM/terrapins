package com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.IModuleUI;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IBiplaneEstimator;

public abstract class IBiplaneEstimatorUI extends IModuleUI<IBiplaneEstimator> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".estimators.biplane";
    }
}
