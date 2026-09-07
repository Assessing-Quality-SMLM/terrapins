package com.coxphysics.terrapins.models.oneclick

import ij.ImagePlus
import ij.ImageStack
import ij.process.FloatProcessor
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Calibrated against a real failure.
 *
 * A ThunderSTORM one-click run on a 500 frame 128x128 acquisition at magnification 10 filled the
 * disk and the assessment died with "No space left on device" after twenty minutes of work. The
 * completed runs it left behind measured 1190 MB as an archive plus 1191 MB extracted - about
 * 2.4 GB each, kept forever.
 */
class DiskSpaceTests
{
    private fun image(width: Int, height: Int): ImagePlus
    {
        val stack = ImageStack(width, height)
        stack.addSlice(FloatProcessor(width, height))
        return ImagePlus("x", stack)
    }

    private fun gb(bytes: Long): Double = bytes / (1024.0 * 1024.0 * 1024.0)

    @Test
    fun the_estimate_matches_the_run_that_ran_out_of_space()
    {
        // 128x128 at magnification 10, twenty HAWKMAN levels: 2.4 GB was observed.
        val estimated = gb(DiskSpace.estimate_bytes(image(128, 128), 10.0, 20))
        assertTrue(estimated > 2.0 && estimated < 3.5,
            "estimated ${"%.2f".format(estimated)} GB against 2.4 GB measured")
    }

    @Test
    fun it_errs_high_rather_than_low()
    {
        // Warning about a run that would have fitted is a nuisance; failing after twenty minutes
        // because the estimate was optimistic is much worse.
        assertTrue(gb(DiskSpace.estimate_bytes(image(128, 128), 10.0, 20)) > 2.4)
    }

    @Test
    fun cost_is_driven_by_rendered_size_and_levels_not_by_frames()
    {
        // The maps are the cost. A longer acquisition renders into the same image.
        val short_stack = image(128, 128)
        val long_stack = ImagePlus("y", ImageStack(128, 128).also {
            repeat(500) { _ -> it.addSlice(FloatProcessor(128, 128)) }
        })
        assertTrue(DiskSpace.estimate_bytes(short_stack, 10.0, 20)
                   == DiskSpace.estimate_bytes(long_stack, 10.0, 20))

        // Magnification is quadratic, which is the setting most likely to surprise someone.
        val at_10 = DiskSpace.estimate_bytes(image(128, 128), 10.0, 20)
        val at_20 = DiskSpace.estimate_bytes(image(128, 128), 20.0, 20)
        assertTrue(at_20 >= 4 * at_10, "doubling magnification should quadruple the maps")

        // Levels are linear.
        assertTrue(DiskSpace.estimate_bytes(image(128, 128), 10.0, 10) * 2
                   == DiskSpace.estimate_bytes(image(128, 128), 10.0, 20))
    }

    @Test
    fun a_roomy_disk_produces_no_warning()
    {
        // A tiny render on a normal filesystem: nothing to say.
        assertNull(DiskSpace.warning(image(16, 16), 1.0, 1, Paths.get(System.getProperty("user.dir"))))
    }

    @Test
    fun an_implausible_run_is_warned_about_with_both_numbers()
    {
        // No filesystem holds this, so the warning must fire wherever the test runs.
        val warning = DiskSpace.warning(image(4096, 4096), 20.0, 20,
            Paths.get(System.getProperty("user.dir")))
        assertNotNull(warning)
        assertTrue(warning.contains("free"), warning)
        // It should say why the cost is what it is, since "twice" is the surprising part.
        assertTrue(warning.contains("twice"), warning)
    }

    @Test
    fun a_working_directory_that_does_not_exist_yet_is_still_checked()
    {
        // The one-click default is created on first use, so the check has to look at the nearest
        // existing ancestor rather than give up.
        val missing = Paths.get(System.getProperty("user.dir"), "no", "such", "place")
        assertNotNull(DiskSpace.usable_bytes(missing))
    }
}
