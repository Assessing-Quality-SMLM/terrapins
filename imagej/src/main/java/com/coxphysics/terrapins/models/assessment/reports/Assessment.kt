package com.coxphysics.terrapins.models.assessment.reports

import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists

enum class Outcome
{
    PASS, FAIL, INDETERMINATE
}

class Assessment private constructor(
    private val name_: String,
    private val score_: Double?,
    private val result_: Pair<Outcome, String>,
    private val message_: String
    )
{
    companion object
    {
        @JvmStatic
        fun decompose(line: String, delim: Char) : Pair<String, String>?
        {
            val splits = line.split(delim)
            if (splits.size < 2)
                return null
            val tail = splits.asSequence().drop(1).joinToString(separator = delim.toString())
            return Pair(splits[0], tail)
        }

        @JvmStatic
        fun parse_score(score: String) : Double?
        {
            return score.toDoubleOrNull()
        }

        @JvmStatic
        fun parse_result(result: String) : Pair<Outcome, String>?
        {
            val values = decompose(result, ',')
            var outcome = result
            var label = result
            if (values != null)
            {
                outcome = values.first
                label = values.second
            }
            if (outcome == "passed")
                return Pair(Outcome.PASS, label)
            if (outcome == "failed")
                return Pair(Outcome.FAIL, label)
            if (outcome == "indeterminate")
                return Pair(Outcome.INDETERMINATE, label)
            return null
        }

        @JvmStatic
        fun from_lines(lines: List<String>): Assessment?
        {
            if (lines.size < 4)
                return null
            val name = decompose(lines[0], ',')?.second
            val score = decompose(lines[1], ',')?.second?.let { parse_score(it) }
            val result = decompose(lines[2], ',')?.second?.let { parse_result(it) }
            val message = decompose(lines[3], ',')?.second
            if (name == null || result == null || message == null)
                return null
            return Assessment(name, score, result, message)

        }
        @JvmStatic
        fun from_stream(stream: InputStream): Assessment?
        {
            val lines = mutableListOf<String>()
            stream.bufferedReader().forEachLine { lines.add(it) }
            return from_lines(lines)
        }

        fun from_disk(filename: Path): Assessment?
        {
            if (!filename.exists())
                return null
            val stream = File(filename.toString()).inputStream()
            return from_stream(stream)
        }
    }

    fun name() : String
    {
        return name_
    }

    fun score(): Double?
    {
        return score_
    }

    private fun outcome() : Outcome
    {
        return result_.first
    }

    fun passed(): Boolean
    {
        return outcome() == Outcome.PASS
    }

    fun failed(): Boolean
    {
        return outcome() == Outcome.FAIL
    }

    fun indeterminate(): Boolean
    {
        return outcome() == Outcome.INDETERMINATE
    }

    fun outcome_label() : String
    {
        return result_.second
    }

    fun message(): String
    {
        return message_

    }
}