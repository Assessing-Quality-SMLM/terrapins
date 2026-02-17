package com.coxphysics.terrapins.plugins.TERRAPINS

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.TERRAPINS
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.macros.MacroUtils
import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_GetFileFromResource
import com.coxphysics.terrapins.view_models.TERRAPINS.TERRAPINSVM
import com.coxphysics.terrapins.view_models.assessment.ReportVM
import com.coxphysics.terrapins.views.TERRAPINS.TERRAPINSTabView
import com.coxphysics.terrapins.views.TERRAPINS.TERRAPINSView
import com.coxphysics.terrapins.views.assessment.results.ReportView
import com.coxphysics.terrapins.views.utils.Utils
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.awt.Dimension
import java.io.File
import javax.swing.JFrame
import javax.swing.SwingUtilities

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
            val options = MacroOptions.default()
            if (options == null)
            {
//                // maybe dont do this as in macro land - what happens in headless mode
//                IJ.log("Could not construct macro options")
                return
            }
            settings_ = Settings.extract_from_macro_options(options)
            val results = run_assessment(settings_) // has side-effects to disk
            // we are in a macro so don't display the results viewer
        }
        else
        {
            val run_linear = false
            if (run_linear)
            {
                run_linear_view()
            }
            else
            {
                run_tabbed_view()
            }
        }
    }

    private fun run_tabbed_view()
    {
        val view_model = TERRAPINSVM.from(settings_)
        val view = TERRAPINSTabView.from(view_model)
        view.preferredSize = Dimension(400, 400)
        view.pack()
        SwingUtilities.invokeLater{
            view.isVisible = true
        }
        // wait on the semaphore
        view.was_canceled()
    }

    private fun run_linear_view()
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
        Utils.run_results_viewer(results)
    }

    private fun run_assessment(settings: Settings): AssessmentResults?
    {
        return TERRAPINS.default().run(settings)
    }
}