package com.coxphysics.terrapins.views.squirrel;

import com.coxphysics.terrapins.models.squirrel.SquirrelSettings;
import ij.gui.NonBlockingGenericDialog;

public class SquirrelDialog extends NonBlockingGenericDialog
{
    private static final String TITLE = "Calculate Error Map, RSE and RSP";

    private SquirrelUI ui_;

    private SquirrelDialog()
    {
        super(TITLE);
    }

    public static SquirrelDialog from(SquirrelSettings settings)
    {
        SquirrelDialog dialog = new SquirrelDialog();
        SquirrelUI ui = SquirrelUI.add_controls_to_dialog(dialog, settings);
        dialog.set_ui(ui);
        return dialog;
    }

    public static SquirrelDialog default_()
    {
        return new SquirrelDialog();
    }

    private void set_ui(SquirrelUI ui)
    {
        ui_ = ui;
    }

    public SquirrelSettings create_settings_record()
    {
        return ui_.creaate_settings_recorded(this);
    }
}
