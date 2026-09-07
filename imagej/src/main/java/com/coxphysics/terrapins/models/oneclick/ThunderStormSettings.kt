package com.coxphysics.terrapins.models.oneclick

/**
 * The ThunderSTORM options the one-click path exposes.
 *
 * Deliberately few. ThunderSTORM has a large parameter surface and its defaults are well chosen;
 * re-exposing them here would rebuild its dialog inside ours and defeat the point. What is here
 * is the two post-processing steps that change what the assessment sees, plus the camera
 * calibration it cannot guess.
 *
 * **Both post-processing steps default to off**, and that is a deliberate choice rather than
 * caution about the implementations:
 *
 * - **Drift correction** removes a real property of the acquisition. TERRAPINS has a drift report
 *   whose whole purpose is to measure it, by comparing FRC curves from two different splits of
 *   the same data. Correcting drift before that measurement makes the report say the data was
 *   never drifting, which is true of the corrected table and false of the experiment. Turn it on
 *   to assess the reconstruction you intend to publish; leave it off to assess the microscope.
 * - **Merging** repeated localisations of one fluorophore changes the count and the spatial
 *   statistics, which the blinking, sampling and FRC reports all read. TERRAPINS' blinking report
 *   exists to tell you whether merging is warranted, so merging first answers the question it was
 *   about to ask.
 *
 * Neither is wrong to use. Both change what the assessment is an assessment *of*, which is why
 * they are opt-in and why the run log says when they were applied.
 */
class ThunderStormSettings private constructor()
{
    private var correct_drift_ = false
    private var drift_bins_ = DEFAULT_DRIFT_BINS
    private var merge_ = false
    private var merge_distance_nm_ = DEFAULT_MERGE_DISTANCE_NM
    private var merge_off_frames_ = DEFAULT_MERGE_OFF_FRAMES

    companion object
    {
        /**
         * Number of temporal bins for cross-correlation drift correction. ThunderSTORM's own
         * default; more bins track faster drift but each bin holds fewer localisations, so the
         * correlation gets noisier.
         */
        const val DEFAULT_DRIFT_BINS = 5

        /** Distance within which localisations in nearby frames are taken to be one molecule. */
        const val DEFAULT_MERGE_DISTANCE_NM = 20.0

        /** How many frames a molecule may be dark for and still be treated as the same one. */
        const val DEFAULT_MERGE_OFF_FRAMES = 1

        @JvmStatic
        fun default(): ThunderStormSettings = ThunderStormSettings()
    }

    fun correct_drift(): Boolean = correct_drift_

    fun set_correct_drift(value: Boolean)
    {
        correct_drift_ = value
    }

    fun drift_bins(): Int = drift_bins_

    fun set_drift_bins(value: Int)
    {
        drift_bins_ = value
    }

    fun merge(): Boolean = merge_

    fun set_merge(value: Boolean)
    {
        merge_ = value
    }

    fun merge_distance_nm(): Double = merge_distance_nm_

    fun set_merge_distance_nm(value: Double)
    {
        merge_distance_nm_ = value
    }

    fun merge_off_frames(): Int = merge_off_frames_

    fun set_merge_off_frames(value: Int)
    {
        merge_off_frames_ = value
    }

    /** What the run log should say about post-processing, or null when none was asked for. */
    fun post_processing_note(): String?
    {
        if (!correct_drift_ && !merge_)
        {
            return null
        }
        val applied = mutableListOf<String>()
        if (correct_drift_)
        {
            applied.add("drift correction ($drift_bins_ bins), so the drift report will describe "
                    + "the corrected table rather than the acquisition")
        }
        if (merge_)
        {
            applied.add("merging within $merge_distance_nm_ nm across $merge_off_frames_ off "
                    + "frame(s), so the blinking and sampling reports will describe the merged "
                    + "table")
        }
        return "ThunderSTORM post-processing: " + applied.joinToString("; ")
    }
}
