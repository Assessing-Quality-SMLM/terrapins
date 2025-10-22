package com.coxphysics.terrapins.plugins.frc;

import com.coxphysics.terrapins.models.frc.FRCResult;
import com.coxphysics.terrapins.views.frc.FrcDialog;
import com.coxphysics.terrapins.models.frc.FRCDialogSettings;
import com.coxphysics.terrapins.models.frc.Model;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

import java.io.File;
import java.net.URL;

public class FrcPlugin implements PlugIn
{
    private final FrcDialog dialog_ = FrcDialog.from(FRCDialogSettings.default_());

    @Override
    public void run(String s)
    {
        dialog_.showDialog();
        if (dialog_.wasCanceled())
            return;
        FRCDialogSettings dialog_settings = dialog_.create_settings_record();
        FRCResult result = Model.fire_from(dialog_settings);
        if (result == null)
            IJ.log("Could not calculate value with these settings");
        String message = result == null ? "Error calculating" : String.format("FIRE: %f", result.fire_number());
        IJ.showStatus(message);
    }

    public static void main(String[] args) throws Exception {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        // see: https://stackoverflow.com/a/7060464/1207769
        Class<?> clazz = FrcPlugin.class;
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        File file = new File(url.toURI());
        System.setProperty("plugins.dir", file.getAbsolutePath());

        // start ImageJ
        new ImageJ();

        IJ.runPlugIn(clazz.getName(), "");
    }
}
