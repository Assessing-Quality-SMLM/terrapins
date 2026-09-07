package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.vendored.thunderstorm.detectors.NonMaxSuppressionDetector
import com.coxphysics.terrapins.vendored.thunderstorm.estimators.ui.IntSymmetricGaussianEstimatorUI
import com.coxphysics.terrapins.vendored.thunderstorm.filters.ui.CompoundWaveletFilterUI
import com.coxphysics.terrapins.vendored.thunderstorm.rendering.ui.EmptyRendererUI
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks the module names against the modules themselves.
 *
 * ThunderSTORM resolves each module by matching a string against what the module reports, and
 * `MacroParser.getModuleIndex` throws "Module not found" for anything else. That aborts the
 * analysis and leaves an empty results table, so the symptom arrives much later and looks
 * unrelated: the export writes nothing and the run reports "produced no localisations - check the
 * pixel size and PSF width", pointing at settings that were never the problem.
 *
 * Two of these were wrong when first written, guessed from ThunderSTORM's user interface rather
 * than its source. Now that it is vendored the names can be read from the classes, so this is
 * cheap to keep honest.
 */
class ThunderStormModuleNamesTests
{
    @Test
    fun the_filter_name_matches_the_filter()
    {
        assertEquals(CompoundWaveletFilterUI().name, ThunderStormFitter.FILTER)
    }

    @Test
    fun the_detector_name_matches_the_detector()
    {
        // Called "Maximum filter", not "Local maximum" as its UI label might suggest.
        assertEquals(NonMaxSuppressionDetector().name, ThunderStormFitter.DETECTOR)
    }

    @Test
    fun the_estimator_name_matches_the_estimator()
    {
        assertEquals(IntSymmetricGaussianEstimatorUI().name, ThunderStormFitter.ESTIMATOR)
    }

    @Test
    fun the_renderer_name_matches_the_renderer()
    {
        // Rendering is switched off: the assessment renders from the table instead.
        assertEquals(EmptyRendererUI().name, ThunderStormFitter.RENDERER)
    }

    @Test
    fun every_named_module_appears_in_the_analysis_options()
    {
        val options = ThunderStormFitter
            .with_runner(com.coxphysics.terrapins.models.equipment.EquipmentSettings.default(),
                ThunderStormSettings.default(), 2.0, false,
                com.coxphysics.terrapins.models.process.RecordingMacroRunner(),
                com.coxphysics.terrapins.models.log.ListLog())
            .analysis_options()
        for (name in listOf(ThunderStormFitter.FILTER, ThunderStormFitter.DETECTOR,
                            ThunderStormFitter.ESTIMATOR, ThunderStormFitter.RENDERER,
                            ThunderStormFitter.FIT_METHOD))
        {
            assertTrue(options.contains("[$name]"), "$name missing from $options")
        }
    }

    @Test
    fun the_options_name_only_parameters_the_chosen_modules_have()
    {
        val options = ThunderStormFitter
            .with_runner(com.coxphysics.terrapins.models.equipment.EquipmentSettings.default(),
                ThunderStormSettings.default(), 2.0, false,
                com.coxphysics.terrapins.models.process.RecordingMacroRunner(),
                com.coxphysics.terrapins.models.log.ListLog())
            .analysis_options()
        // The max-filter detector takes threshold and radius. "connectivity" belongs to a
        // different detector entirely and was silently ignored when sent here.
        assertTrue(options.contains("radius="), options)
        assertTrue(!options.contains("connectivity="),
            "connectivity is not a parameter of $DETECTOR_NOTE: $options")
    }

    private companion object
    {
        const val DETECTOR_NOTE = "the maximum filter detector"
    }
}
