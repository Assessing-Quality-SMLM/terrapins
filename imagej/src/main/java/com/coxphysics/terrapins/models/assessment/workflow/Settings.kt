package com.coxphysics.terrapins.models.assessment.workflow

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.views.ImageSelectorSetttings
import com.coxphysics.terrapins.models.assessment.images.Settings as ImagesSettings
import com.coxphysics.terrapins.models.hawk.Settings as HawkSettings

class Settings private constructor(image_selector_setttings: ImageSelectorSetttings)
{
    private val hawk_stack_image_selector_settings_ = image_selector_setttings
    private val hawk_settings_ = HawkSettings.default()
    private val use_localisations_ = true
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
        fun extract_from_macro(): Settings
        {
            return default()
        }
    }

    fun use_localisations(): Boolean
    {
        return use_localisations_
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

    fun record_values()
    {

    }
}