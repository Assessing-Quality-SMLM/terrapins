package smlm.fastfit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Measures whether {@link PrecisionEstimator} predicts the scatter the fitter actually achieves.
 * <p>
 * The criterion is <b>calibration, not correlation</b>. A predicted precision that correlates
 * perfectly with the truth but is systematically 60% low is a bad estimate — it would be
 * reported to users as a resolution, and in TERRAPINS it feeds a limiting-precision score
 * directly. So what is asserted here is the ratio of observed RMS error to predicted sigma,
 * across photon count, background and pixel size, with a bound on how far from 1 it may drift.
 * <p>
 * A single global {@link PrecisionEstimator#MEASURED_EFFICIENCY_FACTOR} can only be right if the
 * ratio is roughly <em>constant</em> across conditions. {@link #ratioIsStableAcrossConditions()}
 * is the test that would fail if it is not, and if it ever does, a single scalar is the wrong
 * model and the estimate needs a term this one does not have.
 */
class PrecisionCalibrationTest {

    private static final int FRAMES = 12;
    private static final int SIZE = 128;
    private static final int SPACING = 16;
    private static final int MARGIN = 12;
    private static final double GAIN = 2.0;   // photons per ADU; not 1, so a unit error shows up

    /** One condition and what came out of it. */
    private static final class Result {
        final String label;
        final int matched;
        final double observedRmsNm;
        final double predictedNm;
        final double biasNm;
        final double pixelSizeNm;
        final double psfSigmaNm;
        final double photons;
        final double backgroundPhotons;
        final double meanMeasuredPhotons;

        Result(String label, int matched, double observedRmsNm, double predictedNm,
               double biasNm, double pixelSizeNm, double psfSigmaNm, double photons,
               double backgroundPhotons, double meanMeasuredPhotons) {
            this.label = label;
            this.matched = matched;
            this.observedRmsNm = observedRmsNm;
            this.predictedNm = predictedNm;
            this.biasNm = biasNm;
            this.pixelSizeNm = pixelSizeNm;
            this.psfSigmaNm = psfSigmaNm;
            this.photons = photons;
            this.backgroundPhotons = backgroundPhotons;
            this.meanMeasuredPhotons = meanMeasuredPhotons;
        }

        double ratio() {
            return observedRmsNm / predictedNm;
        }
    }

    /**
     * Runs one condition end to end and returns the observed scatter beside the mean prediction.
     * The estimator is built with an efficiency factor of 1, so the ratio it returns <em>is</em>
     * the efficiency factor for that condition.
     */
    private static Result measure(String label, double pixelSizeNm, double psfFwhmNm,
                                  double photons, double backgroundPhotons, long seed) {
        FastFitConfig config = new FastFitConfig(pixelSizeNm, psfFwhmNm);
        config.photonsPerAdu = GAIN;
        config.precisionEfficiencyFactor = 1.0;   // no user override; the model carries the correction
        config.autoBaseline = false;
        config.baselineOffset = 0.0;
        config.renderAsh = false;
        config.numThreads = 1;
        config.reportProgress = false;
        config.derive();

        double psfSigmaPx = MathUtil.fwhmToSigma(psfFwhmNm) / pixelSizeNm;
        SyntheticFrames.Scene scene = SyntheticFrames.generate(
                FRAMES, SIZE, SPACING, MARGIN, photons, backgroundPhotons, psfSigmaPx, GAIN, seed);

        List<Localization> found = new FastLocalizer(config).run(scene.image);

        double[][] err = SyntheticFrames.matchErrorsNm(scene, found, 2.0, pixelSizeNm);
        double[] both = new double[err[0].length + err[1].length];
        System.arraycopy(err[0], 0, both, 0, err[0].length);
        System.arraycopy(err[1], 0, both, err[0].length, err[1].length);

        // Mean of the per-localisation predictions, restricted to the matched set so the two
        // numbers describe the same localisations.
        List<Double> predicted = new ArrayList<>();
        for (Localization l : found) {
            double u = l.getUncertaintyNm();
            if (!Double.isNaN(u)) {
                predicted.add(u);
            }
        }
        double meanPredicted = 0;
        for (double p : predicted) {
            meanPredicted += p;
        }
        meanPredicted /= predicted.size();

        double bias = 0;
        for (double d : both) {
            bias += d;
        }
        bias /= both.length;

        double meanMeasured = 0;
        int nMeasured = 0;
        for (Localization l : found) {
            meanMeasured += l.getIntensity() * GAIN;
            nMeasured++;
        }
        meanMeasured /= nMeasured;

        return new Result(label, err[0].length, SyntheticFrames.rms(both), meanPredicted, bias,
                pixelSizeNm, MathUtil.fwhmToSigma(psfFwhmNm), photons, backgroundPhotons,
                meanMeasured);
    }

    private static void report(List<Result> results) {
        System.out.println();
        System.out.printf("%-34s %7s %10s %10s %8s %8s%n",
                "condition", "matched", "observed", "predicted", "ratio", "bias");
        System.out.printf("%-34s %7s %10s %10s %8s %8s%n",
                "", "", "RMS nm", "nm", "obs/pred", "nm");
        for (Result r : results) {
            System.out.printf("%-34s %7d %10.2f %10.2f %8.3f %8.2f%n",
                    r.label, r.matched, r.observedRmsNm, r.predictedNm, r.ratio(), r.biasNm);
        }
        double mean = 0;
        for (Result r : results) {
            mean += r.ratio();
        }
        mean /= results.size();
        System.out.printf("%nmean observed/predicted = %.3f (1.0 is perfect calibration)%n", mean);
    }

    private static List<Result> sweep() {
        List<Result> results = new ArrayList<>();
        long seed = 20260906L;
        // Photon budget, at a fixed pixel size. Shot-noise dominated at the top, background
        // dominated at the bottom, so both terms of the expression are exercised.
        results.add(measure("100nm px, N=5000, bg=60", 100, 250, 5000, 60, seed++));
        results.add(measure("100nm px, N=2000, bg=60", 100, 250, 2000, 60, seed++));
        results.add(measure("100nm px, N=1000, bg=60", 100, 250, 1000, 60, seed++));
        results.add(measure("100nm px, N=500,  bg=60", 100, 250, 500, 60, seed++));
        // Background, at a fixed photon budget.
        results.add(measure("100nm px, N=2000, bg=10", 100, 250, 2000, 10, seed++));
        results.add(measure("100nm px, N=2000, bg=200", 100, 250, 2000, 200, seed++));
        // Pixel size, which moves both the sampling and the patch-size rule.
        results.add(measure("160nm px, N=2000, bg=60", 160, 250, 2000, 60, seed++));
        results.add(measure("130nm px, N=2000, bg=60", 130, 250, 2000, 60, seed++));
        results.add(measure("80nm px,  N=2000, bg=60", 80, 250, 2000, 60, seed++));
        return results;
    }

    /**
     * Decomposes the discrepancy, to tell a wrong formula apart from a wrong photon count.
     * <p>
     * Prints, for each condition, the observed variance beside the two terms of the Thompson
     * expression evaluated at the <em>true</em> photon count and background, then fits
     * {@code observed_var = k1 * shot + k2 * background} by least squares across all conditions.
     * If two coefficients collapse the spread, the moment estimator simply has a different
     * (constant) efficiency on each noise source and the model is adequate. If they do not, the
     * penalty depends on something neither term captures.
     */
    @Test
    void twoTermModelDiagnostics() {
        List<Result> results = sweep();
        System.out.println();
        System.out.printf("%-34s %10s %10s %10s %9s %9s%n",
                "condition", "obs var", "shot", "backgnd", "trueN", "measN");
        double[][] a = new double[results.size()][2];
        double[] y = new double[results.size()];
        for (int i = 0; i < results.size(); i++) {
            Result r = results.get(i);
            double sa2 = r.psfSigmaNm * r.psfSigmaNm + r.pixelSizeNm * r.pixelSizeNm / 12.0;
            double b = Math.sqrt(r.backgroundPhotons);
            double shot = sa2 / r.photons;
            double bg = 8.0 * Math.PI * sa2 * sa2 * b * b
                    / (r.pixelSizeNm * r.pixelSizeNm * r.photons * r.photons);
            a[i][0] = shot;
            a[i][1] = bg;
            y[i] = r.observedRmsNm * r.observedRmsNm;
            System.out.printf("%-34s %10.3f %10.3f %10.3f %9.0f %9.0f%n",
                    r.label, y[i], shot, bg, r.photons, r.meanMeasuredPhotons);
        }
        double[] k = leastSquares2(a, y);
        System.out.printf("%nfit: observed_var = %.3f * shot + %.3f * background%n", k[0], k[1]);
        System.out.printf("  implied efficiency on shot noise  = %.3f%n", Math.sqrt(k[0]));
        System.out.printf("  implied efficiency on background  = %.3f%n", Math.sqrt(k[1]));
        double min = Double.MAX_VALUE;
        double max = 0;
        for (int i = 0; i < results.size(); i++) {
            double pred = Math.sqrt(k[0] * a[i][0] + k[1] * a[i][1]);
            double ratio = results.get(i).observedRmsNm / pred;
            min = Math.min(min, ratio);
            max = Math.max(max, ratio);
            System.out.printf("  %-34s residual ratio %.3f%n", results.get(i).label, ratio);
        }
        System.out.printf("%nspread after two-term fit = %.3fx (was measured on a single factor)%n",
                max / min);
    }

    /** Least squares for y = k0*a0 + k1*a1 with no intercept, by the 2x2 normal equations. */
    private static double[] leastSquares2(double[][] a, double[] y) {
        double s00 = 0, s01 = 0, s11 = 0, t0 = 0, t1 = 0;
        for (int i = 0; i < y.length; i++) {
            s00 += a[i][0] * a[i][0];
            s01 += a[i][0] * a[i][1];
            s11 += a[i][1] * a[i][1];
            t0 += a[i][0] * y[i];
            t1 += a[i][1] * y[i];
        }
        double det = s00 * s11 - s01 * s01;
        return new double[]{(t0 * s11 - t1 * s01) / det, (s00 * t1 - s01 * t0) / det};
    }

    /**
     * Characterises the axis the two-term fit leaves behind: efficiency against the expected PSF
     * width in pixels, which is what sets how much of the patch is signal and which patch size
     * the derivation rule picks. Fixed photon budget and background throughout, so the only thing
     * moving is the sampling.
     */
    @Test
    void efficiencyVersusSigmaPx() {
        System.out.println();
        System.out.printf("%8s %9s %7s %10s %10s %8s%n",
                "px (nm)", "sigma px", "patch", "observed", "thompson", "ratio");
        long seed = 424242L;
        for (double px : new double[]{60, 70, 80, 90, 100, 110, 120, 130, 145, 160, 180, 200}) {
            Result r = measure(String.format("%.0fnm", px), px, 250, 2000, 60, seed++);
            double sa2 = r.psfSigmaNm * r.psfSigmaNm + px * px / 12.0;
            double b = Math.sqrt(r.backgroundPhotons);
            double thompson = Math.sqrt(sa2 / r.photons
                    + 8.0 * Math.PI * sa2 * sa2 * b * b / (px * px * r.photons * r.photons));
            double sigmaPx = r.psfSigmaNm / px;
            System.out.printf("%8.0f %9.3f %7s %10.2f %10.2f %8.3f%n",
                    px, sigmaPx, (sigmaPx < 1.0 ? "3x3" : "5x5"),
                    r.observedRmsNm, thompson, r.observedRmsNm / thompson);
        }
    }

    /**
     * The full calibration: at each sampling, fit the two noise coefficients from a sweep over
     * photon budget and background. Separating the sweeps this way stops the pixel-size axis and
     * the shot/background axis contaminating each other, which a single pooled fit cannot avoid.
     */
    @Test
    void fullCalibrationGrid() {
        System.out.println();
        System.out.printf("%8s %9s %7s %10s %10s %8s%n",
                "px (nm)", "sigma px", "patch", "k_shot", "k_bg", "spread");
        long seed = 90210L;
        for (double px : new double[]{60, 70, 80, 90, 100, 110, 120, 130, 145, 160, 180, 200}) {
            double[][] a = new double[6][2];
            double[] y = new double[6];
            double[][] cond = {{5000, 60}, {2000, 60}, {1000, 60}, {500, 60},
                               {2000, 10}, {2000, 200}};
            List<Result> rs = new ArrayList<>();
            for (int i = 0; i < cond.length; i++) {
                Result r = measure("", px, 250, cond[i][0], cond[i][1], seed++);
                rs.add(r);
                double sa2 = r.psfSigmaNm * r.psfSigmaNm + px * px / 12.0;
                double b = Math.sqrt(r.backgroundPhotons);
                a[i][0] = sa2 / r.photons;
                a[i][1] = 8.0 * Math.PI * sa2 * sa2 * b * b / (px * px * r.photons * r.photons);
                y[i] = r.observedRmsNm * r.observedRmsNm;
            }
            double[] k = leastSquares2(a, y);
            double min = Double.MAX_VALUE;
            double max = 0;
            for (int i = 0; i < rs.size(); i++) {
                double pred = Math.sqrt(k[0] * a[i][0] + k[1] * a[i][1]);
                double ratio = rs.get(i).observedRmsNm / pred;
                min = Math.min(min, ratio);
                max = Math.max(max, ratio);
            }
            double sigmaPx = rs.get(0).psfSigmaNm / px;
            System.out.printf("%8.0f %9.3f %7s %10.3f %10.3f %8.3f%n",
                    px, sigmaPx, (sigmaPx < 1.0 ? "3x3" : "5x5"),
                    Math.sqrt(Math.max(0, k[0])), Math.sqrt(Math.max(0, k[1])), max / min);
        }
    }

    @Test
    void ratioIsStableAcrossConditions() {
        List<Result> results = sweep();
        report(results);

        for (Result r : results) {
            assertTrue(r.matched > 100,
                    r.label + ": only " + r.matched + " localisations matched, too few to measure");
        }

        double min = Double.MAX_VALUE;
        double max = 0;
        for (Result r : results) {
            min = Math.min(min, r.ratio());
            max = Math.max(max, r.ratio());
        }
        // The two-coefficient model is only the right shape if what is left over is scatter
        // rather than trend. This is the assertion that fails if the estimate is missing a term.
        assertTrue(max / min < 1.35, String.format(
                "observed/predicted still varies by %.2fx across conditions (%.3f to %.3f); "
                        + "the model is missing a term", max / min, min, max));
    }

    @Test
    void predictionMatchesObservedScatter() {
        for (Result r : sweep()) {
            double ratio = r.observedRmsNm / r.predictedNm;
            assertEquals(1.0, ratio, 0.25, String.format(
                    "%s: observed %.2f nm vs predicted %.2f nm (ratio %.3f)",
                    r.label, r.observedRmsNm, r.predictedNm, ratio));
        }
    }

    /**
     * Out of sample. The coefficients were fitted at a 250 nm FWHM over a particular grid of
     * pixel sizes; this runs a different PSF at pixel sizes not in that grid, with fresh seeds.
     * Fitting a model and then reporting its residuals on the same data proves very little, so
     * this is the test that says whether the calibration generalises.
     */
    @Test
    void calibrationGeneralisesToUnseenConditions() {
        List<Result> held = new ArrayList<>();
        long seed = 555000L;
        held.add(measure("350nm FWHM, 150nm px, N=3000", 150, 350, 3000, 40, seed++));
        held.add(measure("350nm FWHM, 105nm px, N=1500", 105, 350, 1500, 80, seed++));
        held.add(measure("350nm FWHM, 125nm px, N=800", 125, 350, 800, 30, seed++));
        held.add(measure("200nm FWHM, 95nm px,  N=2500", 95, 200, 2500, 50, seed++));
        held.add(measure("200nm FWHM, 75nm px,  N=1200", 75, 200, 1200, 100, seed++));
        report(held);
        for (Result r : held) {
            double ratio = r.observedRmsNm / r.predictedNm;
            assertEquals(1.0, ratio, 0.30, String.format(
                    "%s: out-of-sample observed %.2f nm vs predicted %.2f nm (ratio %.3f)",
                    r.label, r.observedRmsNm, r.predictedNm, ratio));
        }
    }

    @Test
    void positionIsUnbiased() {
        // A precision estimate describes scatter, and only means anything if there is no large
        // systematic offset hiding underneath it.
        for (Result r : sweep()) {
            assertTrue(Math.abs(r.biasNm) < 0.15 * r.observedRmsNm, String.format(
                    "%s: mean position error %.2f nm is large next to the %.2f nm scatter",
                    r.label, r.biasNm, r.observedRmsNm));
        }
    }

    @Test
    void withoutGainThereIsNoUncertaintyAndNoPlaceholder() {
        FastFitConfig config = new FastFitConfig(100, 250);
        config.autoBaseline = false;
        config.renderAsh = false;
        config.numThreads = 1;
        config.reportProgress = false;
        config.derive();
        assertFalse(new MomentFitter(config).getCapabilities().hasUncertainty(),
                "a fitter with no gain must not claim an uncertainty capability");

        SyntheticFrames.Scene scene = SyntheticFrames.generate(
                2, SIZE, SPACING, MARGIN, 2000, 60, 1.06, 1.0, 7L);
        for (Localization l : new FastLocalizer(config).run(scene.image)) {
            assertTrue(Double.isNaN(l.getUncertaintyNm()),
                    "uncertainty must be NaN, not 0, when no gain was supplied");
        }
    }

    @Test
    void gainScalesTheEstimateAsPhotonsNotCameraUnits() {
        // Halving the gain halves the photon count, which raises the shot-noise term by sqrt(2).
        PrecisionEstimator a = PrecisionEstimator.of(100, 106, 2.0, 1.0, 1.0, 2);
        PrecisionEstimator b = PrecisionEstimator.of(100, 106, 1.0, 1.0, 1.0, 2);
        // Background zero isolates the shot-noise term, which scales as 1/sqrt(N).
        double ua = a.uncertaintyNm(1000, 0);
        double ub = b.uncertaintyNm(1000, 0);
        assertEquals(Math.sqrt(2.0), ub / ua, 1e-9);
    }

    @Test
    void emccdExcessNoiseCostsSqrtTwo() {
        PrecisionEstimator plain = PrecisionEstimator.of(100, 106, 1.0,
                PrecisionEstimator.NO_EXCESS_NOISE, 1.0, 2);
        PrecisionEstimator em = PrecisionEstimator.of(100, 106, 1.0,
                PrecisionEstimator.EMCCD_EXCESS_NOISE_FACTOR_SQ, 1.0, 2);
        assertEquals(Math.sqrt(2.0), em.uncertaintyNm(1000, 5) / plain.uncertaintyNm(1000, 5),
                1e-9);
    }
}
