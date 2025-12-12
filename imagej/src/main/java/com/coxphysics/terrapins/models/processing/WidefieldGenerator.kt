package com.coxphysics.terrapins.models.processing

import ij.ImageStack
import ij.process.FloatProcessor


class WidefieldGenerator private constructor()
{
    companion object
    {
        @JvmStatic
        fun average_of_frames(stack: ImageStack): FloatProcessor
        {
            val total = FloatProcessor(stack.width, stack.height)
            val n = stack.size()
            for (frame in 0..<n)
            {
                val processor = stack.getProcessor(frame + 1)

                for (row in 0..<processor.height)
                {
                    for (col in 0..<processor.width)
                    {
                        val value = processor.getValue(col, row)
                        val current_value = total.getValue(col, row)
                        val new_value = current_value + value
                        total.setf(col, row, new_value.toFloat())
                    }
                }
            }

            for (row in 0..<total.height)
            {
                for (col in 0..<total.width)
                {
                    val value = total.getValue(col, row)
                    val new_value = value / n
                    total.setf(col, row, new_value.toFloat())
                }
            }
            return total
        }
    }
}