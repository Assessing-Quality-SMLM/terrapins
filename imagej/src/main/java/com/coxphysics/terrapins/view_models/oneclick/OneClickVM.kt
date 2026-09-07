package com.coxphysics.terrapins.view_models.oneclick

import com.coxphysics.terrapins.models.log.IJLog
import com.coxphysics.terrapins.models.oneclick.FitterChoice
import com.coxphysics.terrapins.models.oneclick.OneClickSettings
import com.coxphysics.terrapins.models.oneclick.ThunderStormFitter
import com.coxphysics.terrapins.view_models.TERRAPINS.ImageSelectorVM
import com.coxphysics.terrapins.views.OneClickWorker
import java.awt.Color
import javax.swing.JTextField

/**
 * View model for the one-click tab.
 *
 * Numeric setters take the raw text and return a colour rather than throwing, matching the
 * pattern the existing settings views use: an unparseable field is shown as an error and the
 * previous value is kept, so a half-typed number never becomes a run parameter.
 */
class OneClickVM private constructor(private val settings_: OneClickSettings)
{
    private val image_selector_vm_ = ImageSelectorVM.with_image(settings_.inner_image())

    companion object
    {
        @JvmStatic
        fun from(settings: OneClickSettings): OneClickVM = OneClickVM(settings)

        @JvmStatic
        fun default(): OneClickVM = from(OneClickSettings.default())
    }

    fun image_selector_vm(): ImageSelectorVM = image_selector_vm_

    fun set_image_index(index: Int)
    {
        image_selector_vm_.set_image(index)
    }

    fun image_titles(): Array<String>? = image_selector_vm_.image_titles()

    fun camera_pixel_size_nm(): Double = settings_.equipment().camera_pixel_size_nm()

    fun set_camera_pixel_size_nm(value: String): Color =
        set_positive(value) { settings_.equipment().set_camera_pixel_size_nm(it) }

    fun psf_fwhm_nm(): Double = settings_.equipment().instrument_psf_fwhm_nm()

    fun set_psf_fwhm_nm(value: String): Color =
        set_positive(value) { settings_.equipment().set_instrument_psf_fwhm_nm(it) }

    fun magnification(): Double = settings_.equipment().magnification()

    fun set_magnification(value: String): Color =
        set_positive(value) { settings_.equipment().set_magnification(it) }

    fun hawk_levels(): Int = settings_.hawk_levels()

    fun set_hawk_levels(value: String): Color
    {
        val parsed = value.trim().toIntOrNull()
        if (parsed == null || parsed < 1)
        {
            return error_colour()
        }
        settings_.set_hawk_levels(parsed)
        return default_colour()
    }

    fun photons_per_adu(): Double = settings_.photons_per_adu()

    /**
     * Blank or zero means "not supplied", which is legitimate - the gain only affects the
     * predicted precision - so an empty field is accepted rather than flagged.
     */
    fun set_photons_per_adu(value: String): Color
    {
        if (value.isBlank())
        {
            settings_.set_photons_per_adu(Double.NaN)
            return default_colour()
        }
        val parsed = value.trim().toDoubleOrNull() ?: return error_colour()
        if (parsed < 0.0)
        {
            return error_colour()
        }
        settings_.set_photons_per_adu(parsed)
        return default_colour()
    }

    fun has_photons_per_adu(): Boolean = settings_.has_photons_per_adu()

    fun emccd(): Boolean = settings_.emccd()

    fun set_emccd(value: Boolean)
    {
        settings_.set_emccd(value)
    }

    fun fitter_names(): Array<String> = arrayOf("Fast moment fitter", thunderstorm_label())

    private fun thunderstorm_label(): String
    {
        // The absence is stated in the menu itself, so choosing it is not the way you find out.
        return if (ThunderStormFitter.is_available()) "ThunderSTORM"
               else "ThunderSTORM (not installed)"
    }

    fun set_fitter_index(index: Int)
    {
        settings_.set_fitter(if (index == 1) FitterChoice.THUNDERSTORM else FitterChoice.FAST)
    }

    fun is_thunderstorm(): Boolean = settings_.fitter() == FitterChoice.THUNDERSTORM

    fun correct_drift(): Boolean = settings_.thunderstorm().correct_drift()

    fun set_correct_drift(value: Boolean)
    {
        settings_.thunderstorm().set_correct_drift(value)
    }

    fun merge(): Boolean = settings_.thunderstorm().merge()

    fun set_merge(value: Boolean)
    {
        settings_.thunderstorm().set_merge(value)
    }

    /** What the two post-processing options will do to the report, or null when neither is on. */
    fun post_processing_note(): String? = settings_.thunderstorm().post_processing_note()

    /** What the user should be told about the run they are about to start, or null when ready. */
    fun error_string(): String? = settings_.error_string()

    fun is_runnable(): Boolean = settings_.is_runnable()

    /**
     * The caveat that applies when no gain was given. Shown before the run rather than left to be
     * discovered in the report, because it changes what the limiting precision number means.
     */
    fun precision_note(): String
    {
        return if (has_photons_per_adu())
        {
            "Localisation precision will be predicted from the photon budget."
        }
        else
        {
            "Without a camera gain, the limiting precision report will state an assumed " +
                "precision rather than a measured one. Everything else is unaffected."
        }
    }

    fun run()
    {
        OneClickWorker.from(IJLog.new(), settings_).execute()
    }

    private fun set_positive(value: String, apply: (Double) -> Unit): Color
    {
        val parsed = value.trim().toDoubleOrNull()
        if (parsed == null || parsed <= 0.0)
        {
            return error_colour()
        }
        apply(parsed)
        return default_colour()
    }

    private fun default_colour(): Color = JTextField().background

    private fun error_colour(): Color = Color.RED
}
