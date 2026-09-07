package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.hawk.HAWK
import com.coxphysics.terrapins.models.localisations.LocalisationFile
import com.coxphysics.terrapins.models.localisations.ParseMethod
import com.coxphysics.terrapins.models.log.IJLog
import com.coxphysics.terrapins.models.log.Log
import ij.ImagePlus
import java.nio.file.Path
import com.coxphysics.terrapins.models.hawk.Settings as HawkSettings

/**
 * Raw image stack in, full assessment out.
 *
 * The existing localisation workflow already reduces to one command once it has two tables: the
 * localisations, and the localisations of the same data after HAWK pre-processing. Everything
 * else - rendering, the three FRC splits, HAWKMAN, SQUIRREL, the reports - happens inside the
 * assessment executable. What stopped that being one click was only that the user had to produce
 * those two tables themselves: run HAWK, save the stream to disk, localise it in another package,
 * come back. Every step an opportunity to stop.
 *
 * This class closes that gap. HAWK already runs in-process, and the stream is a virtual stack
 * generated frame by frame, so it can be handed straight to a fitter without ever touching disk.
 *
 * The raw stack is used twice: once to localise, and once more as the SQUIRREL image stack. That
 * second use is easy to miss, and missing it costs a whole report - SQUIRREL needs something to
 * compare the reconstruction against, and with no widefield supplied the average of the raw
 * frames is that something. It is a fair reference precisely because it is the same photons the
 * localisations came from, so it captures non-linearity in the reconstruction without any of the
 * registration and intensity-matching problems a separately acquired widefield brings.
 *
 * It deliberately produces [AssessmentSettings] rather than running the assessment itself. The
 * existing workflow, macro recording and results viewer all already work from those settings, so
 * the one-click path joins the pipeline where the localisation workflow already starts instead of
 * becoming a second route to the same place.
 */
class OneClick private constructor(
    private val fitter_: Fitter,
    private val log_: Log<String>
)
{
    /** Levels the stream was actually built with, which may be fewer than were asked for. */
    private var effective_hawk_levels_ = 0

    companion object
    {
        @JvmStatic
        fun with(fitter: Fitter, log: Log<String>): OneClick = OneClick(fitter, log)

        @JvmStatic
        fun with(fitter: Fitter): OneClick = OneClick(fitter, IJLog.new())
    }

    /**
     * The outcome of a run. The HAWK table is optional: without it the bias and limiting
     * precision reports cannot be produced, but everything driven by the raw table still can, so
     * a failure there degrades the report rather than ending the run.
     */
    class Result internal constructor(
        private val settings_: AssessmentSettings,
        private val n_raw_: Int,
        private val n_hawk_: Int?,
        private val hawk_levels_: Int
    )
    {
        fun settings(): AssessmentSettings = settings_
        fun raw_localisation_count(): Int = n_raw_
        fun hawk_localisation_count(): Int? = n_hawk_
        fun has_hawk(): Boolean = n_hawk_ != null

        /** Levels the HAWK stream was built with, which may be fewer than were requested. */
        fun hawk_levels(): Int = hawk_levels_
    }

    /**
     * Localises [raw] and its HAWK stream, writing both tables under [working_directory], and
     * returns settings ready to hand to the assessment.
     *
     * @param hawk_levels number of HAWK decomposition levels.
     * @return null only if the raw stack could not be localised at all, which leaves nothing to
     *         assess. A HAWK failure returns settings without the HAWK table.
     */
    fun prepare(raw: ImagePlus, equipment: EquipmentSettings, working_directory: Path,
                hawk_levels: Int): Result?
    {
        log_.log("One-click: ${raw.title}, ${raw.stackSize} frames, fitter: ${fitter_.name()}")
        log_.log("The raw stack will also be averaged into a widefield for the SQUIRREL "
                + "non-linearity analysis")
        if (!fitter_.produces_uncertainty())
        {
            // Said once, here, rather than left for the reader to infer from a report that
            // quietly assumed a precision.
            log_.log("${fitter_.name()} reports no localisation precision, so the limiting "
                    + "precision report will state an assumed value rather than a measured one.")
        }

        val raw_table = LocalisationTable.raw_in(working_directory)
        val n_raw = fitter_.localise(raw, raw_table)
        if (n_raw == null)
        {
            log_.log("One-click: could not localise the raw stack, nothing to assess")
            return null
        }

        val n_hawk = localise_hawk(raw, working_directory, hawk_levels)

        val settings = AssessmentSettings.with(working_directory)
        settings.set_equipment_settings(equipment)
        settings.set_localisation_file(localisation_file(raw_table))
        settings.set_image_stack(DiskOrImage.from_image(Image.from(raw)))
        if (n_hawk != null)
        {
            settings.set_hawk_localisation_file(
                localisation_file(LocalisationTable.hawk_in(working_directory)))
            // HAWKMAN's level count is deliberately left at its default and is NOT the HAWK
            // level count. The two are unrelated despite the shared word:
            //
            //   HAWK levels    - depth of the temporal decomposition, so how many difference
            //                    scales the stream contains. Three by default, and bounded by
            //                    the number of frames.
            //   HAWKMAN levels - how many blur scales the bias analysis walks looking for the
            //                    one at which the two reconstructions agree. Twenty by default,
            //                    and nothing to do with the stream.
            //
            // Setting the second from the first restricted HAWKMAN to three blur levels, far too
            // coarse a ladder to find the crossing point, so the bias score never approached one
            // and every level scored zero - a blank bias report from a run that had otherwise
            // worked.
        }
        return Result(settings, n_raw, n_hawk, effective_hawk_levels_)
    }

    /**
     * Generates the HAWK stream and localises it.
     *
     * The stream is roughly `2 * levels` times as long as the raw stack, and each of its frames
     * is regenerated from the raw data on access, so this is the expensive half of a one-click
     * run by a wide margin. It is also the half the bias assessment depends on, which is why a
     * failure here is logged and survived rather than propagated.
     */
    private fun localise_hawk(raw: ImagePlus, working_directory: Path, levels: Int): Int?
    {
        // Asking for more levels than the stack supports silently yields a partial decomposition
        // rather than an error, so the discrepancy has to be caught here or the assessment is
        // told a level count the data does not contain. See HawkLevels.
        val effective = HawkLevels.effective(levels, raw.stackSize)
        if (effective < levels)
        {
            log_.log("HAWK: $levels levels requested but ${raw.stackSize} frames only support "
                    + "$effective; using $effective. More frames are needed for a deeper "
                    + "decomposition.")
        }
        if (effective < 1)
        {
            // A safety net rather than a path anyone will take: real SMLM acquisitions are some
            // hundreds of frames at an absolute minimum, so a stack too short to difference at
            // all means something is wrong with the input rather than with the experiment.
            log_.log("HAWK needs at least 2 frames, so no bias assessment for this stack")
            return null
        }
        effective_hawk_levels_ = effective

        val hawk_settings = HawkSettings.default()
        hawk_settings.set_image(raw)
        hawk_settings.set_n_levels(effective)

        val stream = HAWK.new(hawk_settings, log_).get_hawk_image()
        if (stream == null)
        {
            log_.log("HAWK stream could not be generated, so no bias assessment")
            return null
        }
        log_.log("HAWK stream: ${stream.stackSize} frames from ${raw.stackSize}")
        val count = fitter_.localise(stream, LocalisationTable.hawk_in(working_directory))
        if (count == null)
        {
            log_.log("HAWK stream could not be localised, so no bias assessment")
        }
        return count
    }

    private fun localisation_file(path: Path): LocalisationFile
    {
        // The fitter says how it writes; nothing here assumes ThunderSTORM's format, so a fitter
        // writing something else needs no change on this side.
        val parse_method = ParseMethod.default_()
        return LocalisationFile.new(path.toString(), parse_method)
    }
}
