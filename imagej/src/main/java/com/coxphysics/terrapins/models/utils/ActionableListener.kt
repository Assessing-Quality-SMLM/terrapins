package com.coxphysics.terrapins.models.utils

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.function.Consumer

class ActionableListener<T> private constructor(
    private val view_: T,
    private val action_: Consumer<T>
) : ActionListener
{

    companion object
    {
        @JvmStatic
        fun <T> from(view: T, action: Consumer<T>): ActionableListener<T>
        {
            return ActionableListener(view, action)
        }
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        action_.accept(view_)
    }
}