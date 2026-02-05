package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings


class LocalisationVM private constructor(private var settings_: AssessmentSettings) {
    companion object {
        @JvmStatic
        fun from(settings: AssessmentSettings): LocalisationVM {
            return LocalisationVM(settings)
        }

        @JvmStatic
        fun default(): LocalisationVM {
            return from(AssessmentSettings.default())
        }

        // for calls from Java
        @JvmStatic
        fun default_(): LocalisationVM
        {
            return default()
        }
    }
}