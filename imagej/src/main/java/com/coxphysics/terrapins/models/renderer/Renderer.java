package com.coxphysics.terrapins.models.renderer;

import com.coxphysics.terrapins.models.ffi;
import com.coxphysics.terrapins.models.process.Runner;
import com.coxphysics.terrapins.models.process.SystemRunner;
import com.coxphysics.terrapins.models.utils.FsUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Renderer
{
    private static final String EXE_NAME = "f2i";

    private final Path exe_path_;

    private static Path extracted_path()
    {
        String name = ffi.os_exe_name(EXE_NAME);
        return ffi.extract_resource_to_temp(Renderer.class, name, true, true);
    }

    private Renderer(Path exe_path)
    {
        exe_path_ = exe_path;
    }

    public static Renderer custom(Path exe_path)
    {
        return new Renderer(exe_path);
    }

    public static Renderer default_()
    {
        return custom(extracted_path());
    }

    public boolean is_valid()
    {
        return exe_path_ != null;
    }

    private String program_name()
    {
        return exe_path_.toString();
    }

    private String get_data_file_path(RenderSettings settings)
    {
        if (settings.has_data_output_path_set())
            return settings.data_output_path();
        return default_data_output_path();
    }

    @NotNull
    private String default_data_output_path()
    {
        return default_output_directory().resolve("data.out").toString();
    }

    private double get_pixel_size(RenderSettings settings)
    {
        String data_file_path = get_data_file_path(settings);
        if (Files.exists(Paths.get(data_file_path)))
        {
            RendererOutput output = RendererOutput.from_file(data_file_path);
            if (output == null)
                return settings.camera_pixel_size_nm();
            return output.pixel_size_nm();
        }
        return settings.camera_pixel_size_nm();
    }

    private ImagePlus get_output_image(RenderSettings settings)
    {
        String output_image_path = output_image_path(settings);
        // imageJ will only open 32bit floating point images from editor
        // in imageJ binary openwithHandleExtraFileTypes is callable
        // and seems to open the 64bit files
        // but does not return an image handle
        // meaning you have to search the window manager
        // for the image handle.
        // its hard to figure out as it traps its own exception
        // so you cannot find out reason without stepping through
        // issue is revelad in tiff get file info method chain.
        ImagePlus image = IJ.openImage(output_image_path);
        double pixel_size = get_pixel_size(settings);
        Calibration calibration = image.getCalibration();
        calibration.pixelWidth = pixel_size;
        calibration.pixelHeight = pixel_size;
        calibration.setUnit("nm");
        image.setCalibration(calibration);
        return image;
    }

    public ImagePlus render_localisations(String localisation_file, RenderSettings settings)
    {
        return render_localisations_with(new SystemRunner(), localisation_file, settings);
    }

    public ImagePlus render_localisations_with(Runner process_runner, String localisation_file, RenderSettings settings)
    {
        if (!FsUtils.prepare_directory(default_output_directory()))
            return null;
        set_data_output_path(settings);
        List<String> commands = get_commands(localisation_file, settings);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try
        {
            int exit_code = process_runner.run(pb);
            return exit_code == 0 ? get_output_image(settings) : null;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<String> get_commands(String localisation_file, RenderSettings settings)
    {
        List<String> commands = new ArrayList<>();
        commands.add(program_name());
        commands.add("-i");
        commands.add(localisation_file);

        commands.add("-o");
        commands.add(output_image_path(settings));
        if (settings.has_zoom_path_set())
        {
            commands.add("--output-zoom");
            commands.add(settings.zoom_path());
        }

        if (settings.has_data_output_path_set())
        {
            commands.add("--output-data");
            commands.add(settings.data_output_path());
        }

        commands.add("-c");
        commands.add(Float.toString(settings.camera_pixel_size_nm()));

        commands.add("-m");
        commands.add(Integer.toString(settings.magnification_factor()));

        if (settings.global_reference_frame_set())
        {
            commands.add("-g");
            commands.add(settings.global_reference_frame());
        }

        commands.add("-s");
        commands.add(Float.toString(settings.sigma_scale()));

        commands.add("-n");
        int n_threads = settings.n_threads();
        commands.add(Integer.toString(n_threads));

        commands.add("--f32");

        String parse_method = settings.parse_method();
        if (parse_method != null)
        {
            commands.add("--parse-method");
            commands.add(parse_method);
        }
        return commands;
    }

    public String output_image_path(RenderSettings settings)
    {
        if (settings.has_image_path_set())
        {
            return settings.image_path();
        }
        return default_output_directory().resolve("image.tiff").toString();
    }

    private void set_data_output_path(RenderSettings settings)
    {
        settings.set_data_output_path(default_data_output_path());
    }

    public Path default_output_directory()
    {
        return exe_directory().resolve("renderer_data");
    }

    private Path exe_directory()
    {
        return exe_path_.getParent();
    }
}
