package com.coxphysics.terrapins.views.splitter;

import ij.gui.GenericDialog;

public class SplitterDialog extends GenericDialog
{

    private final SplitterUI splitter_ui_;

    private SplitterDialog(SplitterDialogSettings settings)
    {
        super("Splittter Settings");
        splitter_ui_ = SplitterUI.add_to_dialog(this, settings);
    }

    public static SplitterDialog with_settings(SplitterDialogSettings settings)
    {
        return new SplitterDialog(settings);
    }

    public static SplitterDialog default_()
    {
        return with_settings(SplitterDialogSettings.default_());
    }

    public SplitterDialogSettings create_settings_recorded()
    {
        return SplitterUI.create_settings_recorded(this);
    }

    public SplitterDialogSettings create_settings()
    {
        return splitter_ui_.create_settings();
    }
}
