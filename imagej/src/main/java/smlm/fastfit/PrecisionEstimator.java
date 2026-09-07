package smlm.fastfit;

/**
 * Predicts the localisation precision of a single localisation from its photon budget.
 * <p>
 * This is deliberately <em>not</em> part of {@link SubPixelFitter}. A fitter that produces no
 * residual has no uncertainty of its own, and any replacement for {@link MomentFitter} with the
 * same characteristics — non-iterative, no goodness of fit, no trustworthy per-spot width — has
 * the same gap. Keeping the estimate in its own type means it is written once, calibrated once,
 * and reused by whatever fitter comes next, rather than reimplemented alongside each one.
 *
 * <h3>What it computes</h3>
 * The Thompson, Larson and Webb (2002) expression for the variance of a localised position,
 * with the pixelation and background terms:
 * <pre>
 *     sigma^2 = s_a^2 / N  +  8 * pi * s_a^4 * b^2 / (a^2 * N^2)
 *     s_a^2   = s^2 + a^2 / 12
 * </pre>
 * where {@code s} is the PSF standard deviation, {@code a} the pixel size, {@code N} the photon
 * count of the emitter and {@code b} the background standard deviation per pixel, in photons.
 * The first term is photon shot noise, the second background noise; which dominates depends on
 * the brightness, and both are needed across the range real data covers.
 *
 * <h3>Three things it does not assume</h3>
 * <b>It uses the nominal PSF width, not the fitted one.</b> {@code s} comes from the configured
 * {@link FastFitConfig#psfFwhmNm}, not from {@link Localization#getSigmaPx()}. The per-spot
 * moment width is a rank estimate that degrades badly on dense data, and feeding it in would
 * make the predicted precision track the width estimate's failure modes rather than the photon
 * statistics that actually set the precision. For a fixed optical configuration the true PSF
 * width is very nearly constant, so little is lost and a great deal of noise is avoided.
 * <p>
 * <b>It does not assume the fitter is efficient.</b> Thompson's expression is the variance of a
 * maximum-likelihood estimator, which attains the Cramer-Rao bound. A moment estimator does not:
 * it discards information, so its true scatter sits above that bound — by a factor of roughly
 * 1.15 to 2.0 over the usable sampling range. Measurement showed that gap is not one number: it
 * differs between the shot-noise and background terms, and grows with the PSF width in pixels.
 * Both coefficients are therefore calibrated against simulated ground truth; see
 * {@code PrecisionCalibrationTest#fullCalibrationGrid}.
 * <p>
 * <b>It does not assume photons.</b> The fitter measures ADU, and the conversion needs a gain
 * the fitter cannot know. Without {@link FastFitConfig#photonsPerAdu} this class reports
 * {@link #isCalibrated()} false and returns NaN, rather than returning a number in the wrong
 * units that would look like a precision.
 *
 * <h3>What it is not</h3>
 * It is a <em>prediction from the photon budget</em>, identical for two localisations with the
 * same brightness and background. It is not a per-fit residual and carries no information about
 * whether that particular fit went wrong — a spot corrupted by a close neighbour gets the same
 * optimistic number as a clean one of equal brightness. Aggregate statistics over many
 * localisations are the intended use; treating a single value as a confidence interval on a
 * single molecule is not supported by anything here.
 */
public final class PrecisionEstimator {

    /**
     * Excess noise factor squared for an electron-multiplying camera. Stochastic multiplication
     * in the EM register doubles the variance of the photon count, which is conventionally
     * carried as a factor of two on the localisation variance.
     */
    public static final double EMCCD_EXCESS_NOISE_FACTOR_SQ = 2.0;

    /** No multiplication register, so no excess noise: sCMOS, CCD, CMOS. */
    public static final double NO_EXCESS_NOISE = 1.0;

    // The moment estimator's inefficiency, measured against simulated ground truth by
    // PrecisionCalibrationTest#fullCalibrationGrid.
    //
    // A single scalar will not do. Measurement showed the penalty differs between the two noise
    // sources — background costs more than shot noise, because the ring background estimate and
    // the clamping of negative pixels both act on it — and that both penalties vary with the PSF
    // width in pixels. So there are two coefficients:
    //
    //     variance = k_shot(sigma_px)^2 * shot_term + k_bg(sigma_px)^2 * background_term
    //
    // and each is fitted separately for the 3x3 and 5x5 patch, because the patch size is a step
    // function of sigma_px and the two regimes do not lie on one line. They do not even share a
    // sign: k_bg rises with sigma on a 3x3 patch and falls on a 5x5. Fitting across the step
    // leaves a visible artefact right above it, at sigma_px near 1.06, where a 5x5 patch is
    // admitting background that a 3x3 would have excluded.
    //
    // Fitted over photon budgets 500 to 5000 and backgrounds 10 to 200 photons/pixel, at
    // sigma_px 0.53 to 0.97 (3x3) and 1.06 to 1.77 (5x5). Worst coefficient residual is 0.25;
    // within one sampling the model tracks the data to 4-21% across that photon and background
    // range, and it holds to about 25% on optical configurations it was not fitted to.
    //
    // Re-measure all eight if the moment calibration, the patch sizing rule or the background
    // ring geometry changes: all three change how much information the estimator discards.

    private static final double K_SHOT_3X3_INTERCEPT = 0.1918;
    private static final double K_SHOT_3X3_SLOPE = 1.6854;
    private static final double K_BG_3X3_INTERCEPT = 0.9063;
    private static final double K_BG_3X3_SLOPE = 0.4724;
    private static final double SIGMA_PX_3X3_MIN = 0.53;
    private static final double SIGMA_PX_3X3_MAX = 0.97;

    private static final double K_SHOT_5X5_INTERCEPT = 1.3489;
    private static final double K_SHOT_5X5_SLOPE = 0.4195;
    private static final double K_BG_5X5_INTERCEPT = 2.2442;
    private static final double K_BG_5X5_SLOPE = -0.4522;
    private static final double SIGMA_PX_5X5_MIN = 1.06;
    private static final double SIGMA_PX_5X5_MAX = 1.77;

    private final double pixelSizeNm;
    private final double psfSigmaNm;
    private final double photonsPerAdu;
    private final double excessNoiseFactorSq;
    private final double efficiencyFactor;
    private final int patchHalfWidth;

    private PrecisionEstimator(double pixelSizeNm, double psfSigmaNm, double photonsPerAdu,
                               double excessNoiseFactorSq, double efficiencyFactor,
                               int patchHalfWidth) {
        this.pixelSizeNm = pixelSizeNm;
        this.psfSigmaNm = psfSigmaNm;
        this.photonsPerAdu = photonsPerAdu;
        this.excessNoiseFactorSq = excessNoiseFactorSq;
        this.efficiencyFactor = efficiencyFactor;
        this.patchHalfWidth = patchHalfWidth;
    }

    /**
     * Builds an estimator from a configuration. When the configuration carries no gain the
     * result is an uncalibrated estimator that returns NaN for everything.
     */
    public static PrecisionEstimator from(FastFitConfig config) {
        return new PrecisionEstimator(
                config.pixelSizeNm,
                MathUtil.fwhmToSigma(config.psfFwhmNm),
                config.photonsPerAdu,
                config.emccd ? EMCCD_EXCESS_NOISE_FACTOR_SQ : NO_EXCESS_NOISE,
                config.precisionEfficiencyFactor,
                config.patchHalfWidth);
    }

    /**
     * Builds an estimator directly, for calibration work and tests that need to vary the
     * efficiency factor without going through a configuration.
     *
     * @param pixelSizeNm      camera pixel size in sample space, nm
     * @param psfSigmaNm       nominal PSF standard deviation, nm — not a fitted width
     * @param photonsPerAdu    gain, or NaN for an uncalibrated estimator
     * @param excessNoiseFactorSq {@link #EMCCD_EXCESS_NOISE_FACTOR_SQ} or {@link #NO_EXCESS_NOISE}
     * @param efficiencyFactor optional extra scale, 1.0 for none
     * @param patchHalfWidth   1 for a 3x3 patch, 2 for 5x5; selects the coefficient set
     */
    public static PrecisionEstimator of(double pixelSizeNm, double psfSigmaNm,
                                        double photonsPerAdu, double excessNoiseFactorSq,
                                        double efficiencyFactor, int patchHalfWidth) {
        return new PrecisionEstimator(pixelSizeNm, psfSigmaNm, photonsPerAdu,
                excessNoiseFactorSq, efficiencyFactor, patchHalfWidth);
    }

    /**
     * Whether a gain was supplied. When false, {@link #uncertaintyNm} returns NaN and callers
     * should report no uncertainty rather than substituting a placeholder — a zero in this
     * column reads as perfect confidence to anything that consumes it.
     */
    public boolean isCalibrated() {
        return photonsPerAdu > 0 && !Double.isNaN(photonsPerAdu) && !Double.isInfinite(photonsPerAdu);
    }

    /**
     * Predicted lateral precision in nanometres, or NaN if uncalibrated or the inputs are not
     * physical.
     *
     * @param intensityAdu         emitter photon budget in camera units, aperture corrected
     * @param backgroundStdDevAdu  background spread per pixel in camera units
     */
    public double uncertaintyNm(double intensityAdu, double backgroundStdDevAdu) {
        if (!isCalibrated()) {
            return Double.NaN;
        }
        double n = intensityAdu * photonsPerAdu;
        if (!(n > 0) || Double.isNaN(n) || Double.isInfinite(n)) {
            return Double.NaN;
        }
        double b = backgroundStdDevAdu * photonsPerAdu;
        if (b < 0 || Double.isNaN(b) || Double.isInfinite(b)) {
            return Double.NaN;
        }
        return efficiencyFactor * Math.sqrt(excessNoiseFactorSq * varianceNmSq(n, b));
    }

    /**
     * Variance in nm^2 for a photon count and background, with the measured per-term
     * inefficiencies applied but before the excess-noise factor and any user override.
     */
    public double varianceNmSq(double photons, double backgroundPhotonsPerPixel) {
        double sigmaPx = psfSigmaNm / pixelSizeNm;
        double kShot = kShot(sigmaPx);
        double kBg = kBg(sigmaPx);
        double[] terms = thompsonTerms(photons, backgroundPhotonsPerPixel);
        return kShot * kShot * terms[0] + kBg * kBg * terms[1];
    }

    /**
     * The two terms of the Thompson, Larson and Webb expression, {@code {shot, background}}, in
     * nm^2 and with no efficiency correction — the bound an efficient estimator would attain.
     * Exposed so the calibration harness can fit the coefficients against it.
     */
    public double[] thompsonTerms(double photons, double backgroundPhotonsPerPixel) {
        double a = pixelSizeNm;
        double sa2 = psfSigmaNm * psfSigmaNm + a * a / 12.0;
        double shot = sa2 / photons;
        double background = 8.0 * Math.PI * sa2 * sa2 * backgroundPhotonsPerPixel
                * backgroundPhotonsPerPixel / (a * a * photons * photons);
        return new double[]{shot, background};
    }

    /**
     * Inefficiency on the shot-noise term. Held flat outside the calibrated range rather than
     * extrapolated: a linear fit run far past its data would eventually predict a coefficient
     * below 1, claiming the moment fitter beats the Cramer-Rao bound.
     */
    double kShot(double sigmaPx) {
        return small()
                ? K_SHOT_3X3_INTERCEPT + K_SHOT_3X3_SLOPE * clamped(sigmaPx)
                : K_SHOT_5X5_INTERCEPT + K_SHOT_5X5_SLOPE * clamped(sigmaPx);
    }

    /** Inefficiency on the background term. Clamped for the same reason as {@link #kShot}. */
    double kBg(double sigmaPx) {
        return small()
                ? K_BG_3X3_INTERCEPT + K_BG_3X3_SLOPE * clamped(sigmaPx)
                : K_BG_5X5_INTERCEPT + K_BG_5X5_SLOPE * clamped(sigmaPx);
    }

    /** True for a 3x3 patch. Taken from the configuration, which may have been overridden. */
    private boolean small() {
        return patchHalfWidth <= 1;
    }

    private double clamped(double sigmaPx) {
        return small()
                ? Math.max(SIGMA_PX_3X3_MIN, Math.min(SIGMA_PX_3X3_MAX, sigmaPx))
                : Math.max(SIGMA_PX_5X5_MIN, Math.min(SIGMA_PX_5X5_MAX, sigmaPx));
    }

    /** Whether the configured sampling falls inside the range the coefficients were fitted over. */
    public boolean isWithinCalibratedRange() {
        double sigmaPx = psfSigmaNm / pixelSizeNm;
        return small()
                ? sigmaPx >= SIGMA_PX_3X3_MIN && sigmaPx <= SIGMA_PX_3X3_MAX
                : sigmaPx >= SIGMA_PX_5X5_MIN && sigmaPx <= SIGMA_PX_5X5_MAX;
    }

    /** Nominal PSF standard deviation used by this estimator, in nm. */
    public double psfSigmaNm() {
        return psfSigmaNm;
    }

    /** Gain in photons per camera unit, or NaN when uncalibrated. */
    public double photonsPerAdu() {
        return photonsPerAdu;
    }

    public double efficiencyFactor() {
        return efficiencyFactor;
    }

    public double excessNoiseFactorSq() {
        return excessNoiseFactorSq;
    }

    /** One-line summary for the run protocol file. */
    public String describe() {
        if (!isCalibrated()) {
            return "precision estimate: unavailable (no photonsPerAdu supplied; "
                    + "uncertainty column is empty)";
        }
        double sigmaPx = psfSigmaNm / pixelSizeNm;
        return String.format(
                "precision estimate: Thompson 2002 from nominal PSF sigma %.1f nm (%.2f px), "
                        + "gain %.4g photons/ADU, excess noise factor^2 %.1f, "
                        + "inefficiency k_shot %.2f / k_bg %.2f%s%s",
                psfSigmaNm, sigmaPx, photonsPerAdu, excessNoiseFactorSq,
                kShot(sigmaPx), kBg(sigmaPx),
                efficiencyFactor == 1.0 ? "" : String.format(", user factor %.3f", efficiencyFactor),
                isWithinCalibratedRange() ? ""
                        : String.format(" [WARNING: sigma %.2f px is outside the calibrated "
                                + "range for a %dx%d patch; coefficients held at the nearest "
                                + "edge]", sigmaPx, 2 * patchHalfWidth + 1, 2 * patchHalfWidth + 1));
    }
}
