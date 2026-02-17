package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.log.IJLog
import com.coxphysics.terrapins.models.log.Log
import com.coxphysics.terrapins.models.macros.MacroUtils
import com.coxphysics.terrapins.models.process.LoggingRunner
import com.coxphysics.terrapins.models.assessment.images.Settings as ImagesSettings

class TERRAPINS private constructor(
    private val assessment_: Assessment,
    private val logger_: Log<String>
)
{
    companion object
    {
        @JvmStatic
        fun from(assessment: Assessment, logger: Log<String>) : TERRAPINS
        {
            return TERRAPINS(assessment, logger)
        }
        @JvmStatic
        fun default(): TERRAPINS
        {
            return from(Assessment.default(), IJLog.new())
        }
    }

//    private fun default_runner() = ImageJLoggingRunner()
    private fun default_runner() = LoggingRunner.from(logger_)

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
        val results = assessment_.run_localisations(runner, settings)
        record_localisations_macro(settings)
        return results
    }

    fun run_images(settings: ImagesSettings) : AssessmentResults?
    {
        val runner = default_runner()
        val results = assessment_.run_images(runner, settings)
        record_images_macro(settings)
        return results
    }

    fun record_localisations_macro(settings: AssessmentSettings)
    {
        if (MacroUtils.is_recording())
        {
            settings.record_to_macro()
        }
    }

    fun record_images_macro(settings: ImagesSettings)
    {
        if (MacroUtils.is_recording())
        {
            settings.record_to_macro()
        }
    }
}