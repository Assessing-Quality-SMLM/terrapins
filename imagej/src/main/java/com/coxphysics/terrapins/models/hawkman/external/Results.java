package com.coxphysics.terrapins.models.hawkman.external;

import com.coxphysics.terrapins.models.utils.FsUtils;
import com.coxphysics.terrapins.models.utils.IJUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import kotlin.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class Results
{
    private final Path data_directory_;


    private Results(Path data_directory)
    {
        data_directory_ = data_directory;
    }

    public static Results from(Path data_directory)
    {
        return new Results(data_directory);
    }

    private Pair<double[], double[]> global_scores()
    {
        return parse_scores(confidence_data_path());
    }
    
    public ImagePlus confidence_map_generator()
    {
        return stack_as_image(confidence_data_path(), "confidence");
    }

    private Path confidence_data_path()
    {
        return get_data_path("confidence_map");
    }

    private Pair<double[], double[]> sharpening_scores()
    {
        return parse_scores(sharpening_data_path());
    }

    public ImagePlus sharpening_map_generator()
    {
        return stack_as_image(sharpening_data_path(), "sharpening");
    }

    private Path sharpening_data_path()
    {
        return get_data_path("sharpening_map");
    }

    public ImagePlus skeleton_map_generator()
    {
        return stack_as_image(skeleton_data_path(), "skeleton");
    }

    private Path skeleton_data_path()
    {
        return get_data_path("skeleton_map");
    }

    private Pair<double[], double[]> structure_scores()
    {
        return parse_scores(structure_data_path());
    }

    public ImagePlus structure_map_generator()
    {
        return stack_as_image(structure_data_path(), "structure");
    }

    private Path structure_data_path()
    {
        return get_data_path("structure_map");
    }

    public Path combined_resolution_map_path()
    {
        return get_data_path("combined_resolution_scale_map.tiff");
    }

    public Path resolution_map_path()
    {
        return get_data_path("resolution_map.tiff");
    }

    public Path scale_map_path()
    {
        return get_data_path("resolution_scale_map.tiff");
    }

    private Path get_data_path(String name)
    {
        return data_directory_.resolve(name);
    }

    private ImagePlus show_image(Path image_path)
    {
        ImagePlus image = load_image(image_path);
        if (image == null)
            return null;
        image.show();
        return image;
    }

    @Nullable
    private static ImagePlus load_image(Path image_path)
    {
        if (!FsUtils.exists(image_path))
            return null;
        return IJUtils.load_image(image_path);
    }

    public void display_core_results()
    {
        show_image(resolution_map_path());
        show_image(scale_map_path());
        plot_scores();
    }

    public void display()
    {
        display_core_results();
        confidence_map_generator().show();
        skeleton_map_generator().show();
        sharpening_map_generator().show();
        structure_map_generator().show();
    }

    private void plot_scores()
    {
        Pair<double[], double[]> global_scores = global_scores();
        Pair<double[], double[]> sharpening_scores = sharpening_scores();
        Pair<double[], double[]> structure_scores = structure_scores();
        if (is_empty(global_scores) && is_empty(sharpening_scores) && is_empty(structure_scores))
        {
            return;
        }
        Plot p = new Plot("HAWKMAN scores", "Level", "Correlation");
        p.setColor(Color.RED);
        p.add("line", global_scores.component1(), global_scores.component2());
        p.setColor(Color.BLUE);
        p.add("line", sharpening_scores.component1(), sharpening_scores.component2());
        p.setColor(Color.GREEN);
        p.add("line", structure_scores.component1(), structure_scores.component2());
        p.addLegend("Score\tSharpening\tStructure");
        p.show();
    }

    private boolean is_empty(Pair<double[], double[]> scores)
    {
        return scores.component1().length == 0;
    }


    private Pair<double[], double[]> parse_scores(Path directory)
    {
        Path score_file = directory.resolve("score");
        if (!FsUtils.exists(score_file))
            return new Pair<>(new double[]{}, new double[]{});
        List<Integer> levels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(score_file.toString())))
        {
            String line = reader.readLine();
            while (line != null)
            {
                Pair<Integer, Double> score_values = Helpers.parse_score(line);
                if (score_values == null)
                    return null;
                levels.add(score_values.component1());
                values.add(score_values.component2());
                line = reader.readLine();
            }
            double [] level_array = levels.stream().mapToDouble(Integer::doubleValue).toArray();
            double[] value_array = values.stream().mapToDouble(Double::doubleValue).toArray();
            return new Pair<>(level_array, value_array);
        }
        catch (IOException e)
        {
            IJ.log("Could not read results output at " + score_file);
            return null;
        }
    }

    private ImagePlus stack_as_image(Path directory, String name)
    {
        if (!FsUtils.exists(directory))
            return null;
        ImageStack stack = get_image_stack_from(directory, name);
        return new ImagePlus(name, stack);
    }

    private ImageStack get_image_stack_from(Path directory, String name)
    {
        File folder = new File(directory.toString());
        File[] files = folder.listFiles();
        if (files == null)
            return null;
        File[] sorted_files = Arrays.stream(files).filter(FileComparator::is_number_file)
                                   .sorted(new FileComparator())
                                   .toArray(File[]::new);

        ImagePlus[] images = new ImagePlus[sorted_files.length];
        String[] labels = new String[sorted_files.length];
        for (int idx = 0; idx < sorted_files.length; idx++)
        {
            File file = sorted_files[idx];
            String file_name = file.toString();
            int level = FileComparator.get_name_as_number(file); // fine as filtered
            String label = String.format("%s at level %s", name, level);
            ImagePlus image = IJUtils.load_image(file.toPath());
            images[idx] = image;
            labels[idx] = label;
        }

        ImageStack stack = ImageStack.create(images);
        for (int idx = 0; idx < labels.length; idx++)
        {
            stack.setSliceLabel(labels[idx], idx + 1);
        }

        return stack;
    }

    private static class FileComparator implements Comparator<File>
    {
        @Override
        public int compare(File f1, File f2)
        {
            return Integer.compare(get_name_as_number(f1), get_name_as_number(f2));
        }

        public static boolean is_number_file(File f)
        {
            return get_name_as_number(f) != null;
        }

        public static Integer get_name_as_number(File file)
        {
            String filename = Helpers.filename(file);
            try
            {
                return Integer.parseInt(filename);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }
}