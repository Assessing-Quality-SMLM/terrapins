package com.coxphysics.terrapins.models.frc;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FRCResultsTests
{
    @Test
    public void parse_fire_infinity()
    {
        double value = FRCResult.parse_fire_number("inf");
        assertEquals(value, Double.POSITIVE_INFINITY);
    }

    @Test
    public void parse_fire_negative_infinity()
    {
        double value = FRCResult.parse_fire_number("-inf");
        assertEquals(value, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void parse_fire_value()
    {
        double value = FRCResult.parse_fire_number("26.123");
        assertEquals(value, 26.123);
    }

    @Test
    public void parse_fire_value_error_message()
    {
        double value = FRCResult.parse_fire_number("No Crossing");
        assertEquals(value, Double.POSITIVE_INFINITY);
    }

    @Test
    public void extract_fire_value()
    {
        String text = "26.123\n123,456,789";
        double value = 0;
        try {
            value = FRCResult.extract_fire_number(get_test_reader(text));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        assertEquals(value, 26.123);
    }

    @Test
    public void extract_fire_value_when_extra_info_present()
    {
        String text = "26.123,456,78.9\n123,456,789";
        double value = 0;
        try {
            value = FRCResult.extract_fire_number(get_test_reader(text));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        assertEquals(value, 26.123);
    }

    @Test
    public void parse_line()
    {
        String text = "1.23,4.56,7.89";
        FRCResult.FRCResultLine line = FRCResult.parse_line(text);
        assertEquals(line.q, 1.23);
        assertArrayEquals(line.values, new double[]{4.56});
        assertEquals(line.threshold, 7.89);
    }

    @Test
    public void parse_multiple_frcs_line()
    {
        String text = "1.23,4.56,7.89,10.11";
        FRCResult.FRCResultLine line = FRCResult.parse_line(text);
        assertEquals(line.q, 1.23);
        assertArrayEquals(line.values, new double[]{4.56, 7.89});
        assertEquals(line.threshold, 10.11);
    }

    @Test
    public void parse_results()
    {
        String text = "123,456\n1.23,4.56,7.89,10.11\n1.23,4.56,7.89,10.11";
        FRCResult results = FRCResult.parse_reader(get_test_reader(text));
        assertEquals(results.fire_number(), 123.0);
        assertArrayEquals(results.qs(), new double[]{1.23, 1.23});
        assertArrayEquals(results.get_frcs(0), new double[]{4.56, 4.56});
        assertArrayEquals(results.get_frcs(1), new double[]{7.89, 7.89});
        assertArrayEquals(results.threshold_curve(), new double[]{10.11, 10.11});
    }

    private BufferedReader get_test_reader(String text)
    {
        return new BufferedReader(new StringReader(text));
    }

}
