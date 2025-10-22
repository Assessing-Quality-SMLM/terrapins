package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;

import java.awt.*;

public class Message
{
    private final Label label_;

    private Message(Label label)
    {

        this.label_ = label;
    }
    public static Message from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        Label label = (Label) components[components.length - 1];
        return new Message(label);
    }

    public void set_message(String value)
    {
        label_.setText(value);
    }

    public void set_visibility(boolean value)
    {
        label_.setVisible(value);
    }
}
