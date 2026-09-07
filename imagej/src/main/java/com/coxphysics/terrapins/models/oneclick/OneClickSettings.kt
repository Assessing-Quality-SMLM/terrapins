package com.coxphysics.terrapins.models.oneclick

import com.coxphysics.terrapins.models.Image
import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import ij.ImagePlus
import java.nio.file.Path

/** Which localiser the one-click path should use. */
enum class FitterChoice
{
    /** The built-in fast moment fitter. Runs in this process. */
    FAST,

    /** ThunderSTORM, driven through the commands it registers with ImageJ. */
    THUNDERSTORM,
}

/**
 * Everything the one-click path asks the user for.
 *
 * Deliberately short. The whole point is that a raw stack and the few numbers that describe the
 * microscope are enough, and every additional field is another reason to give up before running
 * anything. What is *not* here is as considered as what is: the FRC splits, the magnification of
 * intermediate renderings, the HAWKMAN and SQUIRREL parameters and the rendering settings are all
 * derived or defaulted, and remain reachable through the advanced workflows for anyone who needs
 * them.
 *
 * It shares its [CoreSettings] with the rest of the workflow, so the working directory set on the
 * advanced tab applies here too rather than being a second, separate setting.
 */
class OneClickSettings private constructor(private val core_settings_: CoreSettings)
{
    private var image_ = Image.default_from_manager()
    private var equipment_ = EquipmentSettings.default()
    private var fitter_ = FitterChoice.FAST
    private var hawk_levels_ = DEFAULT_HAWK_LEVELS
    private var photons_per_adu_ = Double.NaN
    private var emccd_ = false
    private val thunderstorm_ = ThunderStormSettings.default()

    companion object
    {
        /**
         * Matches the HAWK plugin's own default. Deeper decompositions detect bias at coarser
         * scales but cost proportionally more, and the stream is already several times the
         * length of the raw stack.
         */
        const val DEFAULT_HAWK_LEVELS = 3

        @JvmStatic
        fun from(core_settings: CoreSettings): OneClickSettings = OneClickSettings(core_settings)

        @JvmStatic
        fun default(): OneClickSettings = from(CoreSettings.default())
    }

    fun core_settings(): CoreSettings = core_settings_

    fun working_directory(): Path? = core_settings_.working_directory_path()

    fun inner_image(): Image = image_

    fun image(): ImagePlus? = image_.to_image_plus()

    fun set_image(value: ImagePlus?) = image_.set_inner(value)

    fun image_name(): String = image()?.title ?: ""

    fun equipment(): EquipmentSettings = equipment_

    fun set_equipment(value: EquipmentSettings)
    {
        equipment_ = value
    }

    fun thunderstorm(): ThunderStormSettings = thunderstorm_

    fun fitter(): FitterChoice = fitter_

    fun set_fitter(value: FitterChoice)
    {
        fitter_ = value
    }

    fun hawk_levels(): Int = hawk_levels_

    fun set_hawk_levels(value: Int)
    {
        hawk_levels_ = value
    }

    /**
     * Camera gain in photons per ADU, or NaN when the user has not supplied one.
     *
     * Only affects the predicted localisation precision - it cannot move a localisation - so it
     * is optional, and the report says the precision was assumed when it is missing rather than
     * refusing to run.
     */
    fun photons_per_adu(): Double = photons_per_adu_

    fun set_photons_per_adu(value: Double)
    {
        // A dialog cannot express an empty numeric field and no camera has a gain of zero, so
        // zero and negative are both read as "not supplied".
        photons_per_adu_ = if (value > 0.0) value else Double.NaN
    }

    fun has_photons_per_adu(): Boolean = photons_per_adu_ > 0.0 && !photons_per_adu_.isNaN()

    fun emccd(): Boolean = emccd_

    fun set_emccd(value: Boolean)
    {
        emccd_ = value
    }

    /** Why the settings are not runnable yet, or null when they are. */
    fun error_string(): String?
    {
        val image = image() ?: return "Select a raw image stack"
        if (image.stackSize < 2)
        {
            return "A single frame cannot be assessed - open the raw acquisition, not one frame"
        }
        if (equipment_.camera_pixel_size_nm() <= 0.0)
        {
            return "Camera pixel size must be greater than zero"
        }
        if (equipment_.instrument_psf_fwhm_nm() <= 0.0)
        {
            return "PSF FWHM must be greater than zero"
        }
        if (hawk_levels_ < 1)
        {
            return "HAWK needs at least one level"
        }
        if (fitter_ == FitterChoice.THUNDERSTORM && !ThunderStormFitter.is_available())
        {
            // Said now rather than after HAWK has run and the raw stack has been localised.
            return "ThunderSTORM is not installed - install it, or choose the fast moment fitter"
        }
        if (working_directory() == null)
        {
            return "Set a working directory on the Advanced tab"
        }
        return null
    }

    fun is_runnable(): Boolean = error_string() == null
}
