package com.coxphysics.terrapins.models.assessment.reports

import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists

enum class Outcome
{
    PASS, FAIL, INDETERMINATE
}

const val PASSED = "passed"
const val INDERTERMINATE = "indeterminate"
const val FAILED = "failed"

class Assessment private constructor(
    private val name_: String,
    private val score_: Double?,
    private val result_: Pair<Outcome, String>,
    private val colour_: Outcome?,
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
            var outcome = result
            var label = result.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val values = decompose(result, ',')
            if (values != null)
            {
                outcome = values.first
                label = values.second
            }
            if (outcome == PASSED)
                return Pair(Outcome.PASS, label)
            if (outcome == FAILED)
                return Pair(Outcome.FAIL, label)
            if (outcome == INDERTERMINATE)
                return Pair(Outcome.INDETERMINATE, label)
            return null
        }

        fun get_message(lines: List<String>) : String?
        {
            val first = decompose(lines[0], ',')
            if (first == null)
                return null
            var sb = StringBuilder()
            sb.append(first.second)
            for (line in lines.drop(1))
            {
                sb.append(line)
            }
            return sb.toString()
        }
        @JvmStatic
        fun from_lines(lines: List<String>): Assessment?
        {
            if (lines.size < 4)
                return null
            val name = decompose(lines[0], ',')?.second
            val score = decompose(lines[1], ',')?.second?.let { parse_score(it) }
            val result = decompose(lines[2], ',')?.second?.let { parse_result(it) }
            val line_3 = decompose(lines[3], ',')
            if (line_3 == null)
                return null
            var colour : Outcome? = null
            var message : String? = null
            if (line_3.first == "colour")
            {
                colour = parse_result(line_3.second)?.first
                message = get_message(lines.subList(4, lines.size))
            }
            else
            {
                message = get_message(lines.subList(3, lines.size))
            }

            if (name == null || result == null || message == null)
                return null
            return Assessment(name, score, result, colour, message)

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

    fun colour(): Outcome
    {
        return colour_ ?: outcome()
    }
}