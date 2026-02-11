package com.coxphysics.terrapins.plugins.TERRAPINS

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.macros.MacroUtils
import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_GetFileFromResource
import com.coxphysics.terrapins.models.assessment.TERRAPINS
import com.coxphysics.terrapins.view_models.TERRAPINS.TERRAPINSVM
import com.coxphysics.terrapins.view_models.assessment.ReportVM
import com.coxphysics.terrapins.views.TERRAPINS.TERRAPINSView
import com.coxphysics.terrapins.views.assessment.results.ReportView
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.awt.Dimension
import java.io.File

class TERRAPINSPlugin : PlugIn
{
    private var settings_ : Settings = Settings.default()

    companion object
    {
        @JvmStatic
        fun main(args: Array<String>)
        {
            val clazz = TERRAPINSPlugin::class.java
            val url = clazz.getProtectionDomain().getCodeSource().getLocation();
            val file = File(url.toURI());

            System.setProperty("plugins.dir", file.getAbsolutePath());

            // start ImageJ
            ImageJ();
                    // run the plugin
            val reference = IJ.openImage(SQUIRREL_GetFileFromResource.getLocalFileFromResource("/HDVee.tif").getAbsolutePath());
            reference.show();
//            IJ.run("Record...")
            IJ.runPlugIn(clazz.getName(), "");
        }
    }

    override fun run(p0: String?)
    {
        if (MacroUtils.is_ran_from_macro())
        {
            settings_ = Settings.extract_from_macro()
            val results = run_assessment(settings_) // has side-effects to disk
            // we are in a macro so don't display the results viewer
        }
        else
        {
            val view_model = TERRAPINSVM.from(settings_)
            val view = TERRAPINSView.from(view_model)
            view.preferredSize = Dimension(400, 400)
            view.pack()
            // show the window - its modal - see ctor
            view.isVisible = true
            // view is modal dialog - executes when window is closed
            if (view.cancelled())
                return
            if (MacroUtils.is_recording())
            {
                settings_.record_to_macro()
            }
            val results = run_assessment(settings_)
            if (results == null)
            {
                IJ.log("Assessment failed")
                return
            }
            run_results_viewer(results)
        }
    }

    private fun run_assessment(settings: Settings): AssessmentResults?
    {
        return TERRAPINS.default().run(settings)
    }

    private fun run_results_viewer(results: AssessmentResults)
    {
        val report_view_model = ReportVM.from_results(results)
        val report_view = ReportView.from(report_view_model)
        report_view.preferredSize = Dimension(400, 400)
        report_view.pack()
        report_view.isVisible = true
    }

}