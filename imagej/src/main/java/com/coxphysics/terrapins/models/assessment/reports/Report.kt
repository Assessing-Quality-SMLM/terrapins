package com.coxphysics.terrapins.models.assessment.reports

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.results.FRC
import com.coxphysics.terrapins.models.assessment.results.Recon
import com.coxphysics.terrapins.views.frc.CalibrationView
import com.coxphysics.terrapins.views.frc.ResultsView
import java.nio.file.Path
import com.coxphysics.terrapins.models.hawkman.external.Results as HawkmanResults
import com.coxphysics.terrapins.models.squirrel.external.Results as SquirrelResults
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

    private var half_results_ : FRC? = null
    private var half_view_: ResultsView? = null

    private var drift_results_ : FRC? = null
    private var drift_view_: ResultsView? = null

    private var drift_report_view_: ResultsView? = null
    private var blinking_report_view_: ResultsView? = null

    private var zip_results_ : FRC? = null
    private var zip_view_: ResultsView? = null

    private var hawkman_results_ : HawkmanResults? = null
    private var hawkman_results_view_ : HawkmanResultsView? = null

    private var average_of_frames_squirrel_results_ : SquirrelResults? = null
    private var average_of_frames_squirrel_results_view_ : SquirrelResultsView? = null

    private var true_widefield_squirrel_results_ : SquirrelResults? = null
    private var true_widefield_squirrel_results_view_ : SquirrelResultsView? = null

    private var calibration_view_ : CalibrationView? = null

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

    fun recon(): Recon
    {
        return results_.recon()
    }

    fun hawk_recon(): Recon
    {
        return results_.hawk_recon()
    }

    fun half_split_results(): FRC?
    {
        return half_results_
    }

    fun drift_split_results(): FRC?
    {
        return drift_results_
    }

    fun zip_split_results(): FRC?
    {
        return zip_results_
    }

    fun drift_assessment(): Assessment?
    {
        return drift_assessment_
    }

    fun show_drift_report_details()
    {
        drift_report_view_?.show()
    }

    fun hide_drift_report_details()
    {
        drift_report_view_?.hide()
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
        blinking_report_view_?.show()
    }

    fun hide_blinking_details()
    {
        blinking_report_view_?.hide()
    }

    fun sampling_assessment(): Assessment?
    {
        return sampling_assessment_
    }

    fun show_sampling_details()
    {
        calibration_view_?.show()
    }

    fun hide_sampling_details()
    {
        calibration_view_?.hide()
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
        hawkman_results_view_?.show_details()
    }

    fun hide_bias_details()
    {
        hawkman_results_view_?.hide_details()
    }

    fun hawkman_results(): HawkmanResults?
    {
        return hawkman_results_
    }

    fun average_of_frames_results(): SquirrelResults?
    {
        return average_of_frames_squirrel_results_
    }

    fun average_of_frames_data(): Pair<SquirrelResults, SquirrelResultsView>?
    {
        if (average_of_frames_squirrel_results_ == null || average_of_frames_squirrel_results_view_ == null)
        {
            return null
        }
        return Pair(average_of_frames_squirrel_results_!!, average_of_frames_squirrel_results_view_!!)
    }

    fun true_widefield_results(): SquirrelResults?
    {
        return true_widefield_squirrel_results_
    }

    fun true_widefield_data(): Pair<SquirrelResults, SquirrelResultsView>?
    {
        if (true_widefield_squirrel_results_ == null || true_widefield_squirrel_results_view_ == null)
        {
            return null
        }
        return Pair(true_widefield_squirrel_results_!!, true_widefield_squirrel_results_view_!!)
    }

    fun squirrel_assessment(): Assessment?
    {
        return squirrel_assessment_
    }

    fun show_squirrel_details()
    {
        average_of_frames_squirrel_results_view_?.show_error_map(true)
        true_widefield_squirrel_results_view_?.show_error_map(true)
    }

    fun hide_squirrel_details()
    {
        average_of_frames_squirrel_results_view_?.show_error_map(false)
        true_widefield_squirrel_results_view_?.show_error_map(false)
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

        half_results_ = results_.half_split_results()
        val half_results = half_results_?.results()
        half_view_ = half_results?.let{ r -> ResultsView.with(r, "Half Split") }

        zip_results_ = results_.zip_split_results()
        val zip_results = zip_results_?.results()
        zip_view_ = zip_results?.let{ r ->  ResultsView.with(r, "Zip Split") }

        drift_results_ = results_.drift_split_results()
        val drift_results = drift_results_?.results()
        drift_view_ = drift_results?.let{ r ->  ResultsView.with(r, "Drift Split") }

        if (drift_results != null && half_results != null)
            drift_report_view_ = ResultsView.merged("Drift", "Drift", drift_results, "Half", half_results)
        if (drift_results != null && zip_results != null)
            blinking_report_view_ = ResultsView.merged("Blinking", "Drift", drift_results, "Zip", zip_results)

        hawkman_results_ = results_.hawkman_results()
        hawkman_results_view_ = hawkman_results_?.let{ r -> HawkmanResultsView.from(r)}

        average_of_frames_squirrel_results_ = results_.average_of_frames_squirrel_results()
        average_of_frames_squirrel_results_view_ = average_of_frames_squirrel_results_?.let{ r -> SquirrelResultsView.non_linear(r)}

        true_widefield_squirrel_results_ = results_.true_widefield_squirrel_results()
        true_widefield_squirrel_results_view_ = true_widefield_squirrel_results_?.let{ r -> SquirrelResultsView.true_widefield(r)}

        calibration_view_ = CalibrationView.from(results_.calibration_data());
    }
}