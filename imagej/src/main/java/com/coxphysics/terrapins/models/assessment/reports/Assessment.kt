package com.coxphysics.terrapins.models.assessment.reports

import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists

class Assessment private constructor(
    private val name_: String,
    private val score_: Double?,
    private val result_: Boolean,
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
            return Pair(splits[0], splits[1])
        }

        @JvmStatic
        fun parse_score(score: String) : Double?
        {
            return score.toDoubleOrNull()
        }

        @JvmStatic
        fun parse_result(result: String) : Boolean?
        {
            if (result == "passed")
                return true
            if (result == "failed")
                return false
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
        return result_
    }

    fun failed(): Boolean
    {
        return !passed()
    }

    fun message(): String
    {
        return message_

    }
}