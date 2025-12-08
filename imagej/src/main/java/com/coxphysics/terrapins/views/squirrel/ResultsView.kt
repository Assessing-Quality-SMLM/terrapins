package com.coxphysics.terrapins.views.squirrel

import com.coxphysics.terrapins.models.squirrel.external.Results
import com.coxphysics.terrapins.models.squirrel.utils.StackHelper
import com.coxphysics.terrapins.models.utils.IJUtils
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
        if (error_map_ == null)
        {
            error_map_ = results_.load_error_map()
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