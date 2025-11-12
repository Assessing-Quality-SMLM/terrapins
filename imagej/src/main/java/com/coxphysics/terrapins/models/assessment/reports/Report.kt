package com.coxphysics.terrapins.models.assessment.reports

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.frc.FRCResult
import com.coxphysics.terrapins.views.frc.ResultsView
import java.nio.file.Path

class Report private constructor(private val results_: AssessmentResults)
{
    private var blinking_assessment_ : Assessment? = null

    private var drift_results_ : FRCResult? = null
    private var drift_view_: ResultsView? = null

    private var zip_results_ : FRCResult? = null
    private var zip_view_: ResultsView? = null

    companion object
    {
        @JvmStatic
        fun from(results: AssessmentResults): Report
        {
            return Report(results)
        }
    }

    fun data_path(): Path
    {
        return results_.data_path()
    }

    fun set_data_path(path: Path)
    {
        results_.set_data_path(path)
        cache_data()
    }

    fun blinking_assessment(): Assessment?
    {
        return blinking_assessment_
    }

    fun show_blinking_details()
    {
        zip_view_?.show()
        drift_view_?.show()
    }

    fun hide_blinking_details()
    {
        zip_view_?.hide();
        drift_view_?.hide();
    }

    private fun cache_data()
    {
        blinking_assessment_ = results_.blinking_assessment()
        zip_results_ = results_.zip_split_results()
        zip_view_ = zip_results_?.let { r ->  ResultsView.with(r, "Zip Split") }
        drift_results_ = results_.drift_split_results()
        drift_view_ = drift_results_?.let { r -> ResultsView.with(r, "Drift Split") }
    }
}