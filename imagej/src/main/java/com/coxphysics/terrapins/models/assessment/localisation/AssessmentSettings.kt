package com.coxphysics.terrapins.models.assessment.localisation

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.localisations.LocalisationFile
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.reports.EqualSettings
import com.coxphysics.terrapins.plugins.LOCALISATION_SETTINGS_HAWK_LOCALISATIONS
import com.coxphysics.terrapins.plugins.LOCALISATION_SETTINGS_HAWK_LOCALISATIONS_PARSER
import com.coxphysics.terrapins.plugins.LOCALISATION_SETTINGS_RAW_LOCALISATIONS
import com.coxphysics.terrapins.plugins.LOCALISATION_SETTINGS_RAW_LOCALISATIONS_PARSER
import java.nio.file.Path
import java.nio.file.Paths

class AssessmentSettings private constructor(
    private var core_settings_: CoreSettings
)
{
    private var equipment_ = EquipmentSettings.default()
    private var localisation_file_ = LocalisationFile.default()
    private var hawk_localisation_file_ = LocalisationFile.default()

    companion object
    {
        @JvmStatic
        fun with(working_directory: Path): AssessmentSettings
        {
            return AssessmentSettings(CoreSettings.from(working_directory))
        }

        @JvmStatic
        fun default(): AssessmentSettings
        {
            return AssessmentSettings(CoreSettings.default())
        }

        @JvmStatic
        fun from_macro_options(options: MacroOptions) : AssessmentSettings?
        {
            val core_settings = CoreSettings.from_macro_options(options)
            
            val equipment_settings = EquipmentSettings.from_macro_options(options)
            if (equipment_settings == null)
                return null

            val localisation_file = LocalisationFile.from_macro_options(LOCALISATION_SETTINGS_RAW_LOCALISATIONS, LOCALISATION_SETTINGS_RAW_LOCALISATIONS_PARSER, options)
            if (localisation_file == null)
                return null

            val hawk_localisation_file = LocalisationFile.from_macro_options(LOCALISATION_SETTINGS_HAWK_LOCALISATIONS, LOCALISATION_SETTINGS_HAWK_LOCALISATIONS_PARSER, options)
            if (hawk_localisation_file == null)
                return null

            val settings = default()
            settings.core_settings_ = core_settings
            settings.equipment_ = equipment_settings
            settings.localisation_file_ = localisation_file
            settings.hawk_localisation_file_ = hawk_localisation_file
            return settings
        }
    }

    fun core_settings(): CoreSettings
    {
        return core_settings_
    }

    fun set_core_settings(value: CoreSettings)
    {
        core_settings_ = value
    }

    fun working_directory(): Path
    {
        return core_settings_.working_directory()
    }

    fun set_working_directory(value: String)
    {
        core_settings_.set_working_directory(Paths.get(value))
    }

    fun widefield(): DiskOrImage
    {
        return core_settings_.widefield()
    }

    fun image_stack(): DiskOrImage
    {
        return core_settings_.image_stack()
    }

    fun set_widefield(value: DiskOrImage)
    {
        core_settings_.set_widefield(value)
    }

    fun set_n_threads(value: Int)
    {
        core_settings_.set_n_threads(value)
    }

    fun settings_file_nn(): String
    {
        return core_settings_.settings_file_nn()
    }

    fun set_settings_file(value: String)
    {
        core_settings_.set_settings_file(value)
    }

    fun set_image_stack(value: DiskOrImage)
    {
        core_settings_.set_image_stack(value)
    }

    fun equipment(): EquipmentSettings
    {
        return equipment_
    }

    fun set_equipment_settings(value: EquipmentSettings)
    {
        equipment_ = value
    }

    fun magnification(): Double
    {
        return equipment_.magnification()
    }

    fun set_magnification(value: Double)
    {
        equipment_.set_magnification(value)
    }

    fun localisation_file(): LocalisationFile
    {
        return localisation_file_
    }

    fun set_localisation_file(value: LocalisationFile)
    {
        localisation_file_ = value
    }

    fun has_localisation_file(): Boolean
    {
        return localisation_file_.is_set()
    }

    fun localisation_filename(): String
    {
        return localisation_file_.filename_nn()
    }

    fun localisation_file_parse_method(): String
    {
        return localisation_file_.parse_method_string()
    }

    fun hawk_localisation_file(): LocalisationFile
    {
        return hawk_localisation_file_
    }

    fun set_hawk_localisation_file(value: LocalisationFile)
    {
        hawk_localisation_file_ = value
    }

    fun has_hawk_localisation_file(): Boolean
    {
        return hawk_localisation_file_.is_set()
    }

    fun hawk_localisation_filename(): String
    {
        return hawk_localisation_file_.filename_nn()
    }

    fun hawk_localisation_file_parse_method(): String
    {
        return hawk_localisation_file_.parse_method_string()
    }

    /// METHODS
    fun prepare_images_for_analysis(): CoreSettings?
    {
        return prepare_images_for_analysis_in(working_directory())
    }

    private fun prepare_images_for_analysis_in(working_directory: Path): CoreSettings?
    {
        return core_settings_.to_disk_in(working_directory)
    }

    fun record_to_macro()
    {
        core_settings_.record_to_macro()
        equipment_.record_to_macro()
        localisation_file_.record_to_macro(LOCALISATION_SETTINGS_RAW_LOCALISATIONS, LOCALISATION_SETTINGS_RAW_LOCALISATIONS_PARSER)
        hawk_localisation_file_.record_to_macro(LOCALISATION_SETTINGS_HAWK_LOCALISATIONS, LOCALISATION_SETTINGS_HAWK_LOCALISATIONS_PARSER)
    }
}