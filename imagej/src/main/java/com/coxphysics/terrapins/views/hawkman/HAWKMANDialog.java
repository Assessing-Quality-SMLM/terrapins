package com.coxphysics.terrapins.views.hawkman;

import com.coxphysics.terrapins.models.hawkman.Settings;
import ij.gui.NonBlockingGenericDialog;
import java.awt.event.ItemEvent;

public class HAWKMANDialog extends NonBlockingGenericDialog
{
    private static final String TITLE = "HAWKMAN";

    private final HAWKMANUI ui_;

    private HAWKMANDialog(Settings settings)
    {
        super(TITLE);
        ui_ = HAWKMANUI.add_controls_to_dialog(this, settings);
    }
    
    public static HAWKMANDialog from(Settings settings)
    {
        return new HAWKMANDialog(settings);
    }

    public static HAWKMANDialog default_()
    {
        return from(Settings.default_());
    }

    public Settings create_settings_recorded()
    {
        return ui_.create_settings_recorded(this);
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        ui_.handle_event(e);
    }
}
