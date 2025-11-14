package com.coxphysics.terrapins.models.assessment

import com.coxphysics.terrapins.models.assessment.reports.Assessment
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

const val SR_0_005_LABEL = "0.005x"
const val SR_0_05_LABEL = "0.05x"
const val SR_0_5_LABEL = "0.5x"
const val SR_5_LABEL = "5x"
const val SR_25_LABEL = "25x"
const val SR_SAMPLE_LABEL = "Sample"

class AssessmentResults private constructor(private var data_path: Path)
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

    fun set_data_path(value: Path)
    {
        data_path = value
    }

    private fun reports_path(): Path
    {
        return data_path().resolve("report")
    }

    fun drift_assessment_path(): Path
    {
        return reports_path().resolve("drift")
    }

    fun drift_assessment(): Assessment?
    {
        return Assessment.from_disk(drift_assessment_path())
    }

    fun magnification_assessment_path(): Path
    {
        return reports_path().resolve("magnification")
    }

    fun magnification_assessment(): Assessment?
    {
        return Assessment.from_disk(magnification_assessment_path())
    }

    private fun blinking_assessment_path(): Path
    {
        return reports_path().resolve("blinking")
    }

    fun blinking_assessment(): Assessment?
    {
        return Assessment.from_disk(blinking_assessment_path())
    }

    private fun sampling_assessment_path(): Path
    {
        return reports_path().resolve("sampling")
    }

    fun sampling_assessment(): Assessment?
    {
        return Assessment.from_disk(sampling_assessment_path())
    }

    fun localisation_precision_assessment_path(): Path
    {
        return reports_path().resolve("localisation_precision")
    }

    fun localisation_precision_assessment(): Assessment?
    {
        return Assessment.from_disk(localisation_precision_assessment_path())
    }

    private fun frc_resolution_assessment_path() : Path
    {
        return reports_path().resolve("frc_resolution")
    }

    fun frc_resolution_assessment(): Assessment?
    {
        return Assessment.from_disk(frc_resolution_assessment_path())
    }

    private fun bias_assessment_path() : Path
    {
        return reports_path().resolve("bias")
    }

    fun bias_assessment(): Assessment?
    {
        return Assessment.from_disk(bias_assessment_path())
    }

    private fun squirrel_assessment_path() : Path
    {
        return reports_path().resolve("squirrel")
    }

    fun squirrel_assessment(): Assessment?
    {
        return Assessment.from_disk(squirrel_assessment_path())
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

    private fun sr_0_005_path(): Path
    {
        return reports_path().resolve("sr_0_005")
    }

    fun calibration_data() : List<Pair<String, FRCResult?>>
    {
        return listOf(
            Pair(SR_0_005_LABEL, sr_0_005()),
            Pair(SR_0_05_LABEL, sr_0_05()),
            Pair(SR_0_5_LABEL, sr_0_5()),
            Pair(SR_5_LABEL, sr_5()),
            Pair(SR_25_LABEL, sr_25()),
            Pair(SR_SAMPLE_LABEL, aligned_frc())
        )
    }

    fun sr_0_005(): FRCResult?
    {
        return FRCResult.from_filename(sr_0_005_path())
    }

    private fun sr_0_05_path(): Path
    {
        return reports_path().resolve("sr_0_05")
    }

    fun sr_0_05(): FRCResult?
    {
        return FRCResult.from_filename(sr_0_05_path())
    }

    private fun sr_0_5_path(): Path
    {
        return reports_path().resolve("sr_0_5")
    }

    fun sr_0_5(): FRCResult?
    {
        return FRCResult.from_filename(sr_0_5_path())
    }

    private fun sr_5_path(): Path
    {
        return reports_path().resolve("sr_5")
    }

    fun sr_5(): FRCResult?
    {
        return FRCResult.from_filename(sr_5_path())
    }

    private fun sr_25_path(): Path
    {
        return reports_path().resolve("sr_25")
    }

    fun sr_25(): FRCResult?
    {
        return FRCResult.from_filename(sr_25_path())
    }

    private fun aligned_frc_path(): Path
    {
        return reports_path().resolve("frc_calibration_space")
    }

    private fun aligned_frc(): FRCResult?
    {
        return FRCResult.from_filename(aligned_frc_path())
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