package com.coxphysics.terrapins.models.process

import ij.IJ
import ij.ImagePlus

/**
 * Runs an ImageJ command with macro options.
 *
 * A seam over [IJ.run], for the same reason [Runner] is one over [ProcessBuilder]: the thing on
 * the other side is a whole application, and code that calls it directly cannot be tested without
 * starting one. Here it also decides what a test can see - the command names and option strings
 * are the entire contract with ThunderSTORM, so being able to assert on them without ThunderSTORM
 * installed is most of what there is to check.
 */
interface MacroRunner
{
    /** Runs a command with no active image. */
    fun run(command: String, options: String)

    /** Runs a command against [image], which becomes the active image for the duration. */
    fun run_on(image: ImagePlus, command: String, options: String)

    companion object
    {
        /** The real one, which asks ImageJ to run the command. */
        @JvmStatic
        fun ij(): MacroRunner = IJMacroRunner()
    }
}

private class IJMacroRunner : MacroRunner
{
    override fun run(command: String, options: String)
    {
        IJ.run(command, options)
    }

    override fun run_on(image: ImagePlus, command: String, options: String)
    {
        IJ.run(image, command, options)
    }
}

/** Records what was asked for instead of running it. */
class RecordingMacroRunner : MacroRunner
{
    private val calls_ = mutableListOf<Pair<String, String>>()

    override fun run(command: String, options: String)
    {
        calls_.add(Pair(command, options))
    }

    override fun run_on(image: ImagePlus, command: String, options: String)
    {
        calls_.add(Pair(command, options))
    }

    fun calls(): List<Pair<String, String>> = calls_

    fun commands(): List<String> = calls_.map { it.first }

    /** Options for the first call to [command], or null if it was never made. */
    fun options_for(command: String): String? = calls_.firstOrNull { it.first == command }?.second

    fun ran(command: String): Boolean = calls_.any { it.first == command }
}
