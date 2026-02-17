package com.coxphysics.terrapins.plugins.assessment.workflow

import com.coxphysics.terrapins.models.assessment.Assessment
import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.process.ImageJLoggingRunner
import com.coxphysics.terrapins.view_models.assessment.ReportVM
import com.coxphysics.terrapins.views.assessment.results.ReportView
import com.coxphysics.terrapins.views.assessment.workflow.Dialog
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.awt.Dimension
import java.io.File
import com.coxphysics.terrapins.views.assessment.results.Dialog.Companion as ResultsDialog

class Plugin : PlugIn
{
    private val dialog_ = Dialog.new()

    companion object
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
        dialog_.showDialog()
        if (dialog_.wasCanceled())
            return
        val settings = dialog_.create_settings_record()
        if (settings == null)
        {
            IJ.log("Could not create settings")
            return
        }
        val pre_processing_complete = dialog_.pre_processing_complete()
        if (pre_processing_complete == null)
        {
            IJ.log("UI is null - this should never happen - contact the developer")
            return
        }
        if (!pre_processing_complete)
        {
            IJ.log("Pre-processing not complete")
            return
        }

        val results = get_results(settings)
        if (results == null)
        {
            IJ.log("Assessment failed")
            return
        }
        val view_model = ReportVM.from_results(results)
        val view = ReportView.from(view_model)
        view.preferredSize = Dimension(400, 400)
        view.pack()
        view.isVisible = true
    }

    private fun get_results(settings: Settings) : AssessmentResults?
    {
        val assessment = Assessment.default_()
        val runner = ImageJLoggingRunner.new()
        val use_localisations = dialog_.use_localisations() ?: return null
        if (use_localisations)
        {
            return assessment.run_localisations(runner, settings.localisation_settings())
        }
        else
        {
            return assessment.run_images(runner, settings.images_settings())
        }
    }
}