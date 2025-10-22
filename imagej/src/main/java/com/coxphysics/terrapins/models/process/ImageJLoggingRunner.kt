package com.coxphysics.terrapins.models.process

import com.coxphysics.terrapins.models.utils.StreamUtils
import ij.IJ

class ImageJLoggingRunner: Runner
{
    override fun run(builder: ProcessBuilder): Int
    {
        val process = builder.start()
        for (line in StreamUtils.get_lines(process.inputStream))
        {
            IJ.log(line)
        }
        return process.waitFor()
    }
}