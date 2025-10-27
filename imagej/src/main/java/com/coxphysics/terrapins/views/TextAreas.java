package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;

import java.awt.*;


public class TextAreas
{
    private final Panel panel_;
    private final TextArea area_1_;
    private final TextArea area_2_;

    private TextAreas(Panel panel, TextArea area_1, TextArea area_2)
    {
        panel_ = panel;
        area_1_ = area_1;
        area_2_ = area_2;
    }

    public static TextAreas from(GenericDialog dialog)
    {
        Component[] components = dialog.getComponents();
        int start_index = components.length - 1;
        Panel panel = (Panel)components[start_index];
        Component[] panel_components = panel.getComponents();
        int n_areas = panel_components.length;
        TextArea area_1 = null;
        TextArea area_2 = null;
        if (n_areas > 0)
        {
            area_1 = (TextArea)panel_components[0];
        }
        if (n_areas > 1)
        {
            area_2 = (TextArea)panel_components[1];
        }
        return new TextAreas(panel, area_1, area_2);
    }

    public TextArea area_1()
    {
        return area_1_;
    }

    public String area_1_text()
    {
        return area_1_.getText();
    }

    public TextArea area_2()
    {
        return area_2_;
    }

    public String area_2_text()
    {
        if (area_2_ != null)
            return area_2_.getText();
        return "";
    }

    public void set_visibility(boolean value)
    {
        panel_.setVisible(value);
    }
}
