package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.utils.FsUtils;
import com.coxphysics.terrapins.models.utils.StringUtils;
import ij.IJ;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;


public class FRCResult
{
    private static final String RESOLUTION_FILE = "resolution";

    private final double fire_number_;

    private final double[] qs_;

    private final ArrayList<double[]> frcs_;

    private final double[] threshold_curve_;

    private FRCResult(double fire_value, double[] qs, ArrayList<double[]> frcs, double[] threshold_curve)
    {
        fire_number_ = fire_value;
        qs_ = qs;
        frcs_ = frcs;
        threshold_curve_ = threshold_curve;
    }

    public static FRCResult from(Path frc_data_directory)
    {
        Path resolution_file = frc_data_directory.resolve(RESOLUTION_FILE);
        return from_filename(resolution_file);
    }

    @Nullable
    public static FRCResult from_filename(Path resolution_file)
    {
        if (!FsUtils.exists(resolution_file))
        {
            return null;
        }
        return parse_file(resolution_file.toString());
    }

    public double fire_number()
    {
        return fire_number_;
    }

    public double[] qs()
    {
        return qs_;
    }

    public double[] get_frcs(int frc)
    {
        double[] data = new double[qs_.length];
        for (int idx = 0; idx < data.length; idx++)
        {
            data[idx] = frcs_.get(idx)[frc];
        }
        return data;
    }

    public void plot_with_name(String name)
    {
        Plotter.named_plot(this, name);
    }

    public double[] threshold_curve()
    {
        return threshold_curve_;
    }

    public static FRCResult parse_file(String data_path)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(data_path)))
        {
            return parse_reader(reader);
        }
        catch (IOException e)
        {
            IJ.log("Could not read results output at " + data_path);
            return null;
        }
    }

    public static FRCResult parse_reader(BufferedReader reader)
    {
        try
        {
            double fire_value = extract_fire_number(reader);
            ArrayList<FRCResultLine> lines = extract_results(reader);
            if (lines == null)
            {
                return null;
            }
            return create_results(lines, fire_value);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    @NotNull
    private static FRCResult create_results(ArrayList<FRCResultLine> lines, double fire_value)
    {
        double[] qs = new double[lines.size()];
        ArrayList<double[]> frcs = new ArrayList<>();
        double[] threshold = new double[lines.size()];
        for (int idx = 0; idx < lines.size(); idx++)
        {
            qs[idx] = lines.get(idx).q;
            frcs.add(lines.get(idx).values);
            threshold[idx] = lines.get(idx).threshold;
        }
        return new FRCResult(fire_value, qs, frcs, threshold);
    }

    @Nullable
    private static ArrayList<FRCResultLine> extract_results(BufferedReader reader) throws IOException
    {
        ArrayList<FRCResultLine> lines = new ArrayList<>();
        String line = reader.readLine();
        while (line != null)
        {
            FRCResultLine data = parse_line(line);
            if (data == null)
            {
                return null;
            }
            lines.add(data);
            line = reader.readLine();
        }
        return lines;
    }

    public static double extract_fire_number(BufferedReader reader) throws IOException
    {
        String fire_value = reader.readLine();
        return parse_fire_number(fire_value);
    }

    public static double parse_fire_number(String value)
    {
        try
        {
            String[] splits = value.split(",");
            if (splits.length > 0)
            {
                return StringUtils.parse_double(splits[0]);
            }
            else
            {
                return StringUtils.parse_double(value);
            }
        }
        catch (NumberFormatException _e)
        {
            return Double.POSITIVE_INFINITY;
        }
    }

    public static FRCResultLine parse_line(String line)
    {
        String[] splits = line.split(",");
        if (splits.length < 3)
            return null;
        double q = StringUtils.parse_double(splits[0]);
        double[] data = new double[splits.length - 2];
        double threshold = StringUtils.parse_double(splits[splits.length - 1]);
        for (int idx = 1; idx < splits.length - 1; idx ++)
        {
            data[idx - 1] = StringUtils.parse_double(splits[idx]);
        }
        return new FRCResultLine(q, data, threshold);
    }

    public void write_to(Writer writer)
    {
        try
        {
            writer.write("FIRE number" + fire_number());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static class FRCResultLine
    {
        double q;
        double[] values;
        double threshold;

        public FRCResultLine(double q, double[] data, double threshold)
        {
            this.q = q;
            this.values = data;
            this.threshold = threshold;
        }
    }
}