package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.ffi;
import com.coxphysics.terrapins.models.process.Runner;
import com.coxphysics.terrapins.models.process.SystemRunner;
import com.coxphysics.terrapins.models.utils.FsUtils;
import com.coxphysics.terrapins.models.utils.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.coxphysics.terrapins.models.ffi.is_windows;

public class FRC
{
    private final static String EXE_NAME = "frc_this";

    private final static String FFTW = "libfftw3-3";

    private final Path exe_path_;

    private final Path fftw_dll_path_;

    private FRC(Path exe_path, Path fftw_dll_path)
    {
        exe_path_ = exe_path;
        fftw_dll_path_ = fftw_dll_path;
    }

    public static FRC custom(Path exe_path, Path fftw_dll_path)
    {
        return new FRC(exe_path, fftw_dll_path);
    }

    public static FRC default_()
    {
        Path fftw_dll_path = extract_dependencies();
        String name = ffi.os_exe_name(EXE_NAME);
        Path exe_path = ffi.extract_resource_to_temp(FRC.class, name, true, true);
        return custom(exe_path, fftw_dll_path);
    }

    @Nullable
    public static Path extract_dependencies()
    {
        Path fftw_dll_path = null;
        if (is_windows())
        {
            String fftw_name = ffi.windows_dll_name(FFTW);
            fftw_dll_path = ffi.extract_resource_to_temp(FRC.class, fftw_name, true, false);
        }
        return fftw_dll_path;
    }

    public boolean is_valid()
    {
        return exe_path_ != null && if_dll_required_we_have_it();
    }

    private boolean if_dll_required_we_have_it()
    {
        if (is_windows())
        {
            return fftw_dll_path_ != null;
        }
        return true;
    }

    private Path exe_directory()
    {
        return exe_path_.getParent();
    }
    public Path default_output_directory()
    {
        return exe_directory().resolve("frc_data");
    }

    private String program_name()
    {
        return exe_path_.toString();
    }

    public FRCResult calculate_fire_number(FRCDialogSettings settings)
    {
        return calculate_fire_number_with(new SystemRunner(), settings);
    }

    public FRCResult calculate_fire_number_with(Runner process_runner, FRCDialogSettings settings)
    {
        List<String> commands = get_commands(settings);
        ProcessBuilder pb = new ProcessBuilder(commands);
//        try
//        {
//            Process p = pb.start();
//            try(BufferedInputStream isr = new BufferedInputStream(p.getInputStream()))
//            {
//                int c;
//                while ((c = isr.read()) >= 0)
//                {
//                    System.out.print((char) c);
//                    System.out.flush();
//                }
//            }
//        }
//        catch (IOException e)
//        {
//            IJ.log(e.toString());
//        }
        int exit_code = process_runner.run(pb);
        return exit_code == 0 ? get_frc_result(settings) : null;
    }

    public List<String> get_commands(FRCDialogSettings settings)
    {
        List<String> commands = new ArrayList<>();
        commands.add(program_name());

        commands.add("--image-1");
        commands.add(settings.image_1());

        commands.add("--image-2");
        commands.add(settings.image_2());

        commands.add("-f");
        commands.add("tukey");

        commands.add("-p");
        commands.add(String.valueOf(0.25));

        commands.add("-t");
        commands.add(to_command(settings.threshold_method()));

        commands.add("--nm");
        commands.add(String.valueOf(settings.pixel_size()));

        commands.add("--output-data");
        commands.add(get_output_data_path(settings));

        return commands;
    }

    public static String to_command(ThresholdMethod method)
    {
        switch (method)
        {
            case ONE_SEVENTH:
                return "17";
            case HALF_BIT:
                return "half";
            case ONE_SIGMA:
                return "sigma_1";
            case TWO_SIGMA:
                return "sigma_2";
            case THREE_SIGMA:
                return "sigma_3";
            case FOUR_SIGMA:
                return "sigma_4";
        }
        return null;
    }

    private String get_output_data_path(FRCDialogSettings settings)
    {
        String output_data_path = settings.output_data_path();
        return StringUtils.is_set(output_data_path) ? output_data_path : default_output_data_path();
    }

    private String default_output_data_path()
    {
        return default_output_directory().resolve("frc_data").toString();
    }

    private FRCResult get_frc_result(FRCDialogSettings settings)
    {
        String data_path = get_output_data_path(settings);
        return FRCResult.parse_file(data_path);
    }
}
