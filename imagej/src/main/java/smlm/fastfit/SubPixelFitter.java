package smlm.fastfit;

import ij.process.FloatProcessor;

/**
 * Refines an integer-pixel candidate to a sub-pixel localisation.
 * <p>
 * The second seam in the pipeline. {@link MomentFitter} is the fast implementation; a
 * least-squares or maximum-likelihood Gaussian fitter can be substituted without touching
 * detection, which makes a controlled fast-versus-accurate comparison possible on an identical
 * candidate list.
 */
public interface SubPixelFitter {

    /**
     * @param image baseline-subtracted frame, not background-subtracted
     * @param candidate integer-pixel candidate position
     * @param frame 1-based frame index, copied into the result
     * @return the localisation, or null if this candidate was rejected
     */
    Localization fit(FloatProcessor image, SpotDetector.Candidate candidate, int frame);

    /**
     * Pixels of margin required around a candidate. Candidates closer than this to the frame
     * edge are dropped by the caller. Implementations must not compensate by moving a candidate
     * inward, which would report a localisation where nothing was detected.
     */
    int requiredMargin();

    /** What the results from this fitter can support downstream. */
    FitCapabilities getCapabilities();

    /** Human-readable name, recorded in the output metadata. */
    String getName();

    /**
     * Returns an instance safe for use on the calling thread. Stateless implementations may
     * return {@code this}.
     */
    default SubPixelFitter threadLocalCopy() {
        return this;
    }
}
