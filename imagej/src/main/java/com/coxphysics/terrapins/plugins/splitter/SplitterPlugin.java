package com.coxphysics.terrapins.plugins.splitter;

import com.coxphysics.terrapins.views.splitter.SplitterDialog;
import com.coxphysics.terrapins.views.splitter.SplitterDialogSettings;
import com.coxphysics.terrapins.models.localisations.Splitter;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

import java.util.List;

public class SplitterPlugin  implements PlugIn
{
    SplitterDialog dialog_ = SplitterDialog.default_();

    @Override
    public void run(String s)
    {
        dialog_.showDialog();
        if (dialog_.wasCanceled())
            return;
        SplitterDialogSettings settings =  dialog_.create_settings_recorded();
        if (!settings.has_localisation_path())
        {
            IJ.log("Localisation path has not been set");
            return;
        }
        Splitter splitter = Splitter.default_();

        if (!splitter.is_valid())
        {
            IJ.log("Cannot find splitter executable - please contact the developer");
            return;
        }
        IJ.showStatus("split started");
        List<String> localisation_files = splitter.split(settings.localisation_file(), settings.split_settings());
        IJ.showStatus("split finished");
        if (localisation_files == null)
        {
            IJ.log("Splitting failed");
        }
    }


    public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = SplitterPlugin.class;
        java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        java.io.File file = new java.io.File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}