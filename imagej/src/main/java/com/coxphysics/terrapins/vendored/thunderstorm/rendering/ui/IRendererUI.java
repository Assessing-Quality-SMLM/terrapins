package com.coxphysics.terrapins.vendored.thunderstorm.rendering.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.IModuleUI;
import com.coxphysics.terrapins.vendored.thunderstorm.rendering.IncrementalRenderingMethod;

public abstract class IRendererUI extends IModuleUI<IncrementalRenderingMethod> {

    public abstract void setSize(double sizeX, double sizeY);
    public abstract void setSize(double left, double top, double sizeX, double sizeY);
    public abstract void setZRange(double from, double to);
    public abstract void set3D(boolean checked);

    public abstract int getRepaintFrequency();

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".rendering";
    }
}
