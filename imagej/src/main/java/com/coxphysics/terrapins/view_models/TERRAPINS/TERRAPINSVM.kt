package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.TERRAPINS
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.to_nullable_path
import java.awt.Color
import javax.swing.JTextField
import kotlin.io.path.exists

class TERRAPINSVM private constructor(private val settings_: Settings)
{
    private val pre_processing_vm_: PreProcessingVM = PreProcessingVM.from(settings_)
    private val localisations_equipment_settings_vm_: EquipmentSettingsVM = EquipmentSettingsVM.from(settings_.localisation_settings().equipment())
    private val localisation_vm_ : LocalisationVM = LocalisationVM.from(settings_.localisation_settings())
    private val images__equipment_settings_vm_: EquipmentSettingsVM = EquipmentSettingsVM.from(settings_.images_settings().equipment_settings())
    private val images_vm_ : ImagesVM = ImagesVM.from(settings_.images_settings())
    private val settings_vm_: PathSelectorVM = PathSelectorVM.with_path_and_title("Settings File", settings_.settings_file())
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

    fun working_directory(): String
    {
        return settings_.working_directory().toString()
    }

    fun set_working_directory(value: String): Color
    {
        val path = value.to_nullable_path()
        if (path == null)
            return error_colour()
        if (!path.exists())
            return error_colour()
        settings_.set_working_directory(path)
        return default_background_colour()
    }

    private fun default_background_colour(): Color = JTextField().background

    private fun error_colour(): Color = Color.RED

    fun pre_processing_vm(): PreProcessingVM
    {
        return pre_processing_vm_
    }

    fun localisation_equipment_settings_vm(): EquipmentSettingsVM
    {
        return localisations_equipment_settings_vm_
    }

    fun localisation_vm(): LocalisationVM
    {
        return localisation_vm_
    }

    fun images_equipment_settings_vm(): EquipmentSettingsVM
    {
        return images__equipment_settings_vm_
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

    fun settings_vm(): PathSelectorVM
    {
        return settings_vm_
    }

    fun run_localisations() : AssessmentResults?
    {
        return TERRAPINS.default().run_localisations(settings_.localisation_settings())
    }

    fun run_images() : AssessmentResults?
    {
        return TERRAPINS.default().run_images(settings_.images_settings())
    }
}