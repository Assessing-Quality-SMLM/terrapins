package com.coxphysics.terrapins.models.frc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlotterTests
{
    @Test
    public void title_can_be_set()
    {
        String title = Plotter.get_title(1.1, "something");
        assertEquals(title, "something: FIRE: 1.10");
    }

    @Test
    public void title_can_be_left_blank()
    {
        String title = Plotter.get_title(1.1, null);
        assertEquals(title, "FIRE: 1.10");
    }
}
