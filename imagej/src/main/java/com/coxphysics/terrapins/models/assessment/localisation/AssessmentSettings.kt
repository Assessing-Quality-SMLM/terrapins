package com.coxphysics.terrapins.models.assessment.localisation

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.localisations.LocalisationFile
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
            return AssessmentSettings(CoreSettings.new(working_directory))
        }

        @JvmStatic
        fun default(): AssessmentSettings
        {
            return AssessmentSettings(CoreSettings.default())
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
}