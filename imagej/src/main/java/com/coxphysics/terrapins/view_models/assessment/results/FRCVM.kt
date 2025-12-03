package com.coxphysics.terrapins.view_models.assessment.results

import com.coxphysics.terrapins.models.assessment.results.FRC
import com.coxphysics.terrapins.views.frc.ResultsView
import ij.ImagePlus

class FRCVM private constructor(
    private val title_: String,
    private val mdoel_: FRC?,
    private val image_a_: ImagePlus?,
    private val image_b_: ImagePlus?,
    private var results_view_: ResultsView?,
    private var info_: String,
    private var show_plot_: Boolean,
    private var show_images_: Boolean)
{
    companion object
    {
        @JvmStatic
        fun empty(title: String): FRCVM
        {
            return FRCVM(title, null, null, null, null, "", false, false)
        }

        @JvmStatic
        fun from(title: String, model: FRC): FRCVM
        {
            val results = model.results()
            val results_view = results?.let{ r -> ResultsView.with(r, title)}
            return FRCVM(title, model, model.image_a(), model.image_b(), results_view ,  model.info(), false, false)
        }
    }

    fun title(): String
    {
        return title_
    }

    fun info(): String
    {
        return info_
    }

    fun show_images(value: Boolean)
    {
        show_images_ = value
        if (show_images_)
        {
            image_a_?.show()
            image_b_?.show()
        }
        else
        {
            image_a_?.hide()
            image_b_?.hide()
        }
    }

    fun show_results(value: Boolean)
    {
        show_plot_ = value
        if (show_plot_)
        {
            results_view_?.show()
        }
        else
        {
            results_view_?.hide()
        }
    }
}