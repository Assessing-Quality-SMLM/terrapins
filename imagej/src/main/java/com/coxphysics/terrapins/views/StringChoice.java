package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;

import java.awt.*;

public class StringChoice
{

    private final Label label_;
    private final Choice choice_;

    public StringChoice(Label label, Choice choice)
    {
        label_ = label;
        choice_ = choice;
    }

    public static StringChoice from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int idx = components.length - 2;
        Label label = (Label) components[idx];
        Choice choice = (Choice)components[idx + 1];
        return new StringChoice(label, choice);
    }

    public String choice()
    {
        return choice_.getSelectedItem();
    }

    public void set_visible(boolean value)
    {
        label_.setVisible(value);
        choice_.setVisible(value);
    }

    public void set_enabled(boolean value)
    {
        label_.setEnabled(value);
        choice_.setEnabled(value);
    }

    public void reset_choices(String[] values)
    {
        choice_.removeAll();
        for (String value : values)
        {
            choice_.add(value);
        }
    }
}
