package com.coxphysics.terrapins.views.squirrel

import com.coxphysics.terrapins.models.squirrel.external.Results
import ij.ImagePlus
import com.coxphysics.terrapins.views.hawkman.ResultsView as HawkmanResultsView

class ResultsView private constructor(private val results_: Results)
{
    private var error_map_ : ImagePlus? = null

    companion object
    {
        @JvmStatic
        fun from(results: Results): ResultsView
        {
            return ResultsView(results)
        }
    }

    private fun error_map() : ImagePlus?
    {
        if (HawkmanResultsView.is_empty(error_map_))
        {
            error_map_ = HawkmanResultsView.load_image(results_.error_map_path())
        }
        return error_map_
    }

    fun show()
    {
        error_map()?.show()
    }

    fun hide()
    {
        error_map_?.hide()
    }


}