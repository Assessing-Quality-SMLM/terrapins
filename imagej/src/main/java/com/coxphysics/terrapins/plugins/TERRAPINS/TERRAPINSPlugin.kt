package com.coxphysics.terrapins.plugins.TERRAPINS

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.models.assessment.TERRAPINS
import com.coxphysics.terrapins.models.assessment.workflow.Settings
import com.coxphysics.terrapins.models.ij_wrapping.IJWindowManager
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.models.macros.MacroUtils
import com.coxphysics.terrapins.view_models.TERRAPINS.TERRAPINSVM
import com.coxphysics.terrapins.views.TERRAPINS.TERRAPINSTabView
import ij.IJ
import ij.ImageJ
import ij.plugin.PlugIn
import java.awt.Dimension
import java.io.File
import java.util.prefs.Preferences
import javax.swing.SwingUtilities


const val DEFAULT_WIDTH = 400
const val DEFAULT_HEIGHT = 400

const val PERSISTENCE_KEY_WIDTH = "UI_SIZE_WIDTH"
const val PERSISTENCE_KEY_HEIGHT = "UI_SIZE_HEIGHT"

class TERRAPINSPlugin : PlugIn
{
    private var settings_ : Settings = Settings.default()

    companion object
    {
        @JvmStatic
        fun main(args: Array<String>)
        {
            val clazz = TERRAPINSPlugin::class.java
            val url = clazz.getProtectionDomain().getCodeSource().getLocation();
            val file = File(url.toURI());

            System.setProperty("plugins.dir", file.getAbsolutePath());

            // start ImageJ
            ImageJ();
                    // run the plugin
//            val reference = IJ.openImage(SQUIRREL_GetFileFromResource.getLocalFileFromResource("/HDVee.tif").getAbsolutePath());
//            reference.show();
//            IJ.run("Record...")
            IJ.runPlugIn(clazz.getName(), "");
        }
    }

    override fun run(p0: String?)
    {
        if (MacroUtils.is_ran_from_macro())
        {
            val options = MacroOptions.default()
            if (options == null)
            {
//                // maybe dont do this as in macro land - what happens in headless mode
//                IJ.log("Could not construct macro options")
                return
            }
            settings_ = Settings.extract_from_macro_options(options, IJWindowManager.new())
            val results = run_assessment(settings_) // has side-effects to disk
            // we are in a macro so don't display the results viewer
        }
        else {
            val cancelled = run_tabbed_view()
        }
    }

    private fun run_tabbed_view(): Boolean
    {
        val view_model = TERRAPINSVM.from(settings_)
        val view = TERRAPINSTabView.from(view_model)
        view.preferredSize = get_ui_size()
        view.pack()
        SwingUtilities.invokeLater{
            view.isVisible = true
        }
        // wait on the semaphore
        val result = view.was_canceled()
        val dimension = view.size
        if (dimension == null)
            return result
        persist_ui_size(dimension)
        return result
    }

    private fun get_preferences(): Preferences?
    {
        return Preferences.userNodeForPackage(this::class.java)
    }

    fun get_ui_size(): Dimension
    {
        val loaded_size = get_preferences()?.let { p -> load_ui_size_from_persistance(p) }
        if(loaded_size == null)
            return Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        return loaded_size
    }

    fun load_ui_size_from_persistance(preferences: Preferences): Dimension
    {
        val width = preferences.getInt(PERSISTENCE_KEY_WIDTH, DEFAULT_WIDTH)
        val height = preferences.getInt(PERSISTENCE_KEY_HEIGHT, DEFAULT_HEIGHT)
        return Dimension(width, height)
    }

    private fun persist_ui_size(ui_size: Dimension)
    {
        val preferences = get_preferences()
        if (preferences == null)
            return
        persist_ui_size_to(preferences, ui_size)
    }
    private fun persist_ui_size_to(preferences: Preferences, ui_size: Dimension)
    {
        preferences.putInt(PERSISTENCE_KEY_HEIGHT, ui_size.height)
        preferences.putInt(PERSISTENCE_KEY_WIDTH, ui_size.width)
    }
    private fun run_assessment(settings: Settings): AssessmentResults?
    {
        return TERRAPINS.default().run(settings)
    }
}