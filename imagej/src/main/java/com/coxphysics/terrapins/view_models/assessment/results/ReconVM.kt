package com.coxphysics.terrapins.view_models.assessment.results

import com.coxphysics.terrapins.models.assessment.results.Recon
import ij.ImagePlus

class ReconVM private constructor(
    private val title_: String,
    private val model: Recon,
    private val image_: ImagePlus?
)
{
    companion object
    {
        @JvmStatic
        fun from(title: String, model: Recon): ReconVM
        {
            return ReconVM(title, model, model.image())
        }
    }

    fun title(): String
    {
        return title_
    }

    fun show_image(value: Boolean)
    {
        if(value)
            image_?.show()
        else
            image_?.hide()
    }

    fun data(): String?
    {
        return model.data()
    }

    fun localisations(): String?
    {
        return model.localisation_list()
    }
}