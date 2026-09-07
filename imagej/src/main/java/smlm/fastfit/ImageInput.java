package smlm.fastfit;

import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;

/**
 * Validates an input image and works out which slices to process.
 *
 * <h3>Supported input</h3>
 * A single-channel image stack whose slices are successive camera frames: 8-bit, 16-bit or
 * 32-bit float. 16-bit is the usual case for EMCCD and sCMOS cameras.
 * <p>
 * <b>Bit depth does not need to be declared and 12-bit needs no special handling.</b> ImageJ has
 * no 12-bit type; a 12-bit camera writes into a 16-bit container, so the data arrives as an
 * ordinary {@code ShortProcessor} carrying values in 0..4095 (or left-shifted to fill 0..65535,
 * depending on the acquisition software). Either is fine, because every moment this fitter
 * computes is a <em>ratio</em> of sums of the same pixels: multiplying all values by a constant
 * leaves them unchanged. Measured over a 400x range of gain, recovered positions differ by less
 * than 0.0001 nm and the localisation count is identical.
 * <p>
 * The consequence worth remembering is that only {@code intensity} and {@code offset} in the
 * output carry the camera's units; positions and widths do not depend on them at all.
 *
 * <h3>Rejected input, and why</h3>
 * <ul>
 *   <li><b>RGB.</b> ImageJ would convert it to greyscale with a weighted average, so a pixel
 *       reading 200 in one channel arrives at the fitter as 66.7. That silently destroys the
 *       photometry the moments depend on. Convert deliberately (Image &gt; Type &gt; 16-bit)
 *       if the data really is stored as RGB.</li>
 *   <li><b>Single images.</b> Localisation needs a time series. A one-frame image is accepted
 *       but the automatic baseline is meaningless, so it must be set explicitly.</li>
 * </ul>
 *
 * <h3>Hyperstacks</h3>
 * A hyperstack's {@code getStackSize()} counts every channel and z-slice as well as every time
 * point, so iterating it blindly would mix channels together. This class instead enumerates one
 * channel and one z-slice across all time points. Choose them with
 * {@link FastFitConfig#channel} and {@link FastFitConfig#zSlice}.
 */
public final class ImageInput {

    private ImageInput() {
    }

    /**
     * Checks that {@code imp} can be processed.
     *
     * @throws IllegalArgumentException with a message suitable for showing to a user
     */
    public static void validate(ImagePlus imp, FastFitConfig config) {
        if (imp == null) {
            throw new IllegalArgumentException("No image. Open an image stack first.");
        }
        if (imp.getProcessor() instanceof ColorProcessor) {
            throw new IllegalArgumentException(
                    "RGB images are not supported. ImageJ would convert RGB to greyscale with a "
                            + "weighted average, which changes pixel values and corrupts the "
                            + "intensity moments this fitter relies on.\n\n"
                            + "Convert the image deliberately first (Image > Type > 16-bit), or "
                            + "re-export the raw camera data as 16-bit.");
        }
        if (imp.isHyperStack()) {
            if (config.channel < 1 || config.channel > imp.getNChannels()) {
                throw new IllegalArgumentException(String.format(
                        "Channel %d does not exist; this hyperstack has %d channel(s).",
                        config.channel, imp.getNChannels()));
            }
            if (config.zSlice < 1 || config.zSlice > imp.getNSlices()) {
                throw new IllegalArgumentException(String.format(
                        "Z-slice %d does not exist; this hyperstack has %d slice(s).",
                        config.zSlice, imp.getNSlices()));
            }
        }
        if (frameIndices(imp, config).length == 0) {
            throw new IllegalArgumentException("The image contains no frames to process.");
        }
    }

    /**
     * Indices into {@link ImagePlus#getStack()} of the slices to treat as successive frames,
     * in time order.
     * <p>
     * For a plain stack this is simply every slice. For a hyperstack it is the chosen channel
     * and z-slice at each time point.
     */
    public static int[] frameIndices(ImagePlus imp, FastFitConfig config) {
        if (!imp.isHyperStack()) {
            int n = imp.getStackSize();
            int[] idx = new int[n];
            for (int i = 0; i < n; i++) {
                idx[i] = i + 1;
            }
            return idx;
        }
        int nT = imp.getNFrames();
        int[] idx = new int[nT];
        for (int t = 1; t <= nT; t++) {
            idx[t - 1] = imp.getStackIndex(config.channel, config.zSlice, t);
        }
        return idx;
    }

    /**
     * Pixel size in nanometres taken from the image's own spatial calibration, or NaN if the
     * image is uncalibrated or uses units this cannot interpret.
     * <p>
     * Useful for pre-filling the dialog, but the value should still be confirmed by the user:
     * an incorrect calibration silently rescales every coordinate in the output.
     */
    public static double calibratedPixelSizeNm(ImagePlus imp) {
        if (imp == null) {
            return Double.NaN;
        }
        Calibration cal = imp.getCalibration();
        if (cal == null || !cal.scaled()) {
            return Double.NaN;
        }
        double width = cal.pixelWidth;
        String unit = (cal.getUnit() == null) ? "" : cal.getUnit().trim().toLowerCase();
        double toNm;
        if (unit.equals("nm") || unit.startsWith("nanom")) {
            toNm = 1.0;
        } else if (unit.equals("um") || unit.equals("µm") || unit.equals("\u00b5m")
                || unit.startsWith("micro")) {
            toNm = 1000.0;
        } else if (unit.equals("mm") || unit.startsWith("millim")) {
            toNm = 1e6;
        } else {
            return Double.NaN;
        }
        double nm = width * toNm;
        // Guard against a placeholder calibration of 1 unit per pixel.
        return (nm > 1.0 && nm < 10000.0) ? nm : Double.NaN;
    }

    /**
     * True if an intensity calibration function is set that is not a straight line.
     * <p>
     * ImageJ applies the calibration function inside {@code convertToFloat}, so it is not merely
     * a display setting. A straight line, including one with an offset, cancels out of the
     * moments; anything curved does not.
     */
    public static boolean hasNonLinearCalibration(ImagePlus imp) {
        if (imp == null) {
            return false;
        }
        Calibration cal = imp.getCalibration();
        if (cal == null) {
            return false;
        }
        int function = cal.getFunction();
        return function != Calibration.NONE && function != Calibration.STRAIGHT_LINE;
    }

    /** One-line description of the input, for logs and the protocol file. */
    public static String describe(ImagePlus imp, FastFitConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append(imp.getWidth()).append('x').append(imp.getHeight());
        sb.append(", ").append(imp.getBitDepth()).append("-bit");
        if (imp.isHyperStack()) {
            sb.append(String.format(", hyperstack C=%d Z=%d T=%d, using channel %d slice %d",
                    imp.getNChannels(), imp.getNSlices(), imp.getNFrames(),
                    config.channel, config.zSlice));
        } else {
            sb.append(", ").append(imp.getStackSize()).append(" frames");
        }
        if (imp.getStack() instanceof VirtualStack) {
            sb.append(", virtual stack");
        }
        return sb.toString();
    }

    /** Warnings that do not prevent processing but that the user should see. */
    public static String[] warnings(ImagePlus imp, FastFitConfig config) {
        java.util.List<String> w = new java.util.ArrayList<String>();
        int nFrames = frameIndices(imp, config).length;

        if (nFrames < 50 && config.autoBaseline) {
            w.add("Only " + nFrames + " frames. The automatic baseline uses the temporal minimum "
                    + "projection, which reads low on short stacks. Set the baseline explicitly.");
        }
        ImageStack stack = imp.getStack();
        if (stack instanceof VirtualStack) {
            w.add("Virtual stack: every frame is re-read from disk, so throughput will be limited "
                    + "by the file system rather than by the fitter. Consider loading into RAM.");
        }
        if (imp.isHyperStack() && imp.getNChannels() > 1) {
            w.add("Multi-channel hyperstack: only channel " + config.channel
                    + " will be processed. Run again for other channels, or split them first.");
        }
        if (imp.getBitDepth() == 8) {
            w.add("8-bit input. Camera data is normally 12- or 16-bit; if this was converted down "
                    + "from a higher depth the intensity quantisation may degrade both the width "
                    + "estimate and the background estimate.");
        }
        if (hasNonLinearCalibration(imp)) {
            w.add("The image has a NON-LINEAR intensity calibration function applied "
                    + "(Analyze > Calibrate). ImageJ applies it during conversion, so the fitter "
                    + "sees transformed pixel values. A non-linear transform changes the relative "
                    + "weights within a patch and therefore changes the moments: both the number "
                    + "of localisations and the width distribution shift. Remove the calibration "
                    + "function before fitting. Linear calibrations are harmless and are not "
                    + "reported here.");
        }
        return w.toArray(new String[0]);
    }
}
