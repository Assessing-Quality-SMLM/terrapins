package smlm.fastfit;

/**
 * Maps patch moments to sub-pixel position and PSF width.
 * <p>
 * Built by simulating pixel-integrated Gaussian PSFs on a grid of sub-pixel offsets and widths,
 * measuring the same two moments the fitter will measure at run time, and least-squares fitting
 * polynomials in the inverse direction. Construction takes a few milliseconds, so it is done at
 * run time rather than shipped as a table — which means the calibration always matches the
 * configured patch size and expected PSF width.
 *
 * <h3>Why these particular polynomial terms</h3>
 * The position moment {@code (right - left) / centre} is <em>odd</em> in the sub-pixel offset, so
 * the position polynomial uses only terms odd in that moment. The width moment
 * {@code (right + left) / total} is <em>even</em>, so the width polynomial uses only even terms.
 * Fitting the constrained models directly (rather than fitting everything and discarding terms)
 * removes 12 basis functions and any ambiguity about what was dropped.
 *
 * <h3>Why one y-slice suffices</h3>
 * For a separable PSF the column sums factor as (x-profile) x (common y-sum). That common factor
 * cancels in both moments, so neither depends on the y offset. The calibration therefore varies
 * only x offset and width. This does assume a circular, separable PSF; astigmatic 3D would need
 * a genuinely two-dimensional calibration.
 *
 * <h3>Accuracy</h3>
 * Noiseless residual is ~0.01 px for position and ~0.07 px for width. Both are far below the
 * noise-driven error at realistic photon counts, so the calibration is not the limiting factor.
 */
public final class MomentCalibration {

    /** Terms (i,j) of {@code xmom^i * smom^j} used for position: odd in xmom. */
    private static final int[][] POSITION_TERMS = buildPositionTerms();

    /** Terms odd in xmom, total degree at most 6. */
    private static int[][] buildPositionTerms() {
        java.util.List<int[]> t = new java.util.ArrayList<int[]>();
        for (int i = 1; i <= 6; i += 2) {
            for (int j = 0; i + j <= 6; j++) {
                t.add(new int[]{i, j});
            }
        }
        return t.toArray(new int[0][]);
    }

    private final int patchHalfWidth;
    private final double sigmaMin;
    private final double sigmaMax;
    private final double[] positionCoefficients;
    private final double[] widthCoefficients;
    private final int[][] widthTerms;

    // Envelope of the moment values actually covered by the calibration grid. Evaluating the
    // polynomials outside this is extrapolation and is rejected rather than trusted.
    private final double xMomentMin;
    private final double xMomentMax;
    private final double sMomentMin;
    private final double sMomentMax;

    private final double positionResidual;
    private final double widthResidual;

    /**
     * @param patchHalfWidth 1 for a 3x3 patch, 2 for 5x5
     * @param sigmaMin       lowest PSF sigma to cover, in pixels
     * @param sigmaMax       highest PSF sigma to cover, in pixels
     */
    public MomentCalibration(int patchHalfWidth, double sigmaMin, double sigmaMax) {
        if (patchHalfWidth < 1) {
            throw new IllegalArgumentException("patchHalfWidth must be >= 1");
        }
        if (!(sigmaMin > 0) || !(sigmaMax > sigmaMin)) {
            throw new IllegalArgumentException(
                    "need 0 < sigmaMin < sigmaMax, got " + sigmaMin + ".." + sigmaMax);
        }
        this.patchHalfWidth = patchHalfWidth;
        this.sigmaMin = sigmaMin;
        this.sigmaMax = sigmaMax;
        this.widthTerms = buildWidthTerms();

        final int nOffsets = 15;
        final int nWidths = 45;
        final int rows = nOffsets * nWidths;

        double[] xm = new double[rows];
        double[] sm = new double[rows];
        double[] trueOffset = new double[rows];
        double[] trueSigma = new double[rows];

        int r = 0;
        for (int io = 0; io < nOffsets; io++) {
            double offset = -0.5 + io * (1.0 / (nOffsets - 1));
            for (int is = 0; is < nWidths; is++) {
                double sigma = sigmaMin + is * (sigmaMax - sigmaMin) / (nWidths - 1);
                double[] moments = simulateMoments(offset, sigma, patchHalfWidth);
                xm[r] = moments[0];
                sm[r] = moments[1];
                trueOffset[r] = offset;
                trueSigma[r] = sigma;
                r++;
            }
        }

        double lox = Double.POSITIVE_INFINITY, hix = Double.NEGATIVE_INFINITY;
        double los = Double.POSITIVE_INFINITY, his = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < rows; i++) {
            lox = Math.min(lox, xm[i]);
            hix = Math.max(hix, xm[i]);
            los = Math.min(los, sm[i]);
            his = Math.max(his, sm[i]);
        }
        this.xMomentMin = lox;
        this.xMomentMax = hix;
        this.sMomentMin = los;
        this.sMomentMax = his;

        this.positionCoefficients = fit(xm, sm, trueOffset, POSITION_TERMS);
        this.widthCoefficients = fit(xm, sm, trueSigma, widthTerms);
        this.positionResidual = maxResidual(xm, sm, trueOffset, POSITION_TERMS, positionCoefficients);
        this.widthResidual = maxResidual(xm, sm, trueSigma, widthTerms, widthCoefficients);
    }

    /** Terms even in xmom, total degree at most 6. */
    private static int[][] buildWidthTerms() {
        java.util.List<int[]> t = new java.util.ArrayList<int[]>();
        for (int i = 0; i <= 6; i += 2) {
            for (int j = 0; i + j <= 6; j++) {
                t.add(new int[]{i, j});
            }
        }
        return t.toArray(new int[0][]);
    }

    private static double[] fit(double[] xm, double[] sm, double[] target, int[][] terms) {
        int rows = xm.length;
        double[][] a = new double[rows][terms.length];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < terms.length; c++) {
                a[r][c] = Math.pow(xm[r], terms[c][0]) * Math.pow(sm[r], terms[c][1]);
            }
        }
        return LeastSquares.solve(a, target.clone());
    }

    private static double maxResidual(double[] xm, double[] sm, double[] target,
                                      int[][] terms, double[] coef) {
        double worst = 0.0;
        for (int r = 0; r < xm.length; r++) {
            double v = evaluate(xm[r], sm[r], terms, coef);
            worst = Math.max(worst, Math.abs(v - target[r]));
        }
        return worst;
    }

    private static double evaluate(double xm, double sm, int[][] terms, double[] coef) {
        double v = 0.0;
        for (int c = 0; c < terms.length; c++) {
            v += coef[c] * Math.pow(xm, terms[c][0]) * Math.pow(sm, terms[c][1]);
        }
        return v;
    }

    /**
     * Moments of an ideal pixel-integrated Gaussian, as the fitter would measure them.
     *
     * @return {@code {positionMoment, widthMoment}}
     */
    static double[] simulateMoments(double offset, double sigma, int half) {
        int n = 2 * half + 1;
        // Separable, and the y factor cancels in both moments, so the 1-D x profile suffices.
        double[] col = new double[n];
        for (int k = 0; k < n; k++) {
            col[k] = MathUtil.pixelIntegral(k - half, offset, sigma);
        }
        double left = col[0];
        double centre = col[half];
        double right = col[n - 1];
        double total = 0.0;
        for (double c : col) {
            total += c;
        }
        return new double[]{(right - left) / centre, (right + left) / total};
    }

    /** Sub-pixel offset in pixels, from the two moments. */
    public double position(double positionMoment, double widthMoment) {
        return evaluate(positionMoment, widthMoment, POSITION_TERMS, positionCoefficients);
    }

    /** PSF sigma in pixels, from the two moments. */
    public double width(double positionMoment, double widthMoment) {
        return evaluate(positionMoment, widthMoment, widthTerms, widthCoefficients);
    }

    /**
     * Whether a moment pair lies inside the region the calibration actually covers.
     * Outside it the polynomials extrapolate and can return wildly wrong values, so callers
     * should reject rather than clamp.
     */
    public boolean inRange(double positionMoment, double widthMoment) {
        return inRange(positionMoment, widthMoment, 0.0);
    }

    /**
     * As {@link #inRange(double, double)} but with the envelope widened by {@code tolerance}
     * times its own span on each side.
     * <p>
     * Some tolerance is necessary rather than optional. The envelope is measured from
     * <em>noiseless</em> simulated moments, but real moments carry photon noise, so a perfectly
     * good spot sitting near the edge of the envelope scatters outside it perhaps half the time.
     * With zero tolerance that shows up as a large, threshold-independent rejection rate which
     * looks like a detection problem but is not. The polynomials are smooth and degrade
     * gracefully just outside the fitted region, so modest extrapolation is safe; the recovered
     * values are range-checked separately in any case.
     */
    public boolean inRange(double positionMoment, double widthMoment, double tolerance) {
        double xPad = tolerance * (xMomentMax - xMomentMin);
        double sPad = tolerance * (sMomentMax - sMomentMin);
        return positionMoment >= xMomentMin - xPad && positionMoment <= xMomentMax + xPad
                && widthMoment >= sMomentMin - sPad && widthMoment <= sMomentMax + sPad;
    }

    /** True if a recovered width lies inside the range the calibration was fitted over. */
    public boolean widthPlausible(double sigma, double tolerance) {
        double pad = tolerance * (sigmaMax - sigmaMin);
        return sigma >= sigmaMin - pad && sigma <= sigmaMax + pad;
    }

    public int getPatchHalfWidth() {
        return patchHalfWidth;
    }

    public double getSigmaMin() {
        return sigmaMin;
    }

    public double getSigmaMax() {
        return sigmaMax;
    }

    /** Worst-case noiseless position error over the calibration grid, in pixels. */
    public double getPositionResidual() {
        return positionResidual;
    }

    /** Worst-case noiseless width error over the calibration grid, in pixels. */
    public double getWidthResidual() {
        return widthResidual;
    }

    @Override
    public String toString() {
        return String.format(
                "MomentCalibration{patch=%dx%d, sigma %.2f-%.2f px, "
                        + "residual pos %.4f px / width %.4f px}",
                2 * patchHalfWidth + 1, 2 * patchHalfWidth + 1,
                sigmaMin, sigmaMax, positionResidual, widthResidual);
    }
}
