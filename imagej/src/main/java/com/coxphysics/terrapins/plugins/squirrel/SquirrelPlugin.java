package com.coxphysics.terrapins.plugins.squirrel;

import com.coxphysics.terrapins.models.squirrel.SquirrelSettings;
import com.coxphysics.terrapins.models.squirrel.external.Squirrel;
import com.coxphysics.terrapins.models.squirrel.tools.SQUIRREL_GetFileFromResource;
import com.coxphysics.terrapins.views.squirrel.SquirrelDialog;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class SquirrelPlugin implements PlugIn
{
    private final SquirrelDialog dialog_ = SquirrelDialog.from(SquirrelSettings.default_());

    @Override
    public void run(String s)
    {
        dialog_.showDialog();
        if (dialog_.wasCanceled())
            return;
        SquirrelSettings settings = dialog_.create_settings_record();
        com.coxphysics.terrapins.models.squirrel.Squirrel.run(settings);
//        Squirrel.Factory.default_().run(settings);
    }

    public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = SquirrelPlugin.class;
        java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        java.io.File file = new java.io.File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        // run the plugin
        // TODO: open reference and sr image from resources folder
//        ImagePlus reference = IJ.openImage(SQUIRREL_GetFileFromResource.getLocalFileFromResource("/reference.tif").getAbsolutePath());
        ImagePlus reference = IJ.openImage("/home/nik/Documents/support/squirrel/TestDataForNick/AVG_RawHD1-ds.tif");
        reference.show();
//        ImagePlus superresolution = IJ.openImage(SQUIRREL_GetFileFromResource.getLocalFileFromResource("/super-resolution.tif").getAbsolutePath());
        ImagePlus superresolution = IJ.openImage("/home/nik/Documents/support/squirrel/TestDataForNick/ASHRawSE3_X8.tif");
        superresolution.show();
        IJ.runPlugIn(clazz.getName(), "");
    }
}
