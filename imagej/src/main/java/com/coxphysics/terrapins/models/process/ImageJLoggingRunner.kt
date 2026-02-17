package com.coxphysics.terrapins.models.process

import com.coxphysics.terrapins.models.log.IJLog

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