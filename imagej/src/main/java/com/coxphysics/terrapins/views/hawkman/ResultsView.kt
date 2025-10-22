package com.coxphysics.terrapins.views.hawkman

import com.coxphysics.terrapins.models.hawkman.external.Results
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.IJUtils
import ij.ImagePlus
import java.nio.file.Path

class ResultsView private constructor(private val results_: Results )
{
    // CORE
    private var resolution_image_ : ImagePlus? = null
    private var scale_map_ : ImagePlus? = null

    //REST
    private var confidence_map_ : ImagePlus? = null
    private var sharpening_map_ : ImagePlus? = null
    private var skeleton_map_ : ImagePlus? = null
    private var structure_map_ : ImagePlus? = null

    companion object
    {
        @JvmStatic
        fun from(results: Results): ResultsView
        {
            return ResultsView(results)
        }

        fun is_empty(image: ImagePlus?): Boolean
        {
            return image?.image == null
        }

        fun load_image(image_path: Path): ImagePlus?
        {
            if (!FsUtils.exists(image_path))
                return null;
            return IJUtils.load_image(image_path);
        }
    }

    private fun resolution_image() : ImagePlus?
    {
        if (is_empty(resolution_image_))
        {
            resolution_image_ =  load_image(results_.resolution_map_path())
        }
        return resolution_image_
    }

    private fun scale_map() : ImagePlus?
    {
        if (is_empty(scale_map_))
        {
            scale_map_ =  load_image(results_.scale_map_path())
        }
        return scale_map_
    }

    private fun confidence_map() : ImagePlus?
    {
        if (is_empty(confidence_map_))
        {
            confidence_map_ = results_.confidence_map_generator()
        }
        return confidence_map_;
    }

    private fun sharpening_map() : ImagePlus?
    {
        if (is_empty(sharpening_map_))
        {
            sharpening_map_ = results_.sharpening_map_generator()
        }
        return sharpening_map_;
    }

    private fun skeleton_map() : ImagePlus?
    {
        if (is_empty(skeleton_map_))
        {
            skeleton_map_ = results_.skeleton_map_generator()
        }
        return skeleton_map_;
    }

    private fun structure_map() : ImagePlus?
    {
        if (is_empty(structure_map_))
        {
            structure_map_ = results_.structure_map_generator()
        }
        return structure_map_;
    }

    fun show_core()
    {
        resolution_image()?.show()
        scale_map()?.show()
    }

    fun show_details()
    {
        show_core()
        confidence_map()?.show();
        skeleton_map()?.show();
        sharpening_map()?.show();
        structure_map()?.show();
    }

    fun hide_core()
    {
        resolution_image_?.hide()
        scale_map_?.hide()
    }

    fun hide_details()
    {
        confidence_map_?.hide()
        skeleton_map_?.hide()
        sharpening_map_?.hide()
        structure_map_?.hide()
    }
}