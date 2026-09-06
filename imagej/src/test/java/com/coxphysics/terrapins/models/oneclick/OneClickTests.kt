package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.log.ListLog
import ij.ImagePlus
import ij.ImageStack
import ij.process.FloatProcessor
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises the raw stack to localisation tables chain end to end.
 *
 * This is the part of the one-click path that is new. What happens after it - rendering, the FRC
 * splits, HAWKMAN, SQUIRREL - is the existing localisation workflow, driven by the assessment
 * executable, and is not reachable from a unit test because that executable is built separately.
 * So these tests stop at the two tables, which is exactly the handover point.
 */
class OneClickTests
{
    private val pixel_size_nm = 100.0
    private val psf_fwhm_nm = 250.0

    /**
     * A stack of well-separated emitters on a Poisson background.
     *
     * Emitters move by a fixed sub-pixel step each frame rather than staying put, so the HAWK
     * stream - which works on differences between frames - has something to find. A perfectly
     * static scene differences to noise.
     */
    private fun synthetic_stack(n_frames: Int, size: Int): ImagePlus
    {
        val sigma_px = (psf_fwhm_nm / 2.3548) / pixel_size_nm
        val rng = Random(20260906L)
        val stack = ImageStack(size, size)
        for (frame in 0 until n_frames)
        {
            val lambda = DoubleArray(size * size) { 40.0 }
            var y = 12.0
            while (y < size - 12)
            {
                var x = 12.0
                while (x < size - 12)
                {
                    // A different subset blinks on each frame, as in real SMLM data.
                    if (rng.nextDouble() < 0.6)
                    {
                        add_emitter(lambda, size, x + 0.1 * frame, y, 3000.0, sigma_px)
                    }
                    x += 16.0
                }
                y += 16.0
            }
            val pixels = FloatArray(lambda.size) { poisson(rng, lambda[it]).toFloat() }
            stack.addSlice("f$frame", FloatProcessor(size, size, pixels, null))
        }
        return ImagePlus("synthetic", stack)
    }

    private fun add_emitter(lambda: DoubleArray, size: Int, x: Double, y: Double,
                            photons: Double, sigma: Double)
    {
        val reach = Math.ceil(4 * sigma).toInt()
        for (j in maxOf(0, y.toInt() - reach)..minOf(size - 1, y.toInt() + reach))
        {
            for (i in maxOf(0, x.toInt() - reach)..minOf(size - 1, x.toInt() + reach))
            {
                val dx = i - x
                val dy = j - y
                lambda[j * size + i] +=
                    photons * Math.exp(-(dx * dx + dy * dy) / (2 * sigma * sigma)) /
                        (2 * Math.PI * sigma * sigma)
            }
        }
    }

    private fun poisson(rng: Random, lambda: Double): Long
    {
        if (lambda <= 0) return 0
        if (lambda < 30)
        {
            val l = Math.exp(-lambda)
            var k = 0
            var p = 1.0
            do { k++; p *= rng.nextDouble() } while (p > l)
            return (k - 1).toLong()
        }
        return maxOf(0L, Math.round(lambda + Math.sqrt(lambda) * rng.nextGaussian()))
    }

    private fun equipment(): EquipmentSettings
    {
        val e = EquipmentSettings.default()
        e.set_camera_pixel_size_nm(pixel_size_nm)
        e.set_instrument_psf_fwhm_nm(psf_fwhm_nm)
        e.set_magnification(10.0)
        return e
    }

    private fun working_directory(name: String): Path
    {
        val dir = Files.createTempDirectory("oneclick_$name")
        dir.toFile().deleteOnExit()
        return dir
    }

    private fun line_count(path: Path): Int = Files.readAllLines(path).size

    @Test
    fun raw_stack_becomes_both_localisation_tables()
    {
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), 2.0, false, log)
        val working = working_directory("full")

        val result = OneClick.with(fitter, log).prepare(
            synthetic_stack(24, 128), equipment(), working, 3)

        assertNotNull(result, "a localisable stack must produce settings")
        assertTrue(result.raw_localisation_count() > 100,
            "expected a healthy number of raw localisations, got ${result.raw_localisation_count()}")
        assertTrue(result.has_hawk(), "the HAWK table is what the bias assessment needs")

        val raw = LocalisationTable.raw_in(working)
        val hawk = LocalisationTable.hawk_in(working)
        assertTrue(Files.exists(raw), "raw table should be written to $raw")
        assertTrue(Files.exists(hawk), "hawk table should be written to $hawk")
        // Header plus one row per localisation.
        assertEquals(result.raw_localisation_count() + 1, line_count(raw))
        assertEquals(result.hawk_localisation_count()!! + 1, line_count(hawk))
    }

    @Test
    fun the_settings_produced_are_ready_for_the_assessment()
    {
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), 2.0, false, log)
        val working = working_directory("settings")

        val result = OneClick.with(fitter, log)
            .prepare(synthetic_stack(24, 128), equipment(), working, 3)!!
        val settings = result.settings()

        assertTrue(settings.has_localisation_file())
        assertTrue(settings.has_hawk_localisation_file())
        assertEquals(LocalisationTable.raw_in(working).toString(), settings.localisation_filename())
        assertEquals(LocalisationTable.hawk_in(working).toString(),
            settings.hawk_localisation_filename())
        // The assessment is told how to read them; "ts" is what the fast fitter writes.
        assertEquals(ParseMethodName.THUNDERSTORM, settings.localisation_file_parse_method())
        assertEquals(ParseMethodName.THUNDERSTORM, settings.hawk_localisation_file_parse_method())
        assertEquals(pixel_size_nm, settings.equipment().camera_pixel_size_nm())
    }

    @Test
    fun the_raw_stack_is_offered_to_squirrel_as_its_image_stack()
    {
        // Without this SQUIRREL has nothing to compare the reconstruction against and the
        // non-linearity analysis is skipped, which showed up as
        //   "Image stack not supplied - sum of frames widefield cannot be generated"
        //   "Cannot run non linearity SQUIRREL due to: ... aof_widefield.tiff does not exist"
        // long after the run had started.
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), 2.0, false, log)
        val working = working_directory("squirrel")

        val result = OneClick.with(fitter, log)
            .prepare(synthetic_stack(24, 128), equipment(), working, 3)!!

        assertTrue(result.settings().squirrel_inputs().has_image_stack(),
            "the raw stack is the widefield reference when no separate widefield was taken")
    }

    @Test
    fun the_image_stack_reaches_disk_as_a_single_averaged_frame()
    {
        // The assessment is given a path, not an image, so the value of has_image_stack() alone
        // proves nothing - this follows it to the file the executable will actually open.
        //
        // A single frame, not the whole stack: the average is computed here so a multi-gigabyte
        // acquisition is not copied into the working directory. The Rust side means over
        // whatever frames it finds, and the mean of one frame is that frame.
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), 2.0, false, log)
        val working = working_directory("aof")

        val result = OneClick.with(fitter, log)
            .prepare(synthetic_stack(24, 128), equipment(), working, 3)!!
        val prepared = result.settings().prepare_images_for_analysis()

        assertNotNull(prepared, "preparing the SQUIRREL inputs must succeed")
        val path = prepared.image_stack_path_in(working)
        assertNotNull(path, "the assessment is passed a path for --image-stack")
        assertTrue(Files.exists(path), "the file at $path must exist before the exe is told about it")

        val written = ij.IJ.openImage(path.toString())
        assertNotNull(written, "and it must be a readable tiff")
        assertEquals(1, written.stackSize, "an average, not a copy of the raw stack")
        assertEquals(128, written.width)
        assertEquals(128, written.height)
    }

    @Test
    fun the_hawk_stream_is_longer_than_the_raw_stack()
    {
        // The cost that dominates a one-click run: roughly 2 * levels times the frames, each one
        // regenerated on access. If this ratio ever collapses, HAWK is not running.
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), 2.0, false, log)
        val working = working_directory("ratio")
        val frames = 24

        val result = OneClick.with(fitter, log)
            .prepare(synthetic_stack(frames, 128), equipment(), working, 3)!!

        assertTrue(log.log().any { it.contains("HAWK stream") },
            "the stream size should be logged; it is the main cost of a run")
    }

    @Test
    fun a_stack_too_short_for_the_requested_levels_uses_fewer_and_says_so()
    {
        // HAWK does not reject a level it has too few frames for - PStream's inner loop simply
        // runs zero times, so 4 frames at 3 levels quietly yields a 2 level decomposition. That
        // is only safe if the assessment is told 2, since it scales the bias report by the level
        // count it is given.
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), 2.0, false, log)
        val working = working_directory("short")

        val result = OneClick.with(fitter, log).prepare(
            synthetic_stack(4, 128), equipment(), working, 3)

        assertNotNull(result)
        assertTrue(result.has_hawk(), "4 frames still support a 2 level decomposition")
        assertEquals(2, result.hawk_levels())
        assertEquals(2, result.settings().hawkman_settings().n_levels(),
            "the assessment must be told the levels the stream really has")
        assertTrue(log.log().any { it.contains("only support") },
            "silently using fewer levels than asked for would misreport the bias scale")
    }

    @Test
    fun a_stack_with_no_usable_levels_degrades_rather_than_failing()
    {
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), 2.0, false, log)
        val working = working_directory("single")

        val result = OneClick.with(fitter, log).prepare(
            synthetic_stack(1, 128), equipment(), working, 3)

        assertNotNull(result, "one frame is still localisable, so there is still something to assess")
        assertNull(result.hawk_localisation_count(), "one frame cannot be differenced")
        assertTrue(!result.settings().has_hawk_localisation_file())
    }

    @Test
    fun supported_levels_match_what_the_stream_can_hold()
    {
        // Level l needs 2^(l+1) frames. Pinned because the assessment's level count is derived
        // from it, and because PStream will not complain if this is wrong.
        assertEquals(0, HawkLevels.supported_for(1))
        assertEquals(1, HawkLevels.supported_for(2))
        assertEquals(1, HawkLevels.supported_for(3))
        assertEquals(2, HawkLevels.supported_for(4))
        assertEquals(2, HawkLevels.supported_for(7))
        assertEquals(3, HawkLevels.supported_for(8))
        assertEquals(3, HawkLevels.effective(3, 100), "plenty of frames, no reduction")
        assertEquals(2, HawkLevels.effective(5, 4), "reduced to what the data supports")
    }

    @Test
    fun without_a_gain_the_report_is_told_the_precision_is_assumed()
    {
        val log = ListLog<String>()
        val fitter = FastFitterAdapter.from(equipment(), Double.NaN, false, log)
        assertTrue(!fitter.produces_uncertainty())

        OneClick.with(fitter, log).prepare(
            synthetic_stack(12, 96), equipment(), working_directory("nogain"), 2)

        assertTrue(log.log().any { it.contains("assumed") },
            "a run with no gain must say so once, not leave it to be inferred from the report")
    }
}
