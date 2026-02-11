package com.coxphysics.terrapins.models.utils

import java.util.function.Consumer
import javax.swing.event.DocumentListener
import javax.swing.event.DocumentEvent;

class ActionableDocumentListener<T> private constructor(
    private val view_: T,
    private val action_: Consumer<T>
) : DocumentListener
{

    companion object
    {
        @JvmStatic
        fun<T> from(view: T, action: Consumer<T>): ActionableDocumentListener<T>
        {
            return ActionableDocumentListener(view, action)
        }
    }

    override fun insertUpdate(e: DocumentEvent?) {
        action_.accept(view_)
    }

    override fun removeUpdate(e: DocumentEvent?) {
        action_.accept(view_)
    }

    override fun changedUpdate(e: DocumentEvent?) {
        action_.accept(view_)
    }
}