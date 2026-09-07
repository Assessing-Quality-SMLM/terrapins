package com.coxphysics.terrapins.vendored.thunderstorm.estimators;

import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.Molecule;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.MoleculeDescriptor;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.PSF.PSFModel.Params;
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.optimizers.LevenbergMarquardtMLE;
import com.coxphysics.terrapins.vendored.thunderstorm.util.VectorMath;

import static com.coxphysics.terrapins.vendored.thunderstorm.util.VectorMath.sub;

/**
 * Drop-in alternative to {@link MLEFitter}: fits the same Poisson maximum-likelihood objective,
 * on the same PSF models, but drives the search with {@link LevenbergMarquardtMLE} (analytic
 * Jacobian + Fisher scoring) instead of a derivative-free Nelder-Mead simplex. Kept side by side
 * with MLEFitter so the two can be benchmarked against each other directly.
 *
 * A single Fisher-scoring run is a local search, and at low photon counts the Poisson-Gaussian
 * likelihood can have a second, inferior local optimum (a wide, flat, low-amplitude Gaussian that
 * mimics the true compact peak + background) which a single run can converge to cleanly, with zero
 * gradient, well short of the real maximum. Reaching that basin from a reasonable initial guess
 * means travelling a long way in parameter space to get there -- in practice, several times further
 * than a normal, well-behaved fit ever moves from its starting guess. So rather than always paying
 * for extra starting points (which would give away most of the speed advantage over Nelder-Mead on
 * the common, well-behaved case), this runs once from the detector's initial guess and only spends
 * a handful of additional restarts -- offset from that guess along PSFModel's own initial-simplex
 * step scale -- when the first run's result is itself the telltale sign of trouble: having strayed
 * unusually far from where it started. Whichever run reaches the highest likelihood wins.
 */
public class MLEFitterLM implements IOneLocationFitter, IOneLocationBiplaneFitter {

    public final static int MAX_ITERATIONS = 200;
    public final static double GRADIENT_TOLERANCE = 1e-6;
    // A fit landing further than this from its starting guess (in units of
    // PSFModel#getInitialSimplex, Euclidean norm over all parameters) is treated as suspect and
    // triggers extra restarts; a well-conditioned fit typically moves only a couple of step units.
    public final static double SUSPICIOUS_DISTANCE = 5.0;
    // Multipliers (in units of PSFModel#getInitialSimplex) applied to the detector's initial
    // guess to build the extra restarts tried when the first run looks suspect.
    public final static double[] EXTRA_RESTART_OFFSETS = {3.0, -3.0, 8.0, -8.0};

    public double[] fittedParameters;
    public PSFModel psfModel;
    public LevenbergMarquardtMLE optimizer;

    private final int maxIter;

    public MLEFitterLM(PSFModel psfModel) {
        this(psfModel, MAX_ITERATIONS, -1);
    }

    public MLEFitterLM(PSFModel psfModel, int bkgStdIndex) {
        this(psfModel, MAX_ITERATIONS, bkgStdIndex);
    }

    public MLEFitterLM(PSFModel psfModel, int maxIter, int bkgStdIndex) {
        this.psfModel = psfModel;
        this.maxIter = maxIter;
        this.fittedParameters = null;
    }

    @Override
    public Molecule fit(SubImage subimage) {
        subimage.convertTo(MoleculeDescriptor.Units.PHOTON);
        return fit(new LsqMleSinglePlaneFunctions(psfModel, subimage));
    }

    @Override
    public Molecule fit(SubImage plane1, SubImage plane2) {
        plane1.convertTo(MoleculeDescriptor.Units.PHOTON);
        plane2.convertTo(MoleculeDescriptor.Units.PHOTON);
        return fit(new LsqMleBiplaneFunctions(psfModel, plane1, plane2));
    }

    public Molecule fit(ILsqFunctions functions) {
        // init
        double[] observations = functions.getObservations();
        double[] baseStart = psfModel.transformParametersInverse(functions.getInitialParams());
        double[] step = psfModel.getInitialSimplex();

        // fit: single run first; only pay for extra restarts if that result looks suspect (see
        // class docs). Whichever run reaches the highest likelihood wins.
        optimizer = runFrom(functions, observations, baseStart);
        if(scaledDistance(optimizer.xmin, baseStart, step) > SUSPICIOUS_DISTANCE) {
            for(double offset : EXTRA_RESTART_OFFSETS) {
                double[] start = new double[baseStart.length];
                for(int j = 0; j < start.length; j++) {
                    start[j] = baseStart[j] + offset * step[j];
                }

                LevenbergMarquardtMLE candidate = runFrom(functions, observations, start);
                if(candidate.logLikelihood > optimizer.logLikelihood) {
                    optimizer = candidate;
                }
            }
        }
        fittedParameters = optimizer.xmin;

        // estimate background and return an instance of the `Molecule`
        fittedParameters[Params.BACKGROUND] = VectorMath.stddev(sub(observations, functions.getValueFunction().value(fittedParameters)));

        Molecule mol = psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), functions.getImageUnits(), true);

        if(mol.isSingleMolecule()) {
            convertMoleculeToDigitalUnits(mol);
        } else {
            for(Molecule detection : mol.getDetections()) {
                convertMoleculeToDigitalUnits(detection);
            }
        }
        return mol;
    }

    private LevenbergMarquardtMLE runFrom(ILsqFunctions functions, double[] observations, double[] start) {
        LevenbergMarquardtMLE optimizer = new LevenbergMarquardtMLE();
        optimizer.optimize(
                functions.getValueFunction(),
                functions.getJacobianFunction(),
                observations,
                start,
                GRADIENT_TOLERANCE,
                maxIter);
        return optimizer;
    }

    private static double scaledDistance(double[] point, double[] from, double[] scale) {
        double distSq = 0;
        for(int j = 0; j < point.length; j++) {
            double s = scale[j] > 0 ? scale[j] : 1.0;
            double d = (point[j] - from[j]) / s;
            distSq += d * d;
        }
        return Math.sqrt(distSq);
    }

    private void convertMoleculeToDigitalUnits(Molecule mol) {
        for(String param : mol.descriptor.names) {
            MoleculeDescriptor.Units paramUnits = mol.getParamUnits(param);
            MoleculeDescriptor.Units digitalUnits = MoleculeDescriptor.Units.getDigitalUnits(paramUnits);
            if(!digitalUnits.equals(paramUnits)) {
                mol.setParam(param, digitalUnits, mol.getParam(param, digitalUnits));
            }
        }
    }
}
