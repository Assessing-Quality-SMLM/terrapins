package com.coxphysics.terrapins.vendored.thunderstorm.estimators;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.Molecule;

public interface IOneLocationBiplaneFitter {
    Molecule fit(SubImage plane1, SubImage plane2);
}
