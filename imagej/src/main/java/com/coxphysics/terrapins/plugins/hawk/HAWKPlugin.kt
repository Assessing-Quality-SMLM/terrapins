package com.coxphysics.terrapins.plugins.hawk

import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_GetFileFromResource
import com.coxphysics.terrapins.views.hawk.HAWKView
import ij.IJ
import ij.ImageJ
import ij.ImagePlus
import ij.plugin.filter.ExtendedPlugInFilter
import ij.plugin.filter.PlugInFilter.*
import ij.plugin.filter.PlugInFilterRunner
import ij.process.ImageProcessor
import java.awt.Dimension
import java.io.File

private const val FLAGS =  STACK_REQUIRED or NO_CHANGES or DOES_16 or DOES_32 or DOES_8G

class HAWKPlugin : ExtendedPlugInFilter
{
    private var image_ : ImagePlus? = null
    private var n_frames_: Int? = null

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
            IJ.runPlugIn(clazz.getName(), "");
        }
    }

    override fun setup(s: String?, image: ImagePlus?): Int
    {
        image_ = image
        n_frames_ = image_?.stack?.size
        return FLAGS;
    }

    override fun run(p0: ImageProcessor?)
    {
//        var view = get_image_plus();
//        if (view == null)
//            return;
//        view.show();
    }

    override fun showDialog(p0: ImagePlus?, p1: String?, p2: PlugInFilterRunner?): Int
    {
        val view = HAWKView.from()
        view.preferredSize = Dimension(400, 400)
        view.pack()
        view.isVisible = true
        // exectues when window is closed
        return FLAGS
    }

    override fun setNPasses(p0: Int)
    {
        // no-op
    }
}