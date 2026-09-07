package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.log.Log
import com.coxphysics.terrapins.models.process.MacroRunner
import ij.ImagePlus
import java.nio.file.Files
import java.nio.file.Path

/**
 * ThunderSTORM, driven through the commands it registers with ImageJ.
 *
 * Presents the same seam as the built-in fitter - a stack in, a localisation table on disk - so
 * the one-click workflow does not know which one ran. What differs is only what each can do:
 * ThunderSTORM fits a PSF model and so reports a real per-localisation uncertainty, where the
 * moment fitter predicts one from the photon budget.
 *
 * **It is driven by macro command, not by calling its classes.** That is not a workaround; the
 * macro interface is ThunderSTORM's supported entry point, the one its own recorder emits, and it
 * is the only one that works without compiling against it. Two consequences worth knowing:
 *
 * - Nothing here imports a ThunderSTORM class, so TERRAPINS builds and runs whether or not
 *   ThunderSTORM is installed. [is_available] reports which, and the one-click tab uses that to
 *   say so before a run rather than failing partway through one.
 * - The commands are global, so this drives whichever ThunderSTORM ImageJ has registered. When a
 *   private copy is eventually bundled it must be reached differently - see
 *   imagej/PROVENANCE.md - and that is a change inside this class, not to the [Fitter] seam.
 *
 * ThunderSTORM keeps its results in one global table, so a run resets it first. Anything a user
 * had open there is replaced, which is worth knowing but is also what running an analysis does
 * from its own dialog.
 */
class ThunderStormFitter private constructor(
    private val equipment_: EquipmentSettings,
    private val settings_: ThunderStormSettings,
    private val photons_per_adu_: Double,
    private val emccd_: Boolean,
    private val runner_: MacroRunner,
    private val log_: Log<String>
) : Fitter
{
    companion object
    {
        /** A class present in every ThunderSTORM build, used only to test for its presence. */
        private const val PROBE_CLASS = "cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn"

        const val RUN_ANALYSIS = "Run analysis"
        const val CAMERA_SETUP = "Camera setup"
        const val EXPORT_RESULTS = "Export results"
        const val SHOW_RESULTS_TABLE = "Show results table"
        const val CSV_FORMAT = "CSV (comma separated)"

        @JvmStatic
        fun from(equipment: EquipmentSettings, settings: ThunderStormSettings,
                 photons_per_adu: Double, emccd: Boolean, log: Log<String>): ThunderStormFitter
        {
            return ThunderStormFitter(equipment, settings, photons_per_adu, emccd,
                MacroRunner.ij(), log)
        }

        /** For tests, which need to see the commands without ImageJ running them. */
        @JvmStatic
        fun with_runner(equipment: EquipmentSettings, settings: ThunderStormSettings,
                        photons_per_adu: Double, emccd: Boolean, runner: MacroRunner,
                        log: Log<String>): ThunderStormFitter
        {
            return ThunderStormFitter(equipment, settings, photons_per_adu, emccd, runner, log)
        }

        /** Whether ThunderSTORM is on the classpath at all. */
        @JvmStatic
        fun is_available(): Boolean
        {
            return try
            {
                Class.forName(PROBE_CLASS, false, ThunderStormFitter::class.java.classLoader)
                true
            }
            catch (e: ClassNotFoundException)
            {
                false
            }
        }
    }

    override fun name(): String = "ThunderSTORM"

    override fun parse_method_name(): String = ParseMethodName.THUNDERSTORM

    /** ThunderSTORM fits a PSF model, so its uncertainty is a fit result rather than a prediction. */
    override fun produces_uncertainty(): Boolean = true

    /**
     * Camera calibration, which decides whether coordinates come out in nanometres and whether
     * the uncertainty means anything.
     *
     * Without a gain ThunderSTORM still runs and still reports an uncertainty, but that number is
     * computed from intensities in camera units as though they were photons, so it is wrong by
     * roughly the square root of the gain. That is worse than the moment fitter's honest absence,
     * so the gain is passed whenever there is one and the run log says when there is not.
     */
    fun camera_options(): String
    {
        val options = StringBuilder()
        options.append("pixelsize=").append(equipment_.camera_pixel_size_nm())
        if (has_gain())
        {
            // ThunderSTORM asks for photons per A/D count under this name.
            options.append(" photons2adu=").append(photons_per_adu_)
        }
        if (emccd_)
        {
            options.append(" isemgain=true")
        }
        return options.toString()
    }

    private fun has_gain(): Boolean = photons_per_adu_ > 0.0 && !photons_per_adu_.isNaN()

    /**
     * Analysis options.
     *
     * Only the PSF width is set, from the instrument settings the workflow already has; every
     * module keeps ThunderSTORM's own default. Rendering is switched off because the assessment
     * renders from the table itself, and a second reconstruction here would be discarded - it
     * also avoids the asynchronous rendering that a macro can otherwise outrun.
     */
    fun analysis_options(): String
    {
        val sigma_px = sigma_px()
        return "filter=[Wavelet filter (B-Spline)] scale=2.0 order=3 " +
            "detector=[Local maximum] connectivity=8-neighbourhood threshold=std(Wave.F1) " +
            "estimator=[PSF: Integrated Gaussian] sigma=%.3f fitradius=3 method=[Weighted Least squares] "
                .format(sigma_px) +
            "renderer=[No Renderer]"
    }

    /** Expected PSF sigma in pixels, which is what ThunderSTORM's estimator wants. */
    fun sigma_px(): Double
    {
        val sigma_nm = equipment_.instrument_psf_fwhm_nm() / 2.3548200450309493
        return sigma_nm / equipment_.camera_pixel_size_nm()
    }

    /** Cross-correlation drift correction, applied to the results table in place. */
    fun drift_options(): String
    {
        return "action=drift magnification=5.0 method=[Cross correlation] save=false " +
            "steps=${settings_.drift_bins()} showcorrelations=false"
    }

    /** Merges repeated localisations of the same molecule across frames. */
    fun merge_options(): String
    {
        return "action=merge zcoordweight=0.1 offframes=${settings_.merge_off_frames()} " +
            "dist=${settings_.merge_distance_nm()} framespermolecule=0"
    }

    fun export_options(output: Path): String
    {
        // Square brackets are how a macro option carries a value containing spaces, which both
        // the format name and any ordinary file path will.
        return "filepath=[$output] fileformat=[$CSV_FORMAT] " +
            "id=false frame=true x=true y=true sigma=true intensity=true offset=true " +
            "bkgstd=true uncertainty=true detections=false saveprotocol=true"
    }

    override fun localise(image: ImagePlus, output: Path): Int?
    {
        if (!is_available())
        {
            log_.log("ThunderSTORM is not installed, so it cannot be used to localise. "
                    + "Install it, or choose the fast moment fitter.")
            return null
        }
        if (!has_gain())
        {
            log_.log("No camera gain given, so ThunderSTORM's uncertainty is computed from "
                    + "camera units as though they were photons and will be wrong by about the "
                    + "square root of the gain. Supply a gain, or use the fast moment fitter, "
                    + "which reports no uncertainty rather than a misleading one.")
        }

        return try
        {
            LocalisationTable.ensure_parent(output)
            runner_.run(CAMERA_SETUP, camera_options())

            // The table is global and additive; a stale one would be exported along with this run.
            runner_.run(SHOW_RESULTS_TABLE, "action=reset")

            log_.log("${image.title}: running ThunderSTORM")
            runner_.run_on(image, RUN_ANALYSIS, analysis_options())

            if (settings_.correct_drift())
            {
                log_.log("${image.title}: correcting drift")
                runner_.run(SHOW_RESULTS_TABLE, drift_options())
            }
            if (settings_.merge())
            {
                log_.log("${image.title}: merging repeated localisations")
                runner_.run(SHOW_RESULTS_TABLE, merge_options())
            }

            runner_.run(EXPORT_RESULTS, export_options(output))
            val count = written_row_count(output)
            if (count == null || count < 1)
            {
                log_.log("${image.title}: ThunderSTORM produced no localisations - check the "
                        + "pixel size and PSF width")
                return null
            }
            log_.log("${image.title}: $count localisations -> $output")
            count
        }
        catch (e: Exception)
        {
            log_.log("${image.title}: ThunderSTORM failed - ${e.message}")
            null
        }
    }

    /**
     * Counts data rows in the exported file.
     *
     * Taken from the file rather than from the results table because the file is what the
     * assessment will read, so this notices an export that silently wrote nothing.
     */
    private fun written_row_count(output: Path): Int?
    {
        if (!Files.exists(output))
        {
            log_.log("ThunderSTORM reported success but wrote no file at $output")
            return null
        }
        Files.newBufferedReader(output).use { reader ->
            var lines = 0
            while (reader.readLine() != null)
            {
                lines++
            }
            return if (lines < 1) 0 else lines - 1   // the header is not a localisation
        }
    }
}
