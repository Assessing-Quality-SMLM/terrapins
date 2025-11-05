package com.coxphysics.terrapins.views.assessment.results

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import ij.gui.NonBlockingGenericDialog
import java.awt.event.*
import java.nio.file.Path
import com.coxphysics.terrapins.views.frc.ResultsView as FrcResultsView
import com.coxphysics.terrapins.views.hawkman.ResultsView as HawkmanResultsView
import com.coxphysics.terrapins.views.squirrel.ResultsView as SquirrelResultsView

class ResultUpdater(private val dialog_: Dialog): ActionListener, FocusListener, TextListener
{
    override fun actionPerformed(e: ActionEvent?)
    {
        payload()
    }

//    override fun keyTyped(e: KeyEvent?)
//    {
//
//    }
//
//    override fun keyPressed(e: KeyEvent?)
//    {
//
//    }
//
//    override fun keyReleased(e: KeyEvent?)
//    {
//
//    }

    override fun focusGained(e: FocusEvent?)
    {

    }

    override fun focusLost(e: FocusEvent?)
    {
        payload()
    }

    override fun textValueChanged(e: TextEvent?)
    {
        payload()
    }

    fun payload()
    {
        dialog_.data_path_changed()
    }
}

class Dialog private constructor() : NonBlockingGenericDialog("Results"), ActionListener
{
    private var ui_ : UI? = null
    private var results_ : AssessmentResults? = null
    private var half_split_results_ : FrcResultsView? = null
    private var zip_split_results_ : FrcResultsView? = null
    private var drift_split_results_ : FrcResultsView? = null
    private var hawkman_results_ : HawkmanResultsView? = null
    private var squirrel_results_ : SquirrelResultsView? = null

    companion object
    {
        @JvmStatic
        fun from(results: AssessmentResults): Dialog
        {
            val dialog = Dialog()
            val ui = UI.add_controls_to_dialog(dialog, results)

            val result_updater = ResultUpdater(dialog)
            val text_field = ui.text_field()
//            text_field.addKeyListener(Something(dialog))
//            text_field.addActionListener(Something(dialog))
//            text_field.addFocusListener(result_updater)
//            text_field.addActionListener(result_updater)
            text_field.addTextListener(result_updater)

            dialog.set_ui(ui)
            dialog.create_results_views()
            return dialog
        }
    }

    private fun set_ui(ui: UI)
    {
        ui_ = ui
    }

    @Override
    override fun itemStateChanged(e: ItemEvent?)
    {
        if (e == null)
            return
//        ui_?.handle_event(this, e)

        val new_results = generate_results()
        val results = results()
        if (results == null)
            return
        if (ui_?.is_half_split(e) == true)
        {
            handle_half_split()
        }

        if (ui_?.is_zip_split(e) == true)
        {
            handle_zip_split()
        }

        if (ui_?.is_drift_split(e) == true)
        {
            handle_drift_split()
        }

        if (ui_?.is_hawkman_core(e) == true)
        {
            handle_hawkman_core_data()
        }

        if (ui_?.is_hawkman_details(e) == true)
        {
            handle_hawkman_details()
        }
    }

    fun data_path_changed()
    {
        val new_path = generate_results()
        if (!new_path)
            return
        update_outputs()
    }

    fun update_outputs()
    {
        handle_report()
        handle_half_split()
        handle_zip_split()
        handle_drift_split()
        handle_hawkman_core_data()
        handle_hawkman_details()
        handle_squirrel_results()
    }

    private fun generate_results(): Boolean
    {
        val old_data_path = results_?.data_path()
        val new_data_path = get_data_path()
        val paths_same = new_data_path == old_data_path
        if (paths_same)
            return false
        clear_results()
        create_results_views()
        return true
    }

    private fun create_results_views()
    {
        results_ = results()
        half_split_results_ = results_?.half_split_results()?.let { r -> FrcResultsView.with(r, "Half Split") }
        zip_split_results_ = results_?.zip_split_results()?.let { r -> FrcResultsView.with(r, "Zip Split") }
        drift_split_results_ = results_?.drift_split_results()?.let { r -> FrcResultsView.with(r, "Drift Split") }
        hawkman_results_ = results_?.hawkman_results()?.let { r -> HawkmanResultsView.from(r) }
        squirrel_results_ = results_?.squirrel_results()?.let { r -> SquirrelResultsView.from(r) }
    }

    private fun results(): AssessmentResults?
    {
        val data_path = get_data_path() ?: return null
        return AssessmentResults.from(data_path)
    }

    private fun get_data_path(): Path?
    {
        return ui_?.data_path()
    }

    private fun clear_results()
    {
        half_split_results_?.close()
        half_split_results_ = null
        zip_split_results_?.close()
        zip_split_results_ = null
        drift_split_results_?.close()
        drift_split_results_ = null
        hawkman_results_ = null;
    }

    private fun handle_report()
    {
        val report = results_?.report() ?: return
        ui_?.show_report(report)
    }

    private fun handle_half_split()
    {
        val visible = ui_?.half_split_visible() ?: false
        if (visible)
        {
            half_split_results_?.show()
        }
        else
        {
            half_split_results_?.hide()
        }
    }

    private fun handle_zip_split()
    {
        val visible = ui_?.zip_split_visible() ?: false
        if (visible)
        {
            zip_split_results_?.show()
        }
        else
        {
            zip_split_results_?.hide()
        }
    }

    private fun handle_drift_split()
    {
        val visible = ui_?.drift_split_visible() ?: false
        if (visible)
        {
            drift_split_results_?.show()
        }
        else
        {
            drift_split_results_?.hide()
        }
    }

    private fun handle_hawkman_core_data()
    {
        val visible = ui_?.hawkman_core_visible() ?: false
        if (visible)
        {
            hawkman_results_?.show_core()
        }
        else
        {
            hawkman_results_?.hide_core()
        }
    }

    private fun handle_hawkman_details()
    {
        val visible = ui_?.hawkman_details_visible() ?: false
        if (visible)
        {
            hawkman_results_?.show_details()
        }
        else
        {
            hawkman_results_?.hide_details()
        }
    }

    private fun handle_squirrel_results()
    {
        val visible = ui_?.squirrel_results_visible() ?: false
        if (visible)
        {
            squirrel_results_?.show()
        }
        else
        {
            squirrel_results_?.hide()
        }
    }
}