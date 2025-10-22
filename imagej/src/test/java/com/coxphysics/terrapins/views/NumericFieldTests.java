package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NumericFieldTests
{

    @Test
    public void set_visibility()
    {
        GenericDialog dialog = new GenericDialog("something");
        NumericField field = Utils.add_numeric_field(dialog, "a", 10, 0);
        assertTrue(field.label().isVisible());
        assertTrue(field.text_field().isVisible());
        field.set_visibility(false);
        assertFalse(field.label().isVisible());
        assertFalse(field.text_field().isVisible());
        field.set_visibility(true);
        assertTrue(field.label().isVisible());
        assertTrue(field.text_field().isVisible());
    }

        @Test
    public void set_enabled()
    {
        GenericDialog dialog = new GenericDialog("something");
        NumericField field = Utils.add_numeric_field(dialog, "a", 10, 0);
        assertTrue(field.label().isEnabled());
        assertTrue(field.text_field().isEnabled());
        field.set_enabled(false);
        assertFalse(field.label().isEnabled());
        assertFalse(field.text_field().isEnabled());
        field.set_enabled(true);
        assertTrue(field.label().isEnabled());
        assertTrue(field.text_field().isEnabled());
    }
}
