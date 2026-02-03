package com.coxphysics.terrapins.models.io

class FrcImages private constructor(
    private val half_split_: JointImages,
    private val zip_split_: JointImages,
    private val drift_split_: JointImages
)
{
    companion object
    {
        @JvmStatic
        fun new(half_split: JointImages, zip_split: JointImages, drift_split: JointImages) : FrcImages
        {
            return FrcImages(half_split, zip_split, drift_split)
        }

        @JvmStatic
        fun default() : FrcImages
        {
            return new(JointImages.default(), JointImages.default(), JointImages.default())
        }
    }

    fun half_split(): JointImages
    {
        return half_split_
    }

    fun zip_split(): JointImages
    {
        return zip_split_
    }

    fun drift_split(): JointImages
    {
        return drift_split_
    }
}