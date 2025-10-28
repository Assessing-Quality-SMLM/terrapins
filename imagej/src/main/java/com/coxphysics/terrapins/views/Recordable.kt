package com.coxphysics.terrapins.views

import ij.gui.GenericDialog

interface Recordable<T>
{
    fun extract_from(dialog: GenericDialog): T
}