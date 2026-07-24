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

    @Test
    public void y_min_on_frc_plots_floors_to_0()
    {
        double[] data = {1.0, 0.5, 3.0};
        assertEquals(Plotter.get_y_min(data), 0.0);
    }

    @Test
    public void y_min_on_frc_plots_can_be_negative()
    {
        double[] data = {1.0, 0.5, -3.0};
        assertEquals(Plotter.get_y_min(data), -3.0);
    }
}
