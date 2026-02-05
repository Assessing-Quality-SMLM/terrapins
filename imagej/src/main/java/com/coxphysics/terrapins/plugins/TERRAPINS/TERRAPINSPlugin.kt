package com.coxphysics.terrapins.plugins.TERRAPINS

import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.macros.MacroUtils
import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_GetFileFromResource
import com.coxphysics.terrapins.view_models.TERRAPINS.TERRAPINSVM
import com.coxphysics.terrapins.views.TERRAPINS.TERRAPINSView
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.awt.Dimension
import java.io.File

class TERRAPINSPlugin : PlugIn
{
    private var settings_ : Settings = Settings.default()

    private var cancelled_ : Boolean = true

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
        }
        else
        {
            val view = TERRAPINSView.from(TERRAPINSVM.from(settings_))
            view.preferredSize = Dimension(400, 400)
            view.pack()
            // show the window - its modal - see ctor
            view.isVisible = true
            cancelled_ = view.cancelled()
            if (cancelled_)
                return
            // executes when window is closed
            if (MacroUtils.is_recording())
            {
                settings_.record_values()
            }
        }
    }

}