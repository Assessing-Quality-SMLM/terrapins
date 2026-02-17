package com.coxphysics.terrapins.views

import com.coxphysics.terrapins.models.assessment.Assessment
import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.TERRAPINS
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.log.Log
import com.coxphysics.terrapins.views.utils.Utils
import javax.swing.SwingWorker

class AssessmentWorker private constructor(
    private val logger_: Log<String>,
    private val settings_: Settings
): SwingWorker<Void, String>(), Log<String>
{
    companion object
    {
        fun from(logger: Log<String>, settings: Settings): AssessmentWorker
        {
            return AssessmentWorker(logger, settings)
        }
    }

    override fun log(item: String)
    {
        publish(item)
    }

    override fun doInBackground(): Void?
    {
        val results = TERRAPINS.from(Assessment.default(), this).run(settings_)
        display_results(results)
        return null
    }

    override fun process(chunks: MutableList<String>?)
    {
        if (chunks == null)
            return
        for (item in chunks)
        {
            logger_.log(item)
        }
    }

    private fun display_results(results: AssessmentResults?)
    {
        if (results == null)
            return
        Utils.run_results_viewer(results);
    }
}