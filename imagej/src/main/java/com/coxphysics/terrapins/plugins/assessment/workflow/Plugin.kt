package com.coxphysics.terrapins.plugins.assessment.workflow

import com.coxphysics.terrapins.views.assessment.workflow.Dialog
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.io.File

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
    }
}