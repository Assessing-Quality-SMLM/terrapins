package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.workflow.Settings

class TERRAPINSVM private constructor(private val settings_: Settings)
{
    private val pre_processing_vm_: PreProcessingVM = PreProcessingVM.from(settings_)
    private val equipment_settings_vm_: EquipmentSettingsVM = EquipmentSettingsVM.from(settings_.localisation_settings().equipment())
    private val localisation_vm_ : LocalisationVM = LocalisationVM.from(settings_.localisation_settings())
    private val images_vm_ : ImagesVM = ImagesVM.from(settings_.images_settings())
    companion object
    {
        @JvmStatic
        fun from(settings: Settings) : TERRAPINSVM
        {
            return TERRAPINSVM(settings)
        }

        @JvmStatic
        fun default() : TERRAPINSVM
        {
            return from(Settings.default())
        }
    }

    fun pre_processing_vm(): PreProcessingVM
    {
        return pre_processing_vm_
    }

    fun equipment_settings_vm(): EquipmentSettingsVM
    {
        return equipment_settings_vm_
    }

    fun localisation_vm(): LocalisationVM
    {
        return localisation_vm_
    }

    fun images_vm(): ImagesVM
    {
        return images_vm_
    }

    fun use_localisations(): Boolean
    {
        return settings_.use_localisations()
    }

    fun set_use_localisations(value: Boolean)
    {
        settings_.set_use_localisations(value)
    }
}