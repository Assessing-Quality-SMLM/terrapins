package com.coxphysics.terrapins.vendored.thunderstorm.estimators;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.Molecule;

public interface IOneLocationFitter {
    Molecule fit(SubImage img);
}
