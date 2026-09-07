package com.coxphysics.terrapins.vendored.thunderstorm.filters.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.IModuleUI;
import com.coxphysics.terrapins.vendored.thunderstorm.filters.IFilter;

public abstract class IFilterUI extends IModuleUI<IFilter> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".filters";
    }
    //
    
}
