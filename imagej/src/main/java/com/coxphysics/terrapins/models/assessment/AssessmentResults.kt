package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.frc.FRCResult
import com.coxphysics.terrapins.models.utils.FsUtils
import com.coxphysics.terrapins.models.utils.StringUtils
import java.io.File
import java.nio.file.Path
import com.coxphysics.terrapins.models.hawkman.external.Results as HawkmanResults
import com.coxphysics.terrapins.models.squirrel.external.Results as SquirrelResults

private const val HALF_SPLIT_DATA = "frc_half_split"
private const val ZIP_SPLIT_DATA = "frc_zip_split"
private const val DRIFT_SPLIT_DATA = "frc_drift_split"
private const val HAWKMAN_DATA = "hawkman"
private const val SQUIRREL_DATA = "squirrel"
private const val REPORT = "report"

class AssessmentResults private constructor(private val data_path: Path)
{
    companion object
    {
        @JvmStatic
        fun from(data_path: Path): AssessmentResults
        {
            return AssessmentResults(data_path)
        }

        @JvmStatic
        fun empty(): AssessmentResults
        {
            return AssessmentResults(File(StringUtils.EMPTY_STRING).toPath())
        }
    }

    fun data_path(): Path
    {
        return data_path
    }

    private fun half_split_results_path() : Path
    {
        return data_path.resolve(HALF_SPLIT_DATA)
    }

    fun half_split_results() : FRCResult?
    {
        return FRCResult.from(half_split_results_path())
    }

    private fun zip_split_results_path() : Path
    {
        return data_path.resolve(ZIP_SPLIT_DATA)
    }

    fun zip_split_results(): FRCResult?
    {
        return FRCResult.from(zip_split_results_path())
    }

    private fun drift_split_results_path() : Path
    {
        return data_path.resolve(DRIFT_SPLIT_DATA)
    }

    fun drift_split_results(): FRCResult?
    {
        return FRCResult.from(drift_split_results_path())
    }

    private fun hawkman_results_path(): Path
    {
        return data_path.resolve(HAWKMAN_DATA)
    }

    fun hawkman_results(): HawkmanResults?
    {
        return HawkmanResults.from(hawkman_results_path())
    }

    private fun squirrel_results_path(): Path
    {
        return data_path.resolve(SQUIRREL_DATA)
    }

    fun squirrel_results(): SquirrelResults?
    {
        return SquirrelResults.from(squirrel_results_path())
    }

    fun report(): String?
    {
        val path = report_path()
        if (!FsUtils.exists(path))
            return null
        return path.toFile().readText()
    }

    fun report_path(): Path
    {
        return data_path.resolve(REPORT)
    }

}