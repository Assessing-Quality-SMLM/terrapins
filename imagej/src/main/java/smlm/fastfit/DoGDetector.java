package smlm.fastfit;

import ij.IJ;
import ij.process.FloatProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Difference-of-Gaussians band-pass followed by an 8-connected local maximum search.
 * <p>
 * Intended as the default for initial testing. It is cheap — two separable blurs per frame —
 * which matters because once the moment fitter makes fitting nearly free, detection dominates
 * the runtime.
 *
 * <h3>Threshold</h3>
 * The threshold is {@code k} times a robust standard deviation (1.4826 x median absolute
 * deviation) of the <em>filtered</em> image, recomputed per frame. A robust estimator is used
 * rather than an ordinary standard deviation because the bright spots themselves would inflate
 * the latter and push the threshold up as activation density rises. Note this makes sensitivity
 * mildly data-dependent across a stack, the same behaviour ThunderSTORM's {@code std(Wave.F1)}
 * has; use {@link #setAbsoluteThreshold(double)} if a fixed threshold is wanted instead.
 * <p>
 * The estimate comes from a fixed-size subsample of the frame rather than every pixel, so its
 * cost does not grow with frame size — see {@link MathUtil#robustStdSampled(float[], int)}. The
 * sampling is deterministic, so a given frame always yields the same threshold.
 * <p>
 * Where part of a frame holds a single repeated value — a masked, padded or dead sensor region, a
 * saturated plateau — the estimator measures the rest of the frame rather than collapsing, so
 * such a frame still yields the candidates its usable part contains. Only a frame with too little
 * left to measure yields none at all, which is reported once per thread and skips just that
 * frame rather than aborting the run.
 *
 * <h3>Filter widths</h3>
 * Defaults follow the usual band-pass logic: the inner sigma matches the PSF so the filter is
 * approximately matched to it, and the outer sigma is 1.6x that, which approximates a Laplacian
 * of Gaussian. These are the same proportions as ThunderSTORM's own DoG defaults.
 */
public class DoGDetector implements SpotDetector {

    private final double sigmaInner;
    private final double sigmaOuter;
    private final double thresholdFactor;
    private double absoluteThreshold = Double.NaN;
    private boolean scaleThresholdWithFrameSize = true;
    private final SeparableBlur innerBlur;
    private final SeparableBlur outerBlur;
    private boolean degenerateWarningIssued = false;

    // Reused between frames to keep three array allocations per frame down to one. Safe only
    // because threadLocalCopy() gives every worker its own detector.
    private float[] scratch;
    private float[] outerBuffer;

    /**
     * Where the Gaussian kernels are truncated, as a fraction of their peak.
     * <p>
     * Fixed rather than tunable. Relaxing it does shrink the kernels — 0.02 takes the pair from
     * 28 taps to 22 — and on synthetic frames that measured meaningfully faster, but on real
     * large datasets the gain did not materialise, leaving only a filter that departs from the
     * ideal Gaussian for no benefit. 0.002 is ImageJ's own default and the value this detector
     * was characterised at.
     */
    private static final double BLUR_ACCURACY = 0.002;

    /** Frame area at which {@code thresholdFactor} takes its face value. */
    private static final double REFERENCE_AREA = 128.0 * 128.0;

    /** Floor on the scaled factor, so a tiny frame cannot drive the threshold to nothing. */
    private static final double MIN_SCALED_FACTOR = 1.0;

    /**
     * The threshold factor actually used on a frame of this size.
     * <p>
     * A fixed multiple of the noise tests every pixel independently, so the number of noise peaks
     * that clear it grows with frame area: measured on sparse synthetic data, a 512x512 frame
     * holding 10 emitters produced around 200 detections at a factor of 3, nearly all of them
     * background. The same setting on a 128x128 frame is far stricter in practice, which makes
     * the parameter mean different things on different cameras.
     * <p>
     * For noise peaks the expected count above a level falls as {@code exp(-k^2/2)} while rising
     * in proportion to area, so holding the count constant as area changes requires
     * {@code k' = sqrt(k^2 + 2*ln(area/reference))}. Referenced to 128x128, a factor of 3 becomes
     * 3.43 at 256x256, 3.81 at 512x512 and 4.48 at 2048x2048 — the same sensitivity expressed for
     * a larger sensor rather than a stricter one.
     * <p>
     * The correction assumes the PSF width is roughly constant across the frames being compared,
     * which holds for one optical configuration; a large change in sampling shifts the count of
     * independent resolution elements too, but only weakly.
     */
    public static double effectiveThresholdFactor(double thresholdFactor, int width, int height) {
        double area = (double) width * height;
        if (!(area > 0)) {
            return thresholdFactor;
        }
        double scaled = thresholdFactor * thresholdFactor
                + 2.0 * Math.log(area / REFERENCE_AREA);
        return (scaled <= MIN_SCALED_FACTOR * MIN_SCALED_FACTOR)
                ? MIN_SCALED_FACTOR
                : Math.sqrt(scaled);
    }

    /**
     * @param psfSigmaPx      expected PSF sigma in pixels
     * @param thresholdFactor multiples of the robust standard deviation of the filtered image
     */
    public DoGDetector(double psfSigmaPx, double thresholdFactor) {
        this(psfSigmaPx, 1.6 * psfSigmaPx, thresholdFactor);
    }

    public DoGDetector(double sigmaInner, double sigmaOuter, double thresholdFactor) {
        if (!(sigmaInner > 0) || !(sigmaOuter > sigmaInner)) {
            throw new IllegalArgumentException(
                    "need 0 < sigmaInner < sigmaOuter, got " + sigmaInner + ", " + sigmaOuter);
        }
        if (!(thresholdFactor > 0)) {
            throw new IllegalArgumentException("thresholdFactor must be > 0");
        }
        this.sigmaInner = sigmaInner;
        this.sigmaOuter = sigmaOuter;
        this.thresholdFactor = thresholdFactor;
        this.innerBlur = new SeparableBlur(sigmaInner, BLUR_ACCURACY);
        this.outerBlur = new SeparableBlur(sigmaOuter, BLUR_ACCURACY);
    }

    /**
     * Use a fixed threshold on the filtered image instead of the per-frame robust estimate.
     * Set to NaN to return to the adaptive behaviour.
     */
    public void setAbsoluteThreshold(double value) {
        this.absoluteThreshold = value;
    }

    /**
     * Whether the threshold factor is corrected for frame area, so that a given setting means the
     * same sensitivity on any sensor. See {@link #effectiveThresholdFactor(double, int, int)}.
     * Turn off to reproduce results from before the correction existed.
     */
    public void setScaleThresholdWithFrameSize(boolean value) {
        this.scaleThresholdWithFrameSize = value;
    }

    @Override
    public SpotDetector threadLocalCopy() {
        // The detector reuses working buffers between frames, so each thread needs its own.
        DoGDetector copy = new DoGDetector(sigmaInner, sigmaOuter, thresholdFactor);
        copy.absoluteThreshold = this.absoluteThreshold;
        copy.scaleThresholdWithFrameSize = this.scaleThresholdWithFrameSize;
        return copy;
    }

    /** Applies the band-pass. Exposed so callers can inspect or reuse the filtered image. */
    public FloatProcessor filter(FloatProcessor image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int n = w * h;
        float[] src = (float[]) image.getPixels();

        // The returned array is fresh, so a caller may hold the result across further calls; the
        // two working buffers are reused, which is safe because detectors are per-thread.
        if (scratch == null || scratch.length < n) {
            scratch = new float[n];
            outerBuffer = new float[n];
        }
        float[] out = new float[n];

        innerBlur.blur(src, out, w, h, scratch);
        outerBlur.blur(src, outerBuffer, w, h, scratch);
        for (int i = 0; i < n; i++) {
            out[i] -= outerBuffer[i];
        }
        return new FloatProcessor(w, h, out, null);
    }

    @Override
    public List<Candidate> detect(FloatProcessor image) {
        FloatProcessor filtered = filter(image);
        int w = filtered.getWidth();
        int h = filtered.getHeight();
        float[] p = (float[]) filtered.getPixels();

        double threshold;
        if (Double.isNaN(absoluteThreshold)) {
            double noise = MathUtil.robustStdSampled(p, w);
            if (!Double.isFinite(noise) || noise <= 0.0) {
                warnDegenerate(noise);
                return Collections.emptyList();
            }
            double factor = scaleThresholdWithFrameSize
                    ? effectiveThresholdFactor(thresholdFactor, w, h)
                    : thresholdFactor;
            threshold = factor * noise;
        } else {
            threshold = absoluteThreshold;
        }

        List<Candidate> out = new ArrayList<Candidate>();
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int i = y * w + x;
                float v = p[i];
                if (v <= threshold) {
                    continue;
                }
                // Strictly greater than all eight neighbours. Strict comparison means exact
                // plateaus are dropped rather than producing duplicate detections.
                if (v > p[i - w - 1] && v > p[i - w] && v > p[i - w + 1]
                        && v > p[i - 1] && v > p[i + 1]
                        && v > p[i + w - 1] && v > p[i + w] && v > p[i + w + 1]) {
                    out.add(new Candidate(x, y, v));
                }
            }
        }
        return out;
    }

    /**
     * Reports the first degenerate frame this instance sees and stays quiet after that. Detectors
     * are per-thread, so a stack whose frames are all degenerate produces one line per worker
     * rather than one per frame.
     */
    private void warnDegenerate(double noise) {
        if (degenerateWarningIssued) {
            return;
        }
        degenerateWarningIssued = true;
        IJ.log("[fastfit] Warning: no usable noise level for a frame (estimate came out as "
                + noise + "), so it was skipped and produced no localisations. Almost all of that"
                + " frame holds one identical value — typically a blank or dropped frame, or one"
                + " left empty by background subtraction. Check the input if you did not expect"
                + " this. Further such frames on this thread are not reported.");
    }

    @Override
    public String getName() {
        return String.format(
                "Difference of Gaussians (sigma %.2f/%.2f px, kernels %d/%d px, "
                        + "threshold %.1f x robust sd%s)",
                sigmaInner, sigmaOuter,
                2 * innerBlur.getRadius() + 1, 2 * outerBlur.getRadius() + 1,
                thresholdFactor,
                scaleThresholdWithFrameSize ? " scaled to frame area" : " unscaled");
    }
}
