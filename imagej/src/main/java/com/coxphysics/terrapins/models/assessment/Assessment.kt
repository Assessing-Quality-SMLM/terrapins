package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.ffi
import com.coxphysics.terrapins.models.frc.FRC
import com.coxphysics.terrapins.models.fs.FileSystem
import com.coxphysics.terrapins.models.fs.SystemFileSystem
import com.coxphysics.terrapins.models.hawkman.external.Hawkman
import com.coxphysics.terrapins.models.process.Runner
import com.coxphysics.terrapins.models.squirrel.external.Squirrel
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.StringUtils
import ij.IJ
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.exists
import com.coxphysics.terrapins.models.assessment.images.Settings as ImagesSettings

private const val EXE_NAME = "assessment"

class Assessment private constructor(private val exe_location_: Path, private val file_system_: FileSystem)
{
    companion object
    {
        @JvmStatic
        fun custom(exe_path: Path, file_system: FileSystem): Assessment
        {
            return Assessment(exe_path, file_system)
        }

        @JvmStatic
        fun default(): Assessment
        {
            FRC.extract_dependencies()
            Hawkman.extract_default_tool()
            Squirrel.extract_default_tool()
            val name = ffi.os_exe_name(EXE_NAME)
            val exe_path = ffi.extract_resource_to_temp(this::class.java, name, true, true)
            return custom(exe_path, SystemFileSystem());
        }

        // For Java
        @JvmStatic
        fun default_(): Assessment
        {
            return default()
        }
    }

    private fun program_name(): String
    {
        return exe_location_.toString()
    }

    private fun data_directory(working_directory: Path): Path
    {
        val directory_name = working_directory.toString() + "_data"
        return Path(directory_name)
    }

    private fun results(working_directory: Path) : AssessmentResults
    {
        val data_directory = data_directory(working_directory)
        return AssessmentResults.from(data_directory)
    }

    fun run_images(runner : Runner, images: ImagesSettings): AssessmentResults?
    {
        val new_squirrel_inputs = images.prepare_images_for_analysis()
        if (new_squirrel_inputs == null)
        {
            IJ.log("Failed to prepare images for analysis")
            return null
        }
        val data_name = generate_data_name()
        val arguments = get_images_arguments(new_squirrel_inputs, images, data_name)
        val working_directory = images.working_directory()
        if (working_directory == null)
            return null
        return run_arguments(runner, arguments, working_directory, data_name)
    }

    fun get_images_arguments(adjusted_squirrel_inputs: SquirrelInputs, images: ImagesSettings, data_name: String?): List<String>
    {
        val commands = get_commands()
        add_core_commands(images.core_settings(), adjusted_squirrel_inputs, data_name, commands)
        add_equipment(images.equipment_settings(), commands)
        add_image_commands_to(commands, images)
        return commands
    }

    private fun get_commands() : MutableList<String>
    {
        return mutableListOf(program_name())
    }


    fun add_image_commands_to(commands: MutableList<String>, settings: ImagesSettings)
    {
//      --reference-image <REFERENCE_IMAGE>  Image to use as reference
//      --hawk-image <HAWK_IMAGE>            Image that has been generated from HAWK processing
//      --half-split-a <HALF_SPLIT_A>        Rendering of first part of data split in half
//      --half-split-b <HALF_SPLIT_B>        Rendering of second part of data split in half
//      --zip-split-a <ZIP_SPLIT_A>          Rendering of first part of data zip split
//      --zip-split-b <ZIP_SPLIT_B>          Rendering of second part of data zip split
//      --drift-split-a <DRIFT_SPLIT_A>      Rendering of first part of data drift split
//      --drift-split-b <DRIFT_SPLIT_B>      Rendering of second part of data drift split
        commands.add("--magnification")
        commands.add(settings.magnification().toString())
        commands.add("image")
        if (settings.reference_image_is_valid())
        {
            val reference_path = settings.reference_image_path()
            if (reference_path != null)
            {
                add_path_to(commands, "--reference-image", reference_path)
            }
        }

        if (settings.hawk_image_is_valid())
        {
            val hawk_path = settings.hawk_image_path()
            if (hawk_path != null)
            {
                add_path_to(commands, "--hawk-image", hawk_path)
            }
        }

        if (settings.half_split_valid())
        {
            val half_split_a_path = settings.half_split_image_a_filepath()
            val half_split_b_path = settings.half_split_image_b_filepath()
            if (half_split_a_path != null && half_split_b_path != null)
            {
                add_path_to(commands, "--half-split-a", half_split_a_path)
                add_path_to(commands, "--half-split-b", half_split_b_path)
            }
        }

        if (settings.zip_split_valid())
        {
            val zip_split_a_path = settings.zip_split_image_a_filepath()
            val zip_split_b_path = settings.zip_split_image_b_filepath()
            if (zip_split_a_path != null && zip_split_b_path != null)
            {
                add_path_to(commands, "--zip-split-a", zip_split_a_path)
                add_path_to(commands, "--zip-split-b", zip_split_b_path)
            }
        }

        if (settings.drift_split_valid())
        {
            val drift_split_a_path = settings.drift_split_image_a_filepath()
            val drift_split_b_path = settings.drift_split_image_b_filepath()
            if (drift_split_a_path != null && drift_split_b_path != null)
            {
                add_path_to(commands, "--drift-split-a", drift_split_a_path)
                add_path_to(commands, "--drift-split-b", drift_split_b_path)
            }
        }
//        add_equipment(true, settings.equipment_settings(), commands)
    }

    fun add_path_to(commands: MutableList<String>, key: String, path: Path)
    {
        val path_string = path.toString()
        if (path_string == StringUtils.EMPTY_STRING)
            return
        commands.add(key)
        commands.add(path_string)
    }

    fun run_localisations(runner : Runner, localisations: AssessmentSettings): AssessmentResults?
    {
        val adjusted_squirrel_inputs = localisations.prepare_images_for_analysis()
        if (adjusted_squirrel_inputs == null)
        {
            IJ.log("Failed to prepare images for analysis")
            return null
        }
        val data_name = generate_data_name()
        val arguments = get_localisations_arguments(adjusted_squirrel_inputs, localisations, data_name)
        val working_directory = localisations.working_directory()
        if (working_directory == null)
            return null
        return run_arguments(runner, arguments, working_directory, data_name)
    }

    fun get_localisations_arguments(adjusted_squirrel_inputs: SquirrelInputs, localisations: AssessmentSettings, data_name: String?): List<String>
    {
        val commands = get_commands()
        add_core_commands(localisations.core_settings(), adjusted_squirrel_inputs, data_name, commands)
        add_equipment(localisations.equipment(), commands)
        add_localisations_commands(localisations, commands)
        return commands
    }

    private fun add_localisations_commands(settings: AssessmentSettings, commands: MutableList<String>)
    {
        commands.add("--magnification")
        commands.add(settings.magnification().toString())
        commands.add("localisation")
        if (settings.has_localisation_file())
        {
            commands.add("--locs")
            commands.add(settings.localisation_filename())
            commands.add("--locs-format")
            commands.add(settings.localisation_file_parse_method())
        }
        if (settings.has_hawk_localisation_file()) {
            commands.add("--locs-hawk")
            commands.add(settings.hawk_localisation_filename())
            commands.add("--locs-hawk-format")
            commands.add(settings.hawk_localisation_file_parse_method())
        }
//        add_equipment(false, settings.equipment(), commands)
    }

    private fun generate_data_name(): String?
    {
        val date_time = LocalDateTime.now()
        return date_time_to_file_path(date_time)
    }

    fun date_time_to_file_path(date_time: LocalDateTime): String?
    {
        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")
        return date_time.format(formatter)
    }

    private fun run_arguments(runner: Runner, arguments: List<String>, working_directory: Path, data_name: String?): AssessmentResults?
    {
        val pb = ProcessBuilder(arguments)
        ffi.set_library_path_for(pb, exe_location_);
        val exit_code = runner.run(pb)
        if (exit_code != 0)
        {
            return null
        }
        if (data_name == null)
            return results(working_directory)
        return results(working_directory.resolve(data_name))
    }

//  -f, --filename <FILENAME>        filename
//  -i, --input <INPUT>              SMLM file
//  -s, --settings <SETTINGS>        Settings file
//      --widefield <WIDEFIELD>      Widefield file
//      --image-stack <IMAGE_STACK>  Image stack file
//      --metrics-only               Only generate metric file
//      --extract                    Extract Data to directory
    private fun add_core_commands(settings: CoreSettings, squirrel_inputs: SquirrelInputs, data_name: String?, commands: MutableList<String>)
    {
        commands.add("--working-directory")
        var working_directory = settings.working_directory_path()
        if (working_directory == null)
            working_directory = FsUtils.temp_directory()
        commands.add(working_directory.toString())

        if (data_name != null)
        {
            commands.add("--data-name")
            commands.add(data_name)
        }

        if (squirrel_inputs.has_widefield())
        {
            val widefield_path = squirrel_inputs.widefield_path_in(working_directory)
            if (widefield_path != null && file_system_.exists(widefield_path))
            {
                commands.add("--widefield")
                commands.add(widefield_path.toString())
            }
        }

        if (squirrel_inputs.has_image_stack())
        {
            val image_stack_path = squirrel_inputs.image_stack_path_in(working_directory)
            if (image_stack_path != null && file_system_.exists(image_stack_path))
            {
                commands.add("--image-stack")
                commands.add(image_stack_path.toString())
            }
        }

        commands.add("--n-threads")
        commands.add(settings.n_threads().toString())

        if (settings.has_settings_file())
        {
            commands.add("--settings")
            commands.add(settings.settings_file_nn())
        }

        commands.add("--extract")
    }

    private fun add_equipment(settings: EquipmentSettings, commands: MutableList<String>)
    {
        commands.add("--camera-pixel-size-nm")
        commands.add(settings.camera_pixel_size_nm().toString())

        commands.add("--instrument-psf-fwhm-nm")
        commands.add(settings.instrument_psf_fwhm_nm().toString())
    }
}