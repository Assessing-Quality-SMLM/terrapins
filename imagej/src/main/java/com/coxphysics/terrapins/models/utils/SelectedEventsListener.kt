package com.coxphysics.terrapins.models.utils

import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.util.function.Consumer

class SelectedEventsListener<T> private constructor(
    private val view_: T,
    private val action_: Consumer<T>
) : ItemListener
{
    companion object
    {
        @JvmStatic
        fun <T> from(view: T, action: Consumer<T>): SelectedEventsListener<T>
        {
            return SelectedEventsListener(view, action)
        }
    }

    override fun itemStateChanged(e: ItemEvent?)
    {
        if (e == null || is_deselected(e))
            return;
        action_.accept(view_)
    }

    private fun is_deselected(e: ItemEvent): Boolean
    {
        return e.getStateChange() == ItemEvent.DESELECTED;
    }
}