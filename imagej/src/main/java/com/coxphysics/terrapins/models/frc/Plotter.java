package com.coxphysics.terrapins.models.frc;

import ij.gui.Plot;

import java.awt.*;
import java.util.ArrayList;

public class Plotter
{

    private static final float MIN_MAGNIFICATION = 0.2f;
    private static final float MAX_MAGNIFICATION = 0.4f;

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
        draw_bounds_on(p);
        p.addLegend("Correlation\tThreshold\tMagnification too low\tMagnification too high");
        p.show();
        return p;
    }

    private static void draw_bounds_on(Plot plot)
    {
//        float[] corners = new float[]{MIN_MAGNIFICATION, 0.0f, MAX_MAGNIFICATION, 1.0f};
//        ArrayList<float[]> coords = new ArrayList<>();
//        coords.add(corners);
//        plot.drawShapes("rectangles", coords);
//        int plot_object = plot.getNumPlotObjects() - 1;
//        plot.setPlotObjectStyle(plot_object, "black,#A3F7B4");
//        plot.addText("Magnification ok", 0.25, 0.5);
        plot.setColor(Color.black);
        add_vertical_to(plot, MIN_MAGNIFICATION);
        plot.setColor(Color.BLACK);
        add_vertical_to(plot, MAX_MAGNIFICATION);
    }

    private static void add_vertical_to(Plot plot, double x)
    {
        double[] xs = new double[]{x, x};
        double[] ys = new double[]{0, 1};
        plot.add("line", xs, ys);
        int plot_object = plot.getNumPlotObjects() - 1;
        plot.setPlotObjectStyle(plot_object, "black,none,3");
    }

    public static String get_title(double fire_number, String name)
    {
        String title = String.format("FIRE: %.2f", fire_number);
        if (name != null)
            title = String.format("%s: %s", name, title);
        return title;
    }
}
