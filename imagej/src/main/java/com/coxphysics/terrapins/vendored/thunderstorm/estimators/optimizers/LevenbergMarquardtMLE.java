package com.coxphysics.terrapins.vendored.thunderstorm.estimators.optimizers;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import java.util.Arrays;

/**
 * Maximizes a Poisson log-likelihood, L(theta) = sum_i [ y_i*log(mu_i(theta)) - mu_i(theta) ],
 * using Fisher scoring (Newton-Raphson with the observed Hessian replaced by its expectation)
 * damped by a Levenberg-Marquardt trust-region step.
 *
 * Since E[y_i] = mu_i, the expected Hessian collapses to
 *   F_jk = -sum_i J_ij * J_ik / mu_i
 * which is exactly the (negative of the) weighted Gauss-Newton approximation used for
 * nonlinear least squares, but with the iteration-dependent Poisson weights 1/mu_i instead
 * of fixed weights. This lets the same analytic model-value/Jacobian pair already used for
 * weighted least-squares fitting (see ILsqFunctions) drive the search directly, instead of
 * a derivative-free simplex search over the likelihood value alone.
 *
 * A single call to {@link #optimize} is a purely local search: like any Newton-type method it
 * follows the likelihood uphill from `start` and reports whatever stationary point it first
 * reaches. At low photon counts the Poisson-Gaussian likelihood surface can have a genuine
 * second local optimum -- e.g. a wide, flat, low-amplitude Gaussian that mimics the true compact
 * peak + background almost as well -- reachable by a monotonically improving multi-step ascent
 * from a perfectly reasonable starting guess. No amount of step-size/damping control fixes that
 * from inside a single run; callers that need robustness against it should run {@link #optimize}
 * from a few different starting points (see MLEFitterLM) and keep the best-likelihood result,
 * the same role MLEFitter's much larger Nelder-Mead initial simplex plays for the derivative-free
 * search.
 */
public class LevenbergMarquardtMLE {

    private static final double LAMBDA_INIT = 1e-2;
    private static final double LAMBDA_UP = 10.0;
    private static final double LAMBDA_DOWN = 0.1;
    private static final double LAMBDA_MAX = 1e10;
    private static final int MAX_STEP_TRIALS = 30;
    private static final int MAX_GRADIENT_FALLBACK_TRIALS = 60;
    // pixel-value floor: keeps 1/mu_i and log(mu_i) finite for pixels where the current model
    // prediction is (numerically) zero or negative. Scaled to the data rather than a fixed tiny
    // constant: a floor many orders of magnitude below the typical pixel count (e.g. 1e-6 against
    // counts of ~1-100) lets 1/mu_i explode for any pixel that transiently clamps, which corrupts
    // the gradient/Fisher information computed at the next accepted point and can trap the search
    // far from the optimum -- most noticeable at low photon counts, where background-level pixels
    // are the majority and the fit is more likely to pass near-zero during the search.
    private static final double MIN_MODEL_VALUE_FLOOR = 1e-6;
    private static final double MIN_MODEL_VALUE_FRACTION = 1e-2;
    private double minModelValue = MIN_MODEL_VALUE_FLOOR;

    public double[] xmin;       // estimated parameters, in the same (internal) space as `start`
    public double logLikelihood;
    public int iterations;      // number of accepted/rejected Fisher-scoring steps
    public int evaluations;     // number of model-value evaluations
    public boolean converged;   // true if the gradient-norm stopping criterion was met

    /**
     * @param valueFunction    model prediction mu_i(theta) for each pixel; same convention as
     *                         used by LSQFitter (operates in the PSF model's internal/transformed
     *                         parameter space, see PSFModel#transformParameters)
     * @param jacobianFunction analytic d(mu_i)/d(theta_j), same convention/space as valueFunction
     * @param observations     observed pixel values y_i (photon units)
     * @param start             initial guess, internal parameter space
     * @param gradientTolerance stop when the norm of the log-likelihood gradient drops below this
     * @param maxIterations     maximum number of Fisher-scoring steps
     */
    public void optimize(MultivariateVectorFunction valueFunction, MultivariateMatrixFunction jacobianFunction,
                          double[] observations, double[] start, double gradientTolerance, int maxIterations) {
        int n = start.length;
        int m = observations.length;

        double meanObservation = 0;
        for(int i = 0; i < m; i++) {
            meanObservation += observations[i];
        }
        meanObservation /= m;
        minModelValue = Math.max(MIN_MODEL_VALUE_FLOOR, MIN_MODEL_VALUE_FRACTION * meanObservation);

        double[] theta = start.clone();
        double[] mu = clampPositive(valueFunction.value(theta));
        double logLik = logLikelihood(observations, mu);
        double lambda = LAMBDA_INIT;
        int evals = 1;
        boolean conv = false;
        int iter = 0;

        double[] grad = new double[n];
        double[][] fisher = new double[n][n];
        double[][] damped = new double[n][n];

        for(; iter < maxIterations; iter++) {
            double[][] J = jacobianFunction.value(theta);

            for(int j = 0; j < n; j++) {
                Arrays.fill(fisher[j], 0.0);
            }
            Arrays.fill(grad, 0.0);
            for(int i = 0; i < m; i++) {
                double w = 1.0 / mu[i];
                double res = (observations[i] - mu[i]) * w;
                double[] Ji = J[i];
                for(int j = 0; j < n; j++) {
                    grad[j] += res * Ji[j];
                    double wJij = w * Ji[j];
                    for(int k = j; k < n; k++) {
                        fisher[j][k] += wJij * Ji[k];
                    }
                }
            }
            for(int j = 0; j < n; j++) {
                for(int k = 0; k < j; k++) {
                    fisher[j][k] = fisher[k][j];
                }
            }

            double gradNormSq = 0;
            for(int j = 0; j < n; j++) {
                gradNormSq += grad[j] * grad[j];
            }
            if(Math.sqrt(gradNormSq) < gradientTolerance) {
                conv = true;
                break;
            }

            boolean stepAccepted = false;
            for(int trial = 0; trial < MAX_STEP_TRIALS; trial++) {
                for(int j = 0; j < n; j++) {
                    System.arraycopy(fisher[j], 0, damped[j], 0, n);
                    damped[j][j] += lambda * (fisher[j][j] > 0 ? fisher[j][j] : 1.0);
                }

                double[] delta;
                try {
                    delta = solve(damped, grad);
                } catch(SingularMatrixException ex) {
                    lambda *= LAMBDA_UP;
                    continue;
                }

                double[] newTheta = new double[n];
                for(int j = 0; j < n; j++) {
                    newTheta[j] = theta[j] + delta[j];
                }

                double[] newMu = clampPositive(valueFunction.value(newTheta));
                double newLogLik = logLikelihood(observations, newMu);
                evals++;

                if(newLogLik > logLik) {
                    theta = newTheta;
                    mu = newMu;
                    logLik = newLogLik;
                    lambda *= LAMBDA_DOWN;
                    stepAccepted = true;
                    break;
                } else {
                    lambda *= LAMBDA_UP;
                    if(lambda > LAMBDA_MAX) {
                        break;
                    }
                }
            }
            if(!stepAccepted) {
                // The damped Fisher-scoring direction found nothing better anywhere in its trust
                // region. That direction depends on solving a linear system that can be poorly
                // conditioned (e.g. a near-unidentifiable parameter combination at low photon
                // counts), so before declaring convergence, fall back to plain steepest ascent:
                // for a true ascent direction (which grad/||grad|| is, by construction, whenever
                // ||grad|| >= gradientTolerance > 0) a small enough step is guaranteed to improve
                // a smooth objective. This is decoupled from the Fisher solve entirely, so it
                // still finds a way forward when the curvature estimate itself is unreliable.
                double gradNorm = Math.sqrt(gradNormSq);
                double[] dir = new double[n];
                for(int j = 0; j < n; j++) {
                    dir[j] = grad[j] / gradNorm;
                }

                double step = 1.0;
                for(int trial = 0; trial < MAX_GRADIENT_FALLBACK_TRIALS && !stepAccepted; trial++, step *= 0.5) {
                    double[] newTheta = new double[n];
                    for(int j = 0; j < n; j++) {
                        newTheta[j] = theta[j] + step * dir[j];
                    }

                    double[] newMu = clampPositive(valueFunction.value(newTheta));
                    double newLogLik = logLikelihood(observations, newMu);
                    evals++;

                    if(newLogLik > logLik) {
                        theta = newTheta;
                        mu = newMu;
                        logLik = newLogLik;
                        lambda = LAMBDA_INIT;   // trust region was unreliable here; restart it
                        stepAccepted = true;
                    }
                }
            }
            if(!stepAccepted) {
                // neither the damped Fisher-scoring step nor plain steepest ascent could improve
                // the likelihood: this is a local optimum given the achievable numerical precision
                conv = true;
                break;
            }
        }

        xmin = theta;
        logLikelihood = logLik;
        iterations = iter;
        evaluations = evals;
        converged = conv;
    }

    private static double logLikelihood(double[] y, double[] mu) {
        double ll = 0;
        for(int i = 0; i < y.length; i++) {
            ll += y[i] * Math.log(mu[i]) - mu[i];
        }
        return ll;
    }

    private double[] clampPositive(double[] v) {
        for(int i = 0; i < v.length; i++) {
            if(!(v[i] > minModelValue)) {   // also catches NaN, since any comparison with NaN is false
                v[i] = minModelValue;
            }
        }
        return v;
    }

    private static double[] solve(double[][] a, double[] b) {
        RealMatrix m = new Array2DRowRealMatrix(a, false);
        RealVector v = new ArrayRealVector(b, false);
        DecompositionSolver solver = new LUDecomposition(m).getSolver();
        return solver.solve(v).toArray();
    }
}
