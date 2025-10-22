package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;

import java.awt.*;
import java.awt.event.ActionListener;

public class Button
{
    private final java.awt.Button button_;

    private Button(java.awt.Button button)
    {
        button_ = button;
    }

    public static Button from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int idx = components.length - 1;
        java.awt.Button button = (java.awt.Button)components[idx];
        return new Button(button);
    }

    public ActionListener listener()
    {
        ActionListener[] listeners = button_.getActionListeners();
        if (listeners.length > 0)
            return listeners[0];
        return null;
    }

    public void set_visibility(boolean value)
    {
        button_.setVisible(value);
    }
}
