package com.coxphysics.terrapins.views

import com.coxphysics.terrapins.models.assessment.Assessment
import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.TERRAPINS
import com.coxphysics.terrapins.models.log.Log
import com.coxphysics.terrapins.models.oneclick.FastFitterAdapter
import com.coxphysics.terrapins.models.oneclick.Fitter
import com.coxphysics.terrapins.models.oneclick.FitterChoice
import com.coxphysics.terrapins.models.oneclick.OneClick
import com.coxphysics.terrapins.models.oneclick.OneClickSettings
import com.coxphysics.terrapins.models.oneclick.ThunderStormFitter
import com.coxphysics.terrapins.views.utils.Utils
import javax.swing.SwingWorker

/**
 * Runs the one-click path: localise, HAWK, localise again, assess, show the results.
 *
 * A [SwingWorker] because every part of this is slow. The HAWK stream alone is several times the
 * length of the raw stack and each of its frames is regenerated on access, so a run is minutes
 * rather than seconds on real data - long enough that doing it on the event thread would freeze
 * ImageJ outright and look like a crash.
 *
 * Progress is published as it goes rather than saved for the end, because the only thing worse
 * than a long wait is a long wait with nothing on screen.
 */
class OneClickWorker private constructor(
    private val logger_: Log<String>,
    private val settings_: OneClickSettings
) : SwingWorker<Void, String>(), Log<String>
{
    companion object
    {
        @JvmStatic
        fun from(logger: Log<String>, settings: OneClickSettings): OneClickWorker
        {
            return OneClickWorker(logger, settings)
        }
    }

    override fun log(item: String)
    {
        publish(item)
    }

    private fun fitter(): Fitter
    {
        return when (settings_.fitter())
        {
            FitterChoice.FAST -> FastFitterAdapter.from(
                settings_.equipment(), settings_.photons_per_adu(), settings_.emccd(), this)
            FitterChoice.THUNDERSTORM -> ThunderStormFitter.from(
                settings_.equipment(), settings_.thunderstorm(), settings_.photons_per_adu(),
                settings_.emccd(), this)
        }
    }

    override fun doInBackground(): Void?
    {
        val image = settings_.image()
        val working_directory = settings_.working_directory()
        if (image == null || working_directory == null)
        {
            // The view disables Run in this state; this is the belt to that braces.
            log(settings_.error_string() ?: "Nothing to run")
            return null
        }

        // Said once, before the run, because both steps change what the assessment is an
        // assessment of rather than merely how it was computed.
        settings_.thunderstorm().post_processing_note()?.let {
            if (settings_.fitter() == FitterChoice.THUNDERSTORM) log(it)
        }

        val prepared = OneClick.with(fitter(), this)
            .prepare(image, settings_.equipment(), working_directory, settings_.hawk_levels())
        if (prepared == null)
        {
            log("One-click run stopped: no localisations to assess")
            return null
        }
        if (!prepared.has_hawk())
        {
            log("Continuing without the bias and limiting precision reports")
        }

        log("Running the assessment")
        val results = TERRAPINS.from(Assessment.default(), this)
            .run_localisations(prepared.settings())
        if (results == null)
        {
            log("The assessment produced no results - see the log above for which stage failed")
        }
        display_results(results)
        return null
    }

    override fun done()
    {
        try
        {
            get()   // re-throws anything doInBackground swallowed
        }
        catch (e: Exception)
        {
            val cause = e.cause ?: e
            System.err.println("[TERRAPINS] one-click FAILED: $cause")
            cause.printStackTrace()
            logger_.log("One-click run failed: $cause")
        }
    }

    override fun process(chunks: MutableList<String>?)
    {
        if (chunks == null)
        {
            return
        }
        for (item in chunks)
        {
            logger_.log(item)
        }
    }

    private fun display_results(results: AssessmentResults?)
    {
        if (results == null)
        {
            return
        }
        Utils.run_results_viewer(results)
    }
}
