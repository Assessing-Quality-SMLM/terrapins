package smlm.fastfit;

import java.util.Arrays;

/**
 * Small numerical helpers. Kept dependency-free so the fast path needs nothing but {@code ij.jar}.
 */
public final class MathUtil {

    private MathUtil() {
    }

    private static final double SQRT2 = Math.sqrt(2.0);

    /**
     * Complementary error function, Chebyshev approximation with fractional error &lt; 1.2e-7.
     * That is well inside what the calibration needs (its own residual is ~1e-2 px).
     */
    public static double erfc(double x) {
        double z = Math.abs(x);
        double t = 2.0 / (2.0 + z);
        double ty = 4.0 * t - 2.0;

        // Chebyshev coefficients for erfc(z)*exp(z^2)*z
        double[] cof = {
                -1.3026537197817094, 6.4196979235649026e-1,
                1.9476473204185836e-2, -9.561514786808631e-3,
                -9.46595344482036e-4, 3.66839497852761e-4,
                4.2523324806907e-5, -2.0278578112534e-5,
                -1.624290004647e-6, 1.303655835580e-6,
                1.5626441722e-8, -8.5238095915e-8,
                6.529054439e-9, 5.059343495e-9,
                -9.91364156e-10, -2.27365122e-10,
                9.6467911e-11, 2.394038e-12,
                -6.886027e-12, 8.94487e-13,
                3.13092e-13, -1.12708e-13,
                3.81e-16, 7.106e-15
        };
        double d = 0.0, dd = 0.0;
        for (int j = cof.length - 1; j > 0; j--) {
            double tmp = d;
            d = ty * d - dd + cof[j];
            dd = tmp;
        }
        double ans = t * Math.exp(-z * z + 0.5 * (cof[0] + ty * d) - dd);
        return (x >= 0.0) ? ans : 2.0 - ans;
    }

    /** Error function. */
    public static double erf(double x) {
        return 1.0 - erfc(x);
    }

    /** Standard normal CDF with mean {@code mu} and standard deviation {@code sigma}. */
    public static double normCdf(double x, double mu, double sigma) {
        return 0.5 * erfc(-(x - mu) / (sigma * SQRT2));
    }

    /**
     * Integral of a 1-D unit Gaussian over the pixel of unit width centred at {@code pixelCentre}.
     * This is the pixel-integrated PSF model; using it (rather than sampling the Gaussian at pixel
     * centres) matters at the undersampled PSF widths this fitter targets.
     */
    public static double pixelIntegral(double pixelCentre, double mu, double sigma) {
        return normCdf(pixelCentre + 0.5, mu, sigma) - normCdf(pixelCentre - 0.5, mu, sigma);
    }

    /** Median of {@code a}. The array IS reordered. Returns NaN for an empty array. */
    public static double medianInPlace(double[] a, int len) {
        if (len <= 0) {
            return Double.NaN;
        }
        Arrays.sort(a, 0, len);
        int m = len / 2;
        return (len % 2 == 1) ? a[m] : 0.5 * (a[m - 1] + a[m]);
    }

    /** Median of a copy of {@code a}; the input is untouched. */
    public static double median(double[] a) {
        double[] c = a.clone();
        return medianInPlace(c, c.length);
    }

    /**
     * Median of the first {@code len} elements by selection rather than a full sort. The array IS
     * reordered. Returns NaN for an empty range.
     * <p>
     * Expected O(n) against {@code Arrays.sort}'s O(n log n). Worth the extra code only because
     * the detection threshold needs a median of every pixel of every frame; elsewhere prefer
     * {@link #medianInPlace(double[], int)}, which is shorter and exact by inspection.
     */
    public static float medianSelect(float[] a, int len) {
        if (len <= 0) {
            return Float.NaN;
        }
        int m = len / 2;
        float hi = select(a, 0, len - 1, m);
        if ((len & 1) == 1) {
            return hi;
        }
        // select() leaves everything below index m no greater than a[m], so the lower central
        // value is the maximum of that partition and needs no second selection pass.
        float lo = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < m; i++) {
            if (a[i] > lo) {
                lo = a[i];
            }
        }
        return 0.5f * (lo + hi);
    }

    /**
     * Hoare selection of the {@code k}th smallest element within {@code [lo, hi]}, partitioning
     * in place. The median-of-three pivot keeps the near-sorted runs common in image data off
     * quickselect's quadratic worst case.
     */
    private static float select(float[] a, int lo, int hi, int k) {
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid] < a[lo]) {
                swap(a, mid, lo);
            }
            if (a[hi] < a[lo]) {
                swap(a, hi, lo);
            }
            if (a[hi] < a[mid]) {
                swap(a, hi, mid);
            }
            float pivot = a[mid];
            int i = lo;
            int j = hi;
            while (i <= j) {
                while (a[i] < pivot) {
                    i++;
                }
                while (a[j] > pivot) {
                    j--;
                }
                if (i <= j) {
                    swap(a, i, j);
                    i++;
                    j--;
                }
            }
            if (k <= j) {
                hi = j;
            } else if (k >= i) {
                lo = i;
            } else {
                return a[k];
            }
        }
        return a[lo];
    }

    private static void swap(float[] a, int i, int j) {
        float t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    /**
     * Robust standard deviation, 1.4826 * median absolute deviation.
     * Used instead of the sample sd because the images contain bright spots that would
     * inflate an ordinary sd and push the detection threshold up.
     */
    public static double robustStd(float[] a) {
        double[] d = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            d[i] = a[i];
        }
        double med = medianInPlace(d, d.length);
        for (int i = 0; i < d.length; i++) {
            d[i] = Math.abs(a[i] - med);
        }
        return 1.4826 * medianInPlace(d, d.length);
    }

    /** Pixels the sampled robust sd aims to draw, whatever the frame size. */
    private static final int ROBUST_STD_SAMPLES = 16384;

    /**
     * Share of samples that may tie with the median before they are treated as a repeated block
     * rather than as noise. Band-passed float pixels essentially never repeat, so on real data
     * this fraction measures 0.
     */
    private static final double MAX_MEDIAN_TIE_FRACTION = 0.05;

    /** Samples that must survive removal of a repeated block for the estimate to mean anything. */
    private static final int MIN_USABLE_SAMPLES = 1024;

    /**
     * Robust standard deviation from a subsample of {@code a}, for the detection threshold.
     * <p>
     * The threshold only needs a noise <em>level</em>, and a median converges as the square root
     * of the sample count, so reading every pixel is wasted work. Sampling to a fixed count rather
     * than by a fixed stride makes the cost independent of frame size, which matters because the
     * exact version is O(n log n) and so grows faster than the band-pass it feeds: measured over
     * 200 frames it was 63% of detection time at 128x128 but 74% at 256x256. Against the exact
     * value the sampled estimate is within about 0.7% on average, which moved 0.006% of
     * detections on a 100,000 localisation test stack.
     *
     * <h3>Why the width is a parameter</h3>
     * The stride is forced coprime with the image width so that consecutive samples advance
     * across columns instead of landing on the same few. A stride that divides the width reads a
     * fixed subset of columns, and a sensor with column-correlated fixed-pattern noise — normal
     * on sCMOS — then has its noise systematically misread. Measured on a frame carrying a
     * period-4 column pattern, strides of 4, 8 and 16 on a 256-wide image underestimated the
     * robust sd by 32%, which would drop the detection threshold by the same factor and flood the
     * output with false positives. Coprime strides stayed within 1.5%. Sensor widths are usually
     * powers of two, so the plausible-looking strides are exactly the unsafe ones.
     *
     * <h3>Frames holding a repeated value</h3>
     * A band-passed float frame repeats a value only where the input was exactly constant across
     * the whole filter kernel — a masked, padded or dead sensor region, a saturated plateau, or a
     * blank frame. Left alone this collapses the estimate, because the median lands inside the
     * repeated block and the spread around it describes the block rather than the noise: measured
     * on 256x256 frames with a growing zeroed region, at half the frame masked the estimate read
     * 0.07x its true value, which dropped the threshold enough to return four times as many
     * candidates, nearly all noise.
     * <p>
     * Such a block is therefore removed and the estimate taken from the remaining samples, so a
     * frame that is part masked still gets a threshold appropriate to the part that carries data.
     * Only when too little survives — under {@value #MIN_USABLE_SAMPLES} samples — is NaN
     * returned, meaning the frame cannot be thresholded at all. Callers should treat NaN as a
     * refusal rather than substituting a value.
     * <p>
     * Background subtraction that clamps at zero does not trigger this. Isolated zeros do not
     * survive the band-pass, since an output pixel repeats only if every input pixel under the
     * kernel matched: measured on clipped frames, the tie fraction was still exactly 0% at 96.7%
     * of input pixels zeroed, with emitter recall unaffected.
     *
     * @param a     pixel data, not modified
     * @param width image width in pixels; only used to choose a safe stride
     */
    public static double robustStdSampled(float[] a, int width) {
        if (a.length <= 0) {
            return Double.NaN;
        }
        int stride = Math.max(1, a.length / ROBUST_STD_SAMPLES);
        // gcd(n, 0) is n, so a non-positive width would never satisfy the loop below.
        if (width < 1) {
            throw new IllegalArgumentException("width must be >= 1, got " + width);
        }
        while (stride > 1 && gcd(stride, width) != 1) {
            stride++;
        }

        int n = (a.length + stride - 1) / stride;
        float[] d = new float[n];
        for (int i = 0, j = 0; j < n; i += stride, j++) {
            d[j] = a[i];
        }

        float med = medianSelect(d, n);
        int ties = 0;
        for (int i = 0; i < n; i++) {
            if (d[i] == med) {
                ties++;
            }
        }

        if (ties > MAX_MEDIAN_TIE_FRACTION * n) {
            // The median has landed inside a block of one repeated value, so the spread around it
            // describes the block rather than the noise. Drop the block and measure what is left:
            // for a masked or over-subtracted region that is the rest of the frame, which still
            // carries usable data and must not be thrown away with it.
            int kept = 0;
            for (int i = 0; i < n; i++) {
                if (d[i] != med) {
                    d[kept++] = d[i];
                }
            }
            if (kept < MIN_USABLE_SAMPLES) {
                return Double.NaN;
            }
            n = kept;
            med = medianSelect(d, n);
        }

        for (int i = 0; i < n; i++) {
            d[i] = Math.abs(d[i] - med);
        }
        return 1.4826 * medianSelect(d, n);
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    /** Median of a float array. */
    public static double median(float[] a) {
        double[] d = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            d[i] = a[i];
        }
        return medianInPlace(d, d.length);
    }

    /** Convert a PSF full width at half maximum to a Gaussian sigma. */
    public static double fwhmToSigma(double fwhm) {
        return fwhm / (2.0 * Math.sqrt(2.0 * Math.log(2.0)));
    }

    /** Convert a Gaussian sigma to a full width at half maximum. */
    public static double sigmaToFwhm(double sigma) {
        return sigma * 2.0 * Math.sqrt(2.0 * Math.log(2.0));
    }
}
