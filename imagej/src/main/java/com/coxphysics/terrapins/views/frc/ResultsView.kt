package com.coxphysics.terrapins.views.frc

import com.coxphysics.terrapins.models.frc.FRCResult
import com.coxphysics.terrapins.models.frc.Plotter
import ij.gui.Plot
import ij.gui.PlotWindow

class ResultsView private constructor(
    private val title_ : String?,
    private val name_: String?,
    private val results_ : FRCResult,
    private val extra_name_: String?,
    private val extra_results_: FRCResult?)
{
    private var plot_ : Plot? = null
    private var plot_window_ : PlotWindow? = null

    companion object
    {
        @JvmStatic
        fun from(results: FRCResult): ResultsView
        {
            return ResultsView(null, null, results, null, null)
        }

        @JvmStatic
        fun with(results: FRCResult, title: String): ResultsView
        {
            return ResultsView(title, null, results, null, null)
        }

        @JvmStatic
        fun merged(title: String, name: String, results: FRCResult, extra_name: String, extra_results: FRCResult): ResultsView
        {
            return ResultsView(title, name, results, extra_name, extra_results)
        }
    }

    private fun single_plot(): Boolean
    {
        return extra_results_ == null;
    }

    fun show()
    {
        if (plot_ == null)
        {
            val plot = get_plot()
            plot_ = plot
            plot_window_ = plot_!!.show()
        }
        else
        {
            plot_window_ = plot_!!.show()
        }
    }

    fun hide()
    {
        plot_window_?.close()
        plot_ = null
        plot_window_ = null
    }

    fun close()
    {
        hide()
    }

    private fun get_plot(): Plot
    {
        if (single_plot())
            return Plotter.named_plot(results_, title_)
        return Plotter.merge(title_, Pair(name_, results_), Pair(extra_name_, extra_results_))
    }
}