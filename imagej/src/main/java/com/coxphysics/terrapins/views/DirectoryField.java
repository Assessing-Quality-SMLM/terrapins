package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;
import java.awt.Button;

import java.awt.*;
public class DirectoryField
{
    private final Label label_;
    private final TextField text_;
    private final Button button_;

    private DirectoryField(Label label, TextField text, Button button)
    {
        label_ = label;
        text_ = text;
        button_ = button;
    }

    public static DirectoryField from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int idx = components.length - 2;
        Label label = (Label) components[idx];
        Panel panel = (Panel)components[idx + 1];
        TextField text_field = get_text_field_from_panel(panel);
        Button button = get_button_from_panel(panel);
        return new DirectoryField(label, text_field, button);
    }

    private static TextField get_text_field_from_panel(Panel panel)
    {
        Component[] components = panel.getComponents();
        return (TextField)components[components.length - 2];
    }

    private static Button get_button_from_panel(Panel panel)
    {
        Component[] components = panel.getComponents();
        return (Button) components[components.length - 1];
    }

    public Button button()
    {
        return button_;
    }

    public String filepath()
    {
        return text_.getText();
    }

    public TextField text_field()
    {
        return text_;
    }

    public void set_path_to(String value)
    {
        text_.setText(value);
    }

    public void set_visibility(boolean value)
    {
        label_.setVisible(value);
        text_.setVisible(value);
        button_.setVisible(value);
    }
}