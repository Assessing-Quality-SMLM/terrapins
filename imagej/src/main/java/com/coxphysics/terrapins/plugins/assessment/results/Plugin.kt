package com.coxphysics.terrapins.plugins.assessment.results

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.view_models.assessment.ReportVM
import com.coxphysics.terrapins.views.assessment.results.ReportView
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.awt.Dimension
import java.io.File

class Plugin : PlugIn
{
    companion object Factory
    {
        @JvmStatic
        fun main(args: Array<String>)
        {
            val clazz = Plugin::class.java
            val url = clazz.getProtectionDomain().getCodeSource().getLocation();
            val file = File(url.toURI());
            System.setProperty("plugins.dir", file.getAbsolutePath());

            // start ImageJ
            ImageJ();
            IJ.runPlugIn(clazz.getName(), "");
        }
    }

    override fun run(p0: String?)
    {
        val results = AssessmentResults.empty()
        val view_model = ReportVM.from_results(results)
        val view = ReportView.from(view_model)
        view.preferredSize = Dimension(400, 400)
        view.pack()
        view.isVisible = true
    }
}