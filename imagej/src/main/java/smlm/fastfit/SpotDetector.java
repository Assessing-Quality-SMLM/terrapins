package smlm.fastfit;

import ij.process.FloatProcessor;

import java.util.List;

/**
 * Finds candidate emitter positions to integer-pixel accuracy.
 * <p>
 * This is the seam that keeps detection separable from fitting. The built-in
 * {@link DoGDetector} is intended for initial testing; a ThunderSTORM filter/detector pair can
 * be substituted by writing an adapter that implements this interface, without touching the
 * fitting code (see {@code ThunderStormDetector} in the optional {@code adapter} source set).
 *
 * <h3>Contract</h3>
 * <ul>
 *   <li>The image passed in has already had the camera baseline removed by the caller.
 *       Implementations must not assume it is background-free, only baseline-free.</li>
 *   <li>Returned coordinates are integer pixel indices into that image.</li>
 *   <li>Implementations must be usable from multiple threads at once, or must return a fresh
 *       per-thread copy from {@link #threadLocalCopy()}. The default implementation of that
 *       method returns {@code this}, which is correct for stateless detectors.</li>
 *   <li>Candidates within a few pixels of the border may be returned; the fitter discards them.
 *       Detectors should not silently move them inward.</li>
 * </ul>
 */
public interface SpotDetector {

    /**
     * @param image baseline-subtracted frame
     * @return candidate positions, in raster order
     */
    List<Candidate> detect(FloatProcessor image);

    /** Human-readable name, recorded in the output metadata. */
    String getName();

    /**
     * Returns an instance safe for use on the calling thread.
     * <p>
     * Stateless detectors can return {@code this}. Detectors holding scratch buffers or, like
     * ThunderSTORM's, per-thread filter state, must return a fresh instance.
     */
    default SpotDetector threadLocalCopy() {
        return this;
    }

    /** An integer-pixel candidate position with the detector's response value. */
    final class Candidate {
        /** Column index. */
        public final int x;
        /** Row index. */
        public final int y;
        /** Detector response at this pixel; used only for diagnostics and ordering. */
        public final double response;

        public Candidate(int x, int y, double response) {
            this.x = x;
            this.y = y;
            this.response = response;
        }

        @Override
        public String toString() {
            return "Candidate{" + x + "," + y + " response=" + response + "}";
        }
    }
}
