package smlm.fastfit;

/**
 * One localisation produced by the fast fitter.
 * <p>
 * Positions are stored in pixels and converted on read, so the pixel size lives in one place.
 *
 * <h3>On the width estimate — read this before filtering on it</h3>
 * {@link #getSigmaPx()} is <em>not</em> a fitted width. It comes from a saturating moment, and
 * on real data it tracks the true PSF width only in rank, with the dynamic range compressed
 * (Spearman rho of roughly 0.5 on dense, wide-PSF data, rising to ~0.9 on sparse data at
 * standard 100 nm pixel sizes). A real PSF's non-Gaussian tails also add a systematic offset of
 * order +0.2 px that only an empirical calibration against a Gaussian-fit run will remove.
 * <p>
 * Treat it as a coarse quality indicator ({@link #getWidthClass()}), not as a measurement. It
 * should not be used for astigmatic z estimation, nor reported to users as a fitted sigma.
 *
 * <h3>On uncertainty</h3>
 * {@link #getUncertaintyNm()} is a precision <em>predicted from the photon budget</em> by
 * {@link PrecisionEstimator}, not a per-fit residual — the moment method produces none. It is
 * NaN unless a camera gain was supplied, since without one there is no way to convert ADU to
 * the photon count the prediction needs. Consumers should check
 * {@link FitCapabilities#hasUncertainty()} rather than testing for NaN.
 * <p>
 * Because it is a prediction, two localisations of equal brightness and background get the same
 * value whether or not either fit actually went well. It is meaningful in aggregate and should
 * not be read as a confidence interval on one molecule.
 */
public final class Localization {

    /** Coarse width category, the intended way to consume the width estimate. */
    public enum WidthClass {
        /** Narrower than expected; often a noise hit or a very dim spot. */
        NARROW,
        /** Consistent with the expected PSF width. */
        NORMAL,
        /** Wider than expected; defocus, aberration, or unresolved multiple emitters. */
        WIDE,
        /** Moments fell outside the calibrated region, so no width could be assigned. */
        UNKNOWN
    }

    private final int frame;
    private final double xPx;
    private final double yPx;
    private final double sigmaPx;
    private final double intensity;
    private final double patchSum;
    private final double background;
    private final double backgroundStdDev;
    private final double pixelSizeNm;
    private final WidthClass widthClass;
    private final boolean inCalibrationRange;
    private final double uncertaintyNm;

    Localization(int frame, double xPx, double yPx, double sigmaPx, double intensity,
                 double patchSum, double background, double backgroundStdDev,
                 double pixelSizeNm, WidthClass widthClass, boolean inCalibrationRange,
                 double uncertaintyNm) {
        this.frame = frame;
        this.xPx = xPx;
        this.yPx = yPx;
        this.sigmaPx = sigmaPx;
        this.intensity = intensity;
        this.patchSum = patchSum;
        this.background = background;
        this.backgroundStdDev = backgroundStdDev;
        this.pixelSizeNm = pixelSizeNm;
        this.widthClass = widthClass;
        this.inCalibrationRange = inCalibrationRange;
        this.uncertaintyNm = uncertaintyNm;
    }

    /** 1-based frame index within the stack. */
    public int getFrame() {
        return frame;
    }

    public double getXPx() {
        return xPx;
    }

    public double getYPx() {
        return yPx;
    }

    public double getXNm() {
        return xPx * pixelSizeNm;
    }

    public double getYNm() {
        return yPx * pixelSizeNm;
    }

    /** Coarse width estimate in pixels. See the class comment before relying on this. */
    public double getSigmaPx() {
        return sigmaPx;
    }

    /** Coarse width estimate in nanometres. See the class comment before relying on this. */
    public double getSigmaNm() {
        return sigmaPx * pixelSizeNm;
    }

    /**
     * Patch sum corrected for the Gaussian tail falling outside the patch, in camera units
     * (ADU above background, or photons if the caller scaled the input). The correction uses
     * the coarse width estimate, so it inherits that estimate's limitations; it is a reasonable
     * relative brightness measure and a poor absolute photon count.
     */
    public double getIntensity() {
        return intensity;
    }

    /** Raw background-subtracted sum over the fitting patch, with no aperture correction. */
    public double getPatchSum() {
        return patchSum;
    }

    /** Local background level that was subtracted, in camera units. */
    public double getBackground() {
        return background;
    }

    /**
     * Spread of the background ring around that level, in camera units — a local noise estimate,
     * and the quantity ThunderSTORM records as {@code bkgstd}.
     * <p>
     * It is a median absolute deviation scaled to a standard deviation, so a neighbouring emitter
     * clipping the ring inflates it far less than an ordinary sample deviation would. This is the
     * denominator the fitter's own signal-to-noise rejection uses, so a localisation surviving
     * that test has an intensity comfortably above this value.
     */
    public double getBackgroundStdDev() {
        return backgroundStdDev;
    }

    /**
     * Predicted lateral precision in nanometres, or NaN when no camera gain was supplied.
     * See the class comment before treating this as a per-molecule error bar.
     */
    public double getUncertaintyNm() {
        return uncertaintyNm;
    }

    public WidthClass getWidthClass() {
        return widthClass;
    }

    /** False when the moments fell outside the calibrated region and the width is unusable. */
    public boolean isInCalibrationRange() {
        return inCalibrationRange;
    }

    public double getPixelSizeNm() {
        return pixelSizeNm;
    }

    @Override
    public String toString() {
        return String.format("Localization{frame=%d, x=%.1f nm, y=%.1f nm, width=%s}",
                frame, getXNm(), getYNm(), widthClass);
    }
}
