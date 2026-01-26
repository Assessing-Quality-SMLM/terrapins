package com.coxphysics.terrapins.views.assessment

import ij.gui.Plot
import ij.gui.PlotWindow

class PlotManager private constructor(
    private var plot_: Plot?,
    private var plot_window_: PlotWindow?,
    private var plot_generator_: () -> Plot
)
{
    companion object
    {
        @JvmStatic
        fun with(generator: () -> Plot) : PlotManager
        {
            return PlotManager(null, null, generator)
        }
    }

    fun show()
    {
        if (plot_ == null)
        {
            plot_ = plot_generator_()
        }
        plot_window_ = plot_!!.show()
    }

    fun hide()
    {
        plot_window_?.close()
        plot_ = null
        plot_window_ = null
    }
}