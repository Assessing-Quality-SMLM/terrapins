package smlm.fastfit;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.FileInfo;
import ij.measure.ResultsTable;
import ij.plugin.ContrastEnhancer;
import ij.plugin.PlugIn;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ImageJ entry point for the fast fitter.
 *
 * <h3>How the image gets in</h3>
 * The plugin operates on the <b>currently active image window</b>. Open the stack first
 * (File &gt; Open, or File &gt; Import &gt; Image Sequence for a folder of single-frame TIFFs),
 * click its window so it is in front, then run the plugin. There is no file chooser: this is
 * the standard ImageJ convention and means the plugin composes with anything else that produces
 * an image, including macros.
 *
 * <h3>Two entry points</h3>
 * <ul>
 *   <li><b>simple</b> — asks only for pixel size and PSF width, then writes the CSV
 *       automatically beside the image and shows the results table.</li>
 *   <li><b>advanced</b> — exposes threshold, baseline, width bias correction and output
 *       choices.</li>
 * </ul>
 * The mode comes from the {@code arg} in {@code plugins.config}; anything other than
 * {@code "advanced"} gives the simple dialog.
 */
public class FastFitterPlugIn implements PlugIn {

    private static final String PREF = "fastfit.";

    @Override
    public void run(String arg) {
        boolean advanced = "advanced".equalsIgnoreCase(arg);

        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.error("Fast fitter",
                    "No image is open.\n \n"
                            + "This plugin works on the active image window. Open your camera\n"
                            + "stack first:\n \n"
                            + "   File > Open...                     for a multi-page TIFF\n"
                            + "   File > Import > Image Sequence...   for a folder of frames\n \n"
                            + "then select that window and run the plugin again.");
            return;
        }

        FastFitConfig cfg = new FastFitConfig();

        // Reject unusable input before showing a dialog the user cannot act on.
        try {
            ImageInput.validate(imp, cfg);
        } catch (IllegalArgumentException e) {
            IJ.error("Fast fitter", e.getMessage());
            return;
        }

        boolean showTable = true;
        File csv;

        if (advanced) {
            Object[] choices = showAdvancedDialog(imp, cfg);
            if (choices == null) {
                return;
            }
            showTable = (Boolean) choices[0];
            csv = (File) choices[1];
        } else {
            if (!showSimpleDialog(imp, cfg)) {
                return;
            }
            csv = defaultOutputFile(imp);
        }

        try {
            cfg.derive();
            ImageInput.validate(imp, cfg);
        } catch (IllegalArgumentException e) {
            IJ.error("Fast fitter", e.getMessage());
            return;
        }

        for (String warning : ImageInput.warnings(imp, cfg)) {
            IJ.log("[fastfit] Warning: " + warning);
        }
        savePrefs(cfg);

        long start = System.currentTimeMillis();
        FastLocalizer localizer = new FastLocalizer(cfg);
        List<Localization> results;
        try {
            results = localizer.run(imp);
        } catch (IllegalArgumentException e) {
            IJ.error("Fast fitter", e.getMessage());
            return;
        } catch (RuntimeException e) {
            IJ.error("Fast fitter", "Fitting failed: " + e);
            return;
        }
        long elapsed = Math.max(1, System.currentTimeMillis() - start);
        int nFrames = ImageInput.frameIndices(imp, cfg).length;

        IJ.log(String.format(
                "[fastfit] %d localisations from %d frames in %.2f s (%.0f frames/s), "
                        + "%d candidates rejected",
                results.size(), nFrames, elapsed / 1000.0,
                nFrames / (elapsed / 1000.0), localizer.getRejectedCount()));

        if (results.isEmpty()) {
            IJ.error("Fast fitter",
                    "No localisations were found.\n \n"
                            + "Common causes:\n"
                            + "   - PSF FWHM or pixel size wrong, so the expected spot width\n"
                            + "     does not match the data\n"
                            + "   - threshold too high (advanced dialog): try lowering it\n"
                            + "   - baseline wrong, leaving the image mostly at zero");
            return;
        }

        if (csv != null) {
            try {
                LocalizationCsvWriter.write(results, csv, localizer.describeRun());
                IJ.log("[fastfit] wrote " + csv.getAbsolutePath());
                IJ.showStatus("Saved " + csv.getName());
            } catch (IOException e) {
                IJ.error("Fast fitter",
                        "Could not write the CSV to\n" + csv.getAbsolutePath()
                                + "\n \n" + e.getMessage()
                                + "\n \nThe results table is still available.");
            }
        }

        if (cfg.renderAsh) {
            renderReconstruction(imp, cfg, results, csv);
        }

        if (showTable) {
            showResultsTable(results);
        }
    }

    /**
     * Builds and displays the average shifted histogram.
     * <p>
     * Rendering failure must not lose the run: the localisations are already written and shown,
     * so a problem here is reported and swallowed rather than propagated.
     */
    private void renderReconstruction(ImagePlus imp, FastFitConfig cfg,
                                      List<Localization> results, File csv) {
        try {
            List<Localization> toRender = results;
            if (cfg.ashNormalWidthOnly) {
                toRender = new ArrayList<Localization>(results.size());
                for (Localization r : results) {
                    if (r.getWidthClass() == Localization.WidthClass.NORMAL) {
                        toRender.add(r);
                    }
                }
                IJ.log("[fastfit] rendering " + toRender.size() + " of " + results.size()
                        + " localisations (NORMAL width class only)");
            }

            AshRenderer renderer = new AshRenderer(cfg.ashMagnification, cfg.ashShifts);
            ImagePlus ash = renderer.render(
                    toRender, imp.getWidth(), imp.getHeight(), cfg.pixelSizeNm);
            ash.setTitle(imp.getShortTitle() + " - ASH " + cfg.ashMagnification + "x");

            // Localisation density is extremely skewed: most pixels are empty and a few are
            // very bright, so the default full-range scaling renders almost black. Saturating
            // a fraction of a percent is what makes the structure visible.
            new ContrastEnhancer().stretchHistogram(ash, 0.15);
            IJ.log("[fastfit] " + renderer.describe(cfg.pixelSizeNm));

            if (GraphicsEnvironment.isHeadless()) {
                // Nothing can be shown, so write the reconstruction beside the CSV instead.
                // Without this the run would abort here with a HeadlessException *after* the
                // localisations were already written, which looks like a total failure.
                File target = reconstructionFileFor(csv, imp, cfg);
                if (new ij.io.FileSaver(ash).saveAsTiff(target.getAbsolutePath())) {
                    IJ.log("[fastfit] wrote " + target.getAbsolutePath());
                } else {
                    IJ.log("[fastfit] could not write " + target.getAbsolutePath());
                }
            } else {
                ash.show();
            }
        } catch (IllegalArgumentException e) {
            IJ.log("[fastfit] Could not render: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            IJ.log("[fastfit] Not enough memory to render at "
                    + cfg.ashMagnification + "x. Try a lower magnification.");
        } catch (RuntimeException e) {
            // Rendering is a convenience; never let it lose a completed run.
            IJ.log("[fastfit] Rendering failed (" + e + "). The localisations are unaffected.");
        }
    }

    /** Where a headless run puts the reconstruction: beside the CSV if there is one. */
    private static File reconstructionFileFor(File csv, ImagePlus imp, FastFitConfig cfg) {
        String name = sanitise(imp.getShortTitle()) + "-fastfit-ash"
                + cfg.ashMagnification + "x.tif";
        File directory = (csv != null)
                ? csv.getAbsoluteFile().getParentFile()
                : imageDirectory(imp);
        return new File(directory, name);
    }

    /**
     * The two-field dialog. Pixel size is pre-filled from the image's own spatial calibration
     * when it has one, otherwise from the last run.
     *
     * @return false if the user cancelled
     */
    private boolean showSimpleDialog(ImagePlus imp, FastFitConfig cfg) {
        if (isNonInteractive()) {
            return readFromMacroOptions(cfg, false);
        }
        double calibrated = ImageInput.calibratedPixelSizeNm(imp);
        double defaultPixelSize = !Double.isNaN(calibrated)
                ? calibrated
                : Prefs.get(PREF + "pixelSize", 100.0);

        GenericDialog gd = new GenericDialog("Fast moment fitting");
        gd.addMessage("Image: " + ImageInput.describe(imp, cfg));
        gd.addNumericField("Pixel_size_nm:", defaultPixelSize, 1);
        gd.addNumericField("PSF_FWHM_nm:", Prefs.get(PREF + "fwhm", 250.0), 1);
        gd.addNumericField("Render_magnification:",
                Prefs.get(PREF + "ashMag", AshRenderer.DEFAULT_MAGNIFICATION), 0);
        if (!Double.isNaN(calibrated)) {
            gd.addMessage("Pixel size pre-filled from the image calibration - please confirm.");
        }
        gd.addMessage("Typical PSF FWHM is 230-280 nm depending on wavelength and NA.\n \n"
                + "An average shifted histogram reconstruction is displayed at the\n"
                + "chosen magnification. Results will be written to:\n"
                + abbreviate(defaultOutputFile(imp)));
        gd.showDialog();
        if (gd.wasCanceled()) {
            return false;
        }
        cfg.pixelSizeNm = gd.getNextNumber();
        cfg.psfFwhmNm = gd.getNextNumber();
        cfg.ashMagnification = (int) gd.getNextNumber();
        cfg.renderAsh = cfg.ashMagnification >= 1;
        return true;
    }

    /**
     * The full dialog.
     *
     * @return {@code {showTable, csvFileOrNull}}, or null if the user cancelled
     */
    private Object[] showAdvancedDialog(ImagePlus imp, FastFitConfig cfg) {
        if (isNonInteractive()) {
            if (!readFromMacroOptions(cfg, true)) {
                return null;
            }
            String options = Macro.getOptions();
            boolean saveCsv = options == null || options.contains("save_csv");
            return new Object[]{false, saveCsv ? defaultOutputFile(imp) : null};
        }
        double calibrated = ImageInput.calibratedPixelSizeNm(imp);
        double defaultPixelSize = !Double.isNaN(calibrated)
                ? calibrated
                : Prefs.get(PREF + "pixelSize", 100.0);

        GenericDialog gd = new GenericDialog("Fast moment fitting (advanced)");
        gd.addMessage("Image: " + ImageInput.describe(imp, cfg));
        gd.addNumericField("Pixel_size_nm:", defaultPixelSize, 1);
        gd.addNumericField("PSF_FWHM_nm:", Prefs.get(PREF + "fwhm", 250.0), 1);
        gd.addNumericField("Threshold_sd:", Prefs.get(PREF + "threshold", 3.0), 1);
        gd.addCheckbox("Scale_threshold_to_frame_size",
                Prefs.get(PREF + "scaleThreshold", cfg.scaleThresholdWithFrameSize));
        gd.addNumericField("Min_local_SNR:", Prefs.get(PREF + "minSnr", cfg.minLocalSnr), 1);
        if (imp.isHyperStack()) {
            gd.addNumericField("Channel:", 1, 0);
            gd.addNumericField("Z_slice:", 1, 0);
        }
        gd.addCheckbox("Auto_baseline",
                Prefs.get(PREF + "autoBaseline", true));
        gd.addNumericField("Baseline:", Prefs.get(PREF + "baseline", 0.0), 1);
        gd.addNumericField("Width_bias_px:", Prefs.get(PREF + "widthBias", 0.0), 3);
        gd.addNumericField("Photons_per_ADU:", Prefs.get(PREF + "photonsPerAdu", 0.0), 4);
        gd.addCheckbox("EMCCD_camera", Prefs.get(PREF + "emccd", false));
        gd.addNumericField("Render_magnification:",
                Prefs.get(PREF + "ashMag", AshRenderer.DEFAULT_MAGNIFICATION), 0);
        gd.addNumericField("Render_shifts:",
                Prefs.get(PREF + "ashShifts", AshRenderer.DEFAULT_SHIFTS), 0);
        gd.addCheckbox("Render_normal_width_only", false);
        gd.addCheckbox("Show_table", true);
        gd.addCheckbox("Save_csv", true);
        gd.addMessage("Min local SNR rejects patches no brighter than their own background\n"
                + "ring's noise. On sparse 512x512 frames it took background detections\n"
                + "from 110 per frame to 16 with no loss of emitters above 4.5x the noise;\n"
                + "dimmer emitters do start to be lost. 0 disables it.");
        gd.addMessage("Scale threshold to frame size keeps one threshold setting meaning the\n"
                + "same sensitivity on any sensor. Without it, background detections grow\n"
                + "with sensor area: 5 per frame at 128x128 became 110 at 512x512.");
        gd.addMessage("The width column is a coarse rank estimate, not a fitted sigma.\n"
                + "Use it for narrow / normal / wide screening only.");
        gd.addMessage("Photons per ADU is the camera gain, and is only used to predict\n"
                + "localisation precision - it cannot move a localisation. Leave at 0\n"
                + "if unknown and the uncertainty column is left empty. The prediction\n"
                + "comes from the photon budget, not from a fit residual, so it says\n"
                + "nothing about whether an individual fit went wrong.");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return null;
        }

        cfg.pixelSizeNm = gd.getNextNumber();
        cfg.psfFwhmNm = gd.getNextNumber();
        cfg.thresholdFactor = gd.getNextNumber();
        cfg.scaleThresholdWithFrameSize = gd.getNextBoolean();
        cfg.minLocalSnr = gd.getNextNumber();
        if (imp.isHyperStack()) {
            cfg.channel = (int) gd.getNextNumber();
            cfg.zSlice = (int) gd.getNextNumber();
        }
        cfg.autoBaseline = gd.getNextBoolean();
        cfg.baselineOffset = gd.getNextNumber();
        cfg.widthBiasCorrectionPx = gd.getNextNumber();
        // Zero or blank means "not supplied"; the config carries that as NaN. A dialog cannot
        // express an empty numeric field, and no real camera has a gain of zero.
        double photonsPerAdu = gd.getNextNumber();
        cfg.photonsPerAdu = (photonsPerAdu > 0) ? photonsPerAdu : Double.NaN;
        cfg.emccd = gd.getNextBoolean();
        cfg.ashMagnification = (int) gd.getNextNumber();
        cfg.ashShifts = (int) gd.getNextNumber();
        cfg.ashNormalWidthOnly = gd.getNextBoolean();
        cfg.renderAsh = cfg.ashMagnification >= 1;
        boolean showTable = gd.getNextBoolean();
        boolean saveCsv = gd.getNextBoolean();

        return new Object[]{showTable, saveCsv ? defaultOutputFile(imp) : null};
    }

    /**
     * True when no dialog can or should be shown: either a macro supplied the parameters, or
     * there is no display at all.
     * <p>
     * The check matters because ImageJ 1.x builds the AWT dialog inside the
     * {@code GenericDialog} constructor, before it looks at the macro options. So a plugin that
     * constructs one unconditionally throws {@code HeadlessException} under
     * {@code --headless}, even when a macro has supplied every parameter. Reading the options
     * directly avoids constructing a dialog at all, which makes batch runs work without needing
     * Fiji's ij1-patcher.
     */
    private static boolean isNonInteractive() {
        return Macro.getOptions() != null || GraphicsEnvironment.isHeadless();
    }

    /**
     * Fills {@code cfg} from a macro options string, e.g.
     * {@code "pixel_size_nm=100 psf_fwhm_nm=250"}.
     *
     * @return false if required parameters are missing
     */
    private boolean readFromMacroOptions(FastFitConfig cfg, boolean advanced) {
        String options = Macro.getOptions();
        if (options == null) {
            IJ.error("Fast fitter",
                    "No display is available and no macro options were supplied.\n \n"
                            + "When running headless, pass the parameters, for example:\n"
                            + "  run(\"Fast moment fitting\", "
                            + "\"pixel_size_nm=100 psf_fwhm_nm=250\");");
            return false;
        }
        cfg.pixelSizeNm = numeric(options, "pixel_size_nm", Double.NaN);
        cfg.psfFwhmNm = numeric(options, "psf_fwhm_nm", Double.NaN);
        if (Double.isNaN(cfg.pixelSizeNm) || Double.isNaN(cfg.psfFwhmNm)) {
            IJ.error("Fast fitter",
                    "Macro options must supply both pixel_size_nm and psf_fwhm_nm.\n \n"
                            + "Received: " + options);
            return false;
        }
        if (advanced) {
            cfg.thresholdFactor = numeric(options, "threshold_sd", cfg.thresholdFactor);
            cfg.minLocalSnr = numeric(options, "min_local_snr", cfg.minLocalSnr);
            // Checkboxes follow GenericDialog's macro semantics: present means on, absent means
            // off. So a headless run must pass scale_threshold_to_frame_size explicitly to keep
            // the frame-size correction, exactly as it must pass auto_baseline.
            cfg.scaleThresholdWithFrameSize =
                    options.contains("scale_threshold_to_frame_size");
            cfg.channel = (int) numeric(options, "channel", cfg.channel);
            cfg.zSlice = (int) numeric(options, "z_slice", cfg.zSlice);
            cfg.baselineOffset = numeric(options, "baseline", cfg.baselineOffset);
            cfg.widthBiasCorrectionPx =
                    numeric(options, "width_bias_px", cfg.widthBiasCorrectionPx);
            cfg.autoBaseline = options.contains("auto_baseline");
            cfg.ashNormalWidthOnly = options.contains("render_normal_width_only");
        }
        cfg.ashMagnification =
                (int) numeric(options, "render_magnification", cfg.ashMagnification);
        cfg.ashShifts = (int) numeric(options, "render_shifts", cfg.ashShifts);
        cfg.renderAsh = cfg.ashMagnification >= 1;
        return true;
    }

    private static double numeric(String options, String key, double fallback) {
        String value = Macro.getValue(options, key, null);
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            IJ.log("[fastfit] Warning: could not parse " + key + "=" + value
                    + ", using " + fallback);
            return fallback;
        }
    }

    /**
     * Where the CSV goes: beside the source image if its directory is known, otherwise the
     * working directory ImageJ was started in.
     * <p>
     * A numeric suffix is added rather than overwriting, so re-running with different settings
     * never silently destroys an earlier result.
     */
    static File defaultOutputFile(ImagePlus imp) {
        File directory = imageDirectory(imp);
        String stem = sanitise(imp.getShortTitle()) + "-fastfit";
        File candidate = new File(directory, stem + ".csv");
        for (int i = 2; candidate.exists() && i < 1000; i++) {
            candidate = new File(directory, stem + "-" + i + ".csv");
        }
        return candidate;
    }

    /** The image's own directory, or the process working directory if it has none. */
    private static File imageDirectory(ImagePlus imp) {
        FileInfo info = imp.getOriginalFileInfo();
        if (info != null && info.directory != null && !info.directory.isEmpty()) {
            File d = new File(info.directory);
            if (d.isDirectory() && d.canWrite()) {
                return d;
            }
        }
        String dir = IJ.getDirectory("current");
        if (dir != null) {
            File d = new File(dir);
            if (d.isDirectory() && d.canWrite()) {
                return d;
            }
        }
        return new File(System.getProperty("user.dir", "."));
    }

    /** Strips characters that are awkward in filenames on any of the three platforms. */
    private static String sanitise(String name) {
        String cleaned = (name == null) ? "" : name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return cleaned.isEmpty() ? "image" : cleaned;
    }

    /** Shortens a path for display so the dialog does not stretch off screen. */
    private static String abbreviate(File file) {
        String path = file.getAbsolutePath();
        return (path.length() <= 60) ? path : "..." + path.substring(path.length() - 57);
    }

    private void savePrefs(FastFitConfig cfg) {
        Prefs.set(PREF + "pixelSize", cfg.pixelSizeNm);
        Prefs.set(PREF + "fwhm", cfg.psfFwhmNm);
        Prefs.set(PREF + "threshold", cfg.thresholdFactor);
        Prefs.set(PREF + "minSnr", cfg.minLocalSnr);
        Prefs.set(PREF + "scaleThreshold", cfg.scaleThresholdWithFrameSize);
        Prefs.set(PREF + "autoBaseline", cfg.autoBaseline);
        Prefs.set(PREF + "baseline", cfg.baselineOffset);
        Prefs.set(PREF + "widthBias", cfg.widthBiasCorrectionPx);
        // NaN would not round-trip through Prefs, so the "not supplied" case is stored as 0,
        // which is what the dialog shows for it and what the reader turns back into NaN.
        Prefs.set(PREF + "photonsPerAdu",
                Double.isNaN(cfg.photonsPerAdu) ? 0.0 : cfg.photonsPerAdu);
        Prefs.set(PREF + "emccd", cfg.emccd);
        Prefs.set(PREF + "ashMag", cfg.ashMagnification);
        Prefs.set(PREF + "ashShifts", cfg.ashShifts);
    }

    private void showResultsTable(List<Localization> results) {
        ResultsTable rt = new ResultsTable();
        for (Localization r : results) {
            rt.incrementCounter();
            rt.addValue("frame", r.getFrame());
            rt.addValue("x [nm]", r.getXNm());
            rt.addValue("y [nm]", r.getYNm());
            rt.addValue("sigma [nm]", r.getSigmaNm());
            rt.addValue("intensity", r.getIntensity());
            rt.addValue("offset", r.getBackground());
            rt.addValue("width_class", r.getWidthClass().name());
        }
        rt.show("Fast fitter results");
    }
}
