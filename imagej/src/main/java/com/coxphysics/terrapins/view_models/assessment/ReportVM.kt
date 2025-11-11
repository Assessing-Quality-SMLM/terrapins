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
}