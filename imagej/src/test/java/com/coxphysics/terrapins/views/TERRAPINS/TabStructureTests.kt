package com.coxphysics.terrapins.views.TERRAPINS

import com.coxphysics.terrapins.view_models.TERRAPINS.TERRAPINSVM
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Container
import java.awt.GraphicsEnvironment
import javax.swing.JTabbedPane
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the shape of the main window.
 *
 * The tabs are rearranged in [TERRAPINSTabView]'s constructor rather than in the GUI designer's
 * generated setup method, precisely because that method is regenerated wholesale whenever someone
 * opens the .form and saves it. That makes the arrangement survive regeneration - but it also
 * means nothing in the designer records the intended layout, so this test is where the intent is
 * written down.
 *
 * If someone adds a tab in the designer it lands inside Advanced automatically, which is what we
 * want; this will tell them if it does not.
 */
class TabStructureTests
{
    private fun find_tabbed_pane(component: Component?): JTabbedPane?
    {
        if (component is JTabbedPane)
        {
            return component
        }
        if (component is Container)
        {
            for (child in component.components)
            {
                val found = find_tabbed_pane(child)
                if (found != null)
                {
                    return found
                }
            }
        }
        return null
    }

    private fun titles(pane: JTabbedPane): List<String> =
        (0 until pane.tabCount).map { pane.getTitleAt(it) }

    private fun view(): TERRAPINSTabView
    {
        // Swing needs a display even to build a component tree, so skip rather than fail.
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display")
        return TERRAPINSTabView.from(TERRAPINSVM.default())
    }

    @Test
    fun one_click_is_the_first_thing_a_user_sees()
    {
        val view = view()
        try
        {
            val tabs = find_tabbed_pane(view.contentPane)!!
            assertEquals(listOf("One Click", "Advanced"), titles(tabs))
            assertEquals(0, tabs.selectedIndex, "One Click must be the selected tab")
        }
        finally
        {
            view.dispose()
        }
    }

    @Test
    fun the_existing_workflows_are_moved_not_removed()
    {
        // The point of the grouping is that the old routes still work for anyone who needs them.
        val view = view()
        try
        {
            val tabs = find_tabbed_pane(view.contentPane)!!
            val advanced = tabs.getComponentAt(1)
            assertTrue(advanced is JTabbedPane, "Advanced should hold the old tabs")
            assertEquals(
                listOf("Pre-Processing", "Localisation Workflow", "Images Workflow", "Advanced"),
                titles(advanced as JTabbedPane))
        }
        finally
        {
            view.dispose()
        }
    }
}
