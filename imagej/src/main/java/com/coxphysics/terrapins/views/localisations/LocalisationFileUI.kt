package com.coxphysics.terrapins.views.localisations

import com.coxphysics.terrapins.models.localisations.LocalisationFile
import com.coxphysics.terrapins.views.Utils
import ij.gui.GenericDialog

private const val LOCALISATION_FILE = "Localisation File"

private fun get_filename(value: String?): String
{
    return value?: LOCALISATION_FILE
}

class LocalisationFileUI
{
     companion object Factory
     {
         @JvmStatic
         fun add_to_dialog(dialog: GenericDialog, model: LocalisationFile, filename_text: String?)
         {
             val localisation_file = Utils.add_file_field(dialog, get_filename(filename_text), model.filename_nn())
             val localisation_parse_method = ParseMethodsUI.add_to_dialog(dialog, model.parse_method())
         }

         @JvmStatic
         fun create_settings_record(dialog: GenericDialog): LocalisationFile
         {
             val filename = Utils.extract_file_field(dialog)
             val parse_method = ParseMethodsUI.create_settings_recorded(dialog)
             return LocalisationFile.new(filename, parse_method)
         }
     }
}