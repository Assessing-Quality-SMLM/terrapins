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
}