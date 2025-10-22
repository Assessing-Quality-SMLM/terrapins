package com.coxphysics.terrapins.models.hawkman;

import ij.process.FloatProcessor;


public class HAWKMANData
{
    private final Input input_data_;

    private final FloatProcessor half_psf_test_;

    private final FloatProcessor half_psf_ref_;

    public HAWKMANData(Input inputData, FloatProcessor half_psf_test, FloatProcessor half_psf_ref)
    {
        input_data_ = inputData;
        half_psf_test_ = half_psf_test;
        half_psf_ref_ = half_psf_ref;
    }

    public FloatProcessor test_image()
    {
        return input_data_.test_image();
    }

    public FloatProcessor ref_image()
    {
        return input_data_.ref_image();
    }

    public FloatProcessor half_psf_test()
    {
        return half_psf_test_;
    }

    public FloatProcessor half_psf_ref()
    {
        return half_psf_ref_;
    }
}
