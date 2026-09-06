package com.coxphysics.terrapins.models.oneclick

import java.nio.file.Files
import java.nio.file.Path

/** The `--locs-format` values the assessment understands. */
object ParseMethodName
{
    /** ThunderSTORM's column names, which the fast fitter also writes. */
    const val THUNDERSTORM = "ts"
}

/** Where the one-click workflow puts the tables it generates, and how they are named. */
object LocalisationTable
{
    /** The table localised from the raw stack. */
    const val RAW_NAME = "raw-localisations.csv"

    /** The table localised from the HAWK stream, which the bias assessment needs. */
    const val HAWK_NAME = "hawk-localisations.csv"

    /**
     * Subdirectory of the working directory holding generated tables.
     *
     * Kept separate from the assessment's own output so a re-run can be told apart from the
     * inputs that produced it, and so a user who wants to re-localise elsewhere, or feed these
     * tables to another tool, can find them without picking through the report.
     */
    const val DIRECTORY = "localisations"

    @JvmStatic
    fun directory_in(working_directory: Path): Path = working_directory.resolve(DIRECTORY)

    @JvmStatic
    fun raw_in(working_directory: Path): Path = directory_in(working_directory).resolve(RAW_NAME)

    @JvmStatic
    fun hawk_in(working_directory: Path): Path = directory_in(working_directory).resolve(HAWK_NAME)

    /** Creates the parent directory of [path] if it does not exist. */
    @JvmStatic
    fun ensure_parent(path: Path)
    {
        val parent = path.parent ?: return
        if (!Files.exists(parent))
        {
            Files.createDirectories(parent)
        }
    }
}

/**
 * How many HAWK decomposition levels a stack of a given length can actually support.
 *
 * Level `l` differences frames `2^l` apart across a window of `2^(l+1)`, so it contributes
 * nothing once the stack is shorter than that window. `PStream` handles this by simply not
 * emitting frames for such a level - its inner loop runs zero times - so asking for more levels
 * than the data supports produces a *partial* decomposition with no error and no warning:
 * `Config.get_validation_errors` is a stub that always returns null.
 *
 * That silence matters here because the assessment is told `--hawkman-n-levels` separately from
 * being given the stream. Left alone the two disagree, and the bias report would be scaled
 * against levels the data never contained.
 *
 * (`Config.get_output_size` has the same blind spot from the other direction: its closed form
 * assumes every level contributes, so for 4 frames at 3 levels it predicts 1 position where the
 * stream really has 4. It is currently unused, so this is latent rather than live.)
 */
object HawkLevels
{
    /** The deepest usable level count for [n_frames], which may be zero. */
    @JvmStatic
    fun supported_for(n_frames: Int): Int
    {
        var levels = 0
        while ((2 shl levels) <= n_frames)
        {
            levels++
        }
        return levels
    }

    /** [requested], reduced to what [n_frames] can actually support. */
    @JvmStatic
    fun effective(requested: Int, n_frames: Int): Int
    {
        return minOf(requested, supported_for(n_frames))
    }
}
