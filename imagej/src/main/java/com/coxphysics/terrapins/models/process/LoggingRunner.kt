package com.coxphysics.terrapins.models.process

import com.coxphysics.terrapins.models.log.Log
import com.coxphysics.terrapins.models.utils.StreamUtils

class LoggingRunner private constructor(
    private val logger_: Log<String>
): Runner
{
    companion object
    {
        fun from(logger: Log<String>) : LoggingRunner
        {
            return LoggingRunner(logger)
        }
    }

    override fun run(builder: ProcessBuilder): Int
    {
        System.err.println("[TERRAPINS] running: " + builder.command().joinToString(" "))
        builder.redirectErrorStream(true)   // fold the exe's stderr into what we read/log
        val process = builder.start()
        for (line in StreamUtils.get_lines(process.inputStream))
        {
            logger_.log(line)
            System.err.println("[TERRAPINS] $line")
        }
        val exit = process.waitFor()
        System.err.println("[TERRAPINS] exit code: $exit")
        return exit
    }
}