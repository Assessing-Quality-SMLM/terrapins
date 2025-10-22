package com.coxphysics.terrapins.views.frc

import com.coxphysics.terrapins.models.frc.FRCResult
import com.coxphysics.terrapins.models.frc.Plotter
import ij.gui.Plot
import ij.gui.PlotWindow

class ResultsView private constructor(private val results_ : FRCResult,
                                      private val name_ : String?)
{
    private var plot_ : Plot? = null
    private var plot_window_ : PlotWindow? = null

    companion object
    {
        @JvmStatic
        fun from(results: FRCResult): ResultsView
        {
            return ResultsView(results, null)
        }

        @JvmStatic
        fun with(results: FRCResult, name: String): ResultsView
        {
            return ResultsView(results, name)
        }
    }

    fun show()
    {
        if (plot_ == null)
        {
            val plot = Plotter.named_plot(results_, name_)
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
}