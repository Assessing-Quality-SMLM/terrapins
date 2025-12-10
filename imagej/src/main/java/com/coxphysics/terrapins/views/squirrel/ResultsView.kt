package com.coxphysics.terrapins.views.squirrel

import com.coxphysics.terrapins.models.squirrel.external.Results
import ij.ImagePlus

class ResultsView private constructor(private val results_: Results)
{
    private var error_map_ : ImagePlus? = null
    private var widefield_ : ImagePlus? = null
    private var big_widefield_ : ImagePlus? = null
    private var sr_transform_ : ImagePlus? = null

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

    fun widefield(): ImagePlus?
    {
        if (widefield_ == null)
        {
            widefield_ = results_.load_widefield()
        }
        return widefield_
    }

    fun sr_transform(): ImagePlus?
    {
        if (sr_transform_ == null)
        {
            sr_transform_ = results_.load_sr_transform()
        }
        return sr_transform_
    }

    fun big_widefield(): ImagePlus?
    {
        if (big_widefield_ == null)
        {
            big_widefield_ = results_.load_big_widefield()
        }
        return big_widefield_
    }

    fun show_error_map(display: Boolean)
    {
        if(display)
        {
            error_map()?.show()
        }
        else
        {
            error_map_?.hide()
        }
    }

    fun show_sr_transform(display: Boolean)
    {
        if(display)
        {
            sr_transform()?.show()
        }
        else
        {
            sr_transform_?.hide()
        }
    }

    fun show_widefield(display: Boolean)
    {
        if(display)
        {
            widefield()?.show()
        }
        else
        {
            widefield_?.hide()
        }
    }

    fun show_big_widefield(display: Boolean)
    {
        if(display)
        {
            big_widefield()?.show()
        }
        else
        {
            big_widefield_?.hide()
        }
    }

}