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

    fun localisation_precision_assessment(): AssessmentVM?
    {
        val assessment = model_.localisation_precision_assessment() ?: return null
        return AssessmentVM.from(assessment)
    }

    fun blinking_assessment(): AssessmentVM?
    {
        val assessment = model_.blinking_assessment() ?: return null
        return AssessmentVM.from(assessment)
    }

    fun display_blining_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_blinking_details()
        else
            model_.hide_blinking_details()
    }

    fun bias_assessment(): AssessmentVM?
    {
        val assessment = model_.bias_assessment() ?: return null
        return AssessmentVM.from(assessment)
    }

    fun frc_resolution_assessment(): AssessmentVM?
    {
        val assessment = model_.frc_resolution_assessment() ?: return null
        return AssessmentVM.from(assessment)
    }

    fun display_bias_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_bias_details()
        else
            model_.hide_bias_details()
    }

    fun display_frc_resolution_details(show_details: Boolean)
    {
        if (show_details)
            model_.show_frc_resolution_details()
        else
            model_.hide_frc_resolution_details()
    }
}