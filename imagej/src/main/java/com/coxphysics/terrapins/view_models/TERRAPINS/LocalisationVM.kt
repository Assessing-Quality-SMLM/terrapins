package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.localisation.AssessmentSettings


class LocalisationVM private constructor(private var settings_: AssessmentSettings)
{
    private var localisation_file_vm_: LocalisationFileVM = LocalisationFileVM.default()
    private var hawk_loc_file_vm_: LocalisationFileVM = LocalisationFileVM.default()
    companion object {
        @JvmStatic
        fun from(settings: AssessmentSettings): LocalisationVM
        {
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

    fun localisation_file_vm(): LocalisationFileVM
    {
        return localisation_file_vm_
    }

    fun hawk_localisation_file_vm(): LocalisationFileVM
    {
        return hawk_loc_file_vm_
    }

    fun propogate_settings()
    {
//        settings_.set_localisation_file(localisation_file_vm_.current_path())
//        settings_.set_hawk_localisation_file(hawk_localisation_file_vm().current_path())
    }
}