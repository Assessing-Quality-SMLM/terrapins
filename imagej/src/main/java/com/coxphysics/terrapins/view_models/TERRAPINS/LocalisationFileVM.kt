package com.coxphysics.terrapins.view_models.TERRAPINS

import com.coxphysics.terrapins.models.localisations.LocalisationFile
import com.coxphysics.terrapins.models.localisations.ParseMethod

class LocalisationFileVM private constructor(private var model_: LocalisationFile)
{
    private var path_vm_: PathSelectorVM = PathSelectorVM.default_()
    companion object
    {
        @JvmStatic
        fun from(model: LocalisationFile): LocalisationFileVM
        {
            val vm = LocalisationFileVM(model)
            vm.path_vm_.set_is_files_only(true)
            return vm
        }

        @JvmStatic
        fun default(): LocalisationFileVM
        {
            return from(LocalisationFile.default())
        }

        // For Java
        @JvmStatic
        fun default_(): LocalisationFileVM
        {
            return default()
        }
    }

    fun path_selector_vm(): PathSelectorVM
    {
        return path_vm_
    }

    fun update_model_path()
    {
        model_.set_filename(path_vm_.current_path().toString())
    }

    fun set_delimeter(value: String)
    {
        val c_array = value.toCharArray()
        if (c_array.size < 1)
            return
        model_.set_delimeter(c_array[0])
    }

    fun set_n_header_lines(value: String)
    {
        val i = value.toIntOrNull()
        if (i == null)
            return
        model_.set_n_header_lines(i)
    }

    fun set_x_pos(value: String)
    {
        val i = value.toIntOrNull()
        if (i == null)
            return
        model_.set_x_pos(i)
    }

    fun set_y_pos(value: String)
    {
       val i = value.toIntOrNull()
        if (i == null)
            return
        model_.set_y_pos(i)
    }

    fun set_uncertainty_sigma_pos(value: String)
    {
        val i = value.toIntOrNull()
        if (i == null)
            return
        model_.set_uncertainty_sigma_pos(i)
    }

    fun set_frame_number_pos(value: String)
    {
        val i = value.toIntOrNull()
        if (i == null)
            return
        model_.set_frame_number_pos(i)
    }

}