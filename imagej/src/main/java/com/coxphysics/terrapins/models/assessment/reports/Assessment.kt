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
    private val result_: Outcome,
    private val message_: String
    )
{
    companion object
    {
        @JvmStatic
        fun decompose(line: String) : Pair<String, String>?
        {
            val splits = line.split(",")
            if (splits.size < 2)
                return null
            val tail = splits.asSequence().drop(1).joinToString(separator = ",")
            return Pair(splits[0], tail)
        }

        @JvmStatic
        fun parse_score(score: String) : Double?
        {
            return score.toDoubleOrNull()
        }

        @JvmStatic
        fun parse_result(result: String) : Outcome?
        {
            if (result == "passed")
                return Outcome.PASS
            if (result == "failed")
                return Outcome.FAIL
            if (result == "-")
                return Outcome.INDETERMINATE
            return null
        }

        @JvmStatic
        fun from_lines(lines: List<String>): Assessment?
        {
            val name = decompose(lines[0])?.second
            val score = decompose(lines[1])?.second?.let { parse_score(it) }
            val result = decompose(lines[2])?.second?.let { parse_result(it) }
            val message = decompose(lines[3])?.second
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

    fun passed(): Boolean
    {
        return result_ == Outcome.PASS
    }

    fun failed(): Boolean
    {
        return result_ == Outcome.FAIL
    }

    fun indeterminate(): Boolean
    {
        return result_ == Outcome.INDETERMINATE
    }

    fun message(): String
    {
        return message_

    }
}