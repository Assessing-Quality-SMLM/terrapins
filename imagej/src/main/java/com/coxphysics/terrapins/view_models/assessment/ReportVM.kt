package com.coxphysics.terrapins.view_models.assessment

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import java.nio.file.Path

class ReportVM private constructor(private val model_ : AssessmentResults)
{
    companion object
    {
        @JvmStatic
        fun from(model: AssessmentResults) : ReportVM
        {
            return ReportVM(model)
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
}