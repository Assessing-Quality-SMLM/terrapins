package com.coxphysics.terrapins.models.assessment.workflow

import com.coxphysics.terrapins.models.PathWrapper
import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.plugins.WORKFLOW_SETTINGS_USE_LOCALISATIONS
import com.coxphysics.terrapins.views.ImageSelectorSetttings
import ij.plugin.frame.Recorder
import java.nio.file.Path
import com.coxphysics.terrapins.models.assessment.images.Settings as ImagesSettings
import com.coxphysics.terrapins.models.hawk.Settings as HawkSettings

class Settings private constructor(image_selector_setttings: ImageSelectorSetttings)
{
    private val hawk_stack_image_selector_settings_ = image_selector_setttings
    private var hawk_settings_ = HawkSettings.default()
    private var use_localisations_ = true
    private var localisation_settings_ = AssessmentSettings.default()
    private var images_settings_ = ImagesSettings.default()

    companion object
    {
        @JvmStatic
        fun default() : Settings
        {
            val image_selector_settings = ImageSelectorSetttings.default_()
            image_selector_settings.set_n_images(1);
            image_selector_settings.set_image_names(listOf("Image").toTypedArray());
            return Settings(image_selector_settings)
        }

        @JvmStatic
        fun extract_from_macro_options(options: MacroOptions): Settings
        {
            val settings = default()
            val hawk_settings = HawkSettings.from_macro_options(options)
            settings.hawk_settings_ = hawk_settings

            val equipment = EquipmentSettings.from_macro_options(options)

            val localisation_settings = AssessmentSettings.from_macro_options(options)
            if (localisation_settings != null)
            {
                localisation_settings.set_equipment_settings(equipment)
                settings.localisation_settings_ = localisation_settings
            }
            val images_settings = ImagesSettings.from_macro_options(options)
            if (images_settings != null)
            {
                images_settings.set_equipment_settings(equipment)
                settings.images_settings_ = images_settings
            }
            val use_localisations = localisation_settings != null
            settings.use_localisations_ = use_localisations
            return settings
        }
    }

    fun core_settings(): CoreSettings
    {
        if(use_localisations())
            return localisation_settings_.core_settings()
        return images_settings_.core_settings()
    }

    fun working_directory() : Path?
    {
        return core_settings().working_directory_path()
    }

    fun set_working_directory(value: Path)
    {
        localisation_settings_.set_working_directory(value)
        images_settings_.set_working_directory(value)
    }

    fun settings_file(): PathWrapper
    {
        return core_settings().settings_file()
    }

    fun use_localisations(): Boolean
    {
        return use_localisations_
    }

    fun set_use_localisations(value: Boolean)
    {
        use_localisations_ = value
    }

    fun hawk_stack_image_selector_settings(): ImageSelectorSetttings
    {
        return hawk_stack_image_selector_settings_;
    }

    fun hawk_settings() : HawkSettings
    {
        return hawk_settings_
    }

    fun localisation_settings(): AssessmentSettings
    {
        return localisation_settings_
    }

    fun set_localisations(value: AssessmentSettings)
    {
        localisation_settings_ = value
    }

    fun images_settings(): ImagesSettings
    {
        return images_settings_
    }

    fun set_images(value: ImagesSettings)
    {
        images_settings_ = value
    }

    fun record_to_macro()
    {
        hawk_settings_.record_values()
        Recorder.recordOption(WORKFLOW_SETTINGS_USE_LOCALISATIONS, use_localisations_.toString())
        if (use_localisations())
            localisation_settings_.record_to_macro()
        else
        {
            images_settings_.record_to_macro()
        }
    }
}