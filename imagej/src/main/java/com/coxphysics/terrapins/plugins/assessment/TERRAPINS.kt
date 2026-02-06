package com.coxphysics.terrapins.plugins.assessment

import com.coxphysics.terrapins.models.assessment.Assessment
import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.process.ImageJLoggingRunner

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
        val runner = ImageJLoggingRunner()
        val use_localisations = settings.use_localisations()
        if (use_localisations)
        {
            return assessment_.run_localisations(runner, settings.localisation_settings())
        }
        else
        {
            return assessment_.run_images(runner, settings.images_settings())
        }
    }
}