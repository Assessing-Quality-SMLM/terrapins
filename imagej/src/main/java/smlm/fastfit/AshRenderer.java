package smlm.fastfit;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.FloatProcessor;

import java.util.List;

/**
 * Renders localisations as an average shifted histogram (ASH).
 * <p>
 * A plain histogram at high magnification is dominated by binning artefacts: whether two nearby
 * localisations land in the same super-resolution pixel depends on where the bin edges happen to
 * fall. An ASH removes that dependence by averaging several histograms whose grids are offset by
 * fractions of a bin, which suppresses the artefact without the arbitrary width choice a
 * Gaussian rendering imposes.
 *
 * <h3>Algorithm</h3>
 * Averaging {@code n} shifted histograms is equivalent to accumulating each localisation into the
 * surrounding bins with a separable triangular (Bartlett) weight, which is what this class does:
 * for a point falling in bin {@code (u, v)}, bin {@code (u+i, v+j)} receives
 * {@code (n-|i|)(n-|j|)} for {@code i, j} in {@code -(n-1) .. (n-1)}. The weights sum to
 * {@code n^4}, so dividing by that makes each localisation contribute exactly 1.0 and the image
 * total equal the number of localisations rendered.
 * <p>
 * This matches ThunderSTORM's {@code ASHRendering} (which uses the same weights and the same
 * default of 2 lateral shifts), so images from the two are directly comparable apart from
 * ThunderSTORM leaving the result unnormalised.
 *
 * <h3>What this is and is not</h3>
 * The ASH is a <em>density</em> image: brightness is localisations per super-resolution pixel,
 * not photon count and not a probability. It deliberately ignores per-localisation uncertainty,
 * unlike a Gaussian rendering where each point is blurred by its own precision — which suits this
 * fitter, since it produces no uncertainty estimate to blur by.
 * <p>
 * Rendering magnification is a display choice and does not improve resolution. At 5x with 100 nm
 * camera pixels the super-resolution pixel is 20 nm, comfortably below the ~30-50 nm a typical
 * SMLM reconstruction actually resolves, so the grid does not limit the result.
 */
public class AshRenderer {

    /** Default magnification relative to the camera pixel grid. */
    public static final int DEFAULT_MAGNIFICATION = 5;

    /** Default number of lateral shifts, matching ThunderSTORM. */
    public static final int DEFAULT_SHIFTS = 2;

    private final int magnification;
    private final int shifts;
    private boolean normalise = true;

    public AshRenderer() {
        this(DEFAULT_MAGNIFICATION, DEFAULT_SHIFTS);
    }

    /**
     * @param magnification super-resolution pixels per camera pixel, at least 1
     * @param shifts        lateral shifts; 1 gives a plain histogram, 2 is the usual choice
     */
    public AshRenderer(int magnification, int shifts) {
        if (magnification < 1) {
            throw new IllegalArgumentException(
                    "magnification must be >= 1, got " + magnification);
        }
        if (shifts < 1) {
            throw new IllegalArgumentException("shifts must be >= 1, got " + shifts);
        }
        if (magnification > 100) {
            throw new IllegalArgumentException(
                    "magnification " + magnification + " would produce an enormous image; "
                            + "5 to 20 is the useful range");
        }
        this.magnification = magnification;
        this.shifts = shifts;
    }

    /**
     * Divide by the total kernel weight so each localisation contributes 1.0 and the image sums
     * to the number of localisations. Turn off to match ThunderSTORM's raw output exactly.
     */
    public AshRenderer setNormalise(boolean normalise) {
        this.normalise = normalise;
        return this;
    }

    public int getMagnification() {
        return magnification;
    }

    public int getShifts() {
        return shifts;
    }

    /** Super-resolution pixel size in nanometres. */
    public double superPixelSizeNm(double cameraPixelSizeNm) {
        return cameraPixelSizeNm / magnification;
    }

    /**
     * Renders to a 32-bit image, spatially calibrated in nanometres.
     *
     * @param localizations   points to render; an empty list gives a blank image
     * @param cameraWidthPx   width of the source camera image, in camera pixels
     * @param cameraHeightPx  height of the source camera image, in camera pixels
     * @param cameraPixelSizeNm camera pixel size, used to convert nanometres back to the grid
     */
    public ImagePlus render(List<Localization> localizations,
                            int cameraWidthPx, int cameraHeightPx, double cameraPixelSizeNm) {
        if (cameraWidthPx < 1 || cameraHeightPx < 1) {
            throw new IllegalArgumentException("camera image dimensions must be positive");
        }
        if (!(cameraPixelSizeNm > 0)) {
            throw new IllegalArgumentException("cameraPixelSizeNm must be > 0");
        }

        final int width = cameraWidthPx * magnification;
        final int height = cameraHeightPx * magnification;

        // Guard before allocating: a 2048x2048 camera image at 20x is 1.7 GB as floats.
        long pixels = (long) width * (long) height;
        if (pixels > 400_000_000L) {
            throw new IllegalArgumentException(String.format(
                    "rendering %dx%d at %dx magnification needs a %dx%d image (%.1f GPixel). "
                            + "Reduce the magnification.",
                    cameraWidthPx, cameraHeightPx, magnification, width, height, pixels / 1e9));
        }

        final float[] acc = new float[width * height];
        final double superPixelNm = superPixelSizeNm(cameraPixelSizeNm);
        final double weightScale = normalise
                ? 1.0 / ((double) shifts * shifts * shifts * shifts)
                : 1.0;

        for (Localization loc : localizations) {
            double xSuper = loc.getXNm() / superPixelNm;
            double ySuper = loc.getYNm() / superPixelNm;
            if (Double.isNaN(xSuper) || Double.isNaN(ySuper)) {
                continue;
            }
            // Floor, not cast: a cast truncates towards zero and would fold the small negative
            // coordinates that arise near the image edge onto bin 0.
            int u = (int) Math.floor(xSuper);
            int v = (int) Math.floor(ySuper);

            for (int j = -shifts + 1; j < shifts; j++) {
                int y = v + j;
                if (y < 0 || y >= height) {
                    continue;
                }
                int weightY = shifts - Math.abs(j);
                int row = y * width;
                for (int i = -shifts + 1; i < shifts; i++) {
                    int x = u + i;
                    if (x < 0 || x >= width) {
                        continue;
                    }
                    acc[row + x] += (float) ((shifts - Math.abs(i)) * weightY * weightScale);
                }
            }
        }

        FloatProcessor fp = new FloatProcessor(width, height, acc, null);
        fp.resetMinAndMax();

        ImagePlus imp = new ImagePlus(String.format("ASH reconstruction (%dx)", magnification), fp);
        Calibration cal = imp.getCalibration();
        cal.pixelWidth = superPixelNm;
        cal.pixelHeight = superPixelNm;
        cal.setUnit("nm");
        imp.setCalibration(cal);
        return imp;
    }

    /** One-line description for logs and the protocol file. */
    public String describe(double cameraPixelSizeNm) {
        return String.format(
                "average shifted histogram, %dx magnification (%.1f nm super-resolution pixel), "
                        + "%d lateral shifts%s",
                magnification, superPixelSizeNm(cameraPixelSizeNm), shifts,
                normalise ? ", normalised to localisation counts" : ", unnormalised");
    }
}
