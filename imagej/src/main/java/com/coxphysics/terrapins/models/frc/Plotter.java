package com.coxphysics.terrapins.models.frc;

import ij.gui.Plot;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.coxphysics.terrapins.models.assessment.AssessmentResultsKt.*;

public class Plotter
{

    private static final float MIN_MAGNIFICATION = 0.2f;
    private static final float MAX_MAGNIFICATION = 0.4f;

    public static Plot merge(String title, Pair<String, FRCResult> result_1, Pair<String, FRCResult> result_2)
    {
        Plot plot = get_plot(title);
        plot.setColor(Color.RED);
        plot_score(result_1.component2(), plot);
        plot.setColor(Color.BLUE);
        plot_score(result_2.component2(), plot);
        plot.setColor(Color.black);
        double[] threshold_curve = result_1.component2().threshold_curve();
        plot.add("line", result_1.component2().qs(), threshold_curve);
        String legend = String.format("%s\t%s\t%s", result_1.component1(), result_2.component1(), "Threshold");
        plot.addLegend(legend);
        return plot;
    }

    public static Plot plot_sampling_calibration_curves(Pair<String, FRCResult>[] curves)
    {
        Map<String, Color> colour_map = new HashMap<String, Color>();
        colour_map.put(SR_0_005_LABEL, Color.blue);
        colour_map.put(SR_0_05_LABEL, Color.RED);
        colour_map.put(SR_0_5_LABEL, Color.GREEN);
        colour_map.put(SR_5_LABEL, Color.ORANGE);
        colour_map.put(SR_25_LABEL, Color.MAGENTA);
        colour_map.put(SR_SAMPLE_LABEL, Color.black);
        String title = "Sampling Calibration Curves";
        Plot p = get_plot(title);
        String names = "";
        for (Pair<String, FRCResult> curve : curves)
        {
            FRCResult result = curve.component2();
            if (result == null)
                continue;
            Color colour = colour_map.get(curve.component1());
            p.setColor(colour);
            plot_score(result, p);
            String prefix = names.isEmpty() ? "" : "\t";
            names = String.format("%s%s%s", names, prefix, curve.component1());
        }

        p.addLegend(names);
        p.show();
        return p;
    }

    public static Plot plot(FRCResult result)
    {
        return named_plot(result, null);
    }

    public static Plot named_plot(FRCResult result, String name)
    {
        String title = get_title(result.fire_number(), name);
        Plot p = get_plot(title);
        p.setColor(Color.red);
        double[] qs = plot_score(result, p);
        p.setColor(Color.blue);
        double[] threshold_curve = result.threshold_curve();
        p.add("line", qs, threshold_curve);
        draw_bounds_on(p);
        p.addLegend("Correlation\tThreshold\tMagnification too low\tMagnification too high");
        p.show();
        return p;
    }

    @NotNull
    private static Plot get_plot(String title)
    {
        return new Plot(title, "q (spatial frequency)", "Correlation");
    }

    private static double[] plot_score(FRCResult result, Plot p)
    {
        double[] qs = result.qs();
        double[] frcs = result.get_frcs(0);
        p.add("line", qs, frcs);
        return qs;
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
