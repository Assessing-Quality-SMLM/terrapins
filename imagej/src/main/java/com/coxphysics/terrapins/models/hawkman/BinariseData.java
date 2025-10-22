package com.coxphysics.terrapins.models.hawkman;

import ij.process.FloatProcessor;

public class BinariseData
{
    private final FloatProcessor image_;
    private final FloatProcessor threshold_image_;
    private final FloatProcessor half_psf_image_;

    private final BinariseParameters settings_;
    private final BinariseParameters skelontinse_settings_;

    public BinariseData(FloatProcessor image, FloatProcessor threshold_image, FloatProcessor half_psf_image, BinariseParameters settings, BinariseParameters skeleton_settings)
    {
        image_ = image;
        threshold_image_ = threshold_image;
        half_psf_image_ = half_psf_image;
        settings_ = settings;
        skelontinse_settings_ = skeleton_settings;
    }

    private float image_value(int col, int row)
    {
        return image_.getf(col, row);
    }

    private float threshold_value(int col, int row)
    {
        return threshold_image_.getf(col, row);
    }

    private float half_psf_value(int col, int row)
    {
        return half_psf_image_.getf(col, row);
    }

    public Values get_binary_values(int col, int row)
    {
        float threshold_value = threshold_value(col, row);
        float half_psf_value = half_psf_value(col, row);
        double threshold = settings_.threshold_value(threshold_value, half_psf_value);
        double skeleton_threshold = skelontinse_settings_.threshold_value(threshold_value, half_psf_value);
        float image_value = image_value(col, row);
        int binary_value = get_binary_value(image_value, threshold);
        int skeleton_value = get_binary_value(image_value, skeleton_threshold);
        return new Values(binary_value, skeleton_value);
    }

    private static int get_binary_value(float image_value, double threshold)
    {
        return image_value > threshold ? 255 : 0;
    }

    public static class Values
    {
        private final int binary_value_;
        private final int skeleton_value_;

        public Values(int binary_value, int skeleton_value)
        {
            binary_value_ = binary_value;
            skeleton_value_ = skeleton_value;
        }

        public int get_binary_value()
        {
            return binary_value_;
        }

        public int get_skeleton_value()
        {
            return skeleton_value_;
        }
    }
}
