package com.coxphysics.terrapins.models.utils;

import com.coxphysics.terrapins.models.utils.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTests {

    @Test
    void path_set_ok()
    {
        assertTrue(StringUtils.path_set("something"));
    }

    @Test
    void path_not_set_null()
    {
        assertFalse(StringUtils.path_set(null));
    }

    @Test
    void path_not_set_empty_string()
    {
        assertFalse(StringUtils.path_set(StringUtils.EMPTY_STRING));
    }

    @Test
    void get_null_when_parsing_illegal_int()
    {
        assertNull(StringUtils.parse_unisigned_int(StringUtils.EMPTY_STRING));
    }

    @Test
    void get_null_when_parsing_illegal_char()
    {
        assertNull(StringUtils.to_char(StringUtils.EMPTY_STRING));
    }

    @Test
    public void parse_infinity()
    {
        double value = StringUtils.parse_double("inf");
        assertEquals(value, Double.POSITIVE_INFINITY);
    }

    @Test
    public void parse_negative_infinity()
    {
        double value = StringUtils.parse_double("-inf");
        assertEquals(value, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void parse_nan()
    {
        double value = StringUtils.parse_double("NaN");
        assertEquals(value, Double.NaN);
    }
}