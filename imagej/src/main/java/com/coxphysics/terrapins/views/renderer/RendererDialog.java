package com.coxphysics.terrapins.views.renderer;

import ij.gui.GenericDialog;

import java.awt.event.ItemEvent;

public class RendererDialog extends GenericDialog
{
    private final RendererUI ui_;

    private RendererDialog(RendererDialogSettings settings)
    {
        super("Renderer Settings");
        ui_ = RendererUI.add_to_dialog(this, settings, true);
        ui_.set_extra_settings_visibility(false);
    }

    public static RendererDialog with_settings(RendererDialogSettings settings)
    {
        return new RendererDialog(settings);
    }

    public static RendererDialog default_()
    {
        return with_settings(RendererDialogSettings.default_());
    }

    public RendererDialogSettings create_settings_recorded()
    {
        return RendererUI.create_settings_recorded(this);
    }

     @Override
     public void itemStateChanged(ItemEvent e)
     {
         ui_.handle_event(e);
     }
}
