package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.ffi
import com.coxphysics.terrapins.models.frc.FRC
import com.coxphysics.terrapins.models.hawkman.external.Hawkman
import com.coxphysics.terrapins.models.process.Runner
import com.coxphysics.terrapins.models.utils.FsUtils
import ij.IJ
import java.nio.file.Path
import kotlin.io.path.Path
import com.coxphysics.terrapins.models.assessment.images.Settings as ImagesSettings

private const val EXE_NAME = "assessment"

class Assessment private constructor(private val exe_location_: Path)
{
    companion object
    {
        @JvmStatic
        fun custom(exe_path: Path): Assessment
        {
            return Assessment(exe_path)
        }

        @JvmStatic
        fun default_(): Assessment
        {
            FRC.extract_dependencies()
            Hawkman.extract_default_tool()
            val name = ffi.os_exe_name(EXE_NAME)
            val exe_path = ffi.extract_resource_to_temp(this::class.java, name, true, true)
            return custom(exe_path);
        }
    }

    private fun program_name(): String
    {
        return exe_location_.toString()
    }

    private fun working_directory(): Path?
    {
        val parent = exe_location_.parent
        if (parent == null)
        {
            return null
        }
        return parent.resolve("smlm_assessment")
    }

    private fun data_directory(): Path?
    {
        val working_directory = working_directory()
        if (working_directory == null)
        {
            return null
        }
        val directory_name = working_directory.toString() + "_data"
        return Path(directory_name)
    }

    private fun results() : AssessmentResults?
    {
        return data_directory()?.let { AssessmentResults.from(it) }
    }

    fun run_images(runner : Runner, images: ImagesSettings): AssessmentResults?
    {
        val arguments = get_images_arguments(images)
        return run_arguments(runner, arguments)
    }

    fun get_images_arguments(images: ImagesSettings): List<String>
    {
        val commands = get_commands()
        add_core_commands(images.core_settings(), commands)
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
//      --pixel-size-nm <PIXEL_SIZE_NM>      Pixel size (nm) in images
//      --psf-px <PSF_PX>                    PSF size (px) in images
        commands.add("images")
        if (settings.reference_image_is_valid())
        {
            commands.add("--reference-image")
            commands.add(settings.reference_image_nn())
        }

        if (settings.hawk_image_is_valid())
        {
            commands.add("--hawk-image")
            commands.add(settings.hawk_image_nn())
        }

        if (settings.half_split_valid())
        {
            commands.add("--half-split-a")
            commands.add(settings.half_split_image_a_nn())
            commands.add("--half-split-b")
            commands.add(settings.half_split_image_b_nn())
        }

        if (settings.zip_split_valid())
        {
            commands.add("--zip-split-a")
            commands.add(settings.zip_split_image_a_nn())
            commands.add("--zip-split-b")
            commands.add(settings.zip_split_image_b_nn())
        }
        add_equipment(true, settings.equipment_settings(), commands)
    }

    fun run_localisations(runner : Runner, localisations: AssessmentSettings): AssessmentResults?
    {
        val arguments = get_localisations_arguments(localisations)
        return run_arguments(runner, arguments)
    }

    fun get_localisations_arguments(localisations: AssessmentSettings): List<String>
    {
        val commands = get_commands()
        add_core_commands(localisations.core_settings(),commands)
        add_localisations_commands(localisations, commands)
        return commands
    }

    private fun add_localisations_commands(settings: AssessmentSettings, commands: MutableList<String>)
    {
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
        add_equipment(false, settings.equipment(), commands)
    }

    private fun run_arguments(runner: Runner, arguments: List<String>): AssessmentResults?
    {
        val working_directory = working_directory()
        if (!FsUtils.delete_directory_recursive(working_directory))
        {
            IJ.log("Cannot delete working directory: " + working_directory.toString())
            return null
        }
        val data_directory = data_directory()
        if (!FsUtils.delete_directory_recursive(data_directory))
        {
            IJ.log("Cannot delete data directory: " + data_directory.toString())
            return null
        }
        val pb = ProcessBuilder(arguments)
        val exit_code = runner.run(pb)
        if (exit_code != 0)
        {
            return null
        }
        return results()
    }

//  -f, --filename <FILENAME>        filename
//  -i, --input <INPUT>              SMLM file
//  -s, --settings <SETTINGS>        Settings file
//      --widefield <WIDEFIELD>      Widefield file
//      --image-stack <IMAGE_STACK>  Image stack file
//      --metrics-only               Only generate metric file
//      --extract                    Extract Data to directory
    private fun add_core_commands(settings: CoreSettings, commands: MutableList<String>)
    {
        // should I check for null - there shouldn't really be a non-parent location
        // other than in tests - which can do their own thing
        commands.add("--working-directory")
        commands.add(working_directory().toString())

        if (settings.has_widefield())
        {
            commands.add("--widefield")
            commands.add(settings.widefield_nn())
        }

        if (settings.has_image_stack())
        {
            commands.add("--image-stack")
            commands.add(settings.image_stack_nn())
        }

        if (settings.has_settings_file())
        {
            commands.add("--settings")
            commands.add(settings.settings_file_nn())
        }
        commands.add("--extract")
    }

    private fun add_equipment(is_images: Boolean, settings: EquipmentSettings, commands: MutableList<String>)
    {
        if (is_images)
        {
            commands.add("--pixel-size-nm")
        }
        else
        {
            commands.add("--camera-pixel-size-nm")
        }
        commands.add(settings.camera_pixel_size_nm().toString())

        if (is_images)
        {
            commands.add("--psf-px")
        }
        else
        {
            commands.add("--instrument-psf-fwhm-nm")
        }
        commands.add(settings.instrument_psf_fwhm_nm().toString())
    }
}