package com.coxphysics.terrapins.view_models.assessment

import com.coxphysics.terrapins.models.assessment.reports.Assessment
import java.awt.Color
import javax.swing.JPanel

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

    fun passed_text(): String
    {
        if (is_empty())
            return ""
        return if (model_!!.passed()) "Passed" else "Failed"
    }

    fun background_colour(): Color?
    {
        if (is_empty())
            return null
        if (model_!!.passed())
        {
            return Color.GREEN
        }
        return Color.RED
    }

    fun message(): String
    {
        if (is_empty())
            return ""
        return model_!!.message()
    }
}