package com.coxphysics.terrapins.view_models.assessment

import com.coxphysics.terrapins.models.assessment.reports.Assessment
import java.awt.Color

class AssessmentVM private constructor(
    private val model_: Assessment
)
{
    companion object
    {
        @JvmStatic
        fun from(model: Assessment) : AssessmentVM
        {
            return AssessmentVM(model)
        }
    }

    fun name(): String
    {
        return model_.name()
    }

    fun score_text(): String
    {
        val score = model_.score()
        if (score == null)
            return "-"
        return String.format( "%.2f", score)
    }

    fun passed_text(): String
    {
        return if (model_.passed()) "Passed" else "Failed"
    }

    fun background_colour(): Color
    {
        if (model_.passed())
        {
            return Color.GREEN
        }
        return Color.RED
    }

    fun message(): String
    {
        return model_.message()
    }
}