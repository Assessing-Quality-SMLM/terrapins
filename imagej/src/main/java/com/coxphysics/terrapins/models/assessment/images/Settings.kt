package com.coxphysics.terrapins.models.assessment.images

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.io.FrcImages
import com.coxphysics.terrapins.models.io.JointImages
import java.nio.file.Path

class Settings private constructor()
{
    private var equipment = EquipmentSettings.default()
    private var core_settings_ = CoreSettings.default()
    private var reference_image_ = DiskOrImage.default()
    private var hawk_image_ = DiskOrImage.default()
    private var half_split_ = JointImages.default()
    private var zip_split_ = JointImages.default()

    companion object
    {
        @JvmStatic
        fun default(): Settings
        {
            return Settings()
        }
    }

    fun equipment_settings(): EquipmentSettings
    {
        return equipment
    }

    fun set_equipment_settings(value: EquipmentSettings)
    {
        equipment = value
    }

    fun core_settings(): CoreSettings
    {
        return core_settings_
    }

    private fun working_directory(): Path
    {
        return core_settings_.working_directory()
    }

    fun widefield(): DiskOrImage
    {
        return core_settings_.widefield()
    }

    fun set_widefield(value: DiskOrImage)
    {
        core_settings_.set_widefield(value)
    }

    fun set_widefield_filename(value: String)
    {
        core_settings_.set_widefield_filename(value)
    }

    fun image_stack(): DiskOrImage
    {
        return core_settings_.image_stack()
    }

    fun set_image_stack(value: DiskOrImage)
    {
        core_settings_.set_image_stack(value)
    }

    fun set_image_stack_filename(value: String)
    {
        core_settings_.set_image_stack_filename(value)
    }

    fun settings_file_nn(): String
    {
        return core_settings_.settings_file_nn()
    }

    fun set_settings_file(value: String)
    {
        core_settings_.set_settings_file(value)
    }

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

    fun reference_image_path_in(directory: Path): Path?
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

    fun hawk_image_path_in(directory: Path): Path?
    {
        val image_path = directory.resolve("hawk.tiff")
        return hawk_image_.filepath(image_path)
    }

    fun hawk_image_nn(): String
    {
        return hawk_image_.filename_nn()
    }

    fun set_hawk(value: DiskOrImage)
    {
        hawk_image_ = value
    }

    fun set_hawk_filename(value: String)
    {
        hawk_image_.set_filename_and_switch_usage(value)
    }

    fun half_split_model(): JointImages
    {
        return half_split_
    }

    fun zip_split_model(): JointImages
    {
        return zip_split_
    }

    fun frc_model(): FrcImages
    {
        return FrcImages.new(half_split_model(), zip_split_model())
    }

    fun set_frc_images(value: FrcImages)
    {
        half_split_ = value.half_split()
        zip_split_ = value.zip_split()
    }

    fun half_split_valid(): Boolean
    {
        return half_split_.is_valid()
    }

    fun half_split_images_directory(): Path
    {
        return half_split_images_directory_in(working_directory())
    }

    fun half_split_images_directory_in(working_directory: Path): Path
    {
        return working_directory.resolve("half_split_images")
    }

    fun zip_split_images_directory(): Path
    {
        return zip_split_images_directory_in(working_directory())
    }

    fun zip_split_images_directory_in(working_directory: Path): Path
    {
        return working_directory.resolve("zip_split_images")
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

    fun prepare_images_for_analysis(): Boolean
    {
        return prepare_images_for_analysis_in(working_directory())
    }

    fun prepare_images_for_analysis_in(working_directory: Path): Boolean
    {
        val core_ok = core_settings_.to_disk_in(working_directory)
        val reference_path = reference_image_path_in(working_directory)?.let { p -> reference_image().to_disk_with(p) }
        val hawk_path = hawk_image_path_in(working_directory)?.let{p -> hawk_image().to_disk_with(p)}
        val half_split_ok = half_split_.to_disk_in(half_split_images_directory_in(working_directory))
        val zip_split_ok = zip_split_.to_disk_in(zip_split_images_directory_in(working_directory))
        return core_ok && reference_path != null &&
                hawk_path != null &&
                half_split_ok &&
                zip_split_ok
    }
}