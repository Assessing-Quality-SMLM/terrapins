package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.log.ListLog
import com.coxphysics.terrapins.models.process.RecordingMacroRunner
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The command names and option strings are the entire contract with ThunderSTORM.
 *
 * Nothing here needs ThunderSTORM installed, which is the point: the contract is a set of strings,
 * and a typo in one of them fails at run time inside another application, as a dialog that opens
 * on a default value or a command that quietly does nothing. Asserting on the strings is the only
 * cheap way to notice.
 */
class ThunderStormFitterTests
{
    private fun equipment(pixel_size_nm: Double = 100.0, psf_fwhm_nm: Double = 250.0)
            : EquipmentSettings
    {
        val e = EquipmentSettings.default()
        e.set_camera_pixel_size_nm(pixel_size_nm)
        e.set_instrument_psf_fwhm_nm(psf_fwhm_nm)
        return e
    }

    private fun fitter(settings: ThunderStormSettings = ThunderStormSettings.default(),
                       gain: Double = 2.0,
                       emccd: Boolean = false,
                       equipment: EquipmentSettings = equipment(),
                       runner: RecordingMacroRunner = RecordingMacroRunner())
            : Pair<ThunderStormFitter, RecordingMacroRunner>
    {
        val f = ThunderStormFitter.with_runner(
            equipment, settings, gain, emccd, runner, ListLog())
        return Pair(f, runner)
    }

    @Test
    fun it_presents_the_same_seam_as_the_other_fitter()
    {
        val (f, _) = fitter()
        // The assessment is told how to read the table, and reads both fitters' output the same
        // way - which is what lets the workflow not care which one ran.
        assertEquals(ParseMethodName.THUNDERSTORM, f.parse_method_name())
        assertEquals(FastFitterAdapter.from(equipment(), 2.0, false, ListLog()).parse_method_name(),
            f.parse_method_name())
    }

    @Test
    fun it_reports_a_real_uncertainty_where_the_moment_fitter_predicts_one()
    {
        // ThunderSTORM fits a PSF model, so it has a residual and a genuine per-fit uncertainty.
        assertTrue(fitter().first.produces_uncertainty())
        // The moment fitter only claims one when a gain lets it predict from the photon budget.
        assertFalse(FastFitterAdapter.from(equipment(), Double.NaN, false, ListLog())
            .produces_uncertainty())
    }

    @Test
    fun the_psf_width_is_converted_to_pixels_for_the_estimator()
    {
        // ThunderSTORM's estimator wants sigma in pixels; the workflow holds FWHM in nanometres.
        // 250 nm FWHM is 106.2 nm sigma, which at 100 nm pixels is 1.062 px.
        assertEquals(1.062, fitter(equipment = equipment(100.0, 250.0)).first.sigma_px(), 0.001)
        // Same PSF, smaller pixels, so more pixels across it.
        assertEquals(1.327, fitter(equipment = equipment(80.0, 250.0)).first.sigma_px(), 0.001)
    }

    @Test
    fun the_camera_setup_carries_the_pixel_size_and_the_gain()
    {
        val (f, _) = fitter(equipment = equipment(80.0), gain = 3.5)
        val options = f.camera_options()
        assertTrue(options.contains("pixelsize=80.0"), options)
        assertTrue(options.contains("photons2adu=3.5"), options)
        assertFalse(options.contains("isemgain"), "not an EM camera unless said so: $options")
    }

    @Test
    fun an_em_camera_is_declared_so_the_uncertainty_accounts_for_it()
    {
        val (f, _) = fitter(emccd = true)
        assertTrue(f.camera_options().contains("isemgain=true"), f.camera_options())
    }

    @Test
    fun without_a_gain_no_gain_is_sent_rather_than_a_wrong_one()
    {
        // A wrong photons2adu would silently scale the reported uncertainty, which is worse than
        // letting ThunderSTORM use its own default and saying so in the log.
        val (f, _) = fitter(gain = Double.NaN)
        assertFalse(f.camera_options().contains("photons2adu"), f.camera_options())
    }

    @Test
    fun rendering_is_switched_off()
    {
        // The assessment renders from the table, so a reconstruction here would be discarded -
        // and it is ThunderSTORM's asynchronous rendering that a macro can otherwise outrun.
        assertTrue(fitter().first.analysis_options().contains("renderer=[No Renderer]"))
    }

    @Test
    fun the_export_asks_for_the_columns_the_assessment_reads()
    {
        val (f, _) = fitter()
        val options = f.export_options(java.nio.file.Paths.get("/tmp/some dir/locs.csv"))
        // The assessment's ThunderSTORM reader requires all of these by name.
        for (column in listOf("frame=true", "x=true", "y=true", "sigma=true", "intensity=true",
                              "uncertainty=true"))
        {
            assertTrue(options.contains(column), "$column missing from $options")
        }
        assertTrue(options.contains("fileformat=[CSV (comma separated)]"), options)
        // A path with a space in it has to survive as one macro value.
        assertTrue(options.contains("filepath=[/tmp/some dir/locs.csv]"), options)
    }

    @Test
    fun neither_post_processing_step_runs_unless_asked()
    {
        // Both change what the assessment is an assessment of, so both are opt-in.
        val settings = ThunderStormSettings.default()
        assertFalse(settings.correct_drift())
        assertFalse(settings.merge())
        assertEquals(null, settings.post_processing_note(),
            "with neither on there is nothing to warn about")
    }

    @Test
    fun drift_correction_is_requested_only_when_switched_on()
    {
        val on = ThunderStormSettings.default()
        on.set_correct_drift(true)
        val options = fitter(settings = on).first.drift_options()
        assertTrue(options.contains("action=drift"), options)
        assertTrue(options.contains("method=[Cross correlation]"), options)
        assertTrue(options.contains("steps=${ThunderStormSettings.DEFAULT_DRIFT_BINS}"), options)
        // Nothing should be written anywhere the user did not ask for.
        assertTrue(options.contains("save=false"), options)
    }

    @Test
    fun merging_carries_the_distance_and_the_gap_it_tolerates()
    {
        val on = ThunderStormSettings.default()
        on.set_merge(true)
        on.set_merge_distance_nm(35.0)
        on.set_merge_off_frames(2)
        val options = fitter(settings = on).first.merge_options()
        assertTrue(options.contains("action=merge"), options)
        assertTrue(options.contains("dist=35.0"), options)
        assertTrue(options.contains("offframes=2"), options)
    }

    @Test
    fun the_note_says_which_report_each_option_affects()
    {
        // A user turning these on should know what it costs before the run, not afterwards.
        val drift = ThunderStormSettings.default()
        drift.set_correct_drift(true)
        assertTrue(drift.post_processing_note()!!.contains("drift report"),
            drift.post_processing_note()!!)

        val merge = ThunderStormSettings.default()
        merge.set_merge(true)
        val note = merge.post_processing_note()!!
        assertTrue(note.contains("blinking"), note)
    }

    @Test
    fun a_missing_thunderstorm_is_reported_rather_than_attempted()
    {
        // TERRAPINS builds and runs without ThunderSTORM on the classpath, so this is the normal
        // state here, not an error condition.
        assertFalse(ThunderStormFitter.is_available(),
            "ThunderSTORM is not a dependency of this module")

        val log = ListLog<String>()
        val runner = RecordingMacroRunner()
        val f = ThunderStormFitter.with_runner(
            equipment(), ThunderStormSettings.default(), 2.0, false, runner, log)

        val result = f.localise(ij.ImagePlus("x", ij.process.FloatProcessor(4, 4)),
            java.nio.file.Paths.get("/tmp/never-written.csv"))

        assertEquals(null, result)
        assertTrue(runner.calls().isEmpty(), "nothing should be run when it is not installed")
        assertTrue(log.log().any { it.contains("not installed") }, log.log().toString())
    }

    @Test
    fun the_settings_refuse_a_run_that_would_fail_partway_through()
    {
        // Discovered before HAWK has run and the raw stack has been localised, rather than after.
        val settings = OneClickSettings.default()
        settings.set_image(ij.ImagePlus("s", ij.ImageStack(8, 8).also {
            it.addSlice(ij.process.FloatProcessor(8, 8))
            it.addSlice(ij.process.FloatProcessor(8, 8))
        }))
        settings.equipment().set_camera_pixel_size_nm(100.0)
        settings.equipment().set_instrument_psf_fwhm_nm(250.0)
        assertTrue(settings.is_runnable(), "the moment fitter needs nothing installed")

        settings.set_fitter(FitterChoice.THUNDERSTORM)
        assertFalse(settings.is_runnable())
        assertTrue(settings.error_string()!!.contains("not installed"), settings.error_string()!!)
    }
}
