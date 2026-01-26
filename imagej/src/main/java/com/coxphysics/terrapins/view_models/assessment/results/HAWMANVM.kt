package com.coxphysics.terrapins.view_models.assessment.results

import com.coxphysics.terrapins.models.hawkman.external.Results
import com.coxphysics.terrapins.views.hawkman.ResultsView

class HAWMANVM private constructor(
    private val model_ : Results?,
    private val results_view_ : ResultsView?
)
{
    companion object
    {
        @JvmStatic
        fun from(model: Results) : HAWMANVM
        {
            val results_view = ResultsView.from(model)
            return HAWMANVM(model, results_view)
        }

        @JvmStatic
        fun empty() : HAWMANVM
        {
            return HAWMANVM(null, null)
        }
    }

    fun show_combined_resolution_map(show_map: Boolean)
    {
        if (show_map)
        {
            results_view_?.show_combined_resolution_map()
        }
        else
        {
            results_view_?.hide_combined_resolution_map()
        }
    }

    fun show_scores(show_scores: Boolean)
    {
        if (show_scores)
        {
            results_view_?.show_scores()
        }
        else
        {
            results_view_?.hide_scores()
        }
    }

    fun show_confidence_map(show_map: Boolean)
    {
        if (show_map)
        {
            results_view_?.show_confidence_map()
        }
        else
        {
            results_view_?.hide_confidence_map()
        }
    }

    fun show_sharpening_map(show_map: Boolean)
    {
        if (show_map)
        {
            results_view_?.show_sharpening_map()
        }
        else
        {
            results_view_?.hide_sharpening_map()
        }
    }

    fun show_structure_map(show_map: Boolean)
    {
        if (show_map)
        {
            results_view_?.show_structure_map()
        }
        else
        {
            results_view_?.hide_structure_map()
        }
    }

    fun show_skeleton_map(show_map: Boolean)
    {
        if (show_map)
        {
            results_view_?.show_skeleton_map()
        }
        else
        {
            results_view_?.hide_skeleton_map()
        }
    }
}