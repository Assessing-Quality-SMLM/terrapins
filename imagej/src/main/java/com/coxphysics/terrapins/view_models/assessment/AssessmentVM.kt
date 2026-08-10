package com.coxphysics.terrapins.view_models.assessment

import com.coxphysics.terrapins.models.assessment.reports.Assessment
import com.coxphysics.terrapins.models.assessment.reports.Outcome
import java.awt.Color

class AssessmentVM private constructor(
    private val default_name_: String?,
    private val model_: Assessment?,
    private val has_results_: Boolean
)
{
    companion object
    {
        @JvmStatic
        fun empty(name: String, has_results: Boolean) : AssessmentVM
        {
            return AssessmentVM(name, null, has_results)
        }

        @JvmStatic
        fun from(model: Assessment, has_results: Boolean) : AssessmentVM
        {
            return AssessmentVM(null, model, has_results)
        }
    }

    fun has_results_to_display(): Boolean
    {
        return has_results_
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
        val colour = model_!!.colour()
        return when (colour)
        {
            Outcome.PASS -> Color.GREEN
            Outcome.FAIL -> Color.RED
            Outcome.INDETERMINATE -> Color.orange
            else -> null
        }
    }

    fun message(): String
    {
        if (is_empty())
            return ""
        return model_!!.message()
    }
}