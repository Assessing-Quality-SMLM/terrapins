package smlm.fastfit;

/**
 * Gaussian blur by separable convolution with a kernel built once.
 * <p>
 * ImageJ's {@code GaussianBlur} is a general-purpose filter: it rebuilds its kernel on every
 * call, honours ROIs, and can downscale for large sigma. The detector calls it twice per frame at
 * two sigmas that never change, so all of that is repeated work. Building the kernel in the
 * constructor and convolving directly is several times faster at the small sigmas this fitter
 * uses, where the kernel is only 7 to 17 taps wide and downscaling never engages.
 *
 * <h3>Edges</h3>
 * Out-of-frame samples take the value of the nearest edge pixel, which is what ImageJ does. This
 * biases the outermost few pixels of every frame, but candidates that close to the edge are
 * discarded before fitting anyway — a patch centred there would extend outside the image.
 *
 * <h3>Threading</h3>
 * Instances are immutable and hold no scratch state, so one may be shared between threads. The
 * caller supplies the working buffer.
 */
public final class SeparableBlur {

    /** Half kernel, index 0 at the centre; the full kernel is symmetric about it. */
    private final float[] kernel;
    private final int radius;
    private final double sigma;

    /**
     * @param sigma    standard deviation in pixels
     * @param accuracy kernel is truncated where the Gaussian falls below this fraction of its
     *                 peak; 0.002 matches what the detector previously asked ImageJ for
     */
    public SeparableBlur(double sigma, double accuracy) {
        if (!(sigma > 0)) {
            throw new IllegalArgumentException("sigma must be > 0, got " + sigma);
        }
        if (!(accuracy > 0) || !(accuracy < 1)) {
            throw new IllegalArgumentException("accuracy must be in (0, 1), got " + accuracy);
        }
        this.sigma = sigma;
        this.radius = (int) Math.ceil(sigma * Math.sqrt(-2.0 * Math.log(accuracy)));

        double[] w = new double[radius + 1];
        double sum = 0.0;
        for (int i = 0; i <= radius; i++) {
            w[i] = Math.exp(-0.5 * i * i / (sigma * sigma));
            // every tap but the centre appears on both sides
            sum += (i == 0) ? w[i] : 2.0 * w[i];
        }
        this.kernel = new float[radius + 1];
        for (int i = 0; i <= radius; i++) {
            kernel[i] = (float) (w[i] / sum);
        }
    }

    /** Taps either side of the centre. */
    public int getRadius() {
        return radius;
    }

    public double getSigma() {
        return sigma;
    }

    /**
     * Blurs {@code src} into {@code dst}. {@code src} is not modified, so the two may not be the
     * same array.
     *
     * @param scratch working buffer of at least {@code w * h} elements
     */
    public void blur(float[] src, float[] dst, int w, int h, float[] scratch) {
        if (src.length < w * h || dst.length < w * h || scratch.length < w * h) {
            throw new IllegalArgumentException("buffers must hold at least " + (w * h)
                    + " elements");
        }
        horizontal(src, scratch, w, h);
        vertical(scratch, dst, w, h);
    }

    /**
     * Rows are contiguous, so the interior runs without bounds checks and only the two ends need
     * clamping. Splitting it that way keeps a branch out of the hot loop.
     */
    private void horizontal(float[] src, float[] dst, int w, int h) {
        for (int y = 0; y < h; y++) {
            int row = y * w;
            int left = Math.min(radius, w);
            for (int x = 0; x < left; x++) {
                dst[row + x] = clampedRow(src, row, x, w);
            }
            int right = Math.max(left, w - radius);
            for (int x = left; x < right; x++) {
                int i = row + x;
                float sum = kernel[0] * src[i];
                for (int k = 1; k <= radius; k++) {
                    sum += kernel[k] * (src[i - k] + src[i + k]);
                }
                dst[i] = sum;
            }
            for (int x = right; x < w; x++) {
                dst[row + x] = clampedRow(src, row, x, w);
            }
        }
    }

    private float clampedRow(float[] src, int row, int x, int w) {
        float sum = kernel[0] * src[row + x];
        for (int k = 1; k <= radius; k++) {
            int lo = x - k;
            int hi = x + k;
            if (lo < 0) {
                lo = 0;
            }
            if (hi > w - 1) {
                hi = w - 1;
            }
            sum += kernel[k] * (src[row + lo] + src[row + hi]);
        }
        return sum;
    }

    /**
     * Accumulates one tap across a whole output row at a time. Each inner loop is a contiguous
     * multiply-add over three arrays, which the JIT vectorises; summing each pixel's taps
     * individually instead strides by the image width inside the innermost loop, defeating that
     * and measuring about 25% slower on 512x512 frames despite touching the output once.
     */
    private void vertical(float[] src, float[] dst, int w, int h) {
        for (int y = 0; y < h; y++) {
            int out = y * w;
            float k0 = kernel[0];
            for (int x = 0; x < w; x++) {
                dst[out + x] = k0 * src[out + x];
            }
            for (int k = 1; k <= radius; k++) {
                int up = y - k;
                int down = y + k;
                if (up < 0) {
                    up = 0;
                }
                if (down > h - 1) {
                    down = h - 1;
                }
                int rowUp = up * w;
                int rowDown = down * w;
                float kk = kernel[k];
                for (int x = 0; x < w; x++) {
                    dst[out + x] += kk * (src[rowUp + x] + src[rowDown + x]);
                }
            }
        }
    }
}
