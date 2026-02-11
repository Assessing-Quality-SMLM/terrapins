package com.coxphysics.terrapins.plugins.hawk

import com.coxphysics.terrapins.models.hawk.HAWK
import com.coxphysics.terrapins.models.hawk.PStream
import com.coxphysics.terrapins.models.hawk.Settings
import com.coxphysics.terrapins.models.macros.MacroUtils
import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_GetFileFromResource
import com.coxphysics.terrapins.view_models.hawk.HAWKVM
import com.coxphysics.terrapins.views.hawk.HAWKView
import ij.IJ
import ij.ImageJ
import ij.ImagePlus
import ij.measure.Calibration
import ij.plugin.filter.ExtendedPlugInFilter
import ij.plugin.filter.PlugInFilter.*
import ij.plugin.filter.PlugInFilterRunner
import ij.process.ImageProcessor
import java.awt.Dimension
import java.io.File

private const val FLAGS =  STACK_REQUIRED or NO_CHANGES or DOES_16 or DOES_32 or DOES_8G

class HAWKPlugin : ExtendedPlugInFilter
{
    private var settings_ : Settings = Settings.default()
    private var cancelled_: Boolean = true

    companion object
    {
        @JvmStatic
        fun main(args: Array<String>)
        {
            val clazz = HAWKPlugin::class.java
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

    override fun setup(s: String?, image: ImagePlus?): Int
    {
        cancelled_ = true
        if (image != null)
            settings_.set_image(image)
        return FLAGS;
    }

    override fun run(p0: ImageProcessor?)
    {
        if(cancelled_)
            return
        HAWK.from(settings_).get_hawk_image() ?.show()
    }

    override fun showDialog(p0: ImagePlus?, p1: String?, p2: PlugInFilterRunner?): Int
    {
        if (MacroUtils.is_ran_from_macro())
        {
            settings_ = Settings.extract_from_macro()
            cancelled_ = false
        }
        else
        {
            val view_model = HAWKVM.from(settings_)
            val view = HAWKView.from(view_model)
            view.preferredSize = Dimension(400, 400)
            view.pack()
            // show the window - its modal - see ctor
            view.isVisible = true
            cancelled_ = !view.ok()
            if (cancelled_)
                return FLAGS;
            // executes when window is closed
            if (MacroUtils.is_recording())
            {
                settings_.record_values()
            }
        }
        return FLAGS
    }

    override fun setNPasses(p0: Int)
    {
        // no-op
    }
}