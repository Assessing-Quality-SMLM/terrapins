package smlm.fastfit;

/**
 * Settings for the fast fitter.
 * <p>
 * The two inputs that matter are the camera pixel size and the expected PSF width; everything
 * else is derived from them by {@link #derive()} unless explicitly overridden. Derivation is
 * driven by measurements on real data:
 * <ul>
 *   <li><b>Patch size.</b> 3x3 below sigma 1.0 px, 5x5 at or above. The small patch is roughly
 *       three times more robust to background misestimation; the larger one has better photon
 *       statistics and more lever arm on the width. Which dominates flips around 1 px.</li>
 *   <li><b>Background ring radius.</b> At least 4 sigma from the centre. Closer in, the ring
 *       still carries PSF tails at a level comparable to the read noise, which biases the
 *       background high, over-subtracts, and drags the width estimate down.</li>
 *   <li><b>Calibration range.</b> Tied to the expected width. A range of 0.6x to 1.9x the
 *       expected sigma keeps the polynomial residual near 0.003 px; a fixed wide range such as
 *       0.5 to 2.5 px costs an order of magnitude in accuracy for no benefit.</li>
 * </ul>
 */
public class FastFitConfig implements Cloneable {

    // ---------------------------------------------------------------- required

    /** Camera pixel size projected into sample space, in nanometres. */
    public double pixelSizeNm = 100.0;

    /**
     * Expected PSF full width at half maximum, in nanometres.
     * Typical values are 230-280 nm depending on wavelength and objective NA.
     */
    public double psfFwhmNm = 250.0;

    // ---------------------------------------------------------------- input

    /**
     * Channel to process, 1-based. Only consulted for hyperstacks; a plain stack processes
     * every slice. Multi-channel data must be run one channel at a time, because the frames of
     * different channels are not comparable.
     */
    public int channel = 1;

    /**
     * Z-slice to process, 1-based. Only consulted for hyperstacks. This is a 2D fitter, so a
     * z-stack must be run one slice at a time.
     */
    public int zSlice = 1;

    // -------------------------------------------------------- camera / preprocessing

    /**
     * Camera baseline offset in camera units, subtracted from every frame before anything else.
     * When {@link #autoBaseline} is true this is measured from the stack instead.
     */
    public double baselineOffset = 0.0;

    /**
     * Estimate the baseline from the temporal minimum projection of the stack rather than
     * using {@link #baselineOffset}. Robust when most pixels are background most of the time.
     */
    public boolean autoBaseline = true;

    // -------------------------------------------------------- photon calibration

    /**
     * Camera gain in photons per camera unit, used only to predict localisation precision.
     * <p>
     * Leave at NaN if unknown. Position, width and every rejection test are ratios of sums of
     * the same pixels, so none of them depend on this — supplying it wrongly cannot move a
     * localisation. What it does affect is the {@code uncertainty_xy} column: without it there
     * is no way to turn a spot's brightness in ADU into the photon count that sets the
     * precision, so the column is left empty rather than filled with a number in the wrong
     * units. See {@link PrecisionEstimator}.
     * <p>
     * This is the reciprocal of the more commonly quoted "ADU per photon" or "system gain".
     */
    public double photonsPerAdu = Double.NaN;

    /**
     * Whether the camera has an electron-multiplying register. Stochastic multiplication doubles
     * the variance of the photon count, so an EMCCD reaches a precision worse by a factor of
     * sqrt(2) at the same photon budget. Ignored when {@link #photonsPerAdu} is unset.
     */
    public boolean emccd = false;

    /**
     * Optional extra scale on the predicted precision, on top of the calibrated inefficiency
     * coefficients that {@link PrecisionEstimator} already applies.
     * <p>
     * Leave at 1.0 unless you have measured the fitter against ground truth for your own optical
     * configuration and found a residual offset — the simulation the coefficients come from
     * models Poisson noise on an ideal Gaussian PSF, and a real system with read noise or
     * non-Gaussian tails may sit slightly above it.
     */
    public double precisionEfficiencyFactor = 1.0;

    // ---------------------------------------------------------------- detection

    /** Detection threshold, in multiples of the robust standard deviation of the filtered image. */
    public double thresholdFactor = 3.0;

    /**
     * Correct {@link #thresholdFactor} for frame area, so one setting means the same sensitivity
     * whatever the sensor size. See {@link DoGDetector#effectiveThresholdFactor(double, int, int)}
     * for the correction and why it is needed. Turn off to reproduce older results.
     */
    public boolean scaleThresholdWithFrameSize = true;

    /** Inner DoG sigma in pixels. Derived from the PSF width when left at NaN. */
    public double dogSigmaInner = Double.NaN;

    /** Outer DoG sigma in pixels. Derived as 1.6x the inner sigma when left at NaN. */
    public double dogSigmaOuter = Double.NaN;

    // ------------------------------------------------------------------ fitting

    /** Patch half-width: 1 gives 3x3, 2 gives 5x5. Derived from the PSF width when left at 0. */
    public int patchHalfWidth = 0;

    /** Radius of the square ring used for the local background estimate. Derived when 0. */
    public int backgroundRingRadius = 0;

    /** Lowest PSF sigma covered by the calibration, in pixels. Derived when left at NaN. */
    public double calibrationSigmaMin = Double.NaN;

    /** Highest PSF sigma covered by the calibration, in pixels. Derived when left at NaN. */
    public double calibrationSigmaMax = Double.NaN;

    /**
     * Reject a localisation whose recovered offset exceeds this many pixels from the candidate
     * pixel centre. The calibration only covers +/- 0.5 px, so anything much beyond that is
     * extrapolation. Values well above 0.5 let bad fits through.
     */
    public double maxOffsetPx = 0.75;

    /**
     * How far outside the noiseless calibration envelope a moment pair may stray before the
     * candidate is rejected, as a fraction of the envelope's own span.
     * <p>
     * The envelope is measured without noise, so real moments scatter past it even for good
     * spots. Zero produces a large rejection rate that does not respond to the detection
     * threshold. Around 0.25 keeps genuine spots while still catching the wild moment values
     * that come from noise hits and badly overlapping emitters.
     */
    public double calibrationRangeTolerance = 0.25;

    /**
     * Least the patch sum may stand above its own background ring's noise, in standard deviations
     * of the noise-only distribution, before the candidate is rejected.
     * <p>
     * This is the only test that asks whether a candidate is actually brighter than its
     * surroundings; every other rejection is geometric or numerical. It is needed because the
     * detection threshold is a single number for the whole frame, so it cannot adapt to
     * background that varies across the field, and because the patch is clamped at zero before
     * summing, which leaves pure noise summing to a positive number rather than to nothing.
     * <p>
     * Zero disables the test entirely; negative values are rejected rather than silently treated
     * as zero, since one would most likely be a mistake.
     * <p>
     * What it costs depends almost entirely on how bright the emitter is relative to the noise.
     * Measured on 256x256 synthetic frames against known positions, recall by peak-to-noise
     * ratio:
     * <pre>
     *   peak/noise   off     3.0     4.0     6.0     8.0
     *   2.0x        56.3%   47.1%   38.8%   19.8%    8.3%
     *   3.0x        81.5%   79.8%   76.1%   59.4%   36.6%
     *   4.5x        93.8%   93.8%   93.8%   93.0%   87.1%
     *   6.0x        97.0%   97.0%   97.0%   97.0%   96.8%
     * </pre>
     * Above about 4.5x the cut is free; below 3x it removes emitters that were only being
     * recovered half the time anyway, and whose moments are correspondingly unreliable. On sparse
     * 512x512 frames the default took background detections from 110 per frame to 16, and 6.0
     * would take them to 3 — worth considering if dim emitters are not the point of the
     * experiment.
     * <p>
     * The exchange rate is worth confirming on your own data, since the candidates reaching this
     * test have already been selected as local maxima and so are not a fair sample of the noise.
     */
    public double minLocalSnr = 4.0;

    /**
     * Systematic width offset in pixels, subtracted from every width estimate.
     * <p>
     * Real PSFs have heavier tails than a Gaussian, which inflates the width moment by of order
     * +0.2 px. The value is specific to an optical configuration and should be measured once by
     * comparing against a Gaussian-fit run on the same data. Left at zero, widths will read
     * systematically high.
     */
    public double widthBiasCorrectionPx = 0.0;

    /** Width below {@code expected * this} is classified NARROW. */
    public double narrowWidthRatio = 0.75;

    /** Width above {@code expected * this} is classified WIDE. */
    public double wideWidthRatio = 1.35;

    // ----------------------------------------------------------------- rendering

    /**
     * Show an average shifted histogram reconstruction when the run finishes.
     */
    public boolean renderAsh = true;

    /**
     * Rendering magnification: super-resolution pixels per camera pixel. At 5x with 100 nm
     * camera pixels the rendered pixel is 20 nm, below what SMLM typically resolves, so the
     * grid does not limit the result. Magnification is a display choice and does not improve
     * resolution.
     */
    public int ashMagnification = AshRenderer.DEFAULT_MAGNIFICATION;

    /** Lateral shifts averaged in the histogram. 1 gives a plain histogram; 2 is usual. */
    public int ashShifts = AshRenderer.DEFAULT_SHIFTS;

    /**
     * Exclude localisations flagged NARROW or WIDE from the reconstruction.
     * <p>
     * The width classification is coarse (see {@link Localization}), so this is a blunt screen
     * rather than a quality filter. It is off by default so the displayed image shows the same
     * data as the CSV.
     */
    public boolean ashNormalWidthOnly = false;

    // ------------------------------------------------------------------- output

    /** Worker threads. 1 disables the thread pool. */
    public int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());

    /** Report progress to the ImageJ status bar. Set false when running headless. */
    public boolean reportProgress = true;

    public FastFitConfig() {
    }

    public FastFitConfig(double pixelSizeNm, double psfFwhmNm) {
        this.pixelSizeNm = pixelSizeNm;
        this.psfFwhmNm = psfFwhmNm;
    }

    /** Expected PSF sigma in pixels, from the FWHM and pixel size. */
    public double expectedSigmaPx() {
        return MathUtil.fwhmToSigma(psfFwhmNm) / pixelSizeNm;
    }

    /**
     * Fills in every derived field that has been left at its sentinel value, and validates.
     * Safe to call more than once; explicit settings are never overwritten.
     *
     * @return this, for chaining
     */
    public FastFitConfig derive() {
        validateInputs();
        double sigma = expectedSigmaPx();

        if (patchHalfWidth <= 0) {
            patchHalfWidth = (sigma < 1.0) ? 1 : 2;
        }
        if (backgroundRingRadius <= 0) {
            backgroundRingRadius = Math.max(patchHalfWidth + 2, (int) Math.ceil(4.0 * sigma));
        }
        if (Double.isNaN(calibrationSigmaMin)) {
            calibrationSigmaMin = Math.max(0.35, 0.6 * sigma);
        }
        if (Double.isNaN(calibrationSigmaMax)) {
            calibrationSigmaMax = 1.9 * sigma;
        }
        if (Double.isNaN(dogSigmaInner)) {
            dogSigmaInner = sigma;
        }
        if (Double.isNaN(dogSigmaOuter)) {
            dogSigmaOuter = 1.6 * dogSigmaInner;
        }
        validateDerived();
        return this;
    }

    private void validateInputs() {
        if (!(pixelSizeNm > 0)) {
            throw new IllegalArgumentException("pixelSizeNm must be > 0, got " + pixelSizeNm);
        }
        if (!(psfFwhmNm > 0)) {
            throw new IllegalArgumentException("psfFwhmNm must be > 0, got " + psfFwhmNm);
        }
        double sigma = expectedSigmaPx();
        if (sigma < 0.4) {
            throw new IllegalArgumentException(String.format(
                    "expected PSF sigma is %.2f px, which is too undersampled to localise "
                            + "(pixel size %.0f nm, FWHM %.0f nm). Check both values.",
                    sigma, pixelSizeNm, psfFwhmNm));
        }
        if (sigma > 3.0) {
            throw new IllegalArgumentException(String.format(
                    "expected PSF sigma is %.2f px. The moment method needs a patch wider than "
                            + "the PSF; above about 2 px use a Gaussian fitter instead.", sigma));
        }
        if (!(thresholdFactor > 0)) {
            throw new IllegalArgumentException("thresholdFactor must be > 0");
        }
        if (!(minLocalSnr >= 0)) {
            throw new IllegalArgumentException(
                    "minLocalSnr must be >= 0 (0 disables the test), got " + minLocalSnr);
        }
        if (numThreads < 1) {
            throw new IllegalArgumentException("numThreads must be >= 1");
        }
        // NaN means "not supplied", which is legitimate; a supplied value must be usable.
        if (!Double.isNaN(photonsPerAdu) && !(photonsPerAdu > 0)) {
            throw new IllegalArgumentException(
                    "photonsPerAdu must be > 0 when supplied (leave as NaN if unknown), got "
                            + photonsPerAdu);
        }
        if (!(precisionEfficiencyFactor > 0)) {
            throw new IllegalArgumentException(
                    "precisionEfficiencyFactor must be > 0, got " + precisionEfficiencyFactor);
        }
        if (renderAsh) {
            if (ashMagnification < 1) {
                throw new IllegalArgumentException(
                        "ashMagnification must be >= 1, got " + ashMagnification);
            }
            if (ashShifts < 1) {
                throw new IllegalArgumentException(
                        "ashShifts must be >= 1, got " + ashShifts);
            }
        }
    }

    private void validateDerived() {
        if (calibrationSigmaMax <= calibrationSigmaMin) {
            throw new IllegalArgumentException("calibrationSigmaMax must exceed calibrationSigmaMin");
        }
        if (backgroundRingRadius <= patchHalfWidth) {
            throw new IllegalArgumentException(
                    "backgroundRingRadius must exceed patchHalfWidth, else the ring sits inside "
                            + "the fitting patch");
        }
        if (dogSigmaOuter <= dogSigmaInner) {
            throw new IllegalArgumentException("dogSigmaOuter must exceed dogSigmaInner");
        }
    }

    @Override
    public FastFitConfig clone() {
        try {
            return (FastFitConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /** Multi-line description, suitable for a protocol file alongside the results. */
    public String describe() {
        return String.format(
                "pixel size          %.1f nm%n"
                        + "PSF FWHM            %.1f nm (sigma %.2f px)%n"
                        + "patch               %dx%d%n"
                        + "background ring     radius %d px%n"
                        + "calibration range   sigma %.2f-%.2f px%n"
                        + "DoG sigmas          %.2f / %.2f px%n"
                        + "threshold           %.1f x robust sd%s%n"
                        + "min local SNR       %.1f%s%n"
                        + "width bias applied  %.3f px%n"
                        + "photon calibration  %s%n"
                        + "rendering           %s%n",
                pixelSizeNm, psfFwhmNm, expectedSigmaPx(),
                2 * patchHalfWidth + 1, 2 * patchHalfWidth + 1,
                backgroundRingRadius, calibrationSigmaMin, calibrationSigmaMax,
                dogSigmaInner, dogSigmaOuter,
                thresholdFactor,
                scaleThresholdWithFrameSize ? " (scaled to frame area)" : " (unscaled)",
                minLocalSnr, minLocalSnr > 0 ? "" : " (disabled)",
                widthBiasCorrectionPx,
                Double.isNaN(photonsPerAdu)
                        ? "none (no uncertainty reported)"
                        : String.format("%.4g photons/ADU%s, efficiency factor %.3f",
                                photonsPerAdu, emccd ? ", EMCCD excess noise" : "",
                                precisionEfficiencyFactor),
                renderAsh
                        ? new AshRenderer(ashMagnification, ashShifts).describe(pixelSizeNm)
                                + (ashNormalWidthOnly ? ", NORMAL width class only" : "")
                        : "none");
    }
}
