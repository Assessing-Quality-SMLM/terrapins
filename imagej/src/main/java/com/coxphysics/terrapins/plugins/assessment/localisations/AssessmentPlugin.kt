package com.coxphysics.terrapins.plugins.assessment.localisations

import com.coxphysics.terrapins.models.assessment.Assessment
import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.process.ImageJLoggingRunner
import com.coxphysics.terrapins.views.assessment.localisations.AssessmentDialog
import com.coxphysics.terrapins.views.assessment.results.Dialog
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.io.File

class AssessmentPlugin : PlugIn
{
    private val dialog_ = AssessmentDialog.from(AssessmentSettings.default())

    companion object Factory
    {
        @JvmStatic
        fun main(args: Array<String>)
        {
            val clazz = AssessmentPlugin::class.java
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
        val results = Assessment.default_().run_localisations(ImageJLoggingRunner(), settings)
        if (results == null)
        {
            IJ.log("Assessment failed")
            return
        }
        val results_dialog = Dialog.from(results)
        results_dialog.update_outputs()
        results_dialog.showDialog()
    }
}