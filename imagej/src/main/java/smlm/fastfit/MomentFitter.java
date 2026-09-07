package smlm.fastfit;

import ij.process.FloatProcessor;

/**
 * Non-iterative sub-pixel localisation from three patch moments.
 * <p>
 * For each candidate: estimate the local background from a perimeter ring, subtract it, form two
 * moments per axis from the patch column and row sums, and evaluate pre-computed polynomials.
 * There is no iteration and no solver, so the cost is roughly forty floating point operations
 * per molecule against the order of ten thousand for a Levenberg-Marquardt Gaussian fit.
 *
 * <h3>Moments</h3>
 * With column sums {@code L}, {@code C}, {@code R} for the leftmost, centre and rightmost
 * columns of the patch and {@code T} the patch total:
 * <ul>
 *   <li>position moment {@code (R - L) / C} — odd in the sub-pixel offset, and a difference, so
 *       first-order sensitive and largely insensitive to a uniform background pedestal;</li>
 *   <li>width moment {@code (R + L) / T} — even in the offset, but a saturating sum: for a flat
 *       patch it tends to a fixed limit (2/3 for 3x3, 2/5 for 5x5), so it loses sensitivity once
 *       the PSF approaches the patch size.</li>
 * </ul>
 * That difference in conditioning is why position is accurate to a few nanometres here while
 * width is only a rank indicator. The rows give the y equivalents.
 *
 * <h3>Instances are stateless</h3>
 * The calibration is immutable and the scratch arrays are allocated per call, so one instance
 * can be shared across threads.
 */
public class MomentFitter implements SubPixelFitter {

    /** Why a candidate produced no localisation. Counted so a run can be diagnosed. */
    public enum Rejection {
        /** Too close to the frame edge for the background ring to fit. */
        BORDER,
        /** Patch summed to zero or less after background subtraction. */
        EMPTY_PATCH,
        /** Patch was not brighter than the noise of its own background ring. */
        LOW_SNR,
        /** Centre row or column summed to zero, so a moment denominator vanished. */
        ZERO_DENOMINATOR,
        /** Moments lay outside the calibrated region, so the polynomial would extrapolate. */
        OUT_OF_CALIBRATION,
        /** Recovered offset was further from the candidate pixel than maxOffsetPx allows. */
        OFFSET_TOO_LARGE
    }

    private final java.util.concurrent.atomic.AtomicLongArray rejections =
            new java.util.concurrent.atomic.AtomicLongArray(Rejection.values().length);

    private final FastFitConfig config;
    private final MomentCalibration calibration;
    private final int half;
    private final int ring;
    private final double expectedSigmaPx;
    private final PrecisionEstimator precision;

    public MomentFitter(FastFitConfig config) {
        this.config = config.clone().derive();
        this.half = this.config.patchHalfWidth;
        this.ring = this.config.backgroundRingRadius;
        this.expectedSigmaPx = this.config.expectedSigmaPx();
        this.precision = PrecisionEstimator.from(this.config);
        this.calibration = new MomentCalibration(
                half, this.config.calibrationSigmaMin, this.config.calibrationSigmaMax);
    }

    /** The calibration in use; exposed for diagnostics and for the protocol record. */
    public MomentCalibration getCalibration() {
        return calibration;
    }

    @Override
    public int requiredMargin() {
        return ring;
    }

    @Override
    public FitCapabilities getCapabilities() {
        return FitCapabilities.momentFitter(precision.isCalibrated());
    }

    @Override
    public String getName() {
        return String.format("Moment fitter (%dx%d patch, background ring radius %d)",
                2 * half + 1, 2 * half + 1, ring);
    }

    @Override
    public Localization fit(FloatProcessor image, SpotDetector.Candidate candidate, int frame) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        final int cx = candidate.x;
        final int cy = candidate.y;

        if (cx < ring || cy < ring || cx >= w - ring || cy >= h - ring) {
            return reject(Rejection.BORDER);
        }
        final float[] px = (float[]) image.getPixels();

        double[] ringStats = ringStats(px, w, cx, cy);
        double background = ringStats[0];
        double ringNoise = ringStats[1];

        // Patch, background subtracted and clamped at zero. Clamping rather than taking an
        // absolute value matters: over-subtraction should saturate at zero, not reflect a
        // negative value back up into an apparent signal.
        final int n = 2 * half + 1;
        double[][] patch = new double[n][n];
        double total = 0.0;
        for (int dy = -half; dy <= half; dy++) {
            int row = (cy + dy) * w;
            for (int dx = -half; dx <= half; dx++) {
                double v = px[row + cx + dx] - background;
                if (v < 0) {
                    v = 0;
                }
                patch[dy + half][dx + half] = v;
                total += v;
            }
        }
        if (total <= 0) {
            return reject(Rejection.EMPTY_PATCH);
        }
        if (config.minLocalSnr > 0
                && localSnr(total, n * n, ringNoise) < config.minLocalSnr) {
            return reject(Rejection.LOW_SNR);
        }

        double left = 0, right = 0, centreCol = 0;
        double top = 0, bottom = 0, centreRow = 0;
        for (int k = 0; k < n; k++) {
            left += patch[k][0];
            right += patch[k][n - 1];
            centreCol += patch[k][half];
            top += patch[0][k];
            bottom += patch[n - 1][k];
            centreRow += patch[half][k];
        }
        if (centreCol <= 0 || centreRow <= 0) {
            return reject(Rejection.ZERO_DENOMINATOR);
        }

        double xPositionMoment = (right - left) / centreCol;
        double xWidthMoment = (right + left) / total;
        double yPositionMoment = (bottom - top) / centreRow;
        double yWidthMoment = (bottom + top) / total;

        double tol = config.calibrationRangeTolerance;
        boolean inRange = calibration.inRange(xPositionMoment, xWidthMoment, tol)
                && calibration.inRange(yPositionMoment, yWidthMoment, tol);
        if (!inRange) {
            // Outside the calibrated region the polynomials extrapolate and can return anything.
            // Rejecting is the honest response; clamping would invent a localisation.
            return reject(Rejection.OUT_OF_CALIBRATION);
        }

        double dx = calibration.position(xPositionMoment, xWidthMoment);
        double dy = calibration.position(yPositionMoment, yWidthMoment);
        if (!isFinite(dx) || !isFinite(dy)
                || Math.abs(dx) > config.maxOffsetPx || Math.abs(dy) > config.maxOffsetPx) {
            return reject(Rejection.OFFSET_TOO_LARGE);
        }

        double sigmaX = calibration.width(xPositionMoment, xWidthMoment);
        double sigmaY = calibration.width(yPositionMoment, yWidthMoment);
        double sigma = 0.5 * (sigmaX + sigmaY) - config.widthBiasCorrectionPx;

        Localization.WidthClass widthClass;
        if (!isFinite(sigma) || sigma <= 0
                || !calibration.widthPlausible(sigma + config.widthBiasCorrectionPx, tol)) {
            sigma = Double.NaN;
            widthClass = Localization.WidthClass.UNKNOWN;
        } else if (sigma < config.narrowWidthRatio * expectedSigmaPx) {
            widthClass = Localization.WidthClass.NARROW;
        } else if (sigma > config.wideWidthRatio * expectedSigmaPx) {
            widthClass = Localization.WidthClass.WIDE;
        } else {
            widthClass = Localization.WidthClass.NORMAL;
        }

        double intensity = apertureCorrect(total, dx, dy, sigma);

        // Deliberately corrected with the nominal width, not the per-spot one. The precision
        // estimate is meant to track the photon budget; routing it through a width estimate that
        // is only reliable in rank would make it fluctuate for reasons unrelated to precision.
        double nominalIntensity = apertureCorrect(total, dx, dy, expectedSigmaPx);
        double uncertaintyNm = precision.uncertaintyNm(nominalIntensity, ringNoise);

        return new Localization(frame, cx + dx, cy + dy, sigma, intensity, total,
                background, ringNoise, config.pixelSizeNm, widthClass, true, uncertaintyNm);
    }

    /**
     * Scales the patch sum up to a whole-PSF total, using the fraction of a Gaussian of the
     * estimated width that falls inside the patch. Falls back to the uncorrected sum when the
     * width estimate is unusable.
     */
    private double apertureCorrect(double patchSum, double dx, double dy, double sigma) {
        if (!isFinite(sigma) || sigma <= 0) {
            return patchSum;
        }
        double edge = half + 0.5;
        double fx = MathUtil.normCdf(edge, dx, sigma) - MathUtil.normCdf(-edge, dx, sigma);
        double fy = MathUtil.normCdf(edge, dy, sigma) - MathUtil.normCdf(-edge, dy, sigma);
        double fraction = fx * fy;
        // Guard against a tiny denominator inflating the result without bound.
        return (fraction > 0.05) ? patchSum / fraction : patchSum;
    }

    /**
     * Median of the perimeter of the {@code (2*ring+1)} square window centred on the candidate.
     * A ring rather than the whole window so the emitter itself is excluded, and a median rather
     * than a mean so a neighbouring molecule clipping the ring shifts the estimate very little.
     */
    /** Mean of {@code max(x, 0)} for zero-mean unit-variance noise: 1/sqrt(2*pi). */
    private static final double CLAMPED_NOISE_MEAN = 0.3989422804014327;

    /** Standard deviation of the same: sqrt(1/2 - 1/(2*pi)). */
    private static final double CLAMPED_NOISE_SD = 0.5837600372832741;

    /**
     * How far the patch sum stands above what the local noise alone would produce, in standard
     * deviations of that noise-only distribution.
     * <p>
     * The patch is clamped at zero before summing, so a patch containing nothing but noise still
     * sums to a positive number — on average {@code 0.399 * n * sigma} for {@code n} pixels. That
     * is why {@link Rejection#EMPTY_PATCH} almost never fires and why background patches reach
     * the moment stage at all. Subtracting that expectation and dividing by its spread gives a
     * score centred on zero for pure background whatever the patch size, so one cut works for
     * both the 3x3 and 5x5 patch.
     * <p>
     * The score is dimensionless: numerator and denominator both scale with camera gain, so it
     * carries none of the units the rest of this fitter deliberately avoids needing. Note the
     * candidates reaching here have already been selected as local maxima of the band-pass, so
     * their scores sit above the unconditional null even when they are noise — the cut is
     * calibrated empirically rather than read off a normal table.
     *
     * @return the score, or {@link Double#POSITIVE_INFINITY} if the ring gave no usable noise
     *         estimate, which leaves the decision to the other rejection tests
     */
    private static double localSnr(double total, int patchPixels, double ringNoise) {
        if (!(ringNoise > 0) || Double.isInfinite(ringNoise)) {
            return Double.POSITIVE_INFINITY;
        }
        double expected = CLAMPED_NOISE_MEAN * patchPixels * ringNoise;
        double spread = CLAMPED_NOISE_SD * ringNoise * Math.sqrt(patchPixels);
        return (total - expected) / spread;
    }

    /**
     * Background level and noise from the perimeter ring, as {@code {median, robustSd}}.
     * <p>
     * The noise is a median absolute deviation rather than a standard deviation so that a
     * neighbouring emitter clipping the ring inflates it as little as possible; in dense data an
     * ordinary sd there would raise the noise estimate and reject the very spots it should keep.
     */
    private double[] ringStats(float[] px, int w, int cx, int cy) {
        int side = 2 * ring + 1;
        double[] values = new double[4 * (side - 1)];
        int k = 0;
        for (int dx = -ring; dx <= ring; dx++) {
            values[k++] = px[(cy - ring) * w + cx + dx];
            values[k++] = px[(cy + ring) * w + cx + dx];
        }
        for (int dy = -ring + 1; dy <= ring - 1; dy++) {
            values[k++] = px[(cy + dy) * w + cx - ring];
            values[k++] = px[(cy + dy) * w + cx + ring];
        }
        // medianInPlace sorts, which reorders but does not change the multiset, so the deviations
        // computed afterwards are still those of the ring.
        double median = MathUtil.medianInPlace(values, k);
        for (int i = 0; i < k; i++) {
            values[i] = Math.abs(values[i] - median);
        }
        return new double[]{median, 1.4826 * MathUtil.medianInPlace(values, k)};
    }

    private Localization reject(Rejection reason) {
        rejections.incrementAndGet(reason.ordinal());
        return null;
    }

    /** Number of candidates rejected for {@code reason} since construction. */
    public long getRejectionCount(Rejection reason) {
        return rejections.get(reason.ordinal());
    }

    /** One-line summary of why candidates were dropped, for logs and protocol files. */
    public String describeRejections() {
        StringBuilder sb = new StringBuilder();
        long total = 0;
        for (Rejection r : Rejection.values()) {
            total += rejections.get(r.ordinal());
        }
        sb.append("rejections total ").append(total);
        for (Rejection r : Rejection.values()) {
            long v = rejections.get(r.ordinal());
            if (v > 0) {
                sb.append(String.format("; %s %d (%.0f%%)", r.name(), v, 100.0 * v / total));
            }
        }
        return sb.toString();
    }

    private static boolean isFinite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }
}
