package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;

import java.awt.*;

public class EnumChoice
{
    private final Label label_;
    private final Choice choice_;

    public EnumChoice(Label label, Choice choice)
    {
        label_ = label;
        choice_ = choice;
    }

    public static EnumChoice from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int idx = components.length - 2;
        Label label = (Label) components[idx];
        Choice choice = (Choice)components[idx + 1];
        return new EnumChoice(label, choice);
    }

    public Choice choice()
    {
        return choice_;
    }

    public String text()
    {
        return choice_.getSelectedItem();
    }
}
