package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.log.Log
import ij.ImagePlus
import smlm.fastfit.FastFitConfig
import smlm.fastfit.FastLocalizer
import smlm.fastfit.LocalizationCsvWriter
import java.nio.file.Path

/**
 * The built-in fast moment fitter.
 *
 * Runs in this process, so a stack can be localised without leaving ImageJ and without the user
 * exporting anything. It writes ThunderSTORM's columns, which the assessment already reads.
 *
 * The one thing it cannot do on its own is report a localisation precision: a moment method has
 * no fit residual, so its uncertainty is predicted from each spot's photon budget instead, and
 * that prediction needs a camera gain the fitter cannot know. Without a gain the uncertainty
 * column is left absent and the report says the precision was assumed rather than measured.
 */
class FastFitterAdapter private constructor(
    private val equipment_: EquipmentSettings,
    private val photons_per_adu_: Double,
    private val emccd_: Boolean,
    private val log_: Log<String>
) : Fitter
{
    companion object
    {
        /**
         * @param photons_per_adu camera gain, or NaN if unknown. Only affects the predicted
         *                        precision; it cannot move a localisation.
         */
        @JvmStatic
        fun from(equipment: EquipmentSettings, photons_per_adu: Double, emccd: Boolean,
                 log: Log<String>): FastFitterAdapter
        {
            return FastFitterAdapter(equipment, photons_per_adu, emccd, log)
        }
    }

    override fun name(): String = "Fast moment fitter"

    override fun parse_method_name(): String = ParseMethodName.THUNDERSTORM

    override fun produces_uncertainty(): Boolean = photons_per_adu_ > 0.0 && !photons_per_adu_.isNaN()

    private fun config_for(image: ImagePlus): FastFitConfig
    {
        val config = FastFitConfig(equipment_.camera_pixel_size_nm(), equipment_.instrument_psf_fwhm_nm())
        config.photonsPerAdu = if (produces_uncertainty()) photons_per_adu_ else Double.NaN
        config.emccd = emccd_
        // The workflow renders through the assessment, which reads the table; a second
        // reconstruction here would be thrown away.
        config.renderAsh = false
        config.reportProgress = false
        config.numThreads = thread_count_for(image)
        return config
    }

    /**
     * One thread on a virtual stack, however many cores are free otherwise.
     *
     * `ij.VirtualStack.getProcessor` is not safe to call from several threads at once, and the
     * HAWK stream is a virtual stack over another stack that may itself be virtual - so a
     * multi-threaded run over it is reading shared, mutating reader state. The fitter is fast
     * enough that correctness is worth more than the cores here; this is the first thing to
     * revisit if the HAWK pass proves too slow, and the right fix is a cache in the stream
     * rather than unguarded threads.
     */
    private fun thread_count_for(image: ImagePlus): Int
    {
        val stack = image.stack
        if (stack != null && stack.isVirtual)
        {
            log_.log("${image.title}: virtual stack, localising single-threaded for safety")
            return 1
        }
        return Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
    }

    override fun localise(image: ImagePlus, output: Path): Int?
    {
        return try
        {
            val localizer = FastLocalizer(config_for(image))
            val results = localizer.run(image)
            if (results.isEmpty())
            {
                log_.log("${image.title}: no localisations found - check the pixel size and PSF width")
                return null
            }
            LocalisationTable.ensure_parent(output)
            LocalizationCsvWriter.write(results, output.toFile(), localizer.describeRun())
            log_.log("${image.title}: ${results.size} localisations -> $output")
            results.size
        }
        catch (e: Exception)
        {
            // Includes the fitter's own validation, which rejects a sampling it cannot fit
            // rather than returning nonsense. That message is worth showing verbatim.
            log_.log("${image.title}: localisation failed - ${e.message}")
            null
        }
    }
}
