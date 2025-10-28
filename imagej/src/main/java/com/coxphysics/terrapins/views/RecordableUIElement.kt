package com.coxphysics.terrapins.views

import ij.gui.GenericDialog

interface RecordableUIElement<U, T: RecordableElement<U>>
{
    fun add_to_dialog(dialog: GenericDialog): T
}