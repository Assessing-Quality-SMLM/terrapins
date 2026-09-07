package smlm.fastfit;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs detection and fitting over an image stack.
 * <p>
 * Holds no global state, so several instances can run concurrently — a deliberate contrast with
 * ThunderSTORM, whose camera settings and threshold state are process-wide statics.
 *
 * <h3>Typical use</h3>
 * <pre>{@code
 * FastFitConfig cfg = new FastFitConfig(100.0, 250.0);   // pixel size nm, PSF FWHM nm
 * FastLocalizer loc = new FastLocalizer(cfg);
 * List<Localization> results = loc.run(imp);
 * LocalizationCsvWriter.write(results, new File("out.csv"), loc.describeRun());
 * }</pre>
 *
 * To substitute ThunderSTORM detection later, pass a different {@link SpotDetector} to the
 * three-argument constructor; nothing else changes.
 */
public class FastLocalizer {

    private final FastFitConfig config;
    private final SpotDetector detector;
    private final SubPixelFitter fitter;
    private double measuredBaseline = Double.NaN;
    private int rejectedCount;
    private String inputDescription = "";

    /** Uses the built-in DoG detector and the moment fitter. */
    public FastLocalizer(FastFitConfig config) {
        this(config, null, null);
    }

    /**
     * @param config  settings; derived fields are filled in
     * @param detector detector to use, or null for the built-in DoG
     * @param fitter   fitter to use, or null for the moment fitter
     */
    public FastLocalizer(FastFitConfig config, SpotDetector detector, SubPixelFitter fitter) {
        this.config = config.clone().derive();
        this.fitter = (fitter != null) ? fitter : new MomentFitter(this.config);
        if (detector != null) {
            this.detector = detector;
        } else {
            DoGDetector dog = new DoGDetector(
                    this.config.dogSigmaInner, this.config.dogSigmaOuter,
                    this.config.thresholdFactor);
            dog.setScaleThresholdWithFrameSize(this.config.scaleThresholdWithFrameSize);
            this.detector = dog;
        }
    }

    public FastFitConfig getConfig() {
        return config.clone();
    }

    public SpotDetector getDetector() {
        return detector;
    }

    public SubPixelFitter getFitter() {
        return fitter;
    }

    /** Candidates rejected by the fitter during the last run. */
    public int getRejectedCount() {
        return rejectedCount;
    }

    /** Baseline actually used in the last run, in camera units. */
    public double getMeasuredBaseline() {
        return measuredBaseline;
    }

    /**
     * Detects and fits every slice of {@code imp}.
     *
     * @return localisations sorted by frame, then in detection order within a frame
     */
    public List<Localization> run(ImagePlus imp) {
        if (imp == null) {
            throw new IllegalArgumentException("imp must not be null");
        }
        ImageInput.validate(imp, config);
        final ImageStack stack = imp.getStack();
        final int[] sliceIndices = ImageInput.frameIndices(imp, config);
        final int nFrames = sliceIndices.length;
        if (nFrames == 0) {
            return Collections.emptyList();
        }
        this.inputDescription = ImageInput.describe(imp, config);

        measuredBaseline = config.autoBaseline
                ? estimateBaseline(stack, sliceIndices)
                : config.baselineOffset;

        final AtomicInteger rejected = new AtomicInteger();
        final AtomicInteger done = new AtomicInteger();
        final List<Localization> results =
                Collections.synchronizedList(new ArrayList<Localization>());

        final ThreadLocal<SpotDetector> localDetector = ThreadLocal.withInitial(
                detector::threadLocalCopy);
        final ThreadLocal<SubPixelFitter> localFitter = ThreadLocal.withInitial(
                fitter::threadLocalCopy);

        if (config.numThreads == 1) {
            for (int f = 0; f < nFrames; f++) {
                processFrame(stack, sliceIndices[f], f + 1,
                        localDetector.get(), localFitter.get(), results, rejected);
                progress(done.incrementAndGet(), nFrames);
            }
        } else {
            ExecutorService pool =
                    Executors.newFixedThreadPool(Math.min(config.numThreads, nFrames));
            try {
                List<Future<Void>> futures = new ArrayList<Future<Void>>(nFrames);
                for (int f = 0; f < nFrames; f++) {
                    final int slice = sliceIndices[f];
                    final int frame = f + 1;
                    futures.add(pool.submit((Callable<Void>) () -> {
                        processFrame(stack, slice, frame, localDetector.get(), localFitter.get(),
                                results, rejected);
                        progress(done.incrementAndGet(), nFrames);
                        return null;
                    }));
                }
                for (Future<Void> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while fitting", e);
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof RuntimeException) {
                            throw (RuntimeException) cause;
                        }
                        throw new RuntimeException("Fitting failed", cause);
                    }
                }
            } finally {
                pool.shutdownNow();
            }
        }

        rejectedCount = rejected.get();
        results.sort(Comparator.comparingInt(Localization::getFrame));
        if (config.reportProgress) {
            IJ.showProgress(1.0);
            IJ.showStatus("");
        }
        return results;
    }

    private void processFrame(ImageStack stack, int slice, int frame,
                              SpotDetector det, SubPixelFitter fit,
                              List<Localization> sink, AtomicInteger rejected) {
        ImageProcessor ip = stack.getProcessor(slice);
        FloatProcessor fp = (FloatProcessor) ip.convertToFloat();

        float[] px = (float[]) fp.getPixels();
        float base = (float) measuredBaseline;
        for (int i = 0; i < px.length; i++) {
            px[i] -= base;
        }

        List<SpotDetector.Candidate> candidates = det.detect(fp);
        List<Localization> frameResults = new ArrayList<Localization>(candidates.size());
        int localRejected = 0;
        for (SpotDetector.Candidate c : candidates) {
            Localization loc = fit.fit(fp, c, frame);
            if (loc == null) {
                localRejected++;
            } else {
                frameResults.add(loc);
            }
        }
        rejected.addAndGet(localRejected);
        sink.addAll(frameResults);
    }

    /**
     * Baseline from the temporal minimum projection.
     * <p>
     * A pixel's minimum over a long stack is a frame in which no emitter was active there, so
     * the median of the minimum projection tracks the camera offset rather than the fluorescence
     * background. On short stacks this biases low; below about 50 frames set the baseline
     * explicitly instead.
     */
    private double estimateBaseline(ImageStack stack, int[] sliceIndices) {
        int nFrames = sliceIndices.length;
        float[] min = null;
        int step = Math.max(1, nFrames / 200);
        for (int f = 0; f < nFrames; f += step) {
            FloatProcessor fp =
                    (FloatProcessor) stack.getProcessor(sliceIndices[f]).convertToFloat();
            float[] px = (float[]) fp.getPixels();
            if (min == null) {
                min = px.clone();
            } else {
                for (int i = 0; i < px.length; i++) {
                    if (px[i] < min[i]) {
                        min[i] = px[i];
                    }
                }
            }
        }
        if (min == null) {
            return 0.0;
        }
        if (nFrames < 50) {
            IJ.log("[fastfit] Warning: automatic baseline from only " + nFrames
                    + " frames may read low. Consider setting it explicitly.");
        }
        return MathUtil.median(min);
    }

    private void progress(int done, int total) {
        if (config.reportProgress) {
            IJ.showProgress((double) done / total);
            IJ.showStatus("Fast fitting: frame " + done + " of " + total);
        }
    }

    /** Multi-line record of how a run was configured, for a protocol file. */
    public String describeRun() {
        StringBuilder sb = new StringBuilder();
        sb.append("input               ").append(inputDescription).append('\n');
        sb.append("detector            ").append(detector.getName()).append('\n');
        sb.append("fitter              ").append(fitter.getName()).append('\n');
        sb.append(config.describe());
        sb.append(String.format("baseline used       %.2f (%s)%n", measuredBaseline,
                config.autoBaseline ? "measured from stack" : "set explicitly"));
        if (fitter instanceof MomentFitter) {
            sb.append("calibration         ")
                    .append(((MomentFitter) fitter).getCalibration()).append('\n');
        }
        sb.append(String.format("candidates rejected %d%n", rejectedCount));
        return sb.toString();
    }
}
