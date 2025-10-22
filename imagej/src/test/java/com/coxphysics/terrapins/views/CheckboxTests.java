package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CheckboxTests
{

    @Test
    public void set_visibility()
    {
        GenericDialog dialog = new GenericDialog("something");
        Checkbox checkbox = Utils.add_checkbox(dialog, "a", true);
        assertTrue(checkbox.is_visible());
        checkbox.set_visibility(false);
        assertFalse(checkbox.is_visible());
        checkbox.set_visibility(true);
        assertTrue(checkbox.is_visible());
    }

    @Test
    public void set_checked()
    {
        GenericDialog dialog = new GenericDialog("something");
        Checkbox checkbox = Utils.add_checkbox(dialog, "a", true);
        assertTrue(checkbox.is_checked());
        checkbox.set_checked(false);
        assertFalse(checkbox.is_checked());
        checkbox.set_checked(true);
        assertTrue(checkbox.is_checked());
    }

    @Test
    public void labels_match()
    {
        GenericDialog dialog = new GenericDialog("something");
        Checkbox checkbox = Utils.add_checkbox(dialog, "a", true);
        assertTrue(checkbox.labels_match("a"));
        assertFalse(checkbox.labels_match("b"));
    }
}
