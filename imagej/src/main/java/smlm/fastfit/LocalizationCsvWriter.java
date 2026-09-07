package smlm.fastfit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Writes localisations as CSV in ThunderSTORM's format: its column names, in its order, with the
 * header row quoted and the data rows not, so a reader written for its files needs no special
 * case for ours.
 * <p>
 * Two columns cannot mean what a ThunderSTORM file's would. {@code sigma} is a coarse rank
 * estimate from a saturating moment rather than a fitted width — see {@link Localization} — and
 * {@code uncertainty_xy} is a placeholder zero on every row, because a moment method produces no
 * residual and so no goodness of fit. The protocol file written alongside says both.
 * <p>
 * The zero is written only because ThunderSTORM refuses to import a file with the field empty,
 * which would rule out comparing renderings. It is the more dangerous of the two caveats, since
 * an empty field announces itself and a zero does not: read as a precision it claims perfect
 * confidence, and any weighting by {@code 1/uncertainty} divides by zero. See
 * {@link #UNCERTAINTY_PLACEHOLDER}.
 * <p>
 * The width classification is no longer a column, since ThunderSTORM files have no such field and
 * a trailing extra would break a strict reader. It remains on {@link Localization}.
 * <p>
 * Values are formatted with an explicit {@link Locale#US} so a comma decimal separator can never
 * appear in a comma-separated file, and with a fixed number of significant figures rather than
 * {@code Double.toString}, whose output changed in JDK 19 and would otherwise make files written
 * by different Java versions differ textually for identical values.
 */
public final class LocalizationCsvWriter {

    private LocalizationCsvWriter() {
    }

    /**
     * ThunderSTORM's own column set for a 2D fit, in its order, so a reader written for its files
     * needs no special case for ours.
     * <p>
     * The unit on the photometric columns is {@code ADU}, which is ThunderSTORM's label for
     * uncalibrated camera units — the honest description of what this fitter produces. Writing
     * {@code photon} there instead, as a run with ThunderSTORM's camera setup configured would,
     * is the one difference a reader may notice, and claiming photons for values that have never
     * seen a gain calibration would be worse than the mismatch.
     */
    private static final String[] HEADERS = {
            "id", "frame", "x [nm]", "y [nm]", "sigma [nm]", "intensity [ADU]",
            "offset [ADU]", "bkgstd [ADU]", "uncertainty_xy [nm]"
    };

    /** Writes the table with no protocol file. */
    public static void write(List<Localization> results, File file) throws IOException {
        write(results, file, null);
    }

    /**
     * Writes the table, and if {@code protocol} is non-null a sidecar
     * {@code <name>-protocol.txt} recording how the run was configured — the same convention
     * ThunderSTORM uses, so the two are interchangeable to a reader.
     */
    public static void write(List<Localization> results, File file, String protocol)
            throws IOException {
        if (results == null) {
            throw new IllegalArgumentException("results must not be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create directory " + parent);
        }

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (int i = 0; i < HEADERS.length; i++) {
                if (i > 0) {
                    out.write(',');
                }
                out.write('"');
                out.write(HEADERS[i]);
                out.write('"');
            }
            out.write('\n');

            int id = 1;
            for (Localization r : results) {
                out.write(Integer.toString(id++));
                out.write(',');
                out.write(Integer.toString(r.getFrame()));
                out.write(',');
                out.write(num(r.getXNm()));
                out.write(',');
                out.write(num(r.getYNm()));
                out.write(',');
                out.write(sigma(r.getSigmaNm()));
                out.write(',');
                out.write(num(r.getIntensity()));
                out.write(',');
                out.write(num(r.getBackground()));
                out.write(',');
                out.write(num(r.getBackgroundStdDev()));
                out.write(',');
                out.write(uncertainty(r.getUncertaintyNm()));
                out.write('\n');
            }
        }

        if (protocol != null) {
            File protocolFile = protocolFileFor(file);
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(protocolFile), StandardCharsets.UTF_8))) {
                out.write("Fast moment fitter - run protocol\n");
                out.write("=================================\n\n");
                out.write(protocol);
                out.write("\nColumns follow ThunderSTORM's format, with two caveats.\n\n");
                out.write("sigma [nm] is a coarse rank estimate from a saturating moment, not a\n");
                out.write("fitted width. It orders spots correctly but compresses the range, so\n");
                out.write("do not use it for a numeric cut or for astigmatic z estimation.\n\n");
                out.write("A sigma of exactly 300.0000 nm is a placeholder, not a measurement:\n");
                out.write("the moments for that spot fell outside the calibrated envelope and\n");
                out.write("no width could be assigned. Its position and intensity are still\n");
                out.write("valid. Exclude these rows with sigma < 250 before using the width\n");
                out.write("for anything, or to count how many there were.\n\n");
                if (hasUncertainty(results)) {
                    out.write("uncertainty_xy [nm] is a precision PREDICTED from each spot's\n");
                    out.write("photon budget, not a per-fit residual - a moment method produces\n");
                    out.write("none. Two spots of equal brightness and background therefore get\n");
                    out.write("the same value whether or not either fit went well, and a spot\n");
                    out.write("corrupted by a close neighbour gets the same optimistic number as\n");
                    out.write("a clean one. Use it in aggregate; do not read a single value as a\n");
                    out.write("confidence interval on a single molecule.\n\n");
                    out.write("It assumes the nominal PSF width from the run settings, not the\n");
                    out.write("sigma column, and is scaled by a measured efficiency factor\n");
                    out.write("because a moment estimator does not reach the Thompson bound.\n\n");
                } else {
                    out.write("uncertainty_xy [nm] is ZERO on every row, and that zero is a\n");
                    out.write("placeholder, not a measurement: no camera gain was supplied, so\n");
                    out.write("the photon count that sets the precision is unknown. It is\n");
                    out.write("written only because ThunderSTORM will not import the file with\n");
                    out.write("the field empty. Supply photonsPerAdu to get a real estimate.\n\n");
                    out.write("Do not weight, filter or sort on it. Anything treating it as a\n");
                    out.write("precision - BaGoL, and the grouping steps in ThunderSTORM and\n");
                    out.write("Picasso - will read zero as perfect confidence, and any 1/sigma^2\n");
                    out.write("weighting will divide by zero. For ThunderSTORM's Gaussian\n");
                    out.write("rendering, set a fixed lateral uncertainty in the visualisation\n");
                    out.write("dialog instead of letting it read this column.\n\n");
                }
                out.write("intensity, offset and bkgstd are in raw camera units (ADU), not\n");
                out.write("photons. Positions and widths do not depend on them, so an unknown\n");
                out.write("gain cannot corrupt the coordinates; only the uncertainty column\n");
                out.write("needs the gain, and it is applied there alone.\n");
            }
        }
    }

    /**
     * Whether any row carries a real precision estimate, which decides which caveat the protocol
     * file records. Checked over the data rather than taken from the configuration so the file
     * always describes what was actually written.
     */
    private static boolean hasUncertainty(List<Localization> results) {
        for (Localization r : results) {
            double u = r.getUncertaintyNm();
            if (!Double.isNaN(u) && !Double.isInfinite(u)) {
                return true;
            }
        }
        return false;
    }

    /** {@code results.csv} maps to {@code results-protocol.txt}. */
    public static File protocolFileFor(File csv) {
        String name = csv.getName();
        int dot = name.lastIndexOf('.');
        String stem = (dot > 0) ? name.substring(0, dot) : name;
        return new File(csv.getAbsoluteFile().getParentFile(), stem + "-protocol.txt");
    }

    /**
     * Stands in for a width the moment fitter could not measure, in nanometres.
     * <p>
     * The width is unmeasurable when the moments fall outside the calibrated envelope — the case
     * that would otherwise be reported as an UNKNOWN width class — and an empty field there stops
     * readers loading the file at all. The value is chosen to be implausible for any real PSF
     * this fitter can handle, since it rejects an expected sigma above 3 px at construction and a
     * typical sigma is around 100 nm. So it is both obviously not a measurement and easy to
     * exclude, with a cut such as {@code sigma < 250}, which is also the only way to recover
     * which rows these were now that the width class is not a column.
     * <p>
     * Being constant rather than derived from the expected width matters: one filter expression
     * then works across datasets acquired with different optics.
     */
    private static final double SIGMA_PLACEHOLDER_NM = 300.0;

    /** The measured width when there is one, and the placeholder when the moments gave none. */
    private static String sigma(double v) {
        return (Double.isNaN(v) || Double.isInfinite(v)) ? num(SIGMA_PLACEHOLDER_NM) : num(v);
    }

    /**
     * Stands in for an uncertainty this fitter cannot produce.
     * <p>
     * An empty field would be the honest record, but ThunderSTORM's import rejects the file
     * outright, which makes comparing renderings impossible. Zero is therefore written so the
     * file loads. It is not a measurement and must not be treated as one: anything weighting
     * localisations by their precision reads zero as infinite confidence, and anything dividing
     * by it divides by zero. ThunderSTORM's Gaussian rendering in particular takes its blur width
     * from this column, so a fixed value should be set in its visualisation dialog rather than
     * left to read from here.
     */
    private static final String UNCERTAINTY_PLACEHOLDER = "0";

    /** The real uncertainty when a fitter provides one, and the placeholder when none exists. */
    private static String uncertainty(double v) {
        return (Double.isNaN(v) || Double.isInfinite(v)) ? UNCERTAINTY_PLACEHOLDER : num(v);
    }

    private static String num(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "";
        }
        return String.format(Locale.US, "%.4f", v);
    }
}
