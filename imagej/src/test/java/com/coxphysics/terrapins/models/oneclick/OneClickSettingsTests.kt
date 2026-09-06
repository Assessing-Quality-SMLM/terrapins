package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.view_models.oneclick.OneClickVM
import ij.ImagePlus
import ij.ImageStack
import ij.process.FloatProcessor
import org.junit.jupiter.api.Test
import java.awt.Color
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The gate in front of a run.
 *
 * A one-click run is minutes of work on real data, so the checks that decide whether it can start
 * are worth more than usual: getting them wrong means either refusing a valid dataset, or
 * starting a long run that was always going to fail. The view only renders what these say.
 */
class OneClickSettingsTests
{
    private fun stack(n_frames: Int): ImagePlus
    {
        val stack = ImageStack(16, 16)
        for (i in 0 until n_frames)
        {
            stack.addSlice("f$i", FloatProcessor(16, 16))
        }
        return ImagePlus("raw", stack)
    }

    private fun runnable_settings(): OneClickSettings
    {
        val settings = OneClickSettings.from(CoreSettings.default())
        settings.set_image(stack(200))
        settings.equipment().set_camera_pixel_size_nm(100.0)
        settings.equipment().set_instrument_psf_fwhm_nm(250.0)
        return settings
    }

    @Test
    fun a_complete_set_of_inputs_is_runnable()
    {
        val settings = runnable_settings()
        assertNull(settings.error_string())
        assertTrue(settings.is_runnable())
    }

    @Test
    fun without_an_image_there_is_nothing_to_run()
    {
        val settings = OneClickSettings.from(CoreSettings.default())
        settings.set_image(null)
        assertEquals("Select a raw image stack", settings.error_string())
    }

    @Test
    fun a_single_frame_is_refused_with_a_reason()
    {
        // Selecting one frame of an open stack rather than the stack is an easy mistake, and the
        // failure would otherwise appear much later as an empty HAWK stream.
        val settings = runnable_settings()
        settings.set_image(stack(1))
        assertNotNull(settings.error_string())
        assertTrue(settings.error_string()!!.contains("single frame"), settings.error_string()!!)
    }

    @Test
    fun the_gain_is_optional_and_absent_by_default()
    {
        val settings = runnable_settings()
        assertFalse(settings.has_photons_per_adu())
        assertTrue(settings.is_runnable(), "a missing gain must not block a run")
    }

    @Test
    fun a_zero_gain_means_not_supplied_rather_than_a_gain_of_zero()
    {
        // A numeric dialog field cannot be left empty, and no camera has a gain of zero.
        val settings = runnable_settings()
        settings.set_photons_per_adu(0.0)
        assertFalse(settings.has_photons_per_adu())
        settings.set_photons_per_adu(-1.0)
        assertFalse(settings.has_photons_per_adu())
        settings.set_photons_per_adu(2.5)
        assertTrue(settings.has_photons_per_adu())
        assertEquals(2.5, settings.photons_per_adu())
    }

    @Test
    fun the_working_directory_is_shared_with_the_rest_of_the_workflow()
    {
        // Not a separate setting: a one-click run writes where the advanced tab says.
        val core = CoreSettings.default()
        val settings = OneClickSettings.from(core)
        assertEquals(core.working_directory_path(), settings.working_directory())
    }

    @Test
    fun a_half_typed_number_is_rejected_without_changing_the_setting()
    {
        val settings = runnable_settings()
        val vm = OneClickVM.from(settings)
        val before = settings.equipment().camera_pixel_size_nm()

        assertEquals(Color.RED, vm.set_camera_pixel_size_nm("1e"))
        assertEquals(before, settings.equipment().camera_pixel_size_nm(),
            "an unparseable field must leave the previous value alone")

        assertEquals(Color.RED, vm.set_camera_pixel_size_nm("-5"))
        assertEquals(before, settings.equipment().camera_pixel_size_nm())

        vm.set_camera_pixel_size_nm("80")
        assertEquals(80.0, settings.equipment().camera_pixel_size_nm())
    }

    @Test
    fun an_empty_gain_field_is_accepted_but_a_bad_one_is_not()
    {
        val vm = OneClickVM.from(runnable_settings())
        // Blank is the normal way of saying "I do not know the gain".
        assertFalse(vm.set_photons_per_adu("") == Color.RED)
        assertFalse(vm.has_photons_per_adu())
        assertEquals(Color.RED, vm.set_photons_per_adu("abc"))
        vm.set_photons_per_adu("2.0")
        assertTrue(vm.has_photons_per_adu())
    }

    @Test
    fun the_precision_note_says_which_case_applies_before_the_run_starts()
    {
        val vm = OneClickVM.from(runnable_settings())
        assertTrue(vm.precision_note().contains("assumed"),
            "with no gain the user should know the limiting precision is assumed")
        vm.set_photons_per_adu("2.0")
        assertFalse(vm.precision_note().contains("assumed"))
    }

    @Test
    fun hawk_levels_must_be_a_positive_whole_number()
    {
        val settings = runnable_settings()
        val vm = OneClickVM.from(settings)
        assertEquals(OneClickSettings.DEFAULT_HAWK_LEVELS, settings.hawk_levels())
        assertEquals(Color.RED, vm.set_hawk_levels("0"))
        assertEquals(Color.RED, vm.set_hawk_levels("2.5"))
        assertEquals(OneClickSettings.DEFAULT_HAWK_LEVELS, settings.hawk_levels())
        vm.set_hawk_levels("4")
        assertEquals(4, settings.hawk_levels())
    }
}
