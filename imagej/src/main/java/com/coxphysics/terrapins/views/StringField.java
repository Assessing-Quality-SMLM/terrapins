package com.coxphysics.terrapins.views;

import com.coxphysics.terrapins.models.utils.StringUtils;
import ij.gui.GenericDialog;

import java.awt.*;

public class StringField
{

    private final Label label_;

    private final TextField text_field_;

    private StringField(Label label, TextField text_field)
    {

        label_ = label;
        text_field_ = text_field;
    }

    public static StringField from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int idx = components.length - 2;
        Label label = (Label) components[idx];
        TextField text_field = (TextField)components[idx + 1];
        return new StringField(label, text_field);
    }

    public void set_visible(boolean value)
    {
        label_.setVisible(value);
        text_field_.setVisible(value);
    }

    public String text()
    {
        return text_field_.getText();
    }

    public void set_text(String value)
    {
        text_field_.setText(value);
    }

    public Integer unsigned_integer_value()
    {
        return StringUtils.parse_unisigned_int(text());
    }

    public Character char_value()
    {
        return StringUtils.to_char(text());
    }

    public void set_visibility(boolean value)
    {
        label_.setVisible(value);
        text_field_.setVisible(value);
    }
}
