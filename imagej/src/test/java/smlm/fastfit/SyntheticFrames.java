package smlm.fastfit;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates SMLM frames with known emitter positions, so a predicted precision can be checked
 * against the scatter that is actually achieved.
 * <p>
 * Emitters are placed on a coarse grid with a random sub-pixel offset in each frame. The grid
 * spacing is deliberately far wider than the PSF: this harness measures how well the fitter
 * localises an isolated spot at a given photon budget, and crowding is a separate effect that
 * would confound that. A sub-pixel offset that varies per emitter matters — a fitter with a
 * pixel-locking bias would look perfect on a grid of exactly-centred spots.
 * <p>
 * Noise is Poisson on the total photon arrival at each pixel, background included, which is the
 * model Thompson's expression assumes. No read noise is added; adding it would need a camera
 * model the harness has no way to validate against.
 */
final class SyntheticFrames {

    /** One emitter with its true position, in pixels. */
    static final class Emitter {
        final int frame;
        final double xPx;
        final double yPx;

        Emitter(int frame, double xPx, double yPx) {
            this.frame = frame;
            this.xPx = xPx;
            this.yPx = yPx;
        }
    }

    /** A generated stack together with the ground truth that produced it. */
    static final class Scene {
        final ImagePlus image;
        final List<Emitter> truth;
        /** Photons per emitter, before noise. */
        final double photons;
        /** Background photons per pixel, before noise. */
        final double backgroundPhotons;
        /** Gain used to convert photons to the camera units the fitter sees. */
        final double photonsPerAdu;

        Scene(ImagePlus image, List<Emitter> truth, double photons, double backgroundPhotons,
              double photonsPerAdu) {
            this.image = image;
            this.truth = truth;
            this.photons = photons;
            this.backgroundPhotons = backgroundPhotons;
            this.photonsPerAdu = photonsPerAdu;
        }
    }

    private SyntheticFrames() {
    }

    /**
     * Builds a stack of frames of isolated emitters.
     *
     * @param nFrames           frames to generate
     * @param size              frame width and height in pixels
     * @param spacingPx         grid spacing; must be well above the PSF width
     * @param marginPx          keep emitters this far from the edge, at least the ring radius
     * @param photons           photons per emitter
     * @param backgroundPhotons background photons per pixel
     * @param psfSigmaPx        true PSF standard deviation in pixels
     * @param photonsPerAdu     gain; camera units are photons divided by this
     * @param seed              for reproducibility
     */
    static Scene generate(int nFrames, int size, int spacingPx, int marginPx, double photons,
                          double backgroundPhotons, double psfSigmaPx, double photonsPerAdu,
                          long seed) {
        Random rng = new Random(seed);
        ImageStack stack = new ImageStack(size, size);
        List<Emitter> truth = new ArrayList<>();

        for (int f = 1; f <= nFrames; f++) {
            double[] lambda = new double[size * size];
            for (int i = 0; i < lambda.length; i++) {
                lambda[i] = backgroundPhotons;
            }

            for (int gy = marginPx; gy <= size - marginPx - 1; gy += spacingPx) {
                for (int gx = marginPx; gx <= size - marginPx - 1; gx += spacingPx) {
                    // Uniform in [-0.5, 0.5) so the sub-pixel phase is sampled evenly; a fitter
                    // that snaps to pixel centres cannot hide from this.
                    double x = gx + (rng.nextDouble() - 0.5);
                    double y = gy + (rng.nextDouble() - 0.5);
                    truth.add(new Emitter(f, x, y));
                    addEmitter(lambda, size, x, y, photons, psfSigmaPx);
                }
            }

            float[] px = new float[lambda.length];
            for (int i = 0; i < lambda.length; i++) {
                px[i] = (float) (poisson(rng, lambda[i]) / photonsPerAdu);
            }
            stack.addSlice("frame " + f, new FloatProcessor(size, size, px, null));
        }
        return new Scene(new ImagePlus("synthetic", stack), truth, photons, backgroundPhotons,
                photonsPerAdu);
    }

    /**
     * Adds one emitter's expected photon distribution. The per-pixel value is the integral of the
     * Gaussian over the pixel, not its value at the centre — at sigma near 1 px the difference is
     * a percent-level bias in the recovered width, which would contaminate the measurement.
     */
    private static void addEmitter(double[] lambda, int size, double x, double y, double photons,
                                   double sigmaPx) {
        int reach = (int) Math.ceil(4 * sigmaPx);
        int x0 = Math.max(0, (int) Math.floor(x) - reach);
        int x1 = Math.min(size - 1, (int) Math.ceil(x) + reach);
        int y0 = Math.max(0, (int) Math.floor(y) - reach);
        int y1 = Math.min(size - 1, (int) Math.ceil(y) + reach);
        for (int j = y0; j <= y1; j++) {
            double fy = MathUtil.pixelIntegral(j, y, sigmaPx);
            for (int i = x0; i <= x1; i++) {
                double fx = MathUtil.pixelIntegral(i, x, sigmaPx);
                lambda[j * size + i] += photons * fx * fy;
            }
        }
    }

    /**
     * Poisson sample. Knuth's product method below 30, where it is exact and cheap, and a
     * normal approximation above, where Knuth's loop becomes slow and the approximation is good
     * to well under the precision this harness resolves.
     */
    private static double poisson(Random rng, double lambda) {
        if (lambda <= 0) {
            return 0;
        }
        if (lambda < 30) {
            double l = Math.exp(-lambda);
            int k = 0;
            double p = 1.0;
            do {
                k++;
                p *= rng.nextDouble();
            } while (p > l);
            return k - 1;
        }
        return Math.max(0, Math.round(lambda + Math.sqrt(lambda) * rng.nextGaussian()));
    }

    /**
     * Matches localisations to ground truth by nearest neighbour within {@code tolerancePx},
     * one-to-one, and returns the signed position errors in nanometres as {@code {dx, dy}}.
     * <p>
     * Unmatched truth (a missed detection) and unmatched localisations (a false positive) are
     * both simply absent from the result. That is the right behaviour here: a missed dim spot
     * should not enter a scatter measurement as a large error, and the detection rate is not
     * what this harness is measuring.
     */
    static double[][] matchErrorsNm(Scene scene, List<Localization> found, double tolerancePx,
                                    double pixelSizeNm) {
        List<Double> dx = new ArrayList<>();
        List<Double> dy = new ArrayList<>();
        boolean[] used = new boolean[found.size()];
        for (Emitter e : scene.truth) {
            int best = -1;
            double bestD2 = tolerancePx * tolerancePx;
            for (int i = 0; i < found.size(); i++) {
                if (used[i]) {
                    continue;
                }
                Localization l = found.get(i);
                if (l.getFrame() != e.frame) {
                    continue;
                }
                double ddx = l.getXPx() - e.xPx;
                double ddy = l.getYPx() - e.yPx;
                double d2 = ddx * ddx + ddy * ddy;
                if (d2 < bestD2) {
                    bestD2 = d2;
                    best = i;
                }
            }
            if (best >= 0) {
                used[best] = true;
                dx.add((found.get(best).getXPx() - e.xPx) * pixelSizeNm);
                dy.add((found.get(best).getYPx() - e.yPx) * pixelSizeNm);
            }
        }
        double[][] out = new double[2][dx.size()];
        for (int i = 0; i < dx.size(); i++) {
            out[0][i] = dx.get(i);
            out[1][i] = dy.get(i);
        }
        return out;
    }

    /** Root mean square of a sample, the quantity a precision estimate is predicting. */
    static double rms(double[] v) {
        double s = 0;
        for (double x : v) {
            s += x * x;
        }
        return Math.sqrt(s / v.length);
    }
}
