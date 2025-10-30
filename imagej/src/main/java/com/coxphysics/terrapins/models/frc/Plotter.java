package com.coxphysics.terrapins.models.frc;

import ij.gui.Plot;

import java.awt.*;

public class Plotter
{
    public static Plot plot(FRCResult result)
    {
        return named_plot(result, null);
    }

    public static Plot named_plot(FRCResult result, String name)
    {
        String title = get_title(result.fire_number(), name);
        Plot p = new Plot(title, "q (spatial frequency)", "Correlation");
        p.setColor(Color.red);
        double[] frcs = result.get_frcs(0);
        double[] qs = result.qs();
        p.add("line", qs, frcs);
        p.setColor(Color.blue);
        double[] threshold_curve = result.threshold_curve();
        p.add("line", qs, threshold_curve);
        p.addLegend("Correlation\tThreshold");
        p.show();
        return p;
    }

    public static String get_title(double fire_number, String name)
    {
        String title = String.format("FIRE: %.2f", fire_number);
        if (name != null)
            title = String.format("%s: %s", name, title);
        return title;
    }
}
