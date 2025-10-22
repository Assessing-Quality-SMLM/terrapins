package com.coxphysics.terrapins.views.frc;

import com.coxphysics.terrapins.models.frc.FRCDialogSettings;
import ij.gui.NonBlockingGenericDialog;

import java.awt.event.ItemEvent;

public class FrcDialog extends NonBlockingGenericDialog
{
    private static final String TITLE = "Calculate Fourier Ring Correlation";

    private FrcUI ui_;

    private FrcDialog()
    {
        super(TITLE);
    }

    private void set_ui(FrcUI ui)
    {
        ui_ = ui;
    }

    public static FrcDialog from(FRCDialogSettings settings)
    {
        FrcDialog dialog = new FrcDialog();
        FrcUI ui = FrcUI.add_controls_to_dialog(dialog, settings);
        dialog.set_ui(ui);
        return dialog;
    }

    public FRCDialogSettings create_settings_record()
    {
        return ui_.create_settings_record(this);
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        ui_.handle_event(e);
    }
}
