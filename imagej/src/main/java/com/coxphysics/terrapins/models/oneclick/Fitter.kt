package com.coxphysics.terrapins.models.oneclick

import ij.ImagePlus
import java.nio.file.Path

/**
 * Turns an image stack into a localisation table on disk.
 *
 * The seam between the one-click workflow and whichever localiser produced the data. Two things
 * are behind it: the built-in fast moment fitter, which runs in this process, and ThunderSTORM,
 * which does not. Keeping them behind one interface means the workflow does not care which ran,
 * and a third can be added without touching the workflow at all.
 *
 * Implementations write a file the assessment can read directly - see [ParseMethodName] - rather
 * than returning localisations in memory, because the assessment is a separate executable that
 * takes file paths, and a table large enough to matter should not be marshalled twice.
 *
 * The bundled moment fitter is a scaffold, not the intended fitter: an improved one is in testing
 * that corrects systematic bias at the cost of a slightly slower fit. When it lands it replaces
 * the moment fitter here, and `PrecisionEstimator`'s calibration constants do **not** come with
 * it - they measure how far that particular fitter falls short of the Thompson bound, which is
 * the thing the replacement changes. See imagej/PROVENANCE.md.
 */
interface Fitter
{
    /**
     * Localises [image] and writes the table to [output].
     *
     * @param image  the stack to localise. May be a virtual stack, including the HAWK stream,
     *               which is generated frame by frame on demand.
     * @param output where to write the table. The parent directory is created if needed.
     * @return the number of localisations written, or null if the run failed.
     */
    fun localise(image: ImagePlus, output: Path): Int?

    /**
     * The `--locs-format` value the assessment needs in order to read what this fitter writes.
     * "ts" for anything writing ThunderSTORM's columns.
     */
    fun parse_method_name(): String

    /** Whether the tables carry a usable `uncertainty_xy`, which decides what the report can say. */
    fun produces_uncertainty(): Boolean

    /** Shown in the log and recorded in the run settings. */
    fun name(): String
}
