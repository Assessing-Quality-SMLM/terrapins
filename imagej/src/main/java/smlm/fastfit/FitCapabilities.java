package smlm.fastfit;

/**
 * Declares which output columns a fitter can meaningfully populate.
 * <p>
 * This exists so that a filtering stage shared between the fast path and a Gaussian-fitting path
 * can disable the cuts it cannot support, rather than silently filtering on NaN or on a number
 * that looks like a fitted width but is not. A user interface should grey out controls whose
 * capability is absent.
 */
public final class FitCapabilities {

    private final boolean position;
    private final boolean calibratedWidth;
    private final boolean coarseWidth;
    private final boolean intensity;
    private final boolean uncertainty;
    private final boolean goodnessOfFit;

    private FitCapabilities(boolean position, boolean calibratedWidth, boolean coarseWidth,
                            boolean intensity, boolean uncertainty, boolean goodnessOfFit) {
        this.position = position;
        this.calibratedWidth = calibratedWidth;
        this.coarseWidth = coarseWidth;
        this.intensity = intensity;
        this.uncertainty = uncertainty;
        this.goodnessOfFit = goodnessOfFit;
    }

    /**
     * What the moment fitter can do: position and brightness well, width only in rank, and no
     * goodness of fit ever.
     *
     * @param predictedUncertainty whether a camera gain was supplied, letting
     *     {@link PrecisionEstimator} predict a precision from the photon budget. This is not a
     *     residual-based uncertainty and never implies {@link #hasGoodnessOfFit()}.
     */
    public static FitCapabilities momentFitter(boolean predictedUncertainty) {
        return new FitCapabilities(true, false, true, true, predictedUncertainty, false);
    }

    /** The moment fitter with no photon calibration, and so no uncertainty. */
    public static FitCapabilities momentFitter() {
        return momentFitter(false);
    }

    /** What a least-squares or maximum-likelihood Gaussian fitter can do. */
    public static FitCapabilities gaussianFitter() {
        return new FitCapabilities(true, true, true, true, true, true);
    }

    /** Sub-pixel position is available. */
    public boolean hasPosition() {
        return position;
    }

    /** A quantitatively trustworthy width, suitable for a numeric cut or for z estimation. */
    public boolean hasCalibratedWidth() {
        return calibratedWidth;
    }

    /** A rank-only width, suitable for coarse narrow/normal/wide classification. */
    public boolean hasCoarseWidth() {
        return coarseWidth;
    }

    /** A relative brightness measure. */
    public boolean hasIntensity() {
        return intensity;
    }

    /** A localisation uncertainty estimate. */
    public boolean hasUncertainty() {
        return uncertainty;
    }

    /** A residual-based goodness of fit, such as chi squared. */
    public boolean hasGoodnessOfFit() {
        return goodnessOfFit;
    }
}
