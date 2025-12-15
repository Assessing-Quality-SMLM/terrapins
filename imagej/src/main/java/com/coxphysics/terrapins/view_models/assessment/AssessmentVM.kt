package com.coxphysics.terrapins.view_models.assessment

import com.coxphysics.terrapins.models.assessment.reports.Assessment
import java.awt.Color

class AssessmentVM private constructor(
    private val default_name_: String?,
    private val model_: Assessment?
)
{
    companion object
    {
        @JvmStatic
        fun empty(name: String) : AssessmentVM
        {
            return AssessmentVM(name, null)
        }

        @JvmStatic
        fun from(model: Assessment) : AssessmentVM
        {
            return AssessmentVM(null, model)
        }
    }

    private fun is_empty(): Boolean
    {
        return model_ == null
    }

    fun name(): String
    {
        if (is_empty())
            return default_name_!!
        return model_!!.name()
    }

    fun score_text(): String
    {
        if (is_empty())
            return "-"
        val score = model_?.score()
        if (score == null)
            return "-"
        return String.format( "%.2f", score)
    }

    fun outcome_text(): String
    {
        return model_?.outcome_label() ?: ""
    }

    fun background_colour(): Color?
    {
        if (is_empty())
            return null
        if (model_!!.passed())
        {
            return Color.GREEN
        }
        else if (model_.failed())
            return Color.RED
        else if (model_.indeterminate())
            return Color.orange
        return null
    }

    fun message(): String
    {
        if (is_empty())
            return ""
        return model_!!.message()
    }
}