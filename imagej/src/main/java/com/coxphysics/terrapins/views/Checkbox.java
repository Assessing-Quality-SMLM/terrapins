package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;

import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Objects;

public class Checkbox {

    private final java.awt.Checkbox checkbox_;

    private Checkbox(java.awt.Checkbox checkbox)
    {
        this.checkbox_ = checkbox;
    }

    public static Checkbox from_checkbox(java.awt.Checkbox checkbox)
    {
        return new Checkbox(checkbox);
    }

    public static Checkbox from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int idx = components.length - 1;
        java.awt.Checkbox checkbox = (java.awt.Checkbox) components[idx];
        return new Checkbox(checkbox);
    }

    public boolean is_checkbox(Object object)
    {
        return object.equals(checkbox_);
    }

    public String label()
    {
        return checkbox_.getLabel();
    }

    public boolean labels_match(String label)
    {
        return Objects.equals(label(), label);
    }

    public boolean is_checked()
    {
        return checkbox_.getState();
    }

    public void set_checked(boolean value)
    {
        checkbox_.setState(value);
    }


    public boolean is_visible()
    {
        return checkbox_.isVisible();
    }

    public void set_visibility(boolean value)
    {
        checkbox_.setVisible(value);
    }

    public void add_item_listener(ItemListener listener)
    {
        checkbox_.addItemListener(listener);
    }
}
