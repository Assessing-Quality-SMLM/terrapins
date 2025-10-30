package com.coxphysics.terrapins.models.squirrel.external

import com.coxphysics.terrapins.models.ffi
import com.coxphysics.terrapins.models.hawkman.external.name_to_image
import com.coxphysics.terrapins.models.process.ImageJLoggingRunner
import com.coxphysics.terrapins.models.process.Runner
import com.coxphysics.terrapins.models.squirrel.SquirrelSettings
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.IJ
import ij.ImagePlus
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

private val EXE_NAME = "squirrel"

class Squirrel private constructor(private val exe_path_: Path)
{
    companion object Factory
    {
        @JvmStatic
        fun custom(exe_path: Path): Squirrel
        {
            return Squirrel(exe_path)
        }

        @JvmStatic
        fun default_(): Squirrel
        {
            val name = ffi.os_exe_name(EXE_NAME);
            val exe_path = ffi.extract_resource_to_temp(this::class.java, name, true, true)
            return custom(exe_path)
        }
    }

    fun is_valid_install(): Boolean
    {
        return exe_path_.exists()
    }

    private fun program_name(): String
    {
        return exe_path_.toString()
    }

    private fun default_output_directory(): Path
    {
        return exe_path_.parent
    }

    private fun output_directory_path(): Path
    {
        return default_output_directory().resolve("squirrel_data")
    }

    private fun output_directory(): String
    {
        return output_directory_path().toString()
    }

    fun run(settings: SquirrelSettings): Boolean
    {
        if (!is_valid_install())
        {
            IJ.log("exe path does not exist - contact the developer")
            return false
        }
        val ok = run_with(ImageJLoggingRunner(), settings)
        if (!ok)
        {
            IJ.log("SQUIRREL did not execute correctly - contact the developer")
            return false
        }
        val results = Results.from(output_directory_path())
        results.display()
        return true
    }

    private fun run_with(runner: Runner, settings: SquirrelSettings): Boolean
    {
        FsUtils.prepare_directory(output_directory_path())
        val image_paths = prepare_images(settings)
        val commands = get_commands(image_paths.component1(), image_paths.component2(), settings);
//        val builder = process_runnder.create_builder(commands)
        val builder = ProcessBuilder(commands)
        val exit_code = runner.run(builder)
        return exit_code == 0
    }

    fun get_commands(widefield: Path, sr: Path, settings: SquirrelSettings): List<String>
    {
        val commands = mutableListOf<String>()
        commands.add(program_name());

        commands.add(String.format("wf=%s",widefield))
        commands.add(String.format("sr=%s", sr))
        commands.add(String.format("od=%s", output_directory()))
//        commands.add(String.format("px=%s", settings.pixel_size()))
        commands.add(String.format("sigma=%s", settings.sigma_nm()))
        if (settings.show_positive_and_negative())
            commands.add("pn")
        if (settings.register())
            commands.add("reg")
        if (settings.crop_borders())
            commands.add("cb")
//        commands.add("wo") // write optimiser data
        //commands.add("pw") // patchwise
//        commands.add(String.format("ps=%s", settings.patch_size()))
//        commands.add(String.format("ss=%s", settings.step_size()))
//        if (settings.run_mt())
//            commands.add("mt") // multi thread
//        commands.add(String.format("nt=%s", settings.n_threads()))


        return commands;
    }

    private fun prepare_images(settings: SquirrelSettings): Pair<Path, Path>
    {
        val widefield_path = Paths.get(output_directory(), "widefield.tiff")
        val image_1 = get_widefield(settings)
//        image_1.write_to_disk(widefield_path)
        IJUtils.write_to_disk(image_1, widefield_path)
        val sr_path = Paths.get(output_directory(), "super_res.tiff")
        val image_2 = name_to_image(settings.super_res_image())
//        image_2.write_to_disk(sr_path)
        IJUtils.write_to_disk(image_2, sr_path);
        return Pair(widefield_path, sr_path)
    }

    private fun get_widefield(settings: SquirrelSettings): ImagePlus
    {
        return name_to_image(settings.reference_image())
    }
}