package com.coxphysics.terrapins.vendored.thunderstorm

import ij.IJ
import java.io.IOException
import java.util.*

object ThunderSTORM {
    const val FILE_NAME = "Thunder_STORM.jar"

    val VERSION: String
    init {
        try {
            val p = Properties()
            p.load(IJ.getClassLoader().getResourceAsStream("terrapins-thunderstorm.properties"))
            VERSION = p.getProperty("version")
        } catch (e: IOException) {
            throw RuntimeException("Can't read build properties! The package is probably corrupted.")
        }
    }
}
