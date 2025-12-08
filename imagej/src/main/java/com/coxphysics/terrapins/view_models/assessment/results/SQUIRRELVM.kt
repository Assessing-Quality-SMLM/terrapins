package com.coxphysics.terrapins.view_models.assessment.results

import com.coxphysics.terrapins.models.squirrel.external.Results
import com.coxphysics.terrapins.views.squirrel.ResultsView

class SQUIRRELVM private constructor(
    private val title_: String,
    private val results_: Results?,
    private val results_view_: ResultsView?,
)
{
    companion object
    {
        @JvmStatic
        fun from(title: String, model: Results) : SQUIRRELVM
        {
            return SQUIRRELVM(title, model, ResultsView.from(model))
        }

        @JvmStatic
        fun empty(title: String) : SQUIRRELVM
        {
            return SQUIRRELVM(title, null, null)
        }
    }

    fun title(): String
    {
        return title_
    }

    fun optimiser_text() : String
    {
        return results_?.optimiser_output() ?: return ""
    }

    fun show_map(display: Boolean)
    {
        if(display)
        {
            results_view_?.show()
        }
        else
        {
            results_view_?.hide()
        }
    }
}