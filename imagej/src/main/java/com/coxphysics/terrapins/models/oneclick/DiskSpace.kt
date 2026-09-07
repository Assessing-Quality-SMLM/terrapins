package com.coxphysics.terrapins.models.oneclick

import ij.ImagePlus
import java.nio.file.Files
import java.nio.file.Path

/**
 * How much room a run needs, and whether there is that much.
 *
 * A one-click run is minutes of work that writes gigabytes at the very end, so running out of
 * space fails late, after everything expensive has already been done. Worse, it fails
 * *unhelpfully*: the assessment reports "No space left on device" somewhere in its output and
 * exits non-zero, and the plugin says only that an executable exited with code 1. Checking first
 * turns twenty wasted minutes into a sentence before the run starts.
 *
 * ### Where the size comes from
 *
 * Measured, not guessed. A 500 frame 128x128 acquisition at magnification 10 with the default
 * twenty HAWKMAN levels produced 1190 MB as a `.smlm` archive and another 1191 MB extracted
 * beside it - about 2.4 GB for one run.
 *
 * The bulk is HAWKMAN: roughly ten full-size 32-bit maps per level - the binarised and
 * skeletonised images for reference and test, the correlation maps, and the per-level outputs -
 * at every one of its levels. That gives
 *
 *     bytes ~= (width * mag) * (height * mag) * 4 * MAPS_PER_LEVEL * levels * 2
 *
 * where the final 2 is the archive and its extracted copy, which the assessment writes both of.
 * For the measured case that predicts about 2.6 GB against 2.4 GB observed, which is close enough
 * to warn on and deliberately errs high.
 *
 * Note what this does *not* scale with: the number of frames, or of localisations. The maps are
 * the cost, and they are set by the rendered image size and the level count.
 */
object DiskSpace
{
    /**
     * Full-size 32-bit maps HAWKMAN writes per level, from counting the output of a real run.
     * Approximate on purpose - this is a warning threshold, not an accounting.
     */
    const val MAPS_PER_LEVEL = 10

    /** The assessment writes its results as an archive *and* extracts them beside it. */
    const val ARCHIVE_AND_EXTRACTED = 2

    /** Headroom over the estimate before a run is called safe, since the estimate is rough. */
    const val SAFETY_FACTOR = 1.3

    /** Bytes a run is expected to write under the working directory. */
    @JvmStatic
    fun estimate_bytes(image: ImagePlus, magnification: Double, hawkman_levels: Int): Long
    {
        val rendered_width = image.width * magnification
        val rendered_height = image.height * magnification
        val bytes_per_map = rendered_width * rendered_height * 4.0
        return (bytes_per_map * MAPS_PER_LEVEL * hawkman_levels * ARCHIVE_AND_EXTRACTED).toLong()
    }

    /** Usable bytes on the filesystem holding [directory], or null if it cannot be determined. */
    @JvmStatic
    fun usable_bytes(directory: Path): Long?
    {
        return try
        {
            // Walk up to the nearest existing ancestor: the working directory may not exist yet.
            var probe: Path? = directory
            while (probe != null && !Files.exists(probe))
            {
                probe = probe.parent
            }
            probe?.toFile()?.usableSpace
        }
        catch (e: SecurityException)
        {
            null
        }
    }

    /**
     * A sentence about the space this run needs, or null when there is plainly enough.
     *
     * Returns a warning rather than refusing outright. The estimate is approximate, a user may
     * know something it does not, and refusing a run on a guess would be worse than saying what
     * the guess is.
     */
    @JvmStatic
    fun warning(image: ImagePlus, magnification: Double, hawkman_levels: Int,
                working_directory: Path): String?
    {
        val needed = estimate_bytes(image, magnification, hawkman_levels)
        val available = usable_bytes(working_directory) ?: return null
        if (available > needed * SAFETY_FACTOR)
        {
            return null
        }
        return "This run is estimated to need about ${gb(needed)} under $working_directory, " +
            "which has ${gb(available)} free. Results are written twice - an archive and an " +
            "extracted copy - and previous runs are not deleted, so clearing old ones or " +
            "choosing a working directory with more room would be wise."
    }

    private fun gb(bytes: Long): String
    {
        val value = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
        return if (value < 0.1) "%.0f MB".format(bytes.toDouble() / (1024.0 * 1024.0))
               else "%.1f GB".format(value)
    }
}
