package com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.FullImageFitting;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IEstimator;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.LSQFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MLEFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MLEFitterLM;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.MultipleLocationsImageFitting;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.IOneLocationFitter;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.IntegratedSymmetricGaussianPSF;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel.Params;

public class IntSymmetricGaussianEstimatorUI extends SymmetricGaussianEstimatorUI {

    public IntSymmetricGaussianEstimatorUI() {
        super();
        name = "PSF: Integrated Gaussian";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IEstimator getImplementation() {
        method = METHOD.getValue();
        initialSigma = SIGMA.getValue();
        fittingRadius = FITRAD.getValue();
        fullImageFitting = FULL_IMAGE_FITTING.getValue();
        IntegratedSymmetricGaussianPSF psf = new IntegratedSymmetricGaussianPSF(initialSigma);
        IOneLocationFitter fitter;
        if(LSQ.equals(method) || WLSQ.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getLSQImplementation(psf, initialSigma);
            } else {
                fitter = new LSQFitter(psf, WLSQ.equals(method), Params.BACKGROUND);
            }
        } else if(MLE.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getMLEImplementation(psf, initialSigma);
            } else {
                fitter = new MLEFitter(psf, Params.BACKGROUND);

            }
        } else if(MLE_FAST.equals(method)) {
            if(crowdedField.isEnabled()) {
                // MLEFitterLM isn't wired into multi-emitter model selection (MFA_*Fitter) yet;
                // crowded-field mode keeps using the Nelder-Mead MLEFitter regardless of this choice.
                fitter = crowdedField.getMLEImplementation(psf, initialSigma);
            } else {
                fitter = new MLEFitterLM(psf, Params.BACKGROUND);
            }
        } else {
            throw new IllegalArgumentException("Unknown fitting method: " + method);
        }
        if(fullImageFitting) {
            return new FullImageFitting(fitter);
        } else {
            return new MultipleLocationsImageFitting(fittingRadius, fitter);
        }

    }
}
