package com.coxphysics.terrapins.models.hawkman.external;

import com.coxphysics.terrapins.models.ffi;
import com.coxphysics.terrapins.models.hawkman.Settings;
import com.coxphysics.terrapins.models.process.Runner;
import com.coxphysics.terrapins.models.process.SystemRunner;
import com.coxphysics.terrapins.models.utils.FsUtils;
import ij.IJ;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hawkman
{

    private final static String EXE_NAME = "hawkman";

    private final Path exe_path_;

    private final static List<String> windows_extra_exes_ = Arrays.asList("opencv_annotation.exe",
                                                             "opencv_interactive-calibration.exe",
                                                             "opencv_model_diagnostics.exe",
                                                             "opencv_version.exe",
                                                             "opencv_version_win32.exe",
                                                             "opencv_visualisation.exe");
    private final static List<String> windows_extra_dlls_ = Arrays.asList("opencv_videoio_ffmpeg4100_64.dll",
                                                                  "opencv_videoio_msmf4100_64.dll",
                                                                  "opencv_videoio_msmf4100_64d.dll",
                                                                  "opencv_world4100.dll",
                                                                  "opencv_world4100.pdb",
                                                                  "opencv_world4100d.dll",
                                                                  "opencv_world4100d.pdb");


    private Hawkman(Path exe_path)
    {
        exe_path_ = exe_path;
    }

    public static Hawkman custom(Path exe_path)
    {
        return new Hawkman(exe_path);
    }

    public static Hawkman default_()
    {
        Path exe_path = extract_default_tool();
        return custom(exe_path);
    }

    @Nullable
    public static Path extract_default_tool()
    {
        String name = ffi.os_exe_name(EXE_NAME);
        Path exe_path = ffi.extract_resource_to_temp(Hawkman.class, name, true, true);
        if (ffi.is_windows())
        {
             for(String exe_name : windows_extra_exes_)
             {
                 ffi.extract_resource_to_temp(Hawkman.class, exe_name, true, true);
             }
             for(String dll_name : windows_extra_dlls_)
             {
                 ffi.extract_resource_to_temp(Hawkman.class, dll_name, true, true); // artifacts in bin so need this on unix this would fail as try to set execute bit but ok for hack
             }
        }
        return exe_path;
    }

    public boolean is_valid()
    {
        return exe_path_ != null;
    }

    public Path default_output_directory()
    {
        return exe_path_.getParent();
    }

    private String program_name()
    {
        return exe_path_.toString();
    }
    public static void run_with_settings(Settings settings)
    {
        Hawkman hawkman = default_();
        hawkman.run(new SystemRunner(), settings);
    }

    private void run(Runner process_runner, Settings settings)
    {
        FsUtils.prepare_directory(output_directory_path());
        Pair<Path, Path> image_paths = prepare_images(settings);
        boolean ok = run_hawkman(process_runner, settings, image_paths);
        if (!ok)
        {
            IJ.log("HAWKMAN finished with errors");
            return;
        }
        Results results = Results.from(output_directory_path());
        results.display();
    }

    private boolean run_hawkman(Runner process_runner, Settings settings, Pair<Path, Path> image_paths)
    {
        List<String> commands = get_commands(image_paths.component1(), image_paths.component2(), settings);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try
        {
            Process p = pb.start();
            try(BufferedInputStream isr = new BufferedInputStream(p.getInputStream()))
//            try(InputStream isr = p.getInputStream())
            {

                int c;
                StringBuilder builder = new StringBuilder();
                while ((c = isr.read()) >= 0)
                {
                    char character = (char) c;
                    if (character == '\n')
                    {
                        IJ.log(builder.toString());
                        builder = new StringBuilder();
                    }
                    else
                    {
                        builder.append(character);
                    }
                    System.out.print(character);
                    System.out.flush();

                }
                return p.exitValue() == 0;
            }
        }
        catch (IOException e)
        {
            IJ.log(e.toString());
            return false;
        }
//        int exit_code = process_runner.run(pb);
//        return exit_code == 0;
    }

    private List<String> get_commands(Path reference, Path test, Settings settings)
    {
        List<String> commands = new ArrayList<>();
        commands.add(program_name());

        commands.add(String.format("ref=%s",reference));
        commands.add(String.format("test=%s", test));
        commands.add(String.format("n=%s", settings.max_scale()));
        commands.add(String.format("psf=%s", settings.psf_size()));
        commands.add(String.format("fwhm=%4.3f,%4.3f,%4.3f", settings.fwhm_threshold(), settings.fwhm_smoothing(), settings.fwhm_offset()));
        commands.add(String.format("skel=%4.3f,%4.3f,%4.3f", settings.skeletonise_threshold(), settings.skeletonise_smoothing(), settings.skeletonise_offset()));
        commands.add(String.format("o=%s", output_directory()));

        return commands;
    }

    private String output_directory()
    {
        return output_directory_path().toString();
    }

    @NotNull
    private Path output_directory_path()
    {
        return default_output_directory().resolve("hawkman_data");
    }

    private Pair<Path, Path> prepare_images(Settings settings)
    {
        return Helpers.prepare_images(settings.ref_name(), settings.test_name(), output_directory_path());
    }
}