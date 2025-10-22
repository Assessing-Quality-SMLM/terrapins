package com.coxphysics.terrapins.views;

import ij.gui.GenericDialog;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class RadioButtonTests
{

    @Test
    public void set_visibility()
    {
        GenericDialog dialog = new GenericDialog("something");
        RadioButtons buttons = Utils.add_radio_buttons(dialog, "else", new String[]{"a", "b"}, 2, 1, "a");
        assertTrue(buttons.label().isVisible());
        assertTrue(buttons.panel().isVisible());
        for (Component c : buttons.panel().getComponents())
        {
            assertTrue(c.isVisible());
        }

        buttons.set_visibility(false);
        assertFalse(buttons.label().isVisible());
        assertFalse(buttons.panel().isVisible());
        for (Component c : buttons.panel().getComponents())
        {
            assertFalse(c.isVisible());
        }

        buttons.set_visibility(true);
        assertTrue(buttons.label().isVisible());
        assertTrue(buttons.panel().isVisible());
        for (Component c : buttons.panel().getComponents())
        {
            assertTrue(c.isVisible());
        }
    }

    @Test
    public void get_choice()
    {
        GenericDialog dialog = new GenericDialog("something");
        RadioButtons buttons = Utils.add_radio_buttons(dialog, "else", new String[]{"a", "b"}, 2, 1, "a");
        assertEquals(buttons.get_choice(), "a");
    }

    @Test
    public void is_button_group()
    {
        GenericDialog dialog = new GenericDialog("something");
        RadioButtons buttons = Utils.add_radio_buttons(dialog, "else", new String[]{"a", "b"}, 2, 1, "a");
        assertFalse(buttons.is_button_group(buttons.panel()));
        assertFalse(buttons.is_button_group(buttons.label()));
        Component[] components = buttons.panel().getComponents();
        assertTrue(buttons.is_button_group(components[0]));
        assertTrue(buttons.is_button_group(components[1]));
    }

        @Test
    public void is_checked()
    {
        GenericDialog dialog = new GenericDialog("something");
        RadioButtons buttons = Utils.add_radio_buttons(dialog, "else", new String[]{"a", "b"}, 2, 1, "a");
        assertTrue(buttons.is_checked("a"));
        assertFalse(buttons.is_checked("b"));
    }
}
