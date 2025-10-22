package com.coxphysics.terrapins.views.assessment.results

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.views.Checkbox
import com.coxphysics.terrapins.views.DirectoryField
import com.coxphysics.terrapins.views.StringField
import com.coxphysics.terrapins.views.Utils
import ij.gui.GenericDialog
import java.awt.Button
import java.awt.TextField
import java.awt.event.ItemEvent
import java.io.File
import java.nio.file.Path

class UI private constructor(private val data_path_: DirectoryField,
                             private val report_ : StringField,
                             private val half_split_: Checkbox,
                             private val zip_split_: Checkbox,
                             private val hawkman_core_: Checkbox,
                             private val hawkman_details_: Checkbox,
                             private val squirrel_results_: Checkbox)
{
    companion object
    {
        @JvmStatic
        fun add_controls_to_dialog(dialog: GenericDialog, results: AssessmentResults): UI
        {
            val data_path = Utils.add_directory_field(dialog, "Data", results.data_path().toString())

            val report_message = Utils.add_string_field(dialog, "Report", "")

            val half_split = Utils.add_checkbox(dialog, "Show half split results", true)
            val zip_split = Utils.add_checkbox(dialog, "Show zip split results", true)

            val hawkmna_core = Utils.add_checkbox(dialog, "Show HAWKMAN resolution map", true)
            val hawkman_details = Utils.add_checkbox(dialog, "Show all HAWKMAN results", false)

            val squirrel_results = Utils.add_checkbox(dialog, "Show all Squirrel results", true)
            return UI(data_path, report_message, half_split, zip_split, hawkmna_core, hawkman_details, squirrel_results)
        }
    }

    fun text_field(): TextField
    {
        return data_path_.text_field()
    }

    fun directory_selected_button(): Button
    {
        return data_path_.button()
    }

    fun data_path(): Path
    {
        return File(data_path_.filepath()).toPath()
    }

    fun is_half_split(event: ItemEvent) : Boolean
    {
        return half_split_.is_checkbox(event.source)
    }

    fun half_split_visible(): Boolean
    {
        return half_split_.is_checked
    }

    fun is_zip_split(event: ItemEvent) : Boolean
    {
        return zip_split_.is_checkbox(event.source)
    }

    fun zip_split_visible(): Boolean
    {
        return zip_split_.is_checked
    }

    fun is_hawkman_core(event: ItemEvent) : Boolean
    {
        return hawkman_core_.is_checkbox(event.source)
    }

    fun hawkman_core_visible(): Boolean
    {
        return hawkman_core_.is_checked || hawkman_details_visible()
    }

    fun is_hawkman_details(event: ItemEvent) : Boolean
    {
        return hawkman_details_.is_checkbox(event.source)
    }

    fun hawkman_details_visible(): Boolean
    {
        return hawkman_details_.is_checked
    }

    fun is_squirrel_results(event: ItemEvent) : Boolean
    {
        return squirrel_results_.is_checkbox(event.source)
    }

    fun squirrel_results_visible(): Boolean
    {
        return squirrel_results_.is_checked
    }

    fun show_report(data: String)
    {
        report_.set_text(data)
    }

//    fun handle_event(dialog: GenericDialog, event: ItemEvent)
//    {
//        if (!is_hawkman_details(event))
//        {
//            return
//        }
//        if (hawkman_details_visible() && !hawkman_core_.is_checked)
//        {
//            hawkman_core_.set_checked(true)
////            dialog.pack()
//        }
//
//    }
}