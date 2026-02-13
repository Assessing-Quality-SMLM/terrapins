package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.process.ImageJLoggingRunner
import com.coxphysics.terrapins.models.assessment.images.Settings as ImagesSettings

class TERRAPINS private constructor(private val assessment_: Assessment)
{
    companion object
    {
        @JvmStatic
        fun from(assessment: Assessment) : TERRAPINS
        {
            return TERRAPINS(assessment)
        }
        @JvmStatic
        fun default(): TERRAPINS
        {
            return from(Assessment.default())
        }
    }

    fun run(settings: Settings) : AssessmentResults?
    {
        return if (settings.use_localisations())
            run_localisations(settings.localisation_settings())
        else
            run_images(settings.images_settings())
    }

    fun run_localisations(settings: AssessmentSettings) : AssessmentResults?
    {
        val runner = default_runner()
        return assessment_.run_localisations(runner, settings)
    }

    fun run_images(settings: ImagesSettings) : AssessmentResults?
    {
        val runner = default_runner()
        return assessment_.run_images(runner, settings)
    }

    private fun default_runner() = ImageJLoggingRunner()
}