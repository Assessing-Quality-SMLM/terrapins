package com.coxphysics.terrapins.views.frc

import com.coxphysics.terrapins.models.frc.FRCResult
import com.coxphysics.terrapins.models.frc.Plotter
import ij.gui.Plot
import ij.gui.PlotWindow

class CalibrationView private constructor(private val results_ : List<Pair<String, FRCResult?>>)
{
    private var plot_: Plot? = null
    private var plot_window_: PlotWindow? = null

    companion object {
        @JvmStatic
        fun from(results: List<Pair<String, FRCResult?>>): CalibrationView {
            return CalibrationView(results)
        }
    }

    fun show() {
        if (plot_ == null) {
            val plot = Plotter.plot_sampling_calibration_curves(results_.toTypedArray())
            plot_ = plot
            plot_window_ = plot_!!.show()
        } else {
            plot_window_ = plot_!!.show()
        }
    }

    fun hide() {
        plot_window_?.close()
        plot_ = null
        plot_window_ = null
    }

    fun close() {
        hide()
    }
}