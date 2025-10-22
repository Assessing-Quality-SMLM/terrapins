package com.coxphysics.terrapins.views;

import com.coxphysics.terrapins.models.utils.StringUtils;
import ij.gui.GenericDialog;
import java.awt.*;

public class NumericField
{
     private final Label label_;

     private final TextField text_field_;

     private NumericField(Label label, TextField text_field)
     {
        label_ = label;
        text_field_ = text_field;
     }

     // FOR TESTS DO NOT USE
     public Label label()
     {
         return label_;
     }

     // FOR TESTS DO NOT USE
     public TextField text_field()
     {
         return text_field_;
     }

     public static NumericField from(GenericDialog dialog)
     {
        Component[] components = dialog.getComponents();
        int idx = components.length - 2;
        Label label = (Label) components[idx];
        TextField text_field = (TextField)components[idx + 1];
        return new NumericField(label, text_field);
     }

     public int get_value()
     {
         Integer value = get_nullable_value();
         return value == null ? 0 : value;
     }

    public Integer get_nullable_value()
    {
        return StringUtils.parse_unisigned_int(text());
    }

    public String text()
    {
        return text_field_.getText();
    }

    public void set_visible(boolean value)
    {
        label_.setVisible(value);
        text_field_.setVisible(value);
    }

    public void set_text(String value)
    {
        text_field_.setText(value);
    }

    public void set_visibility(boolean value)
    {
        label_.setVisible(value);
        text_field_.setVisible(value);
    }

    public void set_enabled(boolean value)
    {
        label_.setEnabled(value);
        text_field_.setEnabled(value);
    }
}
