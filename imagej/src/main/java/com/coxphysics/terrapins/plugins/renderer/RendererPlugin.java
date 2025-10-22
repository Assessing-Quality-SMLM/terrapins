package com.coxphysics.terrapins.plugins.renderer;

import com.coxphysics.terrapins.views.renderer.RendererDialog;
import com.coxphysics.terrapins.views.renderer.RendererDialogSettings;
import com.coxphysics.terrapins.models.renderer.Renderer;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class RendererPlugin  implements PlugIn
{
    private final RendererDialog dialog_ = RendererDialog.default_();

    @Override
    public void run(String s)
    {
        dialog_.showDialog();
        if (dialog_.wasCanceled())
            return;
        RendererDialogSettings settings =  dialog_.create_settings_recorded();
        if (!settings.has_localisation_path())
        {
            IJ.log("Localisation path has not been set");
            return;
        }
        ImagePlus image = Renderer.default_().render_localisations(settings.localisation_path(), settings.render_settings());
        if (image == null)
        {
            IJ.error("Cannot open rendered image");
            return;
        }
            image.show();
    }


    public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = RendererPlugin.class;
        java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        java.io.File file = new java.io.File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}