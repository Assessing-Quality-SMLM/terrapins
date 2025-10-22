package com.coxphysics.terrapins.plugins.hawkman;

import com.coxphysics.terrapins.models.hawkman.HAWKMAN;
import com.coxphysics.terrapins.models.hawkman.Settings;
import com.coxphysics.terrapins.models.hawkman.external.Hawkman;
import com.coxphysics.terrapins.views.hawkman.HAWKMANDialog;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class HAWKMANPlugin implements PlugIn
{
    private final HAWKMANDialog dialog_ = HAWKMANDialog.default_();

    @Override
    public void run(String s)
    {
        dialog_.showDialog();
        if (dialog_.wasCanceled())
            return;
        Settings settings = dialog_.create_settings_recorded();
        Hawkman.run_with_settings(settings);
    }


    public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = HAWKMANPlugin.class;
        java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        java.io.File file = new java.io.File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        ImagePlus reference = IJ.openImage("C:\\Users\\k1651658\\Documents\\support\\images\\LMC-MT-ME-Raw16.tif");
        ImagePlus test = IJ.openImage("C:\\Users\\k1651658\\Documents\\support\\images\\LMC-MT-ME-HAWK16.tif");
        reference.show();
        test.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}
