package com.coxphysics.terrapins.view_models.assessment

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.reports.Report
import java.nio.file.Path

class ReportVM private constructor(private val model_ : Report)
{
    companion object
    {
        @JvmStatic
        fun from(model: Report) : ReportVM
        {
            return ReportVM(model)
        }

        @JvmStatic
        fun from_results(model: AssessmentResults) : ReportVM
        {
            return from(Report.from(model))
        }
    }

    fun data_path(): Path
    {
        return model_.data_path()
    }

    fun set_data_path(value: Path)
    {
        model_.set_data_path(value)
    }

    fun drift_assessment(): AssessmentVM
    {
        val assessment = model_.drift_assessment() ?: return AssessmentVM.empty("Drift")
        return AssessmentVM.from(assessment)
    }

    fun display_drift_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_drift_details()
        else
            model_.hide_drift_details()
    }

    fun magnification_assessment(): AssessmentVM
    {
        val assessment = model_.magnification_assessment() ?: return AssessmentVM.empty("Magnification")
        return AssessmentVM.from(assessment)
    }

    fun display_magnification_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_magnification_details()
        else
            model_.hide_magnification_details()
    }

     fun blinking_assessment(): AssessmentVM
    {
        val assessment = model_.blinking_assessment() ?: return AssessmentVM.empty("Blinking")
        return AssessmentVM.from(assessment)
    }

    fun display_blinking_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_blinking_details()
        else
            model_.hide_blinking_details()
    }

    fun sampling_assessment(): AssessmentVM
    {
        val assessment = model_.sampling_assessment() ?: return AssessmentVM.empty("Sampling")
        return AssessmentVM.from(assessment)
    }

    fun display_sampling_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_sampling_details()
        else
            model_.hide_sampling_details()
    }

    fun localisation_precision_assessment(): AssessmentVM
    {
        val assessment = model_.localisation_precision_assessment() ?: return AssessmentVM.empty("Localisation Precision")
        return AssessmentVM.from(assessment)
    }

    //    fun display_localisation_precision_details(show_details: Boolean)
//    {
//    }

    fun frc_resolution_assessment(): AssessmentVM
    {
        val assessment = model_.frc_resolution_assessment() ?: return AssessmentVM.empty("Frc Resolution")
        return AssessmentVM.from(assessment)
    }

    fun display_frc_resolution_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_frc_resolution_details()
        else
            model_.hide_frc_resolution_details()
    }

    fun bias_assessment(): AssessmentVM
    {
        val assessment = model_.bias_assessment() ?: return AssessmentVM.empty("Bias")
        return AssessmentVM.from(assessment)
    }


    fun display_bias_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_bias_details()
        else
            model_.hide_bias_details()
    }

    fun squirrel_assessment(): AssessmentVM
    {
        val assessment = model_.squirrel_assessment() ?: return AssessmentVM.empty("SQUIRREL")
        return AssessmentVM.from(assessment)
    }


    fun display_squirrel_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_squirrel_details()
        else
            model_.hide_squirrel_details()
    }
}