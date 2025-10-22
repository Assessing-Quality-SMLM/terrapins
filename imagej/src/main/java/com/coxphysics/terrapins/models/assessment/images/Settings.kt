package com.coxphysics.terrapins.models.assessment.images

import com.coxphysics.terrapins.models.assessment.CoreSettings
import com.coxphysics.terrapins.models.equipment.EquipmentSettings
import com.coxphysics.terrapins.models.non_null

class FRCData private constructor()
{
    private var image_a_: String? = null
    private var image_b_: String? = null
    companion object
    {
        @JvmStatic
        fun default(): FRCData
        {
            return FRCData()
        }
    }

    fun is_valid(): Boolean
    {
        return !(image_a_.isNullOrEmpty() || image_b_.isNullOrEmpty())
    }

    fun image_a_nn(): String
    {
        return image_a_.non_null()
    }

    fun set_image_a(value: String)
    {
        image_a_ = value
    }

    fun image_b_nn(): String
    {
        return image_b_.non_null()
    }

    fun set_image_b(value: String)
    {
        image_b_ = value
    }
}

class Settings private constructor()
{
    private var equipment = EquipmentSettings.default()
    private var core_settings_ = CoreSettings.default()
    private var reference_image : String? = null
    private var hawk_image : String? = null
    private var half_split_ = FRCData.default()
    private var zip_split_ = FRCData.default()

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

    fun widefield_nn(): String
    {
        return core_settings_.widefield_nn()
    }

    fun set_widefield(value: String)
    {
        core_settings_.set_widefield(value)
    }

    fun image_stack_nn(): String
    {
        return core_settings_.image_stack_nn()
    }

    fun set_image_stack(value: String)
    {
        core_settings_.set_image_stack(value)
    }

    fun settings_file_nn(): String
    {
        return core_settings_.settings_file_nn()
    }

    fun set_settings_file(value: String)
    {
        core_settings_.set_settings_file(value)
    }

    fun reference_image_is_valid(): Boolean
    {
        return !reference_image.isNullOrEmpty()
    }

    fun reference_image_nn(): String
    {
        return reference_image.non_null()
    }

    fun set_reference(value: String)
    {
        reference_image = value
    }

    fun hawk_image_is_valid(): Boolean
    {
        return !hawk_image.isNullOrEmpty()
    }

    fun hawk_image_nn(): String
    {
        return hawk_image.non_null()
    }

    fun set_hawk(value: String)
    {
        hawk_image = value
    }

    fun half_split_valid(): Boolean
    {
        return half_split_.is_valid()
    }

    fun half_split_image_a_nn(): String
    {
        return half_split_.image_a_nn()
    }

    fun set_half_split_a(value: String)
    {
        half_split_.set_image_a(value)
    }

    fun half_split_image_b_nn(): String
    {
        return half_split_.image_b_nn()
    }

    fun set_half_split_b(value: String)
    {
        half_split_.set_image_b(value)
    }

    fun zip_split_valid(): Boolean
    {
        return zip_split_.is_valid()
    }

    fun zip_split_image_a_nn(): String
    {
        return zip_split_.image_a_nn()
    }

    fun set_zip_split_a(value: String)
    {
        zip_split_.set_image_a(value)
    }

    fun zip_split_image_b_nn(): String
    {
        return zip_split_.image_b_nn()
    }

    fun set_zip_split_b(value: String)
    {
        zip_split_.set_image_b(value)
    }
}