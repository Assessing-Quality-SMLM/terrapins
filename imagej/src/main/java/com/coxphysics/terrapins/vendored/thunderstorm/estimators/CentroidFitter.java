package com.coxphysics.terrapins.vendored.thunderstorm.estimators;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.Molecule;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel;

public class CentroidFitter implements IOneLocationFitter {

    @Override
    public Molecule fit(SubImage img) {

        double[] coordinates = new double[]{img.detectorX, img.detectorY};
        double sum = 0;
        for(int i = 0; i < img.values.length; i++) {
            sum += img.values[i];
            coordinates[0] += img.values[i] * img.xgrid[i];
            coordinates[1] += img.values[i] * img.ygrid[i];
        }
        coordinates[0] = coordinates[0] / sum;
        coordinates[1] = coordinates[1] / sum;

        return new Molecule(new PSFModel.Params(new int[]{PSFModel.Params.X, PSFModel.Params.Y}, coordinates, false));
    }
}
