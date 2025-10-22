package com.coxphysics.terrapins.models.hawkman;

import com.coxphysics.terrapins.models.UtilsKt;
import ij.plugin.filter.Convolver;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTest
{
    @Test
    public void psf_kernel_size_test()
    {
        int psf_size = 12;
        assertEquals(util.psf_half_kernel_size(psf_size), 5);
    }

    @Test
    public void sigma_scale_number_test()
    {
        assertEquals(util.sigma_from_scale_number(1), 1.0 / 2.355);
        assertEquals(util.sigma_from_scale_number(2), 2.0 / 2.355);
    }

    @Test
    public void image_score_test()
    {
        assertEquals(util.image_score(1f, 1f), 1f);
        assertEquals(util.image_score(1f, 0f), 0.5);
        assertEquals(util.image_score(0f, 1f), 0.5);
        assertEquals(util.image_score(0.2f, 1f), 0.6176470605766072);
    }

    @Test
    public void xor_test()
    {
        byte[] data_1 = new byte[]{0, 0, 1, 1};
        byte[] data_2 = new byte[]{0, 1, 0, 1};
        byte[] expected = new byte[]{0, 1, 1, 0};
        ByteProcessor image_1 = new ByteProcessor(2, 2, data_1);
        ByteProcessor image_2 = new ByteProcessor(2, 2, data_2);
        ByteProcessor xor = util.xor_images(image_1, image_2);
        byte[] xor_data = (byte[]) xor.getPixels();
        assertArrayEquals(xor_data, expected);
    }

    @Test
    public void binary_threshold_test()
    {
        double value;
        value = util.calculate_binary_threshold(1d, 1d, 1d, 1d, 1d);
        assertEquals(value, 3d);
        value = util.calculate_binary_threshold(0.5, 1d, 1d, 1d, 1d);
        assertEquals(value, 2.5);
        value = util.calculate_binary_threshold(1d, 0.5, 1d, 1d, 1d);
        assertEquals(value, 2.5);
        value = util.calculate_binary_threshold(1d, 1d, 10, 1d, 1d);
        assertEquals(value, 12);
    }

    @Test
    public void flatten_test()
    {
        float[] data = UtilsKt.arange_floats(20);
        FloatProcessor image = new FloatProcessor(10, 2, data);
        FloatProcessor new_image = util.flatten_image(image, 10);
        float[] new_image_data = (float[])new_image.getPixels();
        float [] expected = new float[]{0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 15.2f, 15.2f, 15.2f, 15.2f};
        assertArrayEquals(new_image_data, expected);
    }

    @Test
    public void half_psf_test()
    {
        float[] data = UtilsKt.arange_floats(100);
        FloatProcessor image = new FloatProcessor(10, 10, data);
        FloatProcessor new_image = util.half_psf_with(image, 7);
        float[] new_image_data = (float[])new_image.getPixels();
        FloatProcessor original = hawkman_psf(image);
        float [] expected = new float[]{0.0952381f, 0.1010101f, 0.10822511f, 0.116883114f, 0.12698412f, 0.13708514f, 0.14718615f, 0.15584415f, 0.16305916f, 0.16883117f, 0.15295815f, 0.15873016f, 0.16594517f, 0.17460318f, 0.18470418f, 0.19480519f, 0.20490621f, 0.21356422f, 0.22077923f, 0.22655123f, 0.22510822f, 0.23088023f, 0.23809524f, 0.24675325f, 0.25685427f, 0.26695526f, 0.27705628f, 0.2857143f, 0.2929293f, 0.2987013f, 0.3116883f, 0.31746033f, 0.32467532f, 0.33333334f, 0.34343433f, 0.35353535f, 0.36363637f, 0.37229437f, 0.3795094f, 0.38528138f, 0.41269842f, 0.4184704f, 0.42568544f, 0.43434343f, 0.44444445f, 0.45454547f, 0.46464646f, 0.47330448f, 0.48051947f, 0.4862915f, 0.51370853f, 0.5194805f, 0.52669555f, 0.53535354f, 0.54545456f, 0.5555556f, 0.56565654f, 0.5743146f, 0.58152956f, 0.5873016f, 0.6147186f, 0.6204906f, 0.62770563f, 0.6363636f, 0.64646465f, 0.65656567f, 0.6666667f, 0.6753247f, 0.6825397f, 0.6883117f, 0.7012987f, 0.7070707f, 0.71428573f, 0.7229437f, 0.73304474f, 0.74314576f, 0.7532467f, 0.7619048f, 0.76911974f, 0.7748918f, 0.77344877f, 0.77922076f, 0.7864358f, 0.7950938f, 0.8051948f, 0.8152958f, 0.82539684f, 0.8340548f, 0.84126985f, 0.84704185f, 0.83116883f, 0.8369408f, 0.84415585f, 0.85281384f, 0.86291486f, 0.8730159f, 0.8831169f, 0.8917749f, 0.8989899f, 0.9047619f};
        assertArrayEquals(new_image_data, expected);
        assertArrayEquals(new_image_data, (float[])original.getPixels());
    }

    private FloatProcessor hawkman_psf(FloatProcessor imgTestFlat)
    {
        double PSFsize = 13;
        Convolver ConvKernal = new Convolver();
        int PSFhalf = (int)(2*Math.ceil(Math.ceil(PSFsize/4.0)))-1;
        float[] HalfKernal = new float[PSFhalf*PSFhalf];
        Arrays.fill(HalfKernal, 1F);
        FloatProcessor imgTestHalfFP = imgTestFlat.convertToFloatProcessor();

        double normInt = imgTestHalfFP.getMax();
        normInt = (double)1.0/normInt;
        imgTestHalfFP.multiply(normInt);
        imgTestHalfFP.resetMinAndMax();
        double new_max = imgTestHalfFP.getMax();
        Boolean ThreshBool = ConvKernal.convolve(imgTestHalfFP, HalfKernal, PSFhalf, PSFhalf);
        return imgTestHalfFP;
    }
}
