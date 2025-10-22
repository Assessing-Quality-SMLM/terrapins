package com.coxphysics.terrapins.models.localisations;

import com.coxphysics.terrapins.models.ffi;
import com.coxphysics.terrapins.models.fs.FileSystem;
import com.coxphysics.terrapins.models.fs.SystemFileSystem;
import com.coxphysics.terrapins.models.process.Runner;
import com.coxphysics.terrapins.models.process.SystemRunner;
import com.coxphysics.terrapins.models.utils.FsUtils;
import com.coxphysics.terrapins.models.utils.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Splitter
{
    private static final String EXE_NAME = "split";

    private final Path exe_path_;

    private static Path extracted_path()
    {
        String name = ffi.os_exe_name(EXE_NAME);
        return ffi.extract_resource_to_temp(Splitter.class, name, true, true);
    }

    private Splitter(Path exe_path)
    {
        exe_path_ = exe_path;
    }

    public static Splitter custom(Path exe_path)
    {
        return new Splitter(exe_path);
    }

    public static Splitter default_()
    {
        return custom(extracted_path());
    }

    public boolean is_valid()
    {
        return exe_path_ != null && Files.exists(exe_path_);
    }

    private String program_name()
    {
        return exe_path_.toString();
    }

    public List<String> split(String localisation_path, SplitSettings settings)
    {
        return split_localisations_with(new SystemRunner(), new SystemFileSystem(), localisation_path, settings);
    }

    public List<String> split_localisations_with(Runner process_runner, FileSystem file_system, String localisation_path, SplitSettings settings)
    {
        if (!file_system.prepare_directory(default_output_directory()))
            return null;
        List<String> commands = get_commands(localisation_path, settings);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try
        {
            int exit_code = process_runner.run(pb);
            return exit_code == 0 ? get_output_files(settings) : null;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<String> get_commands(String localisation_file, SplitSettings settings)
    {
        List<String> commands = new ArrayList<>();
        commands.add(program_name());
        commands.add("--locs");
        commands.add(localisation_file);

        List<String> files = get_output_files(settings);
        commands.add("-a");
        commands.add(files.get(0));
        commands.add("-b");
        commands.add(files.get(1));

        commands.add("-m");
        commands.add(settings.method());

        String parse_method = settings.parse_method();
        if (parse_method != null)
        {
            commands.add("--parse-method");
            commands.add(parse_method);
        }

        return  commands;
    }

    private List<String> get_output_files(SplitSettings settings)
    {
        List<String> files = new ArrayList<>();
        files.add(output_localisation_path(settings.output_path_1(), "a"));
        files.add(output_localisation_path(settings.output_path_2(), "b"));
        return files;
    }

    private String output_localisation_path(String filepath, String suffix)
    {
        if (StringUtils.path_set(filepath))
        {
            return filepath;
        }
        return default_output_directory().resolve("localisations_split_" + suffix).toString();
    }

    public Path default_output_directory()
    {
        return exe_directory().resolve("splitter_data");
    }

    private Path exe_directory()
    {
        return exe_path_.getParent();
    }
}
