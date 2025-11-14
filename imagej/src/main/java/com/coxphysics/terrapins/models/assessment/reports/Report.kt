package com.coxphysics.terrapins.models.assessment.reports

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.frc.FRCResult
import com.coxphysics.terrapins.views.frc.ResultsView
import java.nio.file.Path
import com.coxphysics.terrapins.views.hawkman.ResultsView as HawkmanResultsView
import com.coxphysics.terrapins.views.squirrel.ResultsView as SquirrelResultsView

class Report private constructor(private val results_: AssessmentResults)
{
    private var drift_assessment_ : Assessment? = null
    private var magnification_assessment_ : Assessment? = null
    private var blinking_assessment_ : Assessment? = null
    private var sampling_assessment_ : Assessment? = null
    private var localisation_precision_assessment_ : Assessment? = null
    private var frc_resolution_assessment_ : Assessment? = null
    private var bias_assessment_ : Assessment? = null
    private var squirrel_assessment_ : Assessment? = null

    private var drift_results_ : FRCResult? = null
    private var drift_view_: ResultsView? = null

    private var zip_results_ : FRCResult? = null
    private var zip_view_: ResultsView? = null

    private var hawkman_results_ : HawkmanResultsView? = null

    private var squirrel_results_ : SquirrelResultsView? = null

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

    fun drift_assessment(): Assessment?
    {
        return drift_assessment_
    }

    fun show_drift_details()
    {
        drift_view_?.show()
    }

    fun hide_drift_details()
    {
        drift_view_?.hide()
    }

    fun magnification_assessment(): Assessment?
    {
        return magnification_assessment_
    }

    fun show_magnification_details()
    {
        drift_view_?.show()
    }

    fun hide_magnification_details()
    {
        drift_view_?.hide()
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
        zip_view_?.hide()
        drift_view_?.hide()
    }

    fun sampling_assessment(): Assessment?
    {
        return sampling_assessment_
    }

    fun show_sampling_details()
    {
    }

    fun hide_sampling_details()
    {
    }

    fun localisation_precision_assessment(): Assessment?
    {
        return localisation_precision_assessment_
    }

    fun frc_resolution_assessment(): Assessment?
    {
        return frc_resolution_assessment_
    }

    fun show_frc_resolution_details()
    {
        drift_view_?.show()
    }

    fun hide_frc_resolution_details()
    {
        drift_view_?.hide()
    }

    fun bias_assessment(): Assessment?
    {
        return bias_assessment_
    }

    fun show_bias_details()
    {
        hawkman_results_?.show_details()
    }

    fun hide_bias_details()
    {
        hawkman_results_?.hide_details()
    }

    fun squirrel_assessment(): Assessment?
    {
        return squirrel_assessment_
    }

    fun show_squirrel_details()
    {
        squirrel_results_?.show()
    }

    fun hide_squirrel_details()
    {
        squirrel_results_?.hide()
    }

    private fun cache_data()
    {
        drift_assessment_ = results_.drift_assessment()
        magnification_assessment_ = results_.magnification_assessment()
        blinking_assessment_ = results_.blinking_assessment()
        sampling_assessment_ = results_.sampling_assessment()
        localisation_precision_assessment_ = results_.localisation_precision_assessment()
        frc_resolution_assessment_ = results_.frc_resolution_assessment()
        bias_assessment_ = results_.bias_assessment()
        squirrel_assessment_ = results_.squirrel_assessment()

        zip_results_ = results_.zip_split_results()
        zip_view_ = zip_results_?.let { r ->  ResultsView.with(r, "Zip Split") }

        drift_results_ = results_.drift_split_results()
        drift_view_ = drift_results_?.let { r -> ResultsView.with(r, "Drift Split") }

        hawkman_results_ = results_.hawkman_results()?.let{ r -> HawkmanResultsView.from(r)}
        squirrel_results_ = results_.squirrel_results()?.let{r -> SquirrelResultsView.from(r)}
    }
}