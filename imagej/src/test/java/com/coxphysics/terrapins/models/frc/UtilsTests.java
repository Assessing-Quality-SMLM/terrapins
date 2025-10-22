package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.frc.Complex;
import com.coxphysics.terrapins.models.frc.FRC;
import com.coxphysics.terrapins.models.frc.UtilKt;
import kotlin.Pair;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTests
{
    @Test
    public void complex_mult_round_trip()
    {
        Complex c = new Complex(1, 2);
        Complex c_bar = new Complex(1, -2);
        assertTrue(c.conjugate().structurally_equal(c_bar));

        Complex other = new Complex(3, 4);
        Complex expected = new Complex(11, 2);
        Complex result = c.mult(other.conjugate());
        assertTrue(result.structurally_equal(expected));
        assertEquals(result.real(), c.real_conj_mult(other));
    }

    @Test
    public void complex_correlation()
    {
        Complex a = new Complex(1, 2);
        Complex b = new Complex(3, 4);
        List<Pair<Complex, Complex>> values = Arrays.asList(new Pair<>(a, b));
        double correlation = UtilKt.complex_correlation_from_stream(values.stream());
        assertEquals(correlation, 0.9838699100999074);
    }

//    @Test
//    public void complex_correlation_other()
//    {
//        float [] real_1 = new float[]{1};
//        float [] imag_1 = new float[]{2};
//        float [] real_2 = new float[]{3};
//        float [] imag_2 = new float[]{4};
//        double correlation = FRC.complex_correlation(real_1, imag_1, real_2, imag_2);
//        assertEquals(correlation, 0.9838699100999074);
//    }
//
//        @Test
//    public void rings()
//    {
//        int size = 10;
//        float [] numerator = arange(size, false);
//        float [] absFFt1 = arange(size, true);
//        float [] absFFt2 = arange(size, true);
//        double [] values = FRC.get_rings(size, numerator, absFFt1, absFFt2);
//        double [] expected = {0, 1.2154640599370006, 1.2261314859375823, 1.2196453421282358};
//        assertArrayEquals(values, expected);
//    }

    private float [] arange(int size, boolean flip)
    {
        int square = size * size;
        float[] data = new float[square];
        for (int idx = 0; idx < square; idx++)
        {
            if (flip)
                data[idx] = (square - idx);
            else data[idx] = idx;
        }
        return data;
    }
}