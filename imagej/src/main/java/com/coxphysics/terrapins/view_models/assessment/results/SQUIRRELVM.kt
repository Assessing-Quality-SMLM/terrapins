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
        results_view_?.show_error_map(display)
    }

    fun show_sr_transform(display: Boolean)
    {
        results_view_?.show_sr_transform(display)
    }

    fun show_widefield(display: Boolean)
    {
        results_view_?.show_widefield(display)
    }

    fun show_big_widefield(display: Boolean)
    {
        results_view_?.show_big_widefield(display)
    }
}