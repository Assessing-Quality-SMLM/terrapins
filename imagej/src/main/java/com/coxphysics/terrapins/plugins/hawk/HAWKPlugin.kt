package com.coxphysics.terrapins.plugins.hawk

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
        if (image != null)
            settings_.set_image(image)
        return FLAGS;
    }

    override fun run(p0: ImageProcessor?)
    {
        if(cancelled_)
            return
        val view = get_new_image();
        if (view == null)
            return
        view.show()
    }

    override fun showDialog(p0: ImagePlus?, p1: String?, p2: PlugInFilterRunner?): Int
    {
        if (MacroUtils.is_ran_from_macro())
        {
            settings_ = Settings.extract_from_macro()
        }
        else
        {
            val view = HAWKView.from(HAWKVM.from(settings_))
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

    private fun get_new_image(): ImagePlus?
    {
        val p_stream = create_p_stream()
        if (p_stream == null)
            return null
        val view = ImagePlus("JHAWK pstream", p_stream)
        view.calibration = get_calibration(settings_)
        val metadata = p_stream._metadata;
        view.setProp("hawk_metadata", metadata)
        return view
    }

    private fun create_p_stream(): PStream?
    {
        return PStream.from(settings_)
    }

    private fun get_calibration(settings: Settings): Calibration?
    {
        val image = settings.image()
        if (image == null)
            return null
        val base = image.getCalibration().copy()
        base.frameInterval = 0.0
        return base;
    }

    override fun setNPasses(p0: Int)
    {
        // no-op
    }
}