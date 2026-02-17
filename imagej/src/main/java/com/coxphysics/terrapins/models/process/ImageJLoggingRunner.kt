package com.coxphysics.terrapins.models.process

import com.coxphysics.terrapins.models.log.IJLog
import com.coxphysics.terrapins.models.log.Log
import com.coxphysics.terrapins.models.utils.StreamUtils
import ij.IJ
import javax.swing.JTextArea
import javax.swing.SwingWorker

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
        val process = builder.start()
        for (line in StreamUtils.get_lines(process.inputStream))
        {
            logger_.log(line)
        }
        return process.waitFor()
    }
}
class ImageJLoggingRunner private constructor() : Runner
{
    private val runner_ = LoggingRunner.from(IJLog.new())

    companion object
    {
        @JvmStatic
        fun new() : ImageJLoggingRunner
        {
            return ImageJLoggingRunner()
        }
    }

    override fun run(builder: ProcessBuilder): Int
    {
        return runner_.run(builder)
    }
}