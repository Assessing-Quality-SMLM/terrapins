package com.coxphysics.terrapins.models.assessment.images

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.io.FrcImages
import com.coxphysics.terrapins.models.io.JointImages
import com.coxphysics.terrapins.models.macros.MacroOptions
import com.coxphysics.terrapins.plugins.*
import java.nio.file.Path
import java.nio.file.Paths

class Settings private constructor(
    private var core_settings_: CoreSettings
)
{
    private var equipment = EquipmentSettings.default()
    private var reference_image_ = DiskOrImage.default()
    private var hawk_image_ = DiskOrImage.default()
    private var half_split_ = JointImages.default()
    private var zip_split_ = JointImages.default()
    private var drift_split_ = JointImages.default()

    companion object
    {
        @JvmStatic
        fun with(working_directory: Path): Settings
        {
            return Settings(CoreSettings.from(working_directory))
        }

        @JvmStatic
        fun default(): Settings
        {
            return Settings(CoreSettings.default())
        }

        @JvmStatic
        fun from_macro_options(options: MacroOptions) : Settings?
        {
            val core_settings = CoreSettings.from_macro_options(options)
            if (core_settings == null)
                return null

            val equipment_settings = EquipmentSettings.from_macro_options(options)
            if (equipment_settings == null)
                return null

            val reference_image = DiskOrImage.from_macro_options_with(IMAGES_SETTINGS_RECON_IMAGE, options)
            if (reference_image == null)
                return null

            val hawk_image = DiskOrImage.from_macro_options_with(IMAGES_SETTINGS_HAWK_IMAGE, options)
            if (hawk_image == null)
                return null

            val half_split = JointImages.from_macro_options_with(IMAGES_SETTINGS_HALF_SPLIT_1, IMAGES_SETTINGS_HALF_SPLIT_2, options)
            if (half_split == null)
                return null

            val drift_split = JointImages.from_macro_options_with(IMAGES_SETTINGS_DRIFT_SPLIT_1, IMAGES_SETTINGS_DRIFT_SPLIT_2, options)
            if (drift_split == null)
                return null

            val zip_split = JointImages.from_macro_options_with(IMAGES_SETTINGS_ZIP_SPLIT_1, IMAGES_SETTINGS_ZIP_SPLIT_2, options)
            if (zip_split == null)
                return null

            val settings = default()
            settings.core_settings_ = core_settings
            settings.equipment = equipment_settings
            settings.reference_image_ = reference_image
            settings.hawk_image_ = hawk_image
            settings.half_split_ = half_split
            settings.drift_split_ = drift_split
            settings.zip_split_ = zip_split

            return settings
        }
    }

    /// EQUIPMENT SETTINGS

    fun equipment_settings(): EquipmentSettings
    {
        return equipment
    }

    fun set_equipment_settings(value: EquipmentSettings)
    {
        equipment = value
    }

    fun magnification(): Double
    {
        return equipment.magnification()
    }

    fun set_magnification(value: Double)
    {
        equipment.set_magnification(value)
    }

    /// CORE SETTINGS

    fun core_settings(): CoreSettings
    {
        return core_settings_
    }

    fun set_core_settings(value: CoreSettings)
    {
        core_settings_ = value
    }

    fun working_directory(): Path
    {
        return core_settings_.working_directory()
    }

    fun set_working_directory(value: String)
    {
        core_settings_.set_working_directory(Paths.get(value))
    }

    fun set_widefield_filename(value: String)
    {
        core_settings_.set_widefield_filename(value)
    }

    fun set_image_stack_filename(value: String)
    {
        core_settings_.set_image_stack_filename(value)
    }

    fun set_n_threads(value: Int)
    {
        core_settings_.set_n_threads(value)
    }

    fun settings_file_nn(): String
    {
        return core_settings_.settings_file_nn()
    }

    fun set_settings_file(value: String)
    {
        core_settings_.set_settings_file(value)
    }

    /// REFERENCE
    fun reference_image(): DiskOrImage
    {
        return reference_image_
    }

    fun reference_image_is_valid(): Boolean
    {
        return reference_image_.has_data()
    }

    fun reference_image_path(): Path?
    {
        return reference_image_path_in(working_directory())
    }

    private fun reference_image_path_in(directory: Path): Path?
    {
        val image_path = directory.resolve("sr.tiff")
        return reference_image_.filepath(image_path)
    }

    fun set_reference(value: DiskOrImage)
    {
        reference_image_ = value
    }

    fun set_reference_filename(value: String)
    {
        reference_image_.set_filename_and_switch_usage(value)
    }

    /// HAWK
    fun hawk_image(): DiskOrImage
    {
        return hawk_image_
    }

    fun hawk_image_is_valid(): Boolean
    {
        return hawk_image_.has_data()
    }

    fun hawk_image_path(): Path?
    {
        return hawk_image_path_in(working_directory())
    }

    private fun hawk_image_path_in(directory: Path): Path?
    {
        val image_path = directory.resolve("hawk.tiff")
        return hawk_image_.filepath(image_path)
    }

    fun set_hawk(value: DiskOrImage)
    {
        hawk_image_ = value
    }

    fun set_hawk_filename(value: String)
    {
        hawk_image_.set_filename_and_switch_usage(value)
    }

    /// FRC MODEL
    fun frc_model(): FrcImages
    {
        return FrcImages.new(half_split_model(), zip_split_model(), drift_split_model())
    }

    fun set_frc_images(value: FrcImages)
    {
        half_split_ = value.half_split()
        zip_split_ = value.zip_split()
    }

    /// HALF SPLIT
    fun half_split_model(): JointImages
    {
        return half_split_
    }

    fun half_split_valid(): Boolean
    {
        return half_split_.is_valid()
    }

    fun half_split_image_a_filepath(): Path?
    {
        return half_split_.image_1_filepath(half_split_images_directory())
    }

    fun set_half_split_a(value: String)
    {
        half_split_.set_image_1_filename(value)
    }

    fun half_split_image_b_filepath(): Path?
    {
        return half_split_.image_2_filepath(half_split_images_directory())
    }

    fun set_half_split_b(value: String)
    {
        half_split_.set_image_2_filename(value)
    }

    private fun half_split_images_directory(): Path
    {
        return half_split_images_directory_in(working_directory())
    }

    private fun half_split_images_directory_in(working_directory: Path): Path
    {
        return working_directory.resolve("half_split_images")
    }

    /// ZIP SPLIT
    fun zip_split_model(): JointImages
    {
        return zip_split_
    }

    fun zip_split_valid(): Boolean
    {
        return zip_split_.is_valid()
    }

    fun zip_split_image_a_filepath(): Path?
    {
        return zip_split_.image_1_filepath(zip_split_images_directory())
    }

    fun zip_split_image_b_filepath(): Path?
    {
        return zip_split_.image_2_filepath(zip_split_images_directory())
    }

    fun set_zip_split_a(value: String)
    {
        zip_split_.set_image_1_filename(value)
    }

    fun set_zip_split_b(value: String)
    {
        zip_split_.set_image_2_filename(value)
    }

    private fun zip_split_images_directory(): Path
    {
        return zip_split_images_directory_in(working_directory())
    }

    private fun zip_split_images_directory_in(working_directory: Path): Path
    {
        return working_directory.resolve("zip_split_images")
    }

    /// DRIIT SPLIT
    fun drift_split_model(): JointImages
    {
        return drift_split_
    }

    fun drift_split_valid(): Boolean
    {
        return drift_split_.is_valid()
    }

    fun drift_split_image_a_filepath(): Path?
    {
        return drift_split_.image_1_filepath(drift_split_images_directory())
    }

    fun drift_split_image_b_filepath(): Path?
    {
        return drift_split_.image_2_filepath(drift_split_images_directory())
    }

    fun set_drift_split_a(value: String)
    {
        drift_split_.set_image_1_filename(value)
    }

    fun set_drift_split_b(value: String)
    {
        drift_split_.set_image_2_filename(value)
    }

    private fun drift_split_images_directory(): Path
    {
        return drift_split_images_directory_in(working_directory())
    }

    private fun drift_split_images_directory_in(working_directory: Path): Path
    {
        return working_directory.resolve("drift_split_images")
    }

    /// METHODS
    fun prepare_images_for_analysis(): CoreSettings?
    {
        return prepare_images_for_analysis_in(working_directory())
    }

    private fun prepare_images_for_analysis_in(working_directory: Path): CoreSettings?
    {
        val new_core_settings = core_settings_.to_disk_in(working_directory)
        var reference_ok = true
        if (reference_image_.has_data())
        {
            val reference_path = reference_image_path_in(working_directory)?.let { p -> reference_image().to_disk_with(p) }
            reference_ok = reference_path != null
        }
        var hawk_ok = true
        if (hawk_image_.has_data())
        {
            val hawk_path = hawk_image_path_in(working_directory)?.let{p -> hawk_image().to_disk_with(p)}
            hawk_ok = hawk_path != null
        }

        val half_split_ok = half_split_.to_disk_in(half_split_images_directory_in(working_directory))
        val zip_split_ok = zip_split_.to_disk_in(zip_split_images_directory_in(working_directory))
        val drift_split_ok = drift_split_.to_disk_in(drift_split_images_directory_in(working_directory))
        val all_ok = reference_ok &&
                     hawk_ok &&
                     half_split_ok &&
                     zip_split_ok &&
                     drift_split_ok
        if (all_ok)
            return new_core_settings
        return null
    }

    fun record_to_macro()
    {
        core_settings_.record_to_macro()
        equipment.record_to_macro()
        reference_image_.record_to_macro_with(IMAGES_SETTINGS_RECON_IMAGE)
        hawk_image_.record_to_macro_with(IMAGES_SETTINGS_HAWK_IMAGE)
        half_split_.record_to_macro_with(IMAGES_SETTINGS_HALF_SPLIT_1, IMAGES_SETTINGS_HALF_SPLIT_2)
        zip_split_.record_to_macro_with(IMAGES_SETTINGS_DRIFT_SPLIT_1, IMAGES_SETTINGS_DRIFT_SPLIT_2)
        drift_split_.record_to_macro_with(IMAGES_SETTINGS_ZIP_SPLIT_1, IMAGES_SETTINGS_ZIP_SPLIT_2)
    }
}