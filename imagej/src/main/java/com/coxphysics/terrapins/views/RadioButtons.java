package com.coxphysics.terrapins.views;

import ij.ImagePlus;
import ij.gui.GenericDialog;

import java.awt.*;
import java.awt.Checkbox;
import java.util.Objects;

import static com.coxphysics.terrapins.views.Checkbox.*;

public class RadioButtons
{
    private final Label label_;
    private final Panel panel_;

    private RadioButtons(Label label, Panel panel)
    {
        this.label_ = label;
        this.panel_ = panel;
    }

    public static RadioButtons from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int idx = components.length - 2;
        Label label = (Label) components[idx];
        Panel panel = (Panel) components[idx + 1];
        return new RadioButtons(label, panel);
    }

    // for tests
    public Label label()
    {
        return label_;
    }

    // for tests
    public Panel panel()
    {
        return panel_;
    }

    public String get_choice()
    {
        Component[] panel_components = panel_.getComponents();
        for (Component component : panel_components)
        {
            Checkbox checkbox = (Checkbox) component;
            if (checkbox.getState())
                return checkbox.getLabel();
        }
        return null;
    }

    public void set_visibility(boolean value)
    {
        label_.setVisible(value);
        for (Component component : panel_.getComponents())
        {
            component.setVisible(value);
        }
        panel_.setVisible(value);
    }

    public void set_enabled(boolean value)
    {
        label_.setEnabled(value);
        for (Component component : panel_.getComponents())
        {
            component.setEnabled(value);
        }
        panel_.setEnabled(value);
    }

    public boolean is_button_group(Object object)
    {
        Component[] panel_components = panel_.getComponents();
        for (Component component : panel_components)
        {
            Checkbox checkbox = (Checkbox) component;
            if (from_checkbox(checkbox).is_checkbox(object))
                return true;
        }
        return false;
    }

    public boolean is_checked(String label)
    {
        Component[] panel_components = panel_.getComponents();
        for (Component component : panel_components)
        {
            com.coxphysics.terrapins.views.Checkbox checkbox = from_checkbox((Checkbox) component);
            if (checkbox.labels_match(label) && checkbox.is_checked())
                return true;
        }
        return false;
    }
}
